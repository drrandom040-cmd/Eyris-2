package Com.elsewhere.eyris.utils

import Com.elsewhere.eyris.domain.models.Lead

object MergeEngine {
    fun merge(lists: List<List<Lead>>): List<Lead> {
        // Implement fuzzy name matching and 50m distance threshold deduplication
        return lists.flatten().distinctBy { it.businessName } // Simple dedupe for now
    }
}
