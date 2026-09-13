package com.eventsphere.scanner.data.repository

import com.eventsphere.scanner.data.api.RetrofitClient
import com.eventsphere.scanner.data.api.models.*
import com.eventsphere.scanner.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val preferencesManager: PreferencesManager) {
    
    private val api = RetrofitClient.getApi(preferencesManager)

    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                saveUserInfo(loginResponse)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Login failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.logout()
            preferencesManager.clearAuth()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Logout failed" }))
            }
        } catch (e: Exception) {
            preferencesManager.clearAuth()
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Failed to get profile" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(token: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.refreshToken(RefreshTokenRequest(token)).execute()
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                preferencesManager.accessToken = loginResponse.accessToken
                preferencesManager.refreshToken = loginResponse.refreshToken
                Result.success(loginResponse)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Token refresh failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUserInfo(response: LoginResponse) {
        preferencesManager.accessToken = response.accessToken
        preferencesManager.refreshToken = response.refreshToken
        preferencesManager.userId = response.user.id
        preferencesManager.userName = response.user.name
        preferencesManager.userEmail = response.user.email
        preferencesManager.userRole = response.user.role
        preferencesManager.orgId = response.user.organizationId
    }
}
