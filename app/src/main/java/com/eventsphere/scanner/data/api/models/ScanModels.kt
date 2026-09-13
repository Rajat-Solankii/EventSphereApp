package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("ticketId") val ticketId: String
)

data class ScanResult(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("attendee") val attendee: AttendeeBasic? = null,
    @SerializedName("ticketInfo") val ticketInfo: TicketInfo? = null
)

data class AttendeeBasic(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("roll_number") val rollNumber: String?,
    @SerializedName("tier_name") val tierName: String?,
    @SerializedName("payment_screenshot") val paymentScreenshot: String?,
    @SerializedName("exit_image") val exitImage: String? = null
)

data class TempExitRequest(
    @SerializedName("exit_image") val exitImage: String
)
