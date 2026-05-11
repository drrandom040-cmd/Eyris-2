package com.elsewhere.eyris.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elsewhere.eyris.data.repositories.ContactedRepository
import com.elsewhere.eyris.data.repositories.LeadsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncWorker (
    appContext: Context,
    workerParams: WorkerParameters,
    private val leadsRepository: LeadsRepository,
    private val contactedRepository: ContactedRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        return try {
            // Sync Leads
            val unsyncedLeads = leadsRepository.getLeadsSync().filter { !it.synced }
            unsyncedLeads.forEach { lead ->
                firestore.collection("leads").document(lead.leadId).set(lead.copy(synced = true)).await()
                leadsRepository.saveLead(lead.copy(synced = true))
            }

            // Sync Contacted
            val unsyncedContacted = contactedRepository.getContactedLeadsSync().filter { !it.synced }
            unsyncedContacted.forEach { contacted ->
                firestore.collection("contacted").document(contacted.contactedId).set(contacted.copy(synced = true)).await()
                contactedRepository.saveContacted(contacted.copy(synced = true))
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}
