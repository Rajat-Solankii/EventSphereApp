package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: UserProfile
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)
