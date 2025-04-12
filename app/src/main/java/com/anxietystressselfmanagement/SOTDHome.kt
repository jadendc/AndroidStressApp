package com.anxietystressselfmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class SOTDHome : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedOption: String? = null
    private lateinit var allToggleButtons: List<ToggleButton>
    private var customStressorActive = false
    private val defaultButtonColor = Color.parseColor("#556874")
    private var selectedDate: String = "" // Store selected date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sotd_home)

        // Get selected date from intent
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val backButton = findViewById<ImageView>(R.id.backButton)

        // Set a click listener to navigate back to SOTD activity
        backButton.setOnClickListener {
            val intent = Intent(this, SOTD::class.java)
            intent.putExtra("selectedDate", selectedDate) // Pass date back
            startActivity(intent)
            finish()
        }

        // Get references to all toggle buttons
        val togglePartner = findViewById<ToggleButton>(R.id.button)
        val toggleFamily = findViewById<ToggleButton>(R.id.button6)
        val toggleInLaws = findViewById<ToggleButton>(R.id.button7)
        val toggleFinancial = findViewById<ToggleButton>(R.id.button8)
        val toggleDomestic = findViewById<ToggleButton>(R.id.button9)
        val toggleSickness = findViewById<ToggleButton>(R.id.button10)

        // Add all toggle buttons to a list for easier management
        allToggleButtons = listOf(
            togglePartner, toggleFamily, toggleInLaws,
            toggleFinancial, toggleDomestic, toggleSickness
        )

        // Initialize all toggle buttons
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
                // Deselect all other toggle buttons
                deselectAllToggles()
                customStressorActive = true

                // Show confirmation toast
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
                saveSOTDHomeToFirestor()
            }
        }
    }

    private fun setupToggleButtons() {
        // Setup code remains the same
        for (toggleButton in allToggleButtons) {
            toggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // Deselect all other toggle buttons
                    for (otherButton in allToggleButtons) {
                        if (otherButton != buttonView) {
                            otherButton.isChecked = false
                            otherButton.setBackgroundColor(defaultButtonColor)
                        }
                    }

                    // Reset custom stressor if a toggle button is selected
                    customStressorActive = false

                    // Update selected button appearance
                    toggleButton.setBackgroundColor(Color.GREEN)
                } else {
                    // If being unchecked, reset to default color
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

    private fun saveSOTDHomeToFirestor() {
        val currentUser = auth.currentUser
        if (currentUser != null && selectedOption != null) {
            val userId = currentUser.uid

            // Show saving indicator
            Toast.makeText(this, "Saving your option selection...", Toast.LENGTH_SHORT).show()

            // Create map with home option data
            val homeOptionData: MutableMap<String, Any?> = hashMapOf(
                "selectedOption" to selectedOption,
            )

            // Save to Firestore using selectedDate instead of today
            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(selectedDate) // Use selectedDate instead of today
                .update(homeOptionData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Option saved successfully!", Toast.LENGTH_SHORT).show()
                    Log.d("SOTD", "Home option updated in existing document")

                    // Navigate to the Awareness activity with the date
                    val intent = Intent(this, AwarenessActivity::class.java)
                    intent.putExtra("selectedDate", selectedDate) // Pass date to next activity
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("SOTD", "Error updating document, trying to create new one", e)

                    // The document might not exist yet
                    db.collection("users")
                        .document(userId)
                        .collection("dailyLogs")
                        .document(selectedDate) // Use selectedDate here too
                        .set(homeOptionData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Option saved successfully!", Toast.LENGTH_SHORT).show()
                            Log.d("SOTD", "Created new document with home option")

                            // Navigate to the Awareness activity with the date
                            val intent = Intent(this, AwarenessActivity::class.java)
                            intent.putExtra("selectedDate", selectedDate) // Pass date
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e2 ->
                            Log.e("SOTD", "Error saving data: ${e2.message}", e2)
                            Toast.makeText(this, "Failed to save your response: ${e2.message}", Toast.LENGTH_LONG).show()
                        }
                }
        } else {
            Toast.makeText(this, "Please select an option first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedStressor(customText: String): String {
        // Existing code remains the same
        if (customStressorActive && customText.isNotEmpty()) {
            return "Custom: $customText"
        }

        val toggleLabels = listOf("Partner", "Family", "In Laws", "Financial", "Domestic Dispute", "Sickness")

        for (i in allToggleButtons.indices) {
            if (allToggleButtons[i].isChecked) {
                return toggleLabels[i]
            }
        }

        return ""
    }
}