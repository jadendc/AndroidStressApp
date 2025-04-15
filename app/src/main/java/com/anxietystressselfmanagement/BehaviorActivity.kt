package com.anxietystressselfmanagement

import CustomSpinnerAdapter
import android.app.AlertDialog
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

class BehaviorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedBehavior: String? = null
    private var selectedDate: String = "" // Store selected date
    private var currentCustomBehavior: String? = null // Store custom text

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_behavior)

        // Get selected date from intent
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        val behaviorSpinner: Spinner = findViewById(R.id.behaviorSpinner)
        val continueButton: Button = findViewById(R.id.continueBehaviorButton)

        backButton.setOnClickListener {
            val intent = Intent(this, AwarenessActivity::class.java)
            intent.putExtra("selectedDate", selectedDate) // Pass date back
            startActivity(intent)
            finish()
        }

        // Add "Custom..." option to the list
        val behaviorOptions = listOf("This may look like... ") +
                listOf("Accident prone", "Insomnia", "Loss of appetite", "Loss of sex drive",
                    "More addiction", "Restlessness", "Custom...")

        // Use custom adapter
        val adapter = CustomSpinnerAdapter(
            this,
            R.layout.spinner_dropdown_item,
            R.layout.spinner_dropdown_item,
            behaviorOptions
        ) { currentCustomBehavior }

        behaviorSpinner.adapter = adapter

        behaviorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == behaviorOptions.size - 1) {
                    // "Custom..." option selected
                    showCustomInputDialog(adapter)
                } else {
                    selectedBehavior = if (position != 0) behaviorOptions[position] else null
                    currentCustomBehavior = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedBehavior = null
                currentCustomBehavior = null
            }
        }

        continueButton.setOnClickListener {
            if (selectedBehavior != null || currentCustomBehavior != null) {
                val behaviorToSave = currentCustomBehavior ?: selectedBehavior!!
                saveSelectionToFirestore(behaviorToSave)
            } else {
                Toast.makeText(this, "Please select a behavior symptom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCustomInputDialog(adapter: CustomSpinnerAdapter) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.customInputEditText)

        editText.setText(currentCustomBehavior)

        builder.setView(dialogView)
            .setTitle("Enter Custom Behavior")
            .setPositiveButton("Save") { dialog, _ ->
                val customText = editText.text.toString().trim()
                if (customText.isNotEmpty()) {
                    currentCustomBehavior = customText
                    selectedBehavior = null
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Custom behavior saved: $customText", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Custom text cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                if (currentCustomBehavior == null) {
                    val spinner: Spinner = findViewById(R.id.behaviorSpinner)
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
                .document(selectedDate)
                .set(selectedData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "$selected saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, StrategiesAndActionsActivity::class.java)
                    intent.putExtra("selectedDate", selectedDate)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}