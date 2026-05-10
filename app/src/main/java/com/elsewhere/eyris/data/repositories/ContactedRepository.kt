package com.elsewhere.eyris.data.repositories

import com.elsewhere.eyris.domain.models.ContactedLead
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactedRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String get() = auth.currentUser?.uid ?: ""

    private val contactedCollection = firestore.collection("contacted")

    suspend fun saveContacted(lead: ContactedLead) {
        val leadWithUser = lead.copy(userId = userId, lastUpdatedAt = System.currentTimeMillis())
        try {
            contactedCollection.document(lead.contactedId).set(leadWithUser).await()
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.WRITE, 
                "contacted/${lead.contactedId}", 
                auth
            )
        }
    }

    suspend fun getContactedLeads(): List<ContactedLead> {
        return try {
            contactedCollection.whereEqualTo("userId", userId)
                .get().await()
                .toObjects(ContactedLead::class.java)
                .sortedByDescending { it.contactedAt }
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.LIST, 
                "contacted", 
                auth
            )
            emptyList()
        }
    }
}
