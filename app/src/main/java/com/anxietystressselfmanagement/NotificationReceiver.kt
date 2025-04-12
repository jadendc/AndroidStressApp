package com.anxietystressselfmanagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "daily_reminder_channel"
        const val ACTION_DAILY_REMINDER = "com.anxietystressselfmanagement.DAILY_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received broadcast: ${intent.action}")

        // Either respond to our specific action or if no action is specified (for backward compatibility)
        if (intent.action == ACTION_DAILY_REMINDER || intent.action == null) {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders to log your daily information"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationReceiver", "Created notification channel")
            }

            // Create intent for when notification is tapped
            val contentIntent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Get icon resource ID safely
            val iconResId = try {
                context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Failed to find ic_notification, using app icon", e)
                context.applicationInfo.icon
            }

            // Use a valid icon resource ID
            val finalIconResId = if (iconResId != 0) iconResId else context.applicationInfo.icon

            // Build the notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Daily Check-in Reminder")
                .setContentText("Time to log your daily information and track your progress!")
                .setSmallIcon(finalIconResId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            // Show the notification
            notificationManager.notify(1, notification)
            Log.d("NotificationReceiver", "Notification displayed")
        } catch (e: Exception) {
            Log.e("NotificationReceiver", "Failed to show notification", e)
        }
    }
}