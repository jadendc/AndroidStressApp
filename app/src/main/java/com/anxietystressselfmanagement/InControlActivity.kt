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

class InControlActivity : AppCompatActivity(), ControlGaugeView.OnControlLevelSelectedListener {

    private lateinit var continueButton: Button
    private lateinit var backButton: ImageView
    private lateinit var controlGaugeView: ControlGaugeView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var controlLevel: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_in_control)

            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            continueButton = findViewById(R.id.continueButton)
            backButton = findViewById(R.id.backButton)
            controlGaugeView = findViewById(R.id.controlGaugeView)

            continueButton.isEnabled = false
            continueButton.alpha = 0.5f

            controlGaugeView.setOnControlLevelSelectedListener(this)

            backButton.setOnClickListener {
                startActivity(Intent(this, MoodActivity::class.java))
                finish()
            }

            continueButton.setOnClickListener {
                saveControlLevelToFirestore()
            }

        } catch (e: Exception) {
            Log.e("InControlActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onControlLevelSelected(level: Int) {
        updateSelectedControlLevel(level)
    }

    private fun updateSelectedControlLevel(level: Int) {
        controlLevel = level
        controlGaugeView.setControlLevel(level)

        continueButton.apply {
            isEnabled = true
            animate().alpha(1f).setDuration(300).start()
        }
    }

    private fun saveControlLevelToFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null && controlLevel > 0) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            Toast.makeText(this, "Saving your control level...", Toast.LENGTH_SHORT).show()

            val controlData = hashMapOf(
                "control" to controlLevel,
                "controlLevel" to controlLevel
            )

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
                .update("control", controlLevel, "controlLevel", controlLevel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Control level saved successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SOTD::class.java))
                    finish()
                }
                .addOnFailureListener {
                    db.collection("users")
                        .document(userId)
                        .collection("dailyLogs")
                        .document(today)
                        .set(controlData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Control level saved successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, SOTD::class.java))
                            finish()
                        }
                        .addOnFailureListener { e2 ->
                            Log.e("InControlActivity", "Error saving data: ${e2.message}", e2)
                            Toast.makeText(this, "Failed to save your response: ${e2.message}", Toast.LENGTH_LONG).show()
                        }
                }
        } else {
            Toast.makeText(this, "Please select a control level first", Toast.LENGTH_SHORT).show()
        }
    }
}
