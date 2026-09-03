package com.eventsphere.scanner.data.api.models

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("date") val date: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("tiers") val tiers: List<Tier>? = null
)

data class Tier(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("available") val available: Int
)

data class EventBasic(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)
