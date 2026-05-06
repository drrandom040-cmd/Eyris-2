package com.elsewhere.eyris.domain.usecases

import com.elsewhere.eyris.domain.models.Lead
import com.elsewhere.eyris.domain.repository.LeadRepository
import javax.inject.Inject

class SearchBusinessesUseCase @Inject constructor(
    private val repository: LeadRepository
) {
    suspend operator fun invoke(location: String, category: String): List<Lead> {
        val results = repository.searchRemote(location, category)
        results.forEach { repository.saveLead(it) }
        return results
    }
}
