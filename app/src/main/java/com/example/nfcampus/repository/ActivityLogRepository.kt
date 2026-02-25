package com.example.nfcampus.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date


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
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    // This function would be used by the web page's backend to fetch logs
    fun getLogsForUser(userId: String, onComplete: (List<LogEntry>) -> Unit) {
        logsCollection.document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.toObjects(LogEntry::class.java)
                onComplete(logs)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }
}