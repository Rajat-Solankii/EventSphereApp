package com.eventsphere.scanner.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.api.models.Event
import com.eventsphere.scanner.data.repository.EventRepository
import com.eventsphere.scanner.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventsViewModel(private val repository: EventRepository = EventRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            repository.getEvents()
                .onSuccess { events ->
                    if (events.isEmpty()) {
                        _uiState.value = EventsUiState.Empty
                    } else {
                        val currentTime = System.currentTimeMillis()
                        val threeHoursInMillis = 3 * 60 * 60 * 1000L
                        val eventDurationInMillis = 4 * 60 * 60 * 1000L

                        val sortedEvents = events.sortedWith(compareBy({ event ->
                            val eventTime = DateUtils.getEventTimeMillis(event.date)
                            if (eventTime == 0L) 3 // Unknown to bottom
                            else if (currentTime > eventTime + eventDurationInMillis) 2 // ENDED
                            else if (currentTime < eventTime - threeHoursInMillis) 1 // UPCOMING
                            else 0 // LIVE
                        }, { event ->
                            DateUtils.getEventTimeMillis(event.date)
                        }))
                        
                        _uiState.value = EventsUiState.Success(sortedEvents)
                    }
                }
                .onFailure { error ->
                    _uiState.value = EventsUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    object Empty : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}
