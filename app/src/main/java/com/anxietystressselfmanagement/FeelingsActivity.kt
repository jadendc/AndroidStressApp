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

class FeelingsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedFeeling: String? = null // Hold selected item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feelings)

        val backButton: ImageView = findViewById(R.id.backButton)
        val feelingsSpinner: Spinner = findViewById(R.id.feelingsSpinner)
        val continueButton: Button = findViewById(R.id.continueFeelingsButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, AwarenessActivity::class.java))
            finish()
        }

        val feelingOptions = listOf("This may look like... ") + listOf("Alienation", "Apathy", "Depression", "Fear", "Irritability", "Loss of confidence")
        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, feelingOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        feelingsSpinner.adapter = adapter

        feelingsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedFeeling = if (position != 0) feelingOptions[position] else null
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedFeeling = null
            }
        }

        continueButton.setOnClickListener {
            if (selectedFeeling != null) {
                saveSelectionToFirestore(selectedFeeling!!)
            } else {
                Toast.makeText(this, "Please select a feeling symptom", Toast.LENGTH_SHORT).show()
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
                    val intent = Intent(this, StrategiesAndActionsActivity::class.java) // Move to next screen
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
