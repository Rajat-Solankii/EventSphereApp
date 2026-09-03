package com.eventsphere.scanner.data.repository

import com.eventsphere.scanner.data.api.RetrofitClient
import com.eventsphere.scanner.data.api.models.ScanRequest
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.data.api.models.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TicketRepository {
    
    private val api = RetrofitClient.apiService

    suspend fun getAllTickets(): Result<List<Ticket>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllTickets()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to fetch tickets" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicket(id: String): Result<Ticket> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTicket(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Ticket not found" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Scans a ticket for a specific event.
     * Note: API requires both ticketId and eventId.
     */
    suspend fun scanTicket(ticketId: String, eventId: String): Result<ScanResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.scanTicket(ScanRequest(ticketId, eventId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Scan failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
