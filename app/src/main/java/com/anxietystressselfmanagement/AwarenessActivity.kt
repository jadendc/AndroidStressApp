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
            finish() // Finish AwarenessActivity when going back
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
                    val dataExists = document != null && document.exists()
                    var hasAnySymptom = false
                    if (dataExists) {
                        hasAnySymptom = document.contains("bodySymptom") ||
                                document.contains("mindSymptom") ||
                                document.contains("feelingSymptom") ||
                                document.contains("behaviorSymptom")
                    }

                    if (hasAnySymptom) {
                        // If symptoms exist, navigate directly
                        navigateToStrategies()
                    } else {
                        // If no symptoms (or document doesn't exist), show the warning dialog
                        showWarningDialog()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Could not verify entries: ${e.message}", Toast.LENGTH_SHORT).show()
                    showWarningDialog()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows a warning dialog if no signs are entered, allowing the user to proceed or cancel.
     */
    private fun showWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Signs Entered") // Updated Title slightly
            .setMessage("You haven't entered any signs (Body, Mind, Feelings, or Behavior) for this date. Do you want to continue without entering any signs?") // Updated Message
            .setPositiveButton("Continue Anyway") { dialog, _ ->
                // User chose to continue despite the warning
                navigateToStrategies()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // User chose to cancel, stay on this screen
                dialog.dismiss()
            }
            .setCancelable(true) // Allow dismissing by tapping outside
            .show()
    }

    /**
     * Navigates to the StrategiesAndActionsActivity.
     */
    private fun navigateToStrategies() {
        val intent = Intent(this, StrategiesActions::class.java)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
        finish()
    }
}