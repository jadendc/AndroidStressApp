package com.anxietystressselfmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SOTDSchool : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sotd_school)
        val backButton = findViewById<ImageView>(R.id.backButton)

        // Set a click listener to navigate back to MainActivity
        backButton.setOnClickListener {
            val intent = Intent(this, SOTD::class.java)
            startActivity(intent)
            finish() // Optional: Closes SOTD activity

        }
        val customInput = findViewById<EditText>(R.id.customInput)
        val customToggle = findViewById<Button>(R.id.saveCustom)


        customToggle.setOnClickListener{
            val inputText = customInput.text.toString()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "Please enter text before saving", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val customToggle2 = findViewById<ToggleButton>(R.id.button)

        // Set an OnCheckedChangeListener to toggle colors
        customToggle2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customToggle2.setBackgroundColor(Color.GREEN) // ON state
                customToggle2.setTextColor(Color.WHITE)
            } else {
                customToggle2.setBackgroundColor(Color.RED) // OFF state
                customToggle2.setTextColor(Color.BLACK)
            }
        }
        val customToggle3 = findViewById<ToggleButton>(R.id.button6)

        // Set an OnCheckedChangeListener to toggle colors
        customToggle3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customToggle3.setBackgroundColor(Color.GREEN) // ON state
                customToggle3.setTextColor(Color.WHITE)
            } else {
                customToggle3.setBackgroundColor(Color.RED) // OFF state
                customToggle3.setTextColor(Color.BLACK)
            }
        }
        val customToggle4 = findViewById<ToggleButton>(R.id.button7)

        // Set an OnCheckedChangeListener to toggle colors
        customToggle4.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customToggle4.setBackgroundColor(Color.GREEN) // ON state
                customToggle4.setTextColor(Color.WHITE)
            } else {
                customToggle4.setBackgroundColor(Color.RED) // OFF state
                customToggle4.setTextColor(Color.BLACK)
            }
        }
        val customToggle5 = findViewById<ToggleButton>(R.id.button8)

        // Set an OnCheckedChangeListener to toggle colors
        customToggle5.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customToggle5.setBackgroundColor(Color.GREEN) // ON state
                customToggle5.setTextColor(Color.WHITE)
            } else {
                customToggle5.setBackgroundColor(Color.RED) // OFF state
                customToggle5.setTextColor(Color.BLACK)
            }
        }
        val customToggle6 = findViewById<ToggleButton>(R.id.button9)

        // Set an OnCheckedChangeListener to toggle colors
        customToggle6.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customToggle6.setBackgroundColor(Color.GREEN) // ON state
                customToggle6.setTextColor(Color.WHITE)
            } else {
                customToggle6.setBackgroundColor(Color.RED) // OFF state
                customToggle6.setTextColor(Color.BLACK)
            }
        }
        val customToggle7 = findViewById<ToggleButton>(R.id.button10)

        // Set an OnCheckedChangeListener to toggle colors
        customToggle7.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                customToggle7.setBackgroundColor(Color.GREEN) // ON state
                customToggle7.setTextColor(Color.WHITE)
            } else {
                customToggle7.setBackgroundColor(Color.RED) // OFF state
                customToggle7.setTextColor(Color.BLACK)
            }
        }
        val checkStatesButton = findViewById<Button>(R.id.continueButtonSotdHome)
        checkStatesButton.setOnClickListener {
            // Check the state of each toggle button
            val toggleStates = mutableListOf<String>()


            if (customToggle2.isChecked) toggleStates.add("Homework")
            if (customToggle3.isChecked) toggleStates.add("Exams")
            if (customToggle4.isChecked) toggleStates.add("Organization")
            if (customToggle5.isChecked) toggleStates.add("Bullying")
            if (customToggle6.isChecked) toggleStates.add("Grades")
            if (customToggle7.isChecked) toggleStates.add("Financial")

            // Display the states
            if (toggleStates.isEmpty()) {
                Toast.makeText(this, "No buttons are ON", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${toggleStates.joinToString(", ")} are ON", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,SOTD::class.java))
            }
        }

    }
}