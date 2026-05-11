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
                val name1 = normalizeName(existingLead.businessName)
                val name2 = normalizeName(newLead.businessName)
                val nameSimilarity = calculateSimilarity(name1, name2)

                val distance = calculateDistance(
                    existingLead.lat, existingLead.lng,
                    newLead.lat, newLead.lng
                )

                // Deterministic duplicate detection: similarity > 0.85 AND distance < 50m
                nameSimilarity > 0.85 && distance < 50.0
            }

            if (duplicate == null) {
                mergedLeads.add(newLead)
            } else {
                // Merge logic: prefer data from provider with more info
                // For now, if we find a duplicate, we keep the one with a website if the new one doesn't have it
                // or the one with higher review count.
                val index = mergedLeads.indexOf(duplicate)
                if (newLead.reviewCount > duplicate.reviewCount || (!duplicate.hasWebsite && newLead.hasWebsite)) {
                    mergedLeads[index] = newLead
                }
            }
        }

        return mergedLeads
    }

    private fun normalizeName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1

        val distance = Levenshtein.distance(longer, shorter)
        return (longer.length - distance) / longer.length.toDouble()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lat2 == 0.0) return 1000.0 // Default to far if no coordinates

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

object Levenshtein {
    fun distance(s1: String, s2: String): Int {
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
}
