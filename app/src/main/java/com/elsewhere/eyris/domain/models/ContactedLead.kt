package com.elsewhere.eyris.domain.models

enum class ContactStatus {
    ANSWERED, ACCEPTED, REJECTED, GHOSTED
}

data class ContactedLead(
    val contactedId: String = "",
    val userId: String = "",
    // Business fields (duplicated from Lead)
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
    // CRM fields
    val status: ContactStatus = ContactStatus.ANSWERED,
    val contactedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val socialHandleTapped: String? = null,
    val synced: Boolean = false
)
