package com.eventsphere.scanner.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.data.repository.EventRepository
import com.eventsphere.scanner.data.repository.TicketRepository
import com.eventsphere.scanner.utils.DateUtils
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val repository: TicketRepository = TicketRepository(),
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _scanResult = MutableLiveData<Result<ScanResult>>()
    val scanResult: LiveData<Result<ScanResult>> = _scanResult

    private val _isScanning = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning
    
    var pendingExitTicketId: String? = null

    // Cache event time
    private var eventTimeMillis: Long? = null

    private suspend fun getEventTimeMillis(eventId: String): Long {
        if (eventTimeMillis != null) return eventTimeMillis!!
        
        val eventResult = eventRepository.getEvents()
        if (eventResult.isSuccess) {
            val event = eventResult.getOrNull()?.find { it.id == eventId }
            eventTimeMillis = DateUtils.getEventTimeMillis(event?.date)
        } else {
            eventTimeMillis = 0L // Fallback if API fails
        }
        return eventTimeMillis!!
    }

    fun scan(ticketId: String, eventId: String?) {
        if (_isScanning.value == true) return
        
        viewModelScope.launch {
            _isScanning.value = true
            
            var targetEventId = eventId
            
            // If quick scan mode, fetch the ticket list to find the event ID before scanning
            // so we don't accidentally mark a ticket INSIDE for an event that hasn't started.
            if (targetEventId.isNullOrEmpty()) {
                val ticketsResult = repository.getAllTickets()
                if (ticketsResult.isSuccess) {
                    val ticket = ticketsResult.getOrNull()?.find { it.id == ticketId }
                    if (ticket != null) {
                        targetEventId = ticket.event.id
                    } else {
                        // Ticket not found in DB
                        _scanResult.value = Result.success(
                            ScanResult(
                                success = false, 
                                message = "Invalid Ticket", 
                                attendee = null, 
                                ticketInfo = null
                            )
                        )
                        return@launch
                    }
                }
            }
            
            // Validate entry time rules
            if (!targetEventId.isNullOrEmpty()) {
                val eventTime = getEventTimeMillis(targetEventId)
                if (eventTime > 0L) {
                    val currentTime = System.currentTimeMillis()
                    val threeHoursInMillis = 3 * 60 * 60 * 1000L
                    val eventDurationInMillis = 4 * 60 * 60 * 1000L // Assuming event runs for 4 hours
                    
                    // Event entry starts 3 hours before event
                    if (currentTime < eventTime - threeHoursInMillis) {
                        _scanResult.value = Result.success(
                            ScanResult(
                                success = false, 
                                message = "Too Early: Seating yet to start. Entry opens 3 hours before the event.", 
                                attendee = null, 
                                ticketInfo = null
                            )
                        )
                        return@launch
                    }
                    
                    // Event entry closes when event theoretically ends
                    if (currentTime > eventTime + eventDurationInMillis) {
                        _scanResult.value = Result.success(
                            ScanResult(
                                success = false, 
                                message = "Event Ended: No entry allowed after event.", 
                                attendee = null, 
                                ticketInfo = null
                            )
                        )
                        return@launch
                    }
                }
            }
            
            // Allow scan
            val result = repository.scanTicket(ticketId, targetEventId ?: "")
            result.onSuccess { scanResult ->
                var finalResult = scanResult
                
                // Check if they are late (arriving after the event start time)
                val resolvedEventId = targetEventId ?: scanResult.event?.id
                if (!resolvedEventId.isNullOrEmpty()) {
                    val eventTimeForLateCheck = getEventTimeMillis(resolvedEventId)
                    if (eventTimeForLateCheck > 0L && System.currentTimeMillis() > eventTimeForLateCheck && finalResult.success) {
                        finalResult = finalResult.copy(message = finalResult.message + " [LATE ENTRY]")
                    }
                }
                
                _scanResult.value = Result.success(finalResult)
            }.onFailure { error ->
                _scanResult.value = Result.success(
                    ScanResult(success = false, message = "Invalid Ticket", attendee = null, ticketInfo = null)
                )
            }
        }
    }
    
    fun markTemporaryExit(ticketId: String, base64Image: String) {
        viewModelScope.launch {
            val result = repository.markTemporaryExit(ticketId, base64Image)
            result.onSuccess {
                _scanResult.value = result
            }.onFailure { error ->
                _scanResult.value = Result.success(
                    ScanResult(success = false, message = error.message ?: "Failed to mark temporary exit", attendee = null, ticketInfo = null)
                )
            }
        }
    }

    fun markReEntry(ticketId: String) {
        viewModelScope.launch {
            val result = repository.markReEntry(ticketId)
            result.onSuccess {
                _scanResult.value = result
            }.onFailure { error ->
                _scanResult.value = Result.success(
                    ScanResult(success = false, message = error.message ?: "Failed to process re-entry", attendee = null, ticketInfo = null)
                )
            }
        }
    }
    
    fun resetScan() {
        _isScanning.value = false
    }
}
