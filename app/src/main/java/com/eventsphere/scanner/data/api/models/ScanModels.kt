package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("ticketId") val ticketId: String,
    @SerializedName("eventId") val eventId: String
)

data class ScanResult(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("attendee") val attendee: AttendeeBasic? = null,
    @SerializedName("ticketInfo") val ticketInfo: TicketInfo? = null
)

data class AttendeeBasic(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)
