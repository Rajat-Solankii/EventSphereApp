package com.eventsphere.scanner.data.repository

import com.eventsphere.scanner.data.api.RetrofitClient
import com.eventsphere.scanner.data.api.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository {
    
    private val api = RetrofitClient.apiService

    suspend fun getEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getEvents()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to fetch events" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
