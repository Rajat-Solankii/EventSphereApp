package com.eventsphere.scanner.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.api.models.Ticket
import com.eventsphere.scanner.data.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketListViewModel(
    private val ticketRepository: TicketRepository = TicketRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TicketListUiState>(TicketListUiState.Loading)
    val uiState: StateFlow<TicketListUiState> = _uiState.asStateFlow()

    fun loadTickets(eventId: String, filterStatus: String) {
        viewModelScope.launch {
            _uiState.value = TicketListUiState.Loading
            
            val ticketsResult = ticketRepository.getAllTickets()
            if (ticketsResult.isSuccess) {
                val allTickets = ticketsResult.getOrNull() ?: emptyList()
                val eventTickets = allTickets.filter { it.event.id == eventId }
                
                val filteredTickets = if (filterStatus.equals("ALL", ignoreCase = true)) {
                    eventTickets
                } else {
                    eventTickets.filter { it.status.equals(filterStatus, ignoreCase = true) }
                }
                
                _uiState.value = TicketListUiState.Success(filteredTickets)
            } else {
                _uiState.value = TicketListUiState.Error(
                    ticketsResult.exceptionOrNull()?.message ?: "Failed to load tickets"
                )
            }
        }
    }
}

sealed class TicketListUiState {
    object Loading : TicketListUiState()
    data class Success(val tickets: List<Ticket>) : TicketListUiState()
    data class Error(val message: String) : TicketListUiState()
}
