package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class StrategiesAndActionsActivity : AppCompatActivity() {

    private lateinit var strategySpinner: Spinner
    private lateinit var actionSpinner: Spinner
    private lateinit var continueButton: Button
    private lateinit var backButton: ImageView
    private val firestore = FirebaseFirestore.getInstance()
    private var selectedDate: String = "" // Store selected date

    // Custom options for strategy and action
    private var customStrategy: String? = null
    private var customAction: String? = null

    // Define shortened labels for strategies and actions
    private val strategyShortenedLabels = mapOf(
        "Breathing: Deep, slow breaths to calm the mind" to "Breathing",
        "Time Management: Plan and prioritize tasks" to "Time Management",
        "Movement: Walk, stretch, or exercise" to "Movement",
        "Digital Detox: Limit screen time" to "Digital Detox",
        "Social Connection: Talk to friends or family" to "Social Connection",
        "Gratitude: Focus on positive" to "Gratitude",
        "Relaxation: Meditate or listen to music" to "Relaxation",
        "Custom..." to "Custom..."
    )

    private val actionShortenedLabels = mapOf(
        "5-min deep breathing in the morning" to "5-min Breathing",
        "Plan tasks using a list or app" to "Plan Tasks",
        "10-min walk or stretch after lunch" to "10-min Walk",
        "No screens 1 hour before bed" to "No Screens",
        "Call or message a friend" to "Call Friend",
        "Write 3 things you're grateful for" to "Gratitude Writing",
        "Listen to relaxing music or meditation" to "Relax Music",
        "Custom..." to "Custom..."
    )

    // Define prompt texts for easier validation
    private val strategyPrompt = "Select one..."
    private val actionPrompt = "Select one..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.strategies_and_actions_activity)

        // Get selected date from intent
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        strategySpinner = findViewById(R.id.strategySpinner)
        actionSpinner = findViewById(R.id.actionSpinner)
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)

        // List of full descriptions for strategies and actions
        val strategyOptions = listOf(strategyPrompt) + listOf(
            "Breathing: Deep, slow breaths to calm the mind",
            "Time Management: Plan and prioritize tasks",
            "Movement: Walk, stretch, or exercise",
            "Digital Detox: Limit screen time",
            "Social Connection: Talk to friends or family",
            "Gratitude: Focus on positive",
            "Relaxation: Meditate or listen to music",
            "Custom..."
        )

        val actionOptions = listOf(actionPrompt) + listOf(
            "5-min deep breathing in the morning",
            "Plan tasks using a list or app",
            "10-min walk or stretch after lunch",
            "No screens 1 hour before bed",
            "Call or message a friend",
            "Write 3 things you're grateful for",
            "Listen to relaxing music or meditation",
            "Custom..."
        )

        // Map full descriptions to shortened labels
        val shortenedStrategyOptions = strategyOptions.map { strategyShortenedLabels[it] ?: it }
        val shortenedActionOptions = actionOptions.map { actionShortenedLabels[it] ?: it }

        // Pass both options to the setupSpinner method
        setupSpinner(strategySpinner, shortenedStrategyOptions, strategyOptions)
        setupSpinner(actionSpinner, shortenedActionOptions, actionOptions)

        backButton.setOnClickListener {
            // We don't know which awareness option they chose, so let's go back to the main AwarenessActivity
            val intent = Intent(this, AwarenessActivity::class.java)
            intent.putExtra("selectedDate", selectedDate) // Pass date back
            startActivity(intent)
            finish()
        }

        continueButton.setOnClickListener {
            if (validateSelections()) {
                saveStrategiesAndActions()
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, shortenedOptions: List<String>,
                             fullOptions: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, shortenedOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Check if "Custom..." is selected
                if (position > 0 && fullOptions[position] == "Custom...") {
                    showCustomInputDialog(spinner === strategySpinner)
                } else if (position > 0) {
                    // Show full description when an item is selected (but not custom)
                    val fullDescription = fullOptions[position]
                    Toast.makeText(this@StrategiesAndActionsActivity, fullDescription,
                        Toast.LENGTH_LONG).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showCustomInputDialog(isStrategy: Boolean) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.customInputEditText)

        builder.setView(dialogView)
            .setTitle(if (isStrategy) "Enter Custom Strategy" else "Enter Custom Action")
            .setPositiveButton("Save") { dialog, _ ->
                val customText = editText.text.toString().trim()
                if (customText.isNotEmpty()) {
                    if (isStrategy) {
                        customStrategy = customText
                    } else {
                        customAction = customText
                    }
                    // Show confirmation
                    Toast.makeText(this, "Custom ${if (isStrategy) "strategy" else "action"} saved",
                        Toast.LENGTH_SHORT).show()
                } else {
                    // If empty, revert to first position (prompt)
                    if (isStrategy) {
                        strategySpinner.setSelection(0)
                    } else {
                        actionSpinner.setSelection(0)
                    }
                    Toast.makeText(this, "Custom text cannot be empty",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Revert to prompt position on cancel
                if (isStrategy) {
                    strategySpinner.setSelection(0)
                } else {
                    actionSpinner.setSelection(0)
                }
                dialog.cancel()
            }
            .setCancelable(false) // Prevent dismissing by clicking outside

        val dialog = builder.create()
        dialog.show()
    }

    private fun validateSelections(): Boolean {
        val strategyPosition = strategySpinner.selectedItemPosition
        val actionPosition = actionSpinner.selectedItemPosition

        // Check if either spinner is still at the first position (prompt text)
        if (strategyPosition == 0 || actionPosition == 0) {
            // Show more specific error message
            val message = when {
                strategyPosition == 0 && actionPosition == 0 -> "Please select both a strategy and an action"
                strategyPosition == 0 -> "Please select a strategy"
                else -> "Please select an action"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveStrategiesAndActions() {
        // Get the selected strategy and action
        val strategyPosition = strategySpinner.selectedItemPosition
        val actionPosition = actionSpinner.selectedItemPosition

        // Get the appropriate text for strategy and action
        val strategyText = if (strategySpinner.selectedItem.toString() == "Custom..." && customStrategy != null) {
            customStrategy!!
        } else {
            val strategyFullOptions = listOf(strategyPrompt) + listOf(
                "Breathing: Deep, slow breaths to calm the mind",
                "Time Management: Plan and prioritize tasks",
                "Movement: Walk, stretch, or exercise",
                "Digital Detox: Limit screen time",
                "Social Connection: Talk to friends or family",
                "Gratitude: Focus on positive",
                "Relaxation: Meditate or listen to music",
                "Custom..."
            )
            strategyFullOptions[strategyPosition]
        }

        val actionText = if (actionSpinner.selectedItem.toString() == "Custom..." && customAction != null) {
            customAction!!
        } else {
            val actionFullOptions = listOf(actionPrompt) + listOf(
                "5-min deep breathing in the morning",
                "Plan tasks using a list or app",
                "10-min walk or stretch after lunch",
                "No screens 1 hour before bed",
                "Call or message a friend",
                "Write 3 things you're grateful for",
                "Listen to relaxing music or meditation",
                "Custom..."
            )
            actionFullOptions[actionPosition]
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        val logData = mapOf(
            "7strategies" to strategyText,
            "7actions" to actionText
        )

        firestore.collection("users")
            .document(userId)
            .collection("dailyLogs")
            .document(selectedDate) // Use selectedDate instead of today
            .set(logData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("selectedDate", selectedDate) // Pass date to dashboard
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}