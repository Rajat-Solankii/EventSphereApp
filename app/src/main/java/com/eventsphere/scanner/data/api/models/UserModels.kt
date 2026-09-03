package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("organizationId") val organizationId: String? = null
)
