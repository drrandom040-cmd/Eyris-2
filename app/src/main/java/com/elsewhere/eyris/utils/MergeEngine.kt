package com.elsewhere.eyris.utils

import com.elsewhere.eyris.data.remote.providers.google.RawBusinessLead
import com.elsewhere.eyris.domain.models.Lead
import kotlin.math.*

object MergeEngine {

    fun dedupe(
        businesses: List<RawBusinessLead>
    ): List<RawBusinessLead> {

        return businesses.distinctBy {
            it.name.lowercase().trim()
        }
    }

    fun merge(lists: List<List<Lead>>): List<Lead> {
        val allLeads = lists.flatten()
        if (allLeads.isEmpty()) return emptyList()

        val mergedLeads = mutableListOf<Lead>()

        for (newLead in allLeads) {
            val duplicate = mergedLeads.find { existingLead ->
                val nameSimilarity = calculateNameSimilarity(existingLead.businessName, newLead.businessName)
                val distance = calculateDistance(
                    existingLead.lat, existingLead.lng,
                    newLead.lat, newLead.lng
                )
                // 85% name similarity and within 50 meters
                nameSimilarity > 0.85 && distance < 50.0
            }

            if (duplicate == null) {
                mergedLeads.add(newLead)
            } else {
                val index = mergedLeads.indexOf(duplicate)
                if (newLead.reviewCount > duplicate.reviewCount || (!duplicate.hasWebsite && newLead.hasWebsite)) {
                    mergedLeads[index] = newLead
                }
            }
        }

        return mergedLeads
    }

    private fun calculateNameSimilarity(s1: String, s2: String): Double {
        val n1 = normalizeName(s1)
        val n2 = normalizeName(s2)
        if (n1 == n2) return 1.0
        if (n1.isEmpty() || n2.isEmpty()) return 0.0

        val longer = if (n1.length > n2.length) n1 else n2
        val shorter = if (n1.length > n2.length) n2 else n1

        val distance = levenshteinDistance(longer, shorter)
        return (longer.length - distance) / longer.length.toDouble()
    }

    private fun normalizeName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lat2 == 0.0) return 1000.0
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
}
