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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sotd)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val button2 = findViewById<Button>(R.id.button2) // Home
        val button3 = findViewById<Button>(R.id.button3) // School
        val button4 = findViewById<Button>(R.id.button4) // Social
        val button5 = findViewById<Button>(R.id.button5) // Work
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, InControlActivity::class.java)
            startActivity(intent)
            finish()
        }

        button2.setOnClickListener {
            saveSelectedSOTD("Home")
            startActivity(Intent(this, SOTDHome::class.java))
        }

        button3.setOnClickListener {
            saveSelectedSOTD("School")
            startActivity(Intent(this, SOTDSchool::class.java))
        }

        button4.setOnClickListener {
            saveSelectedSOTD("Social")
            startActivity(Intent(this, SOTDSocial::class.java))
        }

        button5.setOnClickListener {
            saveSelectedSOTD("Work")
            startActivity(Intent(this, SOTDWork::class.java))
        }
    }

    private fun saveSelectedSOTD(selected: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            val selectedSOTDData: MutableMap<String, Any?> = hashMapOf(
                "selectedSOTD" to selected
            )

            db.collection("users")
                .document(userId)
                .collection("dailyLogs")
                .document(today)
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
