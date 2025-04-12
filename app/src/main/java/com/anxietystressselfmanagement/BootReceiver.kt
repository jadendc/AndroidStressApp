package com.anxietystressselfmanagement

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling notifications")
            rescheduleNotifications(context)
        }
    }

    private fun rescheduleNotifications(context: Context) {
        // Get saved preferences
        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val dailyReminderEnabled = prefs.getBoolean("dailyReminder", false)

        if (dailyReminderEnabled) {
            val useDefaultTime = prefs.getBoolean("useDefaultTime", true)
            val hour = if (useDefaultTime) 12 else prefs.getInt("hour", 12)
            val minute = if (useDefaultTime) 0 else prefs.getInt("minute", 0)

            // Create alarm intent
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_DAILY_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Set the alarm for the next occurrence
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)

                // If time has already passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            Log.d("BootReceiver", "Rescheduling notification for: ${calendar.time}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }
    }
}