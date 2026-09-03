package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class AIChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("history") val history: List<AIChatMessage>? = null
)

data class AIChatMessage(
    @SerializedName("role") val role: String, // "user" or "assistant"
    @SerializedName("content") val content: String
)

data class AIChatResponse(
    @SerializedName("reply") val reply: String,
    @SerializedName("events") val events: List<AIChatEventData>? = null
)

data class AIChatEventData(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("tiers") val tiers: List<AIChatTier>? = null
)

data class AIChatTier(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double
)

data class AIChatHistoryResponse(
    @SerializedName("history") val history: List<AIChatMessage>
)
