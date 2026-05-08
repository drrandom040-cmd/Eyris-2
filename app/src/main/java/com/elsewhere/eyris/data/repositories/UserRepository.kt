package com.elsewhere.eyris.data.repositories

import com.elsewhere.eyris.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")

    suspend fun saveUser(user: User) {
        try {
            usersCollection.document(user.userId).set(user).await()
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.WRITE, 
                "users/${user.userId}", 
                auth
            )
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            com.elsewhere.eyris.utils.FirestoreUtils.handleFirestoreError(
                e, 
                com.elsewhere.eyris.utils.OperationType.GET, 
                "users/$userId", 
                auth
            )
            null
        }
    }
}
