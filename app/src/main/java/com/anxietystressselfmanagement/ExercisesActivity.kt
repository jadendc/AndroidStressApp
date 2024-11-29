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

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, DashBoardActivity::class.java) // Replace with the appropriate activity
            startActivity(intent)
            finish()
        }

        // Button to navigate to "Destress Your Day" activity
        val buttonDestress: Button = findViewById(R.id.buttonDestress)
        buttonDestress.setOnClickListener {
            val intent = Intent(this, DestressActivity::class.java)
            startActivity(intent)
        }

        // Button to navigate to "Ease Your Sleep" activity
        val buttonEaseSleep: Button = findViewById(R.id.buttonEaseSleep)
        buttonEaseSleep.setOnClickListener {
            val intent = Intent(this, SleepActivity::class.java)
            startActivity(intent)
        }

        // Button to navigate to "Strengthen Your Focus" activity
        val buttonStrengthenFocus: Button = findViewById(R.id.buttonStrengthenFocus)
        buttonStrengthenFocus.setOnClickListener {
            val intent = Intent(this, FocusActivity::class.java)
            startActivity(intent)
        }
    }
}
