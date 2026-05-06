package com.elsewhere.eyris.data.repository

import com.elsewhere.eyris.data.local.AppDatabase
import com.elsewhere.eyris.data.local.entities.LeadEntity
import com.elsewhere.eyris.data.remote.scraper.FoursquareApi
import com.elsewhere.eyris.data.remote.scraper.GoogleMapsScraper
import com.elsewhere.eyris.data.remote.scraper.OsmOverpassApi
import com.elsewhere.eyris.domain.models.Lead
import com.elsewhere.eyris.domain.repository.LeadRepository
import com.elsewhere.eyris.utils.MergeEngine
import com.elsewhere.eyris.utils.RankingEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeadRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val googleMapsScraper: GoogleMapsScraper,
    private val foursquareApi: FoursquareApi,
    private val osmOverpassApi: OsmOverpassApi
) : LeadRepository {

    override fun getLeads(): Flow<List<Lead>> = db.leadDao.getAllLeads().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun saveLead(lead: Lead) {
        db.leadDao.insertLead(lead.toEntity())
    }

    override suspend fun deleteLead(lead: Lead) {
        db.leadDao.deleteLead(lead.toEntity())
    }

    override suspend fun searchRemote(location: String, category: String): List<Lead> {
        val googleResults = googleMapsScraper.search(location, category)
        val foursquareResults = foursquareApi.search(location, category)
        val osmResults = osmOverpassApi.search(location, category)
        
        val merged = MergeEngine.merge(listOf(googleResults, foursquareResults, osmResults))
        val ranked = RankingEngine.rank(merged)
        
        // Filter to businesses without websites as per spec
        return ranked.filter { !it.hasWebsite }.take(20)
    }

    private fun LeadEntity.toDomain() = Lead(
        leadId = leadId, businessName = businessName, category = category,
        address = address, lat = lat, lng = lng, hasWebsite = hasWebsite,
        rating = rating, reviewCount = reviewCount, weightedScore = weightedScore
    )

    private fun Lead.toEntity() = LeadEntity(
        leadId = leadId, userId = userId, businessName = businessName,
        category = category, address = address, lat = lat, lng = lng,
        phone = phone, email = email, coverImageUrl = coverImageUrl,
        openingHours = openingHours, instagram = instagram, facebook = facebook,
        tiktok = tiktok, whatsapp = whatsapp, hasWebsite = hasWebsite,
        websiteUrl = websiteUrl, rating = rating, reviewCount = reviewCount,
        weightedScore = weightedScore, savedAt = savedAt, searchQuery = searchQuery,
        synced = synced
    )
}
