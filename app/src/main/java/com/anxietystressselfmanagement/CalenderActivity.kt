package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.CalendarView
import android.widget.ImageView
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        val continueButton: Button = findViewById(R.id.continueButton)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Initialize default date to current date
        val calendar = Calendar.getInstance()
        var selectedDate = String.format(
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Update selectedDate when a date is picked from CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        // Set click listener for the continue button
        continueButton.setOnClickListener {
            val intent = Intent(this, LogListActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }

        // Set click listener for the back button
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
