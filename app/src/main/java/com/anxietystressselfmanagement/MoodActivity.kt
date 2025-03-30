package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import android.widget.ImageView
import android.widget.Toast

class MoodActivity : AppCompatActivity() {

    private lateinit var angryButton: Button
    private lateinit var sadButton: Button
    private lateinit var neutralButton: Button
    private lateinit var happyButton: Button
    private lateinit var excitedButton: Button
    private lateinit var continueButton: Button
    private lateinit var backButton: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedMood: Int = 0 // 0 = none, 1 = angry, 2 = sad, 3 = neutral,
    // 4 = happy, 5 = excited

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        angryButton = findViewById(R.id.angryButton)
        sadButton = findViewById(R.id.sadButton)
        neutralButton = findViewById(R.id.neutralButton)
        happyButton = findViewById(R.id.happyButton)
        excitedButton = findViewById(R.id.excitedButton)
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)

        // Set up button click listeners
        setupButtonListeners()

        // Set up back button click listener
        backButton.setOnClickListener {
            // Navigate back to the previous screen (HomeActivity)
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
            finish() // Close this activity
        }
    }

    private fun setupButtonListeners() {
        // Mood selection buttons
        angryButton.setOnClickListener {
            updateSelectedMood(1)
        }

        sadButton.setOnClickListener {
            updateSelectedMood(2)
        }

        neutralButton.setOnClickListener {
            updateSelectedMood(3)
        }

        happyButton.setOnClickListener {
            updateSelectedMood(4)
        }

        excitedButton.setOnClickListener {
            updateSelectedMood(5)
        }

        // Continue button
        continueButton.setOnClickListener {
            saveMoodToFirestore()
        }
    }

    private fun updateSelectedMood(mood: Int) {
        // Reset all buttons to default state
        angryButton.alpha = 1.0f
        sadButton.alpha = 1.0f
        neutralButton.alpha = 1.0f
        happyButton.alpha = 1.0f
        excitedButton.alpha = 1.0f

        // Highlight selected button
        when (mood) {
            1 -> angryButton.alpha = 0.6f
            2 -> sadButton.alpha = 0.6f
            3 -> neutralButton.alpha = 0.6f
            4 -> happyButton.alpha = 0.6f
            5 -> excitedButton.alpha = 0.6f
        }

        // Update selected mood
        selectedMood = mood

        // Enable continue button
        continueButton.isEnabled = true
    }

    private fun saveMoodToFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null && selectedMood > 0) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val emojiValue = when(selectedMood) {
                1 -> "😢"  // Very sad
                2 -> "😔"  // Sad
                3 -> "😐"  // Neutral
                4 -> "😊"  // Happy
                5 -> "😁"  // Very happy
                else -> ""
            }

            // Create a map with the emoji to match dashboard
            val moodData = hashMapOf("feeling" to emojiValue)

            // Show saving indicator
            Toast.makeText(this, "Saving your mood...", Toast.LENGTH_SHORT).show()

            // Save to Firestore using the same path as the dashboard
            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
                .set(moodData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Mood saved successfully!",
                        Toast.LENGTH_SHORT).show()

                    // Navigate to InControlActivity
                    val intent = Intent(this, InControlActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Show error message with details
                    Toast.makeText(this, "Error saving mood: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    Log.e("MoodActivity", "Error saving to Firestore", e)
                }
        } else {
            Toast.makeText(this, "Please select a mood first",
                Toast.LENGTH_SHORT).show()
        }
    }
}