package com.elsewhere.eyris.utils

import com.elsewhere.eyris.domain.models.Lead

object RankingEngine {

    fun calculateScore(
        rating: Double,
        reviews: Int
    ): Double {

        return (
            (reviews.toDouble() / (reviews + 10)) * rating
        ) + (
            (10.0 / (reviews + 10)) * 3.5
        )
    }

    fun rank(leads: List<Lead>): List<Lead> {
        return leads.map { lead ->
            val weightedScore = calculateScore(lead.rating, lead.reviewCount)
            lead.copy(weightedScore = weightedScore)
        }.sortedByDescending { it.weightedScore }
    }
}
