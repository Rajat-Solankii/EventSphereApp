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
    
    var pendingExitTicketId: String? = null

    fun scan(ticketId: String, eventId: String) {
        if (_isScanning.value == true) return
        
        viewModelScope.launch {
            _isScanning.value = true
            
            val result = repository.scanTicket(ticketId, eventId)
            result.onSuccess {
                _scanResult.value = result
            }.onFailure { error ->
                _scanResult.value = Result.success(
                    ScanResult(success = false, message = error.message ?: "Invalid Ticket or Not Found", attendee = null, ticketInfo = null)
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
