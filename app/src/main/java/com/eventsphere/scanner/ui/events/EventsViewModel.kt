package com.eventsphere.scanner.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.api.models.Event
import com.eventsphere.scanner.data.repository.EventRepository
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
                        _uiState.value = EventsUiState.Success(events)
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
