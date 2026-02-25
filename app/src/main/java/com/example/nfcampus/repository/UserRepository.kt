package com.example.nfcampus.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.nfcampus.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun saveUser(user: User) {
        // Use the user's secure UID as the document ID
        usersCollection.document(user.uid).set(user)
    }

    suspend fun getUserByUid(uid: String): User? {
        return try {
            val document = usersCollection.document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null // Return null if there's an error or the document doesn't exist
        }
    }
}