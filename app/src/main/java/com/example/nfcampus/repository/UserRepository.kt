package com.example.nfcampus.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.nfcampus.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun saveUser(user: User) {
        usersCollection.document(user.email).set(user).await()
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            usersCollection.document(email).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByIdentifier(identifier: String): User? {
        return try {
            // Query by studentId
            val studentIdQuery = usersCollection
                .whereEqualTo("studentId", identifier)
                .get()
                .await()

            if (!studentIdQuery.isEmpty) {
                return studentIdQuery.documents[0].toObject(User::class.java)
            }

            // If not found by studentId, query by email
            val emailQuery = usersCollection
                .whereEqualTo("email", identifier)
                .get()
                .await()

            if (!emailQuery.isEmpty) {
                return emailQuery.documents[0].toObject(User::class.java)
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            usersCollection.get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateUserNfcUid(email: String, nfcUid: String) {
        usersCollection.document(email).update("nfcUid", nfcUid).await()
    }

    suspend fun deleteUser(email: String) {
        usersCollection.document(email).delete().await()
    }
}