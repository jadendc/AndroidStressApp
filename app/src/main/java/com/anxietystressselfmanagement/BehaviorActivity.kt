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

class BehaviorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedBehavior: String? = null  // Store selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_behavior)

        val backButton: ImageView = findViewById(R.id.backButton)
        val behaviorSpinner: Spinner = findViewById(R.id.behaviorSpinner)
        val continueButton: Button = findViewById(R.id.continueBehaviorButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, AwarenessActivity::class.java))
            finish()
        }

        val behaviorOptions = listOf("This may look like... ") +
                listOf("Accident prone", "Insomnia", "Loss of appetite", "Loss of sex drive", "More addiction", "Restlessness")

        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, behaviorOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        behaviorSpinner.adapter = adapter

        behaviorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBehavior = if (position != 0) behaviorOptions[position] else null
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedBehavior = null
            }
        }

        continueButton.setOnClickListener {
            if (selectedBehavior != null) {
                saveSelectionToFirestore(selectedBehavior!!)
            } else {
                Toast.makeText(this, "Please select a behavior symptom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSelectionToFirestore(selected: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val selectedData = mapOf("selectedSymptom" to selected)

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
                .set(selectedData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "$selected saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, StrategiesAndActionsActivity::class.java) // go to next screen
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
