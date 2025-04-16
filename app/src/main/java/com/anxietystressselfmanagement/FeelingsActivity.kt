package com.anxietystressselfmanagement

import CustomSpinnerAdapter
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
    private var selectedFeeling: String? = null
    private var selectedDate: String = ""
    private var currentCustomFeeling: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feelings)

        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        val feelingsSpinner: Spinner = findViewById(R.id.feelingsSpinner)
        val continueButton: Button = findViewById(R.id.continueFeelingsButton)

        backButton.setOnClickListener {
            navigateToAwareness()
        }

        val feelingOptions = listOf("This may look like... ") +
                listOf("Alienation", "Apathy", "Depression", "Fear",
                    "Irritability", "Loss of confidence", "Custom...")

        val adapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_dropdown_item,
            R.layout.spinner_dropdown_item,
            feelingOptions
        ) { currentCustomFeeling }

        feelingsSpinner.adapter = adapter

        feelingsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (position == feelingOptions.size - 1) {
                    showCustomInputDialog(adapter)
                } else {
                    selectedFeeling = if (position != 0) feelingOptions[position] else null
                    currentCustomFeeling = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedFeeling = null
                currentCustomFeeling = null
            }
        }

        continueButton.setOnClickListener {
            val feelingToSave = currentCustomFeeling ?: selectedFeeling
            if (feelingToSave != null) {
                saveSelectionToFirestore(feelingToSave)
            } else {
                Toast.makeText(this, "Please select or enter a feeling symptom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCustomInputDialog(adapter: CustomSpinnerAdapter) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.customInputEditText)

        editText.setText(currentCustomFeeling)

        builder.setView(dialogView)
            .setTitle("Enter Custom Feeling")
            .setPositiveButton("Save") { dialog, _ ->
                val customText = editText.text.toString().trim()
                if (customText.isNotEmpty()) {
                    currentCustomFeeling = customText
                    selectedFeeling = null
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Custom feeling saved: $customText", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Custom text cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (currentCustomFeeling == null && selectedFeeling == null) {
                    val spinner: Spinner = findViewById(R.id.feelingsSpinner)
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

            val selectedData = mapOf("feelingSymptom" to selected) // Use specific key

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(selectedDate)
                .set(selectedData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "$selected saved", Toast.LENGTH_SHORT).show()
                    navigateToAwareness() // Go back to AwarenessActivity
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToAwareness() {
        val intent = Intent(this, AwarenessActivity::class.java)
        intent.putExtra("selectedDate", selectedDate)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Optional
        startActivity(intent)
        finish()
    }
}