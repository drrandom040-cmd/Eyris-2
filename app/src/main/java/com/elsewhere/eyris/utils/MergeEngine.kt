package com.elsewhere.eyris.utils

import com.elsewhere.eyris.domain.models.Lead
import kotlin.math.*

object MergeEngine {
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
                // 80% name similarity and within 50 meters
                nameSimilarity > 0.8 && distance < 50.0
            }

            if (duplicate == null) {
                mergedLeads.add(newLead)
            } else {
                // Optionally merge details (e.g., take the one with more reviews or better data)
                // For now, we just keep the first one found as per simple deduplication
            }
        }

        return mergedLeads
    }

    private fun calculateNameSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1.lowercase() else s2.lowercase()
        val shorter = if (s1.length > s2.length) s2.lowercase() else s1.lowercase()

        if (longer.isEmpty()) return 1.0

        val distance = levenshteinDistance(longer, shorter)
        return (longer.length - distance) / longer.length.toDouble()
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
        val r = 6371e3 // Earth radius in meters
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
