package com.elsewhere.eyris.domain.models

data class Lead(
    val leadId: String = "",
    val userId: String = "",
    val businessName: String = "",
    val category: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val phone: String? = null,
    val email: String? = null,
    val coverImageUrl: String? = null,
    val openingHours: String? = null,
    val instagram: String? = null,
    val facebook: String? = null,
    val tiktok: String? = null,
    val whatsapp: String? = null,
    val hasWebsite: Boolean = false,
    val websiteUrl: String? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val weightedScore: Double = 0.0,
    val savedAt: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
    val synced: Boolean = false
)
