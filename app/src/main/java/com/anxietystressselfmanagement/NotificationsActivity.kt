package com.anxietystressselfmanagement

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    private lateinit var switchDaily: Switch
    private lateinit var switchDefaultTime: Switch
    private lateinit var switchTimeFormat: Switch
    private lateinit var timePicker: TimePicker
    private lateinit var btnSave: Button
    private lateinit var backButton: ImageView
    private lateinit var btnTestNotification: Button
    private lateinit var toolbarTitle: TextView

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("NotificationsActivity", "Notification permission granted")
        } else {
            Log.d("NotificationsActivity", "Notification permission denied")
            Toast.makeText(
                this,
                "Notification permission denied. You won't receive reminders.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Initialize views
        switchDaily = findViewById(R.id.switchDaily)
        switchDefaultTime = findViewById(R.id.switchDefaultTime)
        switchTimeFormat = findViewById(R.id.switchTimeFormat)
        timePicker = findViewById(R.id.timePicker)
        btnSave = findViewById(R.id.btnSaveSettings)
        backButton = findViewById(R.id.backButton)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        // Set up the toolbar without a title (we'll use our custom TextView)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Check and request notification permission for Android 13+
        checkNotificationPermission()

        // Check for Alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Prompt user to grant permission
                Toast.makeText(
                    this,
                    "Please allow exact alarm scheduling for reliable reminders",
                    Toast.LENGTH_LONG
                ).show()

                try {
                    // Open settings page for our app
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    })
                } catch (e: Exception) {
                    Log.e("NotificationsActivity", "Failed to open alarm settings", e)
                }
            }
        }

        // Set up back button click listener
        backButton.setOnClickListener {
            // Simply finish this activity to return to the previous one (SettingActivity)
            finish()
        }

        // Set up time format toggle
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val use24HourFormat = sharedPreferences.getBoolean("use24HourFormat", true)
        switchTimeFormat.isChecked = use24HourFormat
        timePicker.setIs24HourView(use24HourFormat)

        switchTimeFormat.setOnCheckedChangeListener { _, isChecked ->
            timePicker.setIs24HourView(isChecked)
            sharedPreferences.edit().putBoolean("use24HourFormat", isChecked).apply()
        }

        // Set up UI toggle behaviors
        switchDaily.setOnCheckedChangeListener { _, isChecked ->
            val defaultTimeLayout = findViewById<LinearLayout>(R.id.defaultTimeLayout)
            defaultTimeLayout.visibility = if (isChecked) LinearLayout.VISIBLE else LinearLayout.GONE

            // If daily reminder is turned off, hide custom time layout as well
            if (!isChecked) {
                findViewById<LinearLayout>(R.id.customTimeLayout).visibility = LinearLayout.GONE
            } else if (!switchDefaultTime.isChecked) {
                findViewById<LinearLayout>(R.id.customTimeLayout).visibility = LinearLayout.VISIBLE
            }

            // If enabling notifications, check permission
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkNotificationPermission()
            }
        }

        switchDefaultTime.setOnCheckedChangeListener { _, isChecked ->
            val customTimeLayout = findViewById<LinearLayout>(R.id.customTimeLayout)
            customTimeLayout.visibility = if (!isChecked && switchDaily.isChecked) LinearLayout.VISIBLE else LinearLayout.GONE
            timePicker.isEnabled = !isChecked
        }

        btnSave.setOnClickListener {
            saveSettings()
        }

        // Setup test notification button if it exists in layout
        findViewById<Button?>(R.id.btnTestNotification)?.let { button ->
            btnTestNotification = button
            button.setOnClickListener {
                testNotification()
            }
        }

        loadSettings()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    Log.d("NotificationsActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain to the user why we need this permission
                    Toast.makeText(
                        this,
                        "Notification permission is needed to send you daily reminders",
                        Toast.LENGTH_LONG
                    ).show()

                    // Then request the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request the permission directly
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    fun testNotification() {
        // Create an intent with our action
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_DAILY_REMINDER
        }

        // Send the broadcast immediately
        sendBroadcast(intent)

        Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show()
        Log.d("NotificationsActivity", "Test notification broadcast sent")
    }

    private fun saveSettings() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("dailyReminder", switchDaily.isChecked)
            putBoolean("useDefaultTime", switchDefaultTime.isChecked)
            putBoolean("use24HourFormat", switchTimeFormat.isChecked)
            putInt("hour", timePicker.hour)
            putInt("minute", timePicker.minute)
            apply()
        }

        if (switchDaily.isChecked) {
            // Check permission before scheduling
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        this,
                        "Notification permission required for reminders",
                        Toast.LENGTH_LONG
                    ).show()
                    checkNotificationPermission()
                    return
                }
            }

            scheduleNotifications()
            Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show()
        } else {
            cancelNotifications()
            Toast.makeText(this, "Reminder cancelled!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val dailyReminder = sharedPreferences.getBoolean("dailyReminder", false)
        val useDefaultTime = sharedPreferences.getBoolean("useDefaultTime", true)
        val use24HourFormat = sharedPreferences.getBoolean("use24HourFormat", true)
        val hour = sharedPreferences.getInt("hour", 12)
        val minute = sharedPreferences.getInt("minute", 0)

        switchDaily.isChecked = dailyReminder
        switchDefaultTime.isChecked = useDefaultTime
        switchTimeFormat.isChecked = use24HourFormat
        timePicker.setIs24HourView(use24HourFormat)
        timePicker.hour = hour
        timePicker.minute = minute
        timePicker.isEnabled = !useDefaultTime

        // Update visibility of settings based on loaded preferences
        val defaultTimeLayout = findViewById<LinearLayout>(R.id.defaultTimeLayout)
        val customTimeLayout = findViewById<LinearLayout>(R.id.customTimeLayout)

        defaultTimeLayout.visibility = if (dailyReminder) LinearLayout.VISIBLE else LinearLayout.GONE
        customTimeLayout.visibility = if (dailyReminder && !useDefaultTime) LinearLayout.VISIBLE else LinearLayout.GONE
    }

    private fun scheduleNotifications() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Create an intent with explicit action
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_DAILY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, if (switchDefaultTime.isChecked) 12 else timePicker.hour)
            set(Calendar.MINUTE, if (switchDefaultTime.isChecked) 0 else timePicker.minute)
            set(Calendar.SECOND, 0)

            // If time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("NotificationsActivity", "Scheduling notification for: ${calendar.time}")

        try {
            // For Android 12 (API 31+) use canScheduleExactAlarms check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fall back to inexact alarm
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )

                    Toast.makeText(
                        this,
                        "For exact time reminders, enable Alarms & Reminders permission in app settings",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            // For Android 6-11 (API 23-30)
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            // For older Android versions
            else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }

            // Use appropriate time format based on user preference
            val timeFormat = if (switchTimeFormat.isChecked) {
                SimpleDateFormat("HH:mm", Locale.getDefault()) // 24-hour format
            } else {
                SimpleDateFormat("h:mm a", Locale.getDefault()) // 12-hour format with AM/PM
            }

            Toast.makeText(
                this,
                "Reminder set for ${timeFormat.format(calendar.time)}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // Log the exception and show a toast
            Log.e("NotificationsActivity", "Failed to schedule notification", e)
            Toast.makeText(
                this,
                "Failed to set reminder: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cancelNotifications() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}