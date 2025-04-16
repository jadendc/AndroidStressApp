package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AwarenessActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var bodyButton: Button
    private lateinit var mindButton: Button
    private lateinit var feelingsButton: Button
    private lateinit var behaviorButton: Button
    private lateinit var continueAwarenessButton: Button
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContentView(R.layout.activity_awareness)

        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        bodyButton = findViewById(R.id.bodyButton)
        mindButton = findViewById(R.id.mindButton)
        feelingsButton = findViewById(R.id.feelingButton)
        behaviorButton = findViewById(R.id.behaviorButton)
        continueAwarenessButton = findViewById(R.id.continueAwarenessButton)

        bodyButton.setOnClickListener {
            navigateToCategory(BodyActivity::class.java)
        }
        mindButton.setOnClickListener {
            navigateToCategory(MindActivity::class.java)
        }
        feelingsButton.setOnClickListener {
            navigateToCategory(FeelingsActivity::class.java)
        }
        behaviorButton.setOnClickListener {
            navigateToCategory(BehaviorActivity::class.java)
        }

        continueAwarenessButton.setOnClickListener {
            checkSymptomsAndProceed()
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, SOTD::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToCategory(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
    }

    private fun checkSymptomsAndProceed() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(selectedDate)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val hasBody = document.contains("bodySymptom")
                        val hasMind = document.contains("mindSymptom")
                        val hasFeelings = document.contains("feelingSymptom")
                        val hasBehavior = document.contains("behaviorSymptom")

                        if (hasBody || hasMind || hasFeelings || hasBehavior) {
                            navigateToStrategies()
                        } else {
                            showWarningDialog()
                        }
                    } else {
                        showWarningDialog()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking data: ${e.message}", Toast.LENGTH_SHORT).show()
                    showWarningDialog() // Show warning on error too, as we can't confirm data
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle("Incomplete Entry")
            .setMessage("Please enter at least one sign (Body, Mind, Feelings, or Behavior) before continuing.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToStrategies() {
        val intent = Intent(this, StrategiesAndActionsActivity::class.java)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
        finish() // Finish AwarenessActivity when proceeding
    }
}