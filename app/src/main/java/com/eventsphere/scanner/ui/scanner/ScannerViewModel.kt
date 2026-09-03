package com.eventsphere.scanner.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.data.repository.TicketRepository
import kotlinx.coroutines.launch

class ScannerViewModel(private val repository: TicketRepository = TicketRepository()) : ViewModel() {

    private val _scanResult = MutableLiveData<Result<ScanResult>>()
    val scanResult: LiveData<Result<ScanResult>> = _scanResult

    private val _isScanning = MutableLiveData<Boolean>(false)
    val isScanning: LiveData<Boolean> = _isScanning

    fun scan(ticketId: String, eventId: String) {
        if (_isScanning.value == true) return
        
        viewModelScope.launch {
            _isScanning.value = true
            // Implementation of "fetch ticket info first" logic could go here if needed,
            // but the repo.scanTicket already handles the scan endpoint.
            // Requirement: "Ensure the scanning logic handles the PENDING and DECLINED statuses 
            // before calling the scan endpoint if possible (by fetching ticket info first as per spec)."
            
            val ticketInfoResult = repository.getTicket(ticketId)
            ticketInfoResult.onSuccess { ticket ->
                if (ticket.status == "PENDING" || ticket.status == "DECLINED") {
                     _scanResult.value = Result.failure(Exception("Ticket status is ${ticket.status}"))
                     _isScanning.value = false
                     return@launch
                }
                
                // If not pending/declined, proceed to scan
                val result = repository.scanTicket(ticketId, eventId)
                _scanResult.value = result
                _isScanning.value = false
            }.onFailure {
                _scanResult.value = Result.failure(it)
                _isScanning.value = false
            }
        }
    }
    
    fun resetScan() {
        _isScanning.value = false
    }
}
