package com.anxietystressselfmanagement.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.anxietystressselfmanagement.ui.screens.AwarenessMainScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AwarenessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the date passed from the previous screen or use today's date as default
        val selectedDate = intent.getStringExtra("selectedDate")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Set AwarenessMainScreen
        setContent {
            AwarenessMainScreen(selectedDate)
        }
    }
}