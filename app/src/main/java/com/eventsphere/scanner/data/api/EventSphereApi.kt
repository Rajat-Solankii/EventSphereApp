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

    @GET("api/events")
    suspend fun getEvents(): Response<List<Event>>

    @GET("api/tickets")
    suspend fun getAllTickets(): Response<List<Ticket>>

    @GET("api/tickets/{id}")
    suspend fun getTicket(@Path("id") id: String): Response<Ticket>

    @POST("api/scan")
    suspend fun scanTicket(@Body request: ScanRequest): Response<ScanResult>

    @GET("api/ai/chat/history")
    suspend fun getAIChatHistory(): Response<AIChatHistoryResponse>

    @POST("api/ai/chat")
    suspend fun sendAIChatMessage(@Body request: AIChatRequest): Response<AIChatResponse>
}
