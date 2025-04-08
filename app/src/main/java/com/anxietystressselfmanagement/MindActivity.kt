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

class MindActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedMind: String? = null // Hold selected item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mind)

        val backButton: ImageView = findViewById(R.id.backButton)
        val mindSpinner: Spinner = findViewById(R.id.mindSpinner)
        val continueButton: Button = findViewById(R.id.continueMindButton)

        backButton.setOnClickListener {
            startActivity(Intent(this, AwarenessActivity::class.java))
            finish()
        }

        val mindOptions = listOf("This may look like... ") +
                listOf("Difficulty concentrating", "Impaired judgement", "Indecision", "Muddled thinking", "Negativity", "Worrying")

        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, mindOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        mindSpinner.adapter = adapter

        mindSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedMind = if (position != 0) mindOptions[position] else null
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMind = null
            }
        }

        continueButton.setOnClickListener {
            if (selectedMind != null) {
                saveSelectionToFirestore(selectedMind!!)
            } else {
                Toast.makeText(this, "Please select a mind symptom", Toast.LENGTH_SHORT).show()
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
