package com.example.nfcampus.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "access_alerts_channel"
    private const val CHANNEL_NAME = "Access Alerts"
    private const val NOTIFICATION_ID = 1001

    fun showAccessNotification(context: Context, granted: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for access events"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = if (granted) "Access Granted" else "Access Denied"
        val content = if (granted) "Your NFC card was accepted" else "Your NFC card was not recognized"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setColor(if (granted) 0xFF2E7D32.toInt() else 0xFFC62828.toInt())
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}