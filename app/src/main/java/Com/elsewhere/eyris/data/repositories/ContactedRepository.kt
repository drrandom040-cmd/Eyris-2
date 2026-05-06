package Com.elsewhere.eyris.data.repositories

import Com.elsewhere.eyris.domain.models.ContactedLead
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
        contactedCollection.document(lead.contactedId).set(leadWithUser).await()
    }

    suspend fun getContactedLeads(): List<ContactedLead> {
        return contactedCollection.whereEqualTo("userId", userId)
            .orderBy("contactedAt", Query.Direction.DESCENDING)
            .get().await().toObjects(ContactedLead::class.java)
    }
}
