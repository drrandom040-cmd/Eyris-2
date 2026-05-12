package com.elsewhere.eyris.data.remote.providers.google

data class RawBusinessLead(
    val source: String,
    val sourceId: String,

    val name: String,
    val category: String?,

    val latitude: Double?,
    val longitude: Double?,

    val address: String?,
    val phone: String?,

    val website: String?,

    val rating: Double?,
    val reviewCount: Int?,

    val openingHours: List<String>?,

    val imageUrl: String?
)
