package com.elsewhere.eyris.utils

import com.elsewhere.eyris.domain.models.BusinessLead
import com.elsewhere.eyris.domain.models.Lead
import java.util.UUID
import kotlin.math.*

object LeadPipeline {

    fun process(leads: List<BusinessLead>): List<Lead> {
        val validLeads = leads.filter { isValidLead(it) }
        val deduplicated = deduplicate(validLeads)
        return deduplicated.map { it.toDomain() }
    }

    private fun isValidLead(lead: BusinessLead): Boolean {
        return lead.name.isNotBlank() && (lead.address != null || (lead.latitude != null && lead.longitude != null))
    }

    private fun deduplicate(leads: List<BusinessLead>): List<BusinessLead> {
        val result = mutableListOf<BusinessLead>()
        for (lead in leads) {
            val duplicate = result.find { existing ->
                val nameMatch = normalizeName(existing.name) == normalizeName(lead.name)
                val distance = if (existing.latitude != null && existing.longitude != null && lead.latitude != null && lead.longitude != null) {
                    calculateDistance(existing.latitude, existing.longitude, lead.latitude, lead.longitude)
                } else null

                nameMatch && (distance == null || distance < 100.0)
            }

            if (duplicate == null) {
                result.add(lead)
            } else {
                // Merge logic: keep the more complete record
                val index = result.indexOf(duplicate)
                if (isMoreComplete(lead, duplicate)) {
                    result[index] = lead
                }
            }
        }
        return result
    }

    private fun normalizeName(name: String): String {
        return name.lowercase().replace(Regex("[^a-z0-9]"), "")
    }

    private fun isMoreComplete(new: BusinessLead, old: BusinessLead): Boolean {
        var newScore = 0
        if (new.website != null) newScore++
        if (new.rating != null) newScore++
        if (new.reviewCount != null) newScore++
        if (new.address != null) newScore++

        var oldScore = 0
        if (old.website != null) oldScore++
        if (old.rating != null) oldScore++
        if (old.reviewCount != null) oldScore++
        if (old.address != null) oldScore++

        return newScore > oldScore
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val deltaPhi = (lat2 - lat1) * PI / 180
        val deltaLambda = (lon2 - lon1) * PI / 180

        val a = sin(deltaPhi / 2).pow(2.0) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    private fun BusinessLead.toDomain() = Lead(
        leadId = id,
        businessName = name,
        category = category ?: "General",
        address = address ?: "Location via Coordinates",
        lat = latitude ?: 0.0,
        lng = longitude ?: 0.0,
        hasWebsite = website != null,
        websiteUrl = website,
        rating = rating ?: 0.0,
        reviewCount = reviewCount ?: 0,
        weightedScore = 0.0, // Calculated later in ranking
        instagram = instagram,
        facebook = facebook
    )
}
