package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SleepActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Display the background GIF
        val backgroundGif: ImageView = findViewById(R.id.backgroundGif)
        Glide.with(this)
            .asGif()
            .load(R.raw.nightgif) // Replace with the SleepActivity-specific background GIF
            .into(backgroundGif)

        // Navigate to MusicChoiceActivity
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, MusicChoiceActivity::class.java)
            intent.putExtra("previousActivity", "SleepActivity") // Pass the previous activity identifier
            startActivity(intent)
            finish()
        }
    }
}
