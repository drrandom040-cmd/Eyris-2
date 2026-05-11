package com.elsewhere.eyris.data.repositories

import com.elsewhere.eyris.data.local.dao.ContactedDao
import com.elsewhere.eyris.data.local.entities.ContactedEntity
import com.elsewhere.eyris.data.local.entities.LeadEntity
import com.elsewhere.eyris.domain.models.ContactedLead
import com.elsewhere.eyris.domain.models.LeadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactedRepository @Inject constructor(
    private val contactedDao: ContactedDao
) {
    fun getAllContacted(): Flow<List<ContactedLead>> = contactedDao.getAllContacted().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveContacted(lead: ContactedLead) {
        contactedDao.insertContacted(lead.toEntity())
    }

    suspend fun getContactedLeadsSync(): List<ContactedLead> {
        return contactedDao.getAllContactedSync().map { it.toDomain() }
    }

    private fun ContactedEntity.toDomain() = ContactedLead(
        contactedId = contactedId,
        userId = userId,
        businessName = lead.businessName,
        category = lead.category,
        address = lead.address,
        lat = lead.lat,
        lng = lead.lng,
        phone = lead.phone,
        email = lead.email,
        coverImageUrl = lead.coverImageUrl,
        openingHours = lead.openingHours,
        instagram = lead.instagram,
        facebook = lead.facebook,
        tiktok = lead.tiktok,
        whatsapp = lead.whatsapp,
        hasWebsite = lead.hasWebsite,
        websiteUrl = lead.websiteUrl,
        rating = lead.rating,
        reviewCount = lead.reviewCount,
        weightedScore = lead.weightedScore,
        status = status,
        contactedAt = contactedAt,
        lastUpdatedAt = lastUpdatedAt,
        notes = notes,
        socialHandleTapped = socialHandleTapped,
        synced = synced
    )

    private fun ContactedLead.toEntity() = ContactedEntity(
        contactedId = contactedId,
        userId = userId,
        lead = LeadEntity(
            leadId = contactedId,
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
            savedAt = contactedAt,
            searchQuery = "",
            synced = synced
        ),
        status = status,
        contactedAt = contactedAt,
        lastUpdatedAt = lastUpdatedAt,
        notes = notes,
        socialHandleTapped = socialHandleTapped,
        synced = synced
    )
}
