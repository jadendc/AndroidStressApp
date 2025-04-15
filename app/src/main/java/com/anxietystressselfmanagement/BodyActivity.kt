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

class BodyActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedSymptom: String? = null
    private var selectedDate: String = "" // Store selected date
    private var currentCustomSymptom: String? = null // Store custom text

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body)

        // Get selected date from intent
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        val bodySpinner: Spinner = findViewById(R.id.bodySpinner)
        val continueButton: Button = findViewById(R.id.continueBodyButton)

        backButton.setOnClickListener {
            val intent = Intent(this, AwarenessActivity::class.java)
            intent.putExtra("selectedDate", selectedDate) // Pass date back
            startActivity(intent)
            finish()
        }

        val bodyOptions = listOf("This may look like... ") +
                listOf("Difficulty breathing", "Fatigue", "Headaches", "High Blood Pressure",
                    "Palpitations", "Skin irritations", "Custom...")

        // Use custom adapter instead of standard ArrayAdapter
        val adapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_dropdown_item,
            R.layout.spinner_dropdown_item,
            bodyOptions
        ) { currentCustomSymptom }

        bodySpinner.adapter = adapter

        bodySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == bodyOptions.size - 1) {
                    // "Custom..." option selected
                    showCustomInputDialog(adapter)
                } else {
                    selectedSymptom = if (position != 0) bodyOptions[position] else null
                    currentCustomSymptom = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSymptom = null
                currentCustomSymptom = null
            }
        }

        continueButton.setOnClickListener {
            if (selectedSymptom != null || currentCustomSymptom != null) {
                val symptomToSave = currentCustomSymptom ?: selectedSymptom!!
                saveSelectionToFirestore(symptomToSave)
            } else {
                Toast.makeText(this, "Please select a body symptom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Show dialog for custom input
    private fun showCustomInputDialog(adapter: CustomSpinnerAdapter) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.customInputEditText)

        editText.setText(currentCustomSymptom)

        builder.setView(dialogView)
            .setTitle("Enter Custom Symptom")
            .setPositiveButton("Save") { dialog, _ ->
                val customText = editText.text.toString().trim()
                if (customText.isNotEmpty()) {
                    currentCustomSymptom = customText
                    selectedSymptom = null
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Custom symptom saved: $customText", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Custom text cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (currentCustomSymptom == null) {
                    // If no previous custom text, reset selection
                    val spinner: Spinner = findViewById(R.id.bodySpinner)
                    spinner.setSelection(0)
                }
                dialog.cancel()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveSelectionToFirestore(selected: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val selectedData = mapOf("selectedSymptom" to selected)

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(selectedDate) // Use selectedDate instead of today
                .set(selectedData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "$selected saved", Toast.LENGTH_SHORT).show()
                    // Go to StrategiesAndActionsActivity
                    val intent = Intent(this, StrategiesAndActionsActivity::class.java)
                    intent.putExtra("selectedDate", selectedDate) // Pass date to next activity
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}