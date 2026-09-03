package com.eventsphere.scanner.data.api

import com.eventsphere.scanner.data.api.models.RefreshTokenRequest
import com.eventsphere.scanner.data.local.PreferencesManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val preferencesManager: PreferencesManager,
    private val apiProvider: () -> EventSphereApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = preferencesManager.refreshToken ?: return null

        synchronized(this) {
            // Check if token was already refreshed by another thread
            val currentToken = preferencesManager.accessToken
            if (response.request.header("Authorization") != "Bearer $currentToken") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val api = apiProvider()
            val refreshResponse = api.refreshToken(RefreshTokenRequest(refreshToken)).execute()

            return if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newAuth = refreshResponse.body()!!
                preferencesManager.accessToken = newAuth.accessToken
                preferencesManager.refreshToken = newAuth.refreshToken
                
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newAuth.accessToken}")
                    .build()
            } else {
                preferencesManager.clearAuth()
                // You might want to trigger a logout event here, e.g., via a Flow or EventBus
                null
            }
        }
    }
}
