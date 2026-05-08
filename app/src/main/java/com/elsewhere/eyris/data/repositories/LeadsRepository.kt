package com.elsewhere.eyris.data.repositories

import com.elsewhere.eyris.domain.models.Lead
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String get() = auth.currentUser?.uid ?: ""

    private val leadsCollection = firestore.collection("leads")

    suspend fun saveLead(lead: Lead) {
        val leadWithUser = lead.copy(userId = userId)
        try {
            leadsCollection.document(lead.leadId).set(leadWithUser).await()
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.WRITE, 
                "leads/${lead.leadId}", 
                auth
            )
        }
    }

    suspend fun getLeads(): List<Lead> {
        return try {
            leadsCollection.whereEqualTo("userId", userId)
                .get().await().toObjects(Lead::class.java)
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.LIST, 
                "leads", 
                auth
            )
            emptyList()
        }
    }

    suspend fun deleteLead(leadId: String) {
        try {
            leadsCollection.document(leadId).delete().await()
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.DELETE, 
                "leads/$leadId", 
                auth
            )
        }
    }
}
