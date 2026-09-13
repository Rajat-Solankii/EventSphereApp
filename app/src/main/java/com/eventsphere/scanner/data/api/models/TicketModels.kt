package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("id") val id: String,
    @SerializedName("event") val event: EventBasic,
    @SerializedName("tier_name") val tier: String?,
    @SerializedName("attendee_name") val attendeeName: String?,
    @SerializedName("attendee_email") val attendeeEmail: String?,
    @SerializedName("status") val status: String,
    @SerializedName("payment_screenshot") val paymentScreenshot: String?
)

data class TicketInfo(
    @SerializedName("ticketId") val ticketId: String,
    @SerializedName("attendeeName") val attendeeName: String,
    @SerializedName("eventName") val eventName: String,
    @SerializedName("tierName") val tierName: String,
    @SerializedName("isUsed") val isUsed: Boolean
)
