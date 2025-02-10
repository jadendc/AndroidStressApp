package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ExercisesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        val btnDestress: Button = findViewById(R.id.buttonDestress)
        val btnSleep: Button = findViewById(R.id.buttonEaseSleep)
        val btnFocus: Button = findViewById(R.id.buttonStrengthenFocus)
        val btnPyschSigh: Button = findViewById(R.id.buttonPsychologicalSigh)

        btnDestress.setOnClickListener {
            val intent = Intent(this, DestressActivity::class.java)
            startActivity(intent)
        }

        btnSleep.setOnClickListener {
            val intent = Intent(this, SleepActivity::class.java)
            startActivity(intent)
        }

        btnFocus.setOnClickListener {
            val intent = Intent(this, FocusActivity::class.java)
            startActivity(intent)
        }

        btnPyschSigh.setOnClickListener {
            val intent = Intent(this, PsychSighActivity::class.java)
            startActivity(intent)
        }

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java) // Navigate back to Exercises page
            startActivity(intent)
            finish() // Finish current activity to prevent stacking
        }
    }
}
