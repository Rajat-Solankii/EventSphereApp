package com.eventsphere.scanner.data.repository

import com.eventsphere.scanner.data.api.RetrofitClient
import com.eventsphere.scanner.data.api.models.ScanRequest
import com.eventsphere.scanner.data.api.models.ScanResult
import com.eventsphere.scanner.data.api.models.TempExitRequest
import com.eventsphere.scanner.data.api.models.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TicketRepository {
    
    private val api get() = RetrofitClient.apiService

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

    suspend fun scanTicket(ticketId: String, eventId: String): Result<ScanResult> = withContext(Dispatchers.IO) {
        try {
            // eventId is ignored in backend now, but passed for compatibility if needed
            val response = api.scanTicket(ScanRequest(ticketId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Scan failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markTemporaryExit(ticketId: String, exitImageBase64: String): Result<ScanResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.markTemporaryExit(ticketId, TempExitRequest(exitImageBase64))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Temporary exit failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markReEntry(ticketId: String): Result<ScanResult> = withContext(Dispatchers.IO) {
        try {
            val response = api.markReEntry(ticketId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Re-entry failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
