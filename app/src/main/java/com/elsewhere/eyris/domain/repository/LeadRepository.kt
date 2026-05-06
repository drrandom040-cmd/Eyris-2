package com.elsewhere.eyris.domain.repository

import com.elsewhere.eyris.domain.models.Lead
import kotlinx.coroutines.flow.Flow

interface LeadRepository {
    fun getLeads(): Flow<List<Lead>>
    suspend fun saveLead(lead: Lead)
    suspend fun deleteLead(lead: Lead)
    suspend fun searchRemote(location: String, category: String): List<Lead>
}
