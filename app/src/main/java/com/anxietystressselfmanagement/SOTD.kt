package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class SOTD : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sotd)

        // Get selected date from intent
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val button2 = findViewById<Button>(R.id.button2) // Home
        val button3 = findViewById<Button>(R.id.button3) // School
        val button4 = findViewById<Button>(R.id.button4) // Social
        val button5 = findViewById<Button>(R.id.button5) // Work
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, InControlActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
            finish()
        }

        button2.setOnClickListener {
            saveSelectedSOTD("Home")
            val intent = Intent(this, SOTDHome::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }

        button3.setOnClickListener {
            saveSelectedSOTD("School")
            val intent = Intent(this, SOTDSchool::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }

        button4.setOnClickListener {
            saveSelectedSOTD("Social")
            val intent = Intent(this, SOTDSocial::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }

        button5.setOnClickListener {
            saveSelectedSOTD("Work")
            val intent = Intent(this, SOTDWork::class.java)
            intent.putExtra("selectedDate", selectedDate)
            startActivity(intent)
        }
    }

    private fun saveSelectedSOTD(selected: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val selectedSOTDData: MutableMap<String, Any?> = hashMapOf(
                "selectedSOTD" to selected
            )

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(selectedDate)  // Use selectedDate
                .set(selectedSOTDData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "$selected selected", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save SOTD: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}