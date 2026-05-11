package com.elsewhere.eyris.utils

import com.elsewhere.eyris.domain.models.Lead

object RankingEngine {
    /**
     * Exact Bayesian Average Formula:
     * Score = (reviews / (reviews + 10)) * rating + (10 / (reviews + 10)) * 3.5
     */
    fun rank(leads: List<Lead>): List<Lead> {
        return leads.map { lead ->
            val reviews = lead.reviewCount.toDouble()
            val rating = lead.rating

            // Bayesian Average
            val weightedScore = (reviews / (reviews + 10.0)) * rating + (10.0 / (reviews + 10.0)) * 3.5

            lead.copy(weightedScore = weightedScore)
        }.sortedByDescending { it.weightedScore }
    }
}
