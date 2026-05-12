package com.elsewhere.eyris.domain.models

enum class SourceType {
    GOOGLE, FOURSQUARE, OSM
}

data class BusinessLead(
    val id: String,
    val source: SourceType,
    val name: String,
    val category: String?,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val city: String?,
    val country: String?,
    val phone: String?,
    val website: String?,
    val rating: Double?,
    val reviewCount: Int?,
    val imageUrl: String?,
    val instagram: String?,
    val facebook: String?,
    val confidence: Double
)
