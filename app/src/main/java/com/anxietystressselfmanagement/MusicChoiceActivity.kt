package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MusicChoiceActivity : AppCompatActivity() {
    private var selectedSound: String? = null
    private var selectedCycles: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_choice)

        // Sound choice toggle buttons
        val btnMeditation: ToggleButton = findViewById(R.id.toggleMeditation)
        val btnWaves: ToggleButton = findViewById(R.id.toggleWaves)
        val btnBirds: ToggleButton = findViewById(R.id.toggleBirds)
        val btnFire: ToggleButton = findViewById(R.id.toggleFire)
        val btnForest: ToggleButton = findViewById(R.id.toggleForest)
        val btnRain: ToggleButton = findViewById(R.id.toggleRain)

        // Cycle choice toggle buttons
        val btnCycle1: ToggleButton = findViewById(R.id.cycle1)
        val btnCycle2: ToggleButton = findViewById(R.id.cycle2)
        val btnCycle3: ToggleButton = findViewById(R.id.cycle3)
        val btnCycle5: ToggleButton = findViewById(R.id.cycle5)
        val btnCycle10: ToggleButton = findViewById(R.id.cycle10)

        // Next button
        val nextButton: Button = findViewById(R.id.nextButton)
        // Back button
        val backButton: ImageView = findViewById(R.id.backButton)

        // Handle sound choice toggling
        val soundButtons = listOf(btnMeditation, btnWaves, btnBirds, btnFire, btnForest, btnRain)
        soundButtons.forEach { button ->
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedSound = button.text.toString().lowercase()
                    // Deselect other buttons
                    soundButtons.filter { it != button }.forEach { it.isChecked = false }
                } else if (selectedSound == button.text.toString().lowercase()) {
                    selectedSound = null
                }
            }
        }

        // Handle cycle choice toggling
        val cycleButtons = listOf(btnCycle1, btnCycle2, btnCycle3, btnCycle5, btnCycle10)
        cycleButtons.forEach { button ->
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCycles = button.text.toString().toInt()
                    // Deselect other buttons
                    cycleButtons.filter { it != button }.forEach { it.isChecked = false }
                } else if (selectedCycles == button.text.toString().toInt()) {
                    selectedCycles = null
                }
            }
        }

        // Handle Next button click
        nextButton.setOnClickListener {
            if (selectedSound != null && selectedCycles != null) {
                val intent = when (selectedSound) {
                    "meditation" -> Intent(this, MeditationActivity::class.java)
                    "waves" -> Intent(this, WavesActivity::class.java)
                    "birds" -> Intent(this, BirdsActivity::class.java)
                    "fire" -> Intent(this, FireActivity::class.java)
                    "forest" -> Intent(this, ForestActivity::class.java)
                    "rain" -> Intent(this, RainActivity::class.java)
                    else -> null
                }
                intent?.putExtra("selectedCycles", selectedCycles)
                if (intent != null) {
                    startActivity(intent)
                }
            } else {
                Toast.makeText(
                    this,
                    "Please select a sound and a number of cycles.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Handle Back button click
        backButton.setOnClickListener {
            val intent = Intent(this, DashBoardActivity::class.java) // Replace with your main menu activity
            startActivity(intent)
            finish() // Finish current activity to avoid stacking
        }


    }
}


