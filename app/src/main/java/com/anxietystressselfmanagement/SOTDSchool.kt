package com.anxietystressselfmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class SOTDSchool : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var allToggleButtons: List<ToggleButton>
    private var customStressorActive = false
    private val defaultButtonColor = Color.parseColor("#556874") // Match the XML background color
    private var selectedOption: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sotd_school)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, SOTD::class.java)
            startActivity(intent)
            finish()
        }

        // Get references to all toggle buttons
        val toggleFinancial = findViewById<ToggleButton>(R.id.button10)
        val toggleBullying = findViewById<ToggleButton>(R.id.button8)
        val toggleGrades = findViewById<ToggleButton>(R.id.button9)
        val toggleHomework = findViewById<ToggleButton>(R.id.button)
        val toggleExams = findViewById<ToggleButton>(R.id.button6)
        val toggleOrganization = findViewById<ToggleButton>(R.id.button7)

        // Add all toggle buttons to a list for easier management
        allToggleButtons = listOf(
            toggleFinancial, toggleBullying, toggleGrades,
            toggleHomework, toggleExams, toggleOrganization
        )

        // Initialize all toggle buttons (hide text and set default color)
        for (button in allToggleButtons) {
            button.textOn = ""
            button.textOff = ""
            button.text = ""
            button.setBackgroundColor(defaultButtonColor)
        }

        // Get references to other UI elements
        val customInput = findViewById<EditText>(R.id.customInput)
        val saveCustomButton = findViewById<Button>(R.id.saveCustom)

        // Set up toggle button listeners
        setupToggleButtons()

        // Set up custom stressor functionality
        saveCustomButton.setOnClickListener {
            val inputText = customInput.text.toString()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter text before saving", Toast.LENGTH_SHORT).show()
            } else {
                deselectAllToggles()
                customStressorActive = true
                Toast.makeText(this, "Custom stressor selected: $inputText", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up continue button
        val continueButton = findViewById<Button>(R.id.continueButtonSotdHome)
        continueButton.setOnClickListener {
            selectedOption = getSelectedStressor(customInput.text.toString())
            if (selectedOption.isNullOrEmpty()) {
                Toast.makeText(this, "Please select a stressor", Toast.LENGTH_SHORT).show()
            } else {
                saveSOTDSchoolToFirestore()
            }
        }
    }

    private fun setupToggleButtons() {
        for (toggleButton in allToggleButtons) {
            toggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    for (otherButton in allToggleButtons) {
                        if (otherButton != buttonView) {
                            otherButton.isChecked = false
                            otherButton.setBackgroundColor(defaultButtonColor)
                        }
                    }
                    customStressorActive = false
                    toggleButton.setBackgroundColor(Color.GREEN)
                } else {
                    toggleButton.setBackgroundColor(defaultButtonColor)
                }
            }
        }
    }

    private fun deselectAllToggles() {
        for (toggleButton in allToggleButtons) {
            toggleButton.isChecked = false
            toggleButton.setBackgroundColor(defaultButtonColor)
        }
    }

    private fun getSelectedStressor(customText: String): String {
        if (customStressorActive && customText.isNotEmpty()) {
            return "Custom: $customText"
        }
        val toggleLabels = listOf("Teachers", "Classmates", "Grades", "Homework", "Exams", "Deadlines")
        for (i in allToggleButtons.indices) {
            if (allToggleButtons[i].isChecked) {
                return toggleLabels[i]
            }
        }
        return ""
    }

    private fun saveSOTDSchoolToFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null && selectedOption != null) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val stressorData: MutableMap<String, Any?> = hashMapOf(
                "selectedOption" to selectedOption,
            )

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
                .update(stressorData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Stressor saved successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AwarenessActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    db.collection("users")
                        .document(userId)
                        .collection("dailyLogs")
                        .document(today)
                        .set(stressorData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Stressor saved successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AwarenessActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save stressor: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
        } else {
            Toast.makeText(this, "Please select a stressor", Toast.LENGTH_SHORT).show()
        }
    }
}
