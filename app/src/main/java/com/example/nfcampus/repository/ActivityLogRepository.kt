package com.example.nfcampus.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date
import android.provider.Settings



data class LogEntry(
    val userId: String = "",
    val action: String = "",
    val timestamp: Date = Date()
)
class ActivityLogRepository {
    private val db = FirebaseFirestore.getInstance()
    private val logsCollection = db.collection("activity_logs")

    fun addLog(userId: String, action: String) {
        val logEntry = LogEntry(
            userId = userId,
            action = action
        )
        // Use the user's ID as the document ID for easy querying
        logsCollection.document(userId).collection("entries").add(logEntry)
            .addOnSuccessListener {
                // Log successfully added
                Log.d("LogDebug", "Successfully added log for $userId")
            }
            .addOnFailureListener { e ->
                // Handle error
                Log.e("LogDebug", "Failed to add log", e)
            }
    }

    // This function would be used by the web page's backend to fetch logs
    fun getLogsForUser(userId: String, onComplete: (List<LogEntry>) -> Unit) {
        db.collection("activity_logs")
            .whereEqualTo("userId", userId) // Filter by the specific user
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100) // Fetch the last 100 logs
            .get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.toObjects(LogEntry::class.java)
                Log.d("LogDebug", "Fetched ${logs.size} logs for user $userId")
                onComplete(logs)
            }
            .addOnFailureListener { e ->
                Log.e("LogDebug", "Failed to fetch logs", e)
                onComplete(emptyList())
            }
    }
}