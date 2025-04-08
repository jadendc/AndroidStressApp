package com.anxietystressselfmanagement

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.strategies_and_actions_activity)

        strategySpinner = findViewById(R.id.strategySpinner)
        actionSpinner = findViewById(R.id.actionSpinner)
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)

        val strategyOptions = listOf("7 Stress Management Strategies...") + listOf(
            "Breathing: Deep, slow breaths to calm the mind",
            "Time Management: Plan and prioritize tasks",
            "Movement: Walk, stretch, or exercise",
            "Digital Detox: Limit screen time",
            "Social Connection: Talk to friends or family",
            "Gratitude: Focus on positive",
            "Relaxation: Meditate or listen to music"
        )

        val actionOptions = listOf("7 Stress Management Actions...") + listOf(
            "5-min deep breathing in the morning",
            "Plan tasks using a list or app",
            "10-min walk or stretch after lunch",
            "No screens 1 hour before bed",
            "Call or message a friend",
            "Write 3 things you’re grateful for",
            "Listen to relaxing music or meditation"
        )

        setupSpinner(strategySpinner, strategyOptions)
        setupSpinner(actionSpinner, actionOptions)

        backButton.setOnClickListener {
            startActivity(Intent(this, AwarenessActivity::class.java))
            finish()
        }

        continueButton.setOnClickListener {
            saveStrategiesAndActions()
        }
    }

    private fun setupSpinner(spinner: Spinner, options: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, options)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(resources.getColor(R.color.white))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun saveStrategiesAndActions() {
        val strategy = strategySpinner.selectedItem.toString()
        val action = actionSpinner.selectedItem.toString()

        if (strategy.startsWith("Select") || action.startsWith("Select")) {
            Toast.makeText(this, "Please select both a strategy and an action", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormatter.format(Date())

        val logData = mapOf(
            "7strategies" to strategy,
            "7actions" to action
        )

        firestore.collection("users")
            .document(userId)
            .collection("strategiesActions")
            .document(currentDate)
            .set(logData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
