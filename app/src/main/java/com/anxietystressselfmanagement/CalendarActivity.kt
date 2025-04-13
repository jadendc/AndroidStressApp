package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.CalendarView
import androidx.appcompat.widget.Toolbar
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Setup toolbar with back button
        setupToolbar()

        val continueButton: Button = findViewById(R.id.continueButton)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)

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
            val intent = Intent(this, MoodActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }
    }

    /**
     * Setup toolbar with back button
     */
    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Calendar"
    }

    /**
     * Handle back button press in the toolbar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Navigate back to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}