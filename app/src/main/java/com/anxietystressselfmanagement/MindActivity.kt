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

class MindActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedMind: String? = null
    private var selectedDate: String = ""
    private var currentCustomMind: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mind)

        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        val mindSpinner: Spinner = findViewById(R.id.mindSpinner)
        val continueButton: Button = findViewById(R.id.continueMindButton)

        backButton.setOnClickListener {
            navigateToAwareness()
        }

        val mindOptions = listOf("This may look like... ") +
                listOf("Difficulty concentrating", "Impaired judgement", "Indecision",
                    "Muddled thinking", "Negativity", "Worrying", "Custom...")

        val adapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_dropdown_item,
            R.layout.spinner_dropdown_item,
            mindOptions
        ) { currentCustomMind }

        mindSpinner.adapter = adapter

        mindSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if (position == mindOptions.size - 1) {
                    showCustomInputDialog(adapter)
                } else {
                    selectedMind = if (position != 0) mindOptions[position] else null
                    currentCustomMind = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedMind = null
                currentCustomMind = null
            }
        }

        continueButton.setOnClickListener {
            val mindToSave = currentCustomMind ?: selectedMind
            if (mindToSave != null) {
                saveSelectionToFirestore(mindToSave)
            } else {
                Toast.makeText(this, "Please select or enter a mind symptom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCustomInputDialog(adapter: CustomSpinnerAdapter) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.customInputEditText)

        editText.setText(currentCustomMind)

        builder.setView(dialogView)
            .setTitle("Enter Custom Mind Symptom")
            .setPositiveButton("Save") { dialog, _ ->
                val customText = editText.text.toString().trim()
                if (customText.isNotEmpty()) {
                    currentCustomMind = customText
                    selectedMind = null
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Custom symptom saved: $customText", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Custom text cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (currentCustomMind == null && selectedMind == null) {
                    val spinner: Spinner = findViewById(R.id.mindSpinner)
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

            val selectedData = mapOf("mindSymptom" to selected) // Use specific key

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