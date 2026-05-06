package Com.elsewhere.eyris.data.repositories

import Com.elsewhere.eyris.domain.models.Lead
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
        leadsCollection.document(lead.leadId).set(leadWithUser).await()
    }

    suspend fun getLeads(): List<Lead> {
        return leadsCollection.whereEqualTo("userId", userId)
            .get().await().toObjects(Lead::class.java)
    }

    suspend fun deleteLead(leadId: String) {
        leadsCollection.document(leadId).delete().await()
    }
}
