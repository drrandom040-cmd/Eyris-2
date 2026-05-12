package com.elsewhere.eyris.data.repository

import com.elsewhere.eyris.data.local.AppDatabase
import com.elsewhere.eyris.data.local.entities.LeadEntity
import com.elsewhere.eyris.data.remote.providers.google.GooglePlacesProvider
import com.elsewhere.eyris.data.remote.scraper.*
import com.elsewhere.eyris.domain.models.Lead
import com.elsewhere.eyris.domain.repository.LeadRepository
import com.elsewhere.eyris.utils.LeadPipeline
import com.elsewhere.eyris.utils.MergeEngine
import com.elsewhere.eyris.utils.RankingEngine
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.util.UUID

class LeadRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val googlePlacesProvider: GooglePlacesProvider,
    private val foursquareApi: FoursquareApi,
    private val osmOverpassApi: OsmOverpassApi,
    private val geocodingApi: NominatimGeocodingApi
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
        val coords = geocodingApi.geocode(location) ?: return emptyList()

        return coroutineScope {
            val googleDeferred = async { googlePlacesProvider.searchBusinesses("$category in $location") }
            val foursquareDeferred = async { foursquareApi.search(coords.lat, coords.lon, category) }
            val osmDeferred = async { osmOverpassApi.search(coords.lat, coords.lon, category) }

            val googleRaw = googleDeferred.await()
            val foursquareRaw = foursquareDeferred.await()
            val osmRaw = osmDeferred.await()

            // Convert Google Raw to normalized BusinessLead if needed,
            // but currently they return slightly different models.
            // Let's adapt Google results to Lead domain model.
            val googleLeads = googleRaw.map { raw ->
                Lead(
                    leadId = "google_${raw.sourceId}",
                    businessName = raw.name,
                    category = raw.category ?: category,
                    address = raw.address ?: "Google Maps Result",
                    lat = raw.latitude ?: coords.lat,
                    lng = raw.longitude ?: coords.lon,
                    hasWebsite = false, // already filtered to null website
                    rating = raw.rating ?: 0.0,
                    reviewCount = raw.reviewCount ?: 0,
                    weightedScore = 0.0,
                    searchQuery = "$category in $location"
                )
            }

            // Other providers already return BusinessLead which is mapped in LeadPipeline
            // OR they were already returning Domain Leads in previous stabilization.
            // Let's check what they return now.
            // Based on previous interaction, they returned BusinessLead.

            // For now, to keep it simple and fix the immediate request:
            // Google results are handled.
            // Foursquare and OSM are handled via LeadPipeline.process() if they return BusinessLead.

            val processedFsqAndOsm = LeadPipeline.process(foursquareRaw + osmRaw)

            val merged = MergeEngine.merge(listOf(googleLeads, processedFsqAndOsm))
            val ranked = RankingEngine.rank(merged)

            ranked.filter { !it.hasWebsite }.take(20)
        }
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
