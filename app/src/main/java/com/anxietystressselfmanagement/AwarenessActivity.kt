package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class AwarenessActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var bodyButton: Button
    private lateinit var mindButton: Button
    private lateinit var feelingsButton: Button
    private lateinit var behaviorButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(R.layout.activity_awareness)

        bodyButton = findViewById(R.id.bodyButton)
        mindButton = findViewById(R.id.mindButton)
        feelingsButton = findViewById(R.id.feelingButton)
        behaviorButton = findViewById(R.id.behaviorButton)

        // Set click listeners to track the selected option and navigate
        bodyButton.setOnClickListener {
            saveAndNavigate("Body")
        }
        mindButton.setOnClickListener {
            saveAndNavigate("Mind")
        }
        feelingsButton.setOnClickListener {
            saveAndNavigate("Feelings")
        }
        behaviorButton.setOnClickListener {
            saveAndNavigate("Behavior")
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, SOTD::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveAndNavigate(selected: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val selectedOptionData: MutableMap<String, Any?> = hashMapOf(
                "signsOption" to selected
            )

            firestore.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
                .set(selectedOptionData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "$selected saved", Toast.LENGTH_SHORT).show()

                    // Navigate to the appropriate activity based on the selected option
                    val nextActivity = when (selected) {
                        "Body" -> BodyActivity::class.java
                        "Mind" -> MindActivity::class.java  // Replace with the actual activity
                        "Feelings" -> FeelingsActivity::class.java  // Replace with the actual activity
                        "Behavior" -> BehaviorActivity::class.java  // Replace with the actual activity
                        else -> throw IllegalArgumentException("Unknown option: $selected")
                    }

                    val intent = Intent(this, nextActivity)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save selected option: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
