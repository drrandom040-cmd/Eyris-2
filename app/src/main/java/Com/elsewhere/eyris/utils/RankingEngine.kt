package Com.elsewhere.eyris.utils

import Com.elsewhere.eyris.domain.models.Lead
import kotlin.math.roundToInt

object RankingEngine {
    fun rank(leads: List<Lead>): List<Lead> {
        // Bayesian Average: (v / (v + m)) * R + (m / (v + m)) * C
        // v = number of reviews, m = min reviews (10), R = rating, C = global avg (3.5)
        val m = 10.0
        val C = 3.5
        
        return leads.map { lead ->
            val v = lead.reviewCount.toDouble()
            val R = lead.rating
            val weightedScore = (v / (v + m)) * R + (m / (v + m)) * C
            lead.copy(weightedScore = weightedScore)
        }.sortedByDescending { it.weightedScore }
    }
}
