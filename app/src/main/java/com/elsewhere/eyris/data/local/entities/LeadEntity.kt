package com.elsewhere.eyris.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey val leadId: String,
    val userId: String,
    val businessName: String,
    val category: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val phone: String?,
    val email: String?,
    val coverImageUrl: String?,
    val openingHours: String?,
    val instagram: String?,
    val facebook: String?,
    val tiktok: String?,
    val whatsapp: String?,
    val hasWebsite: Boolean,
    val websiteUrl: String?,
    val rating: Double,
    val reviewCount: Int,
    val weightedScore: Double,
    val savedAt: Long,
    val searchQuery: String,
    val synced: Boolean
)
