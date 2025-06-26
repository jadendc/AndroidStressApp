package com.anxietystressselfmanagement.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.anxietystressselfmanagement.ui.screens.StrategiesActionsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main entry point Activity for the Strategies and Actions screen.
 * It receives a selected date from the previous screen and passes it
 * to the Compose UI.
 */
class StrategiesActionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the date passed from the previous screen or use today's date as default
        val selectedDate = intent.getStringExtra("selectedDate")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Set the UI content using Jetpack Compose
        setContent {
            StrategiesActionsScreen(selectedDate)
        }
    }
}