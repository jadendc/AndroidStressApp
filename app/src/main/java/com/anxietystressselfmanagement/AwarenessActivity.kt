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
    private lateinit var bodyButton: Button
    private lateinit var mindButton: Button
    private lateinit var feelingsButton: Button
    private lateinit var behaviorButton: Button
    private lateinit var BMFBbut: Button
    private var selectedOption: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_awareness)

        bodyButton = findViewById(R.id.bodyButton)
        mindButton = findViewById(R.id.mindButton)
        feelingsButton = findViewById(R.id.feelingButton)
        behaviorButton = findViewById(R.id.behaviorButton)
        BMFBbut = findViewById(R.id.BMFBbut)

        // Set click listeners to track the selected option
        bodyButton.setOnClickListener {
            selectedOption = "Body"
        }
        mindButton.setOnClickListener {
            selectedOption = "Mind"
        }
        feelingsButton.setOnClickListener {
            selectedOption = "Feelings"
        }
        behaviorButton.setOnClickListener {
            selectedOption = "Behavior"
        }

        // Continue button functionality
        BMFBbut.setOnClickListener {
            if (selectedOption != null) {
                saveAwareness(selectedOption!!)
            } else {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, SOTD::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveAwareness(option: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val awarenessData = mapOf(
            "selectedOption" to option
        )

        firestore.collection("users")
            .document(userId)
            .collection("awarenessLogs")
            .document(currentDate)
            .set(awarenessData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "$option saved!", Toast.LENGTH_SHORT).show()

                // Navigate to the next activity (e.g., BodyActivity)
                val intent = Intent(this, BodyActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
