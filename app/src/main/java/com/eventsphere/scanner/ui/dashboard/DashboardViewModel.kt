package com.eventsphere.scanner.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.api.models.Event
import com.eventsphere.scanner.data.api.models.Ticket
import com.eventsphere.scanner.data.repository.EventRepository
import com.eventsphere.scanner.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val eventRepository: EventRepository = EventRepository(),
    private val ticketRepository: TicketRepository = TicketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadData(eventId: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            val eventResult = eventRepository.getEvents()
            val ticketsResult = ticketRepository.getAllTickets()

            if (eventResult.isSuccess && ticketsResult.isSuccess) {
                val eventList = eventResult.getOrNull()
                val event = eventList?.find { it.id == eventId }
                val allTickets = ticketsResult.getOrNull() ?: emptyList()
                val eventTickets = allTickets.filter { it.event.id == eventId }

                if (event != null) {
                    val stats = computeStats(eventTickets)
                    _uiState.value = DashboardUiState.Success(event, stats)
                } else {
                    _uiState.value = DashboardUiState.Error("Event not found")
                }
            } else {
                val error = eventResult.exceptionOrNull()?.message 
                    ?: ticketsResult.exceptionOrNull()?.message 
                    ?: "Failed to load data"
                _uiState.value = DashboardUiState.Error(error)
            }
        }
    }

    private fun computeStats(tickets: List<Ticket>): EventStats {
        val total = tickets.size
        val inside = tickets.count { it.status.equals("Inside", ignoreCase = true) }
        val outside = tickets.count { it.status.equals("Outside", ignoreCase = true) }
        val pending = tickets.count { it.status.equals("Pending", ignoreCase = true) }
        return EventStats(total, inside, outside, pending)
    }
}

data class EventStats(
    val total: Int,
    val inside: Int,
    val outside: Int,
    val pending: Int
)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val event: Event, val stats: EventStats) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
