package com.elsewhere.eyris.data.local.dao

import androidx.room.*
import com.elsewhere.eyris.data.local.entities.LeadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY savedAt DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)

    @Query("SELECT * FROM leads WHERE leadId = :id")
    suspend fun getLeadById(id: String): LeadEntity?
}
