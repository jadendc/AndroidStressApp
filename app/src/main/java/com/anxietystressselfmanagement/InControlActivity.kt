package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class InControlActivity : AppCompatActivity() {

    private lateinit var control1Button: Button
    private lateinit var control2Button: Button
    private lateinit var control3Button: Button
    private lateinit var control4Button: Button
    private lateinit var control5Button: Button
    private lateinit var continueButton: Button
    private lateinit var backButton: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var controlLevel: Int = 0 // 0 = none selected, 1-5 = control levels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_in_control)

            // Initialize Firebase
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            // Initialize views with null checks
            control1Button = findViewById(R.id.control1Button) ?: return
            control2Button = findViewById(R.id.control2Button) ?: return
            control3Button = findViewById(R.id.control3Button) ?: return
            control4Button = findViewById(R.id.control4Button) ?: return
            control5Button = findViewById(R.id.control5Button) ?: return
            continueButton = findViewById(R.id.continueButton) ?: return
            backButton = findViewById(R.id.backButton) ?: return

            // Set up button click listeners
            setupButtonListeners()

            // Set up back button click listener
            backButton.setOnClickListener {
                // Navigate back to the previous screen (MoodActivity)
                val intent = Intent(this, MoodActivity::class.java)
                startActivity(intent)
                finish() // Close this activity
            }
        } catch (e: Exception) {
            // Log the error and show a helpful message
            Log.e("InControlActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Something went wrong. Please try again later.",
                Toast.LENGTH_SHORT).show()
            finish() // Close the activity if it can't initialize properly
        }
    }

    private fun setupButtonListeners() {
        // Control level selection buttons
        control1Button.setOnClickListener {
            updateSelectedControlLevel(1)
        }

        control2Button.setOnClickListener {
            updateSelectedControlLevel(2)
        }

        control3Button.setOnClickListener {
            updateSelectedControlLevel(3)
        }

        control4Button.setOnClickListener {
            updateSelectedControlLevel(4)
        }

        control5Button.setOnClickListener {
            updateSelectedControlLevel(5)
        }

        // Continue button
        continueButton.setOnClickListener {
            saveControlLevelToFirestore()
        }
    }

    private fun updateSelectedControlLevel(level: Int) {
        // Reset all buttons to default state
        control1Button.alpha = 1.0f
        control2Button.alpha = 1.0f
        control3Button.alpha = 1.0f
        control4Button.alpha = 1.0f
        control5Button.alpha = 1.0f

        // Highlight selected button
        when (level) {
            1 -> control1Button.alpha = 0.6f
            2 -> control2Button.alpha = 0.6f
            3 -> control3Button.alpha = 0.6f
            4 -> control4Button.alpha = 0.6f
            5 -> control5Button.alpha = 0.6f
        }

        // Update selected control level
        controlLevel = level

        // Enable continue button
        continueButton.isEnabled = true
    }

    private fun saveControlLevelToFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null && controlLevel > 0) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            // Show saving indicator
            Toast.makeText(this, "Saving your control level...",
                Toast.LENGTH_SHORT).show()

            // Create maps with control level data
            // Store both the original field name for backward compatibility
            // and a more descriptive field name for future use
            val controlDataUpdate = hashMapOf(
                "control" to controlLevel,
                "controlLevel" to controlLevel  // More descriptive field name for future use
            )

            // Save to Firestore - using the same path as your dashboard
            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
                .update("control", controlLevel, "controlLevel",
                    controlLevel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Control level saved successfully!",
                        Toast.LENGTH_SHORT).show()
                    Log.d("InControlActivity", "Control level updated in existing" +
                            " document")

                    // Navigate to the SOTD activity
                    val intent = Intent(this, SOTD::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("InControlActivity", "Error updating document, trying to" +
                            " create new one", e)

                    // The document might not exist yet if they somehow skipped mood selection
                    // In that case, we'll create a new document
                    val controlData = hashMapOf(
                        "control" to controlLevel,
                        "controlLevel" to controlLevel
                    )

                    db.collection("users")
                        .document(userId)
                        .collection("dailyLogs")
                        .document(today)
                        .set(controlData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Control level saved successfully!",
                                Toast.LENGTH_SHORT).show()
                            Log.d("InControlActivity", "Created new document with" +
                                    " control level")

                            // Navigate to the SOTD activity
                            val intent = Intent(this, SOTD::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e2 ->
                            Log.e("InControlActivity", "Error saving data:" +
                                    " ${e2.message}", e2)
                            Toast.makeText(this, "Failed to save your response:" +
                                    " ${e2.message}", Toast.LENGTH_LONG).show()
                        }
                }
        } else {
            Toast.makeText(this, "Please select a control level first",
                Toast.LENGTH_SHORT).show()
        }
    }
}