package com.anxietystressselfmanagement

import android.content.Intent
import android.widget.ImageView
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SOTD : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sotd) // Ensure activity_sotd.xml exists
        // Find the button by its ID
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        val button5 = findViewById<Button>(R.id.button5)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Set a click listener to navigate back to MainActivity
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Optional: Closes SOTD activity

        }
        // Set up an onClickListener
        button2.setOnClickListener {

            val intent1 = Intent(this, SOTDHome::class.java)
            startActivity(intent1)
        }
        button3.setOnClickListener {

            val intent2 = Intent(this, SOTDSchool::class.java)
            startActivity(intent2)
        }
        button4.setOnClickListener {

            val intent3 = Intent(this, SOTDSocial::class.java)
            startActivity(intent3)
        }
        button5.setOnClickListener {

            val intent4 = Intent(this, SOTDWork::class.java)
            startActivity(intent4)

        }
    }
}