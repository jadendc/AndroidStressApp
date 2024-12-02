package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FocusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus) // Ensure this matches the correct XML file

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java) // Navigate back to Exercises page
            startActivity(intent)
            finish() // Finish current activity to prevent stacking
        }
    }
}
