package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MembershipDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership_details) // Ensure activity_sotd.xml exists
        // Find the button by its ID
        val button2 = findViewById<Button>(R.id.confirmPayment)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Set a click listener to navigate back to MainActivity
        backButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: Closes SOTD activity

        }
        button2.setOnClickListener {

            val intent1 = Intent(this, MainActivity::class.java)
            startActivity(intent1)
            }
        }
}