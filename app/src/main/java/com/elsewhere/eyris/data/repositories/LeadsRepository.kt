package com.elsewhere.eyris.data.repositories

import com.elsewhere.eyris.data.local.dao.LeadDao
import com.elsewhere.eyris.data.local.entities.LeadEntity
import com.elsewhere.eyris.domain.models.Lead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadsRepository @Inject constructor(
    private val leadDao: LeadDao
) {
    fun getAllLeads(): Flow<List<Lead>> = leadDao.getAllLeads().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveLead(lead: Lead) {
        leadDao.insertLead(lead.toEntity())
    }

    suspend fun deleteLead(leadId: String) {
        val lead = leadDao.getLeadById(leadId)
        if (lead != null) {
            leadDao.deleteLead(lead)
        }
    }

    suspend fun getLeadsSync(): List<Lead> {
        return leadDao.getAllLeadsSync().map { it.toDomain() }
    }

    private fun LeadEntity.toDomain() = Lead(
        leadId = leadId,
        userId = userId,
        businessName = businessName,
        category = category,
        address = address,
        lat = lat,
        lng = lng,
        phone = phone,
        email = email,
        coverImageUrl = coverImageUrl,
        openingHours = openingHours,
        instagram = instagram,
        facebook = facebook,
        tiktok = tiktok,
        whatsapp = whatsapp,
        hasWebsite = hasWebsite,
        websiteUrl = websiteUrl,
        rating = rating,
        reviewCount = reviewCount,
        weightedScore = weightedScore,
        savedAt = savedAt,
        searchQuery = searchQuery,
        synced = synced
    )

    private fun Lead.toEntity() = LeadEntity(
        leadId = leadId,
        userId = userId,
        businessName = businessName,
        category = category,
        address = address,
        lat = lat,
        lng = lng,
        phone = phone,
        email = email,
        coverImageUrl = coverImageUrl,
        openingHours = openingHours,
        instagram = instagram,
        facebook = facebook,
        tiktok = tiktok,
        whatsapp = whatsapp,
        hasWebsite = hasWebsite,
        websiteUrl = websiteUrl,
        rating = rating,
        reviewCount = reviewCount,
        weightedScore = weightedScore,
        savedAt = savedAt,
        searchQuery = searchQuery,
        synced = synced
    )
}
