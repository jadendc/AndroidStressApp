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

class BodyActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedSymptom: String? = null  // Hold selection until Continue is clicked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body)

        val backButton: ImageView = findViewById(R.id.backButton)
        val bodySpinner: Spinner = findViewById(R.id.bodySpinner)
        val continueButton: Button = findViewById(R.id.continueBodyButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, AwarenessActivity::class.java))
            finish()
        }

        val bodyOptions = listOf("This may look like... ") +
                listOf("Difficulty breathing", "Fatigue", "Headaches", "High Blood Pressure", "Palpitations", "Skin irritations")

        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, bodyOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        bodySpinner.adapter = adapter

        bodySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSymptom = if (position != 0) bodyOptions[position] else null
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSymptom = null
            }
        }

        continueButton.setOnClickListener {
            if (selectedSymptom != null) {
                saveSelectionToFirestore(selectedSymptom!!)
            } else {
                Toast.makeText(this, "Please select a body symptom", Toast.LENGTH_SHORT).show()
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
                    // Go to StrategiesAndActionsActivity
                    val intent = Intent(this, StrategiesAndActionsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
