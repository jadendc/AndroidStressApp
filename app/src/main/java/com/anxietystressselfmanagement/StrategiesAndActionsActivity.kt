package com.anxietystressselfmanagement

import CustomSpinnerAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var selectedDate: String = ""

    private val fullStrategyOptions = mutableListOf<String>()
    private val fullActionOptions = mutableListOf<String>()
    private val shortenedStrategyOptions = mutableListOf<String>()
    private val shortenedActionOptions = mutableListOf<String>()

    private var currentCustomStrategy: String? = null
    private var currentCustomAction: String? = null

    private lateinit var originalFullStrategyOptions: List<String>
    private lateinit var originalFullActionOptions: List<String>

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

    private val strategyPrompt = "Select one..."
    private val actionPrompt = "Select one..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.strategies_and_actions_activity)

        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        strategySpinner = findViewById(R.id.strategySpinner)
        actionSpinner = findViewById(R.id.actionSpinner)
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)

        setupOptions() // Initialize options lists
        setupSpinners() // Setup spinners with adapters and listeners

        backButton.setOnClickListener {
            val intent = Intent(this, AwarenessActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
            finish()
        }

        continueButton.setOnClickListener {
            if (validateSelections()) {
                saveStrategiesAndActions()
            }
        }
    }

    private fun setupOptions() {
        originalFullStrategyOptions = listOf(strategyPrompt) + listOf(
            "Breathing: Deep, slow breaths to calm the mind",
            "Time Management: Plan and prioritize tasks",
            "Movement: Walk, stretch, or exercise",
            "Digital Detox: Limit screen time",
            "Social Connection: Talk to friends or family",
            "Gratitude: Focus on positive",
            "Relaxation: Meditate or listen to music",
            "Custom..."
        )

        originalFullActionOptions = listOf(actionPrompt) + listOf(
            "5-min deep breathing in the morning",
            "Plan tasks using a list or app",
            "10-min walk or stretch after lunch",
            "No screens 1 hour before bed",
            "Call or message a friend",
            "Write 3 things you're grateful for",
            "Listen to relaxing music or meditation",
            "Custom..."
        )

        shortenedStrategyOptions.clear()
        shortenedStrategyOptions.addAll(originalFullStrategyOptions.map { strategyShortenedLabels[it] ?: it })

        shortenedActionOptions.clear()
        shortenedActionOptions.addAll(originalFullActionOptions.map { actionShortenedLabels[it] ?: it })

        fullStrategyOptions.clear()
        fullStrategyOptions.addAll(originalFullStrategyOptions)

        fullActionOptions.clear()
        fullActionOptions.addAll(originalFullActionOptions)
    }

    private fun setupSpinners() {
        val strategyAdapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_item_selected,
            R.layout.spinner_dropdown_item,
            shortenedStrategyOptions
        ) {
            if (isCustomStrategySelected()) currentCustomStrategy else null
        }
        strategySpinner.adapter = strategyAdapter

        val actionAdapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_item_selected,
            R.layout.spinner_dropdown_item,
            shortenedActionOptions
        ) {
            if (isCustomActionSelected()) currentCustomAction else null
        }
        actionSpinner.adapter = actionAdapter

        strategySpinner.onItemSelectedListener = createItemSelectedListener(
            isStrategy = true,
            adapter = strategyAdapter,
            originalFullOptions = originalFullStrategyOptions,
            shortenedOptions = shortenedStrategyOptions
        )

        actionSpinner.onItemSelectedListener = createItemSelectedListener(
            isStrategy = false,
            adapter = actionAdapter,
            originalFullOptions = originalFullActionOptions,
            shortenedOptions = shortenedActionOptions
        )
    }

    private fun createItemSelectedListener(
        isStrategy: Boolean,
        adapter: CustomSpinnerAdapter,
        originalFullOptions: List<String>,
        shortenedOptions: List<String>
    ): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedShortenedItem = shortenedOptions[position]

                // Check if "Custom..." is selected based on the shortened label
                if (selectedShortenedItem == strategyShortenedLabels["Custom..."]) {
                    showCustomInputDialog(isStrategy)
                } else {

                    if (isStrategy) {
                        if (currentCustomStrategy != null) {
                            currentCustomStrategy = null
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        if (currentCustomAction != null) {
                            currentCustomAction = null
                            adapter.notifyDataSetChanged()
                        }
                    }

                    if (position > 0 && selectedShortenedItem != strategyShortenedLabels["Custom..."]) {
                        val fullDescription = originalFullOptions[position]
                        Toast.makeText(
                            this@StrategiesAndActionsActivity,
                            fullDescription,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                if (isStrategy) {
                    currentCustomStrategy = null
                } else {
                    currentCustomAction = null
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showCustomInputDialog(isStrategy: Boolean) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.customInputEditText)
        val spinner = if (isStrategy) strategySpinner else actionSpinner
        val adapter = spinner.adapter as CustomSpinnerAdapter

        editText.setText(if (isStrategy) currentCustomStrategy else currentCustomAction)

        builder.setView(dialogView)
            .setTitle(if (isStrategy) "Enter Custom Strategy" else "Enter Custom Action")
            .setPositiveButton("Save") { dialog, _ ->
                val customText = editText.text.toString().trim()
                if (customText.isNotEmpty()) {
                    if (isStrategy) {
                        currentCustomStrategy = customText

                        val customPosition = shortenedStrategyOptions.indexOf(strategyShortenedLabels["Custom..."])
                        if (spinner.selectedItemPosition != customPosition) {
                            spinner.setSelection(customPosition, false)
                        }
                    } else {
                        currentCustomAction = customText
                        val customPosition = shortenedActionOptions.indexOf(actionShortenedLabels["Custom..."])
                        if (spinner.selectedItemPosition != customPosition) {
                            spinner.setSelection(customPosition, false)
                        }
                    }
                    adapter.notifyDataSetChanged()

                    Toast.makeText(
                        this,
                        "Custom ${if (isStrategy) "strategy" else "action"} saved: $customText",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    if (isStrategy) {
                        currentCustomStrategy = null
                    } else {
                        currentCustomAction = null
                    }
                    spinner.setSelection(0)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Custom text cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() // Dismiss dialog
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                val previousCustomText = if (isStrategy) currentCustomStrategy else currentCustomAction
                if (previousCustomText == null) {
                    spinner.setSelection(0)
                }
                adapter.notifyDataSetChanged()

                dialog.cancel()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }

    private fun validateSelections(): Boolean {
        val strategyPosition = strategySpinner.selectedItemPosition
        val actionPosition = actionSpinner.selectedItemPosition
        val isStrategyPrompt = strategyPosition == 0
        val isActionPrompt = actionPosition == 0

        val isStrategyCustomInvalid = isCustomStrategySelected() && currentCustomStrategy.isNullOrBlank()
        val isActionCustomInvalid = isCustomActionSelected() && currentCustomAction.isNullOrBlank()


        if (isStrategyPrompt || isActionPrompt || isStrategyCustomInvalid || isActionCustomInvalid) {
            val message = when {
                isStrategyPrompt && isActionPrompt -> "Please select both a strategy and an action"
                isStrategyPrompt -> "Please select a strategy"
                isActionPrompt -> "Please select an action"
                isStrategyCustomInvalid -> "Please enter or re-select a valid custom strategy"
                isActionCustomInvalid -> "Please enter or re-select a valid custom action"
                else -> "Please complete your selections" // Fallback
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isCustomStrategySelected(): Boolean {
        val pos = strategySpinner.selectedItemPosition
        return pos >= 0 && pos < shortenedStrategyOptions.size &&
                shortenedStrategyOptions[pos] == strategyShortenedLabels["Custom..."]
    }

    private fun isCustomActionSelected(): Boolean {
        val pos = actionSpinner.selectedItemPosition
        return pos >= 0 && pos < shortenedActionOptions.size &&
                shortenedActionOptions[pos] == actionShortenedLabels["Custom..."]
    }

    private fun saveStrategiesAndActions() {
        // Determine the text to save
        val strategyToSave: String
        if (isCustomStrategySelected() && !currentCustomStrategy.isNullOrBlank()) {
            strategyToSave = currentCustomStrategy!!
        } else {
            val position = strategySpinner.selectedItemPosition
            strategyToSave = if (position >= 0 && position < originalFullStrategyOptions.size) {
                originalFullStrategyOptions[position]
            } else {
                "Error: Invalid Strategy"
            }
        }

        val actionToSave: String
        if (isCustomActionSelected() && !currentCustomAction.isNullOrBlank()) {
            actionToSave = currentCustomAction!!
        } else {
            val position = actionSpinner.selectedItemPosition
            actionToSave = if (position >= 0 && position < originalFullActionOptions.size) {
                originalFullActionOptions[position]
            } else {
                "Error: Invalid Action"
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid
        val logData = mapOf(
            "7strategies" to strategyToSave,
            "7actions" to actionToSave
        )

        firestore.collection("users")
            .document(userId)
            .collection("dailyLogs")
            .document(selectedDate)
            .set(logData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("selectedDate", selectedDate)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}