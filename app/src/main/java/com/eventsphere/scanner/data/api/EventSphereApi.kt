package com.eventsphere.scanner.data.api

import com.eventsphere.scanner.data.api.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface EventSphereApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserProfile>

    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<LoginResponse>

    @GET("api/v1/events")
    suspend fun getEvents(): Response<List<Event>>

    @GET("api/v1/tickets")
    suspend fun getAllTickets(): Response<List<Ticket>>

    @GET("api/tickets/{id}")
    suspend fun getTicket(@Path("id") id: String): Response<Ticket>

    @POST("api/v1/tickets/scan")
    suspend fun scanTicket(@Body request: ScanRequest): Response<ScanResult>

    @POST("api/v1/tickets/{id}/temp-exit")
    suspend fun markTemporaryExit(@Path("id") id: String, @Body request: TempExitRequest): Response<ScanResult>

    @POST("api/v1/tickets/{id}/re-enter")
    suspend fun markReEntry(@Path("id") id: String): Response<ScanResult>

    @GET("api/v1/events/ai-chat/history")
    suspend fun getAIChatHistory(): Response<AIChatHistoryResponse>

    @POST("api/v1/events/ai-chat")
    suspend fun sendAIChatMessage(@Body request: AIChatRequest): Response<AIChatResponse>
}
