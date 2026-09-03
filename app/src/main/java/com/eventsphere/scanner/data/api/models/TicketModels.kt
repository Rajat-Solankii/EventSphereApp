package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("id") val id: String,
    @SerializedName("event") val event: EventBasic,
    @SerializedName("tier") val tier: String,
    @SerializedName("attendeeName") val attendeeName: String,
    @SerializedName("status") val status: String
)

data class TicketInfo(
    @SerializedName("ticketId") val ticketId: String,
    @SerializedName("attendeeName") val attendeeName: String,
    @SerializedName("eventName") val eventName: String,
    @SerializedName("tierName") val tierName: String,
    @SerializedName("isUsed") val isUsed: Boolean
)
