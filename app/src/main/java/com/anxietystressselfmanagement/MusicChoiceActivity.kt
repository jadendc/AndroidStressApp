package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // Get the previous activity identifier from the Intent
        val previousActivity = intent.getStringExtra("previousActivity")
        Log.d("MusicChoiceActivity", "Previous Activity: $previousActivity")

        // Sound choice toggle buttons
        val btnMeditation: ToggleButton = findViewById(R.id.toggleMeditation)
        val btnWaves: ToggleButton = findViewById(R.id.toggleWaves)
        val btnBirds: ToggleButton = findViewById(R.id.toggleBirds)
        val btnFire: ToggleButton = findViewById(R.id.toggleFire)
        val btnForest: ToggleButton = findViewById(R.id.toggleForest)
        val btnRain: ToggleButton = findViewById(R.id.toggleRain)

        val soundButtons = listOf(btnMeditation, btnWaves, btnBirds, btnFire, btnForest, btnRain)

        // Handle sound choice toggling
        soundButtons.forEach { button ->
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedSound = button.text.toString().lowercase()
                    soundButtons.filter { it != button }.forEach { it.isChecked = false }
                } else if (selectedSound == button.text.toString().lowercase()) {
                    selectedSound = null
                }
            }
        }

        // Cycle choice toggle buttons
        val btnCycle1: ToggleButton = findViewById(R.id.cycle1)
        val btnCycle2: ToggleButton = findViewById(R.id.cycle2)
        val btnCycle3: ToggleButton = findViewById(R.id.cycle3)
        val btnCycle5: ToggleButton = findViewById(R.id.cycle5)
        val btnCycle10: ToggleButton = findViewById(R.id.cycle10)

        val cycleButtons = listOf(btnCycle1, btnCycle2, btnCycle3, btnCycle5, btnCycle10)

        // Handle cycle choice toggling
        cycleButtons.forEach { button ->
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCycles = button.text.toString().toInt()
                    cycleButtons.filter { it != button }.forEach { it.isChecked = false }
                } else if (selectedCycles == button.text.toString().toInt()) {
                    selectedCycles = null
                }
            }
        }

        // Next button functionality
        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            if (selectedSound != null && selectedCycles != null) {
                val intent = when (selectedSound) {
                    "waves" -> Intent(this, WavesActivity::class.java)
                    "birds" -> Intent(this, BirdsActivity::class.java)
                    "fire" -> Intent(this, FireActivity::class.java)
                    "forest" -> Intent(this, ForestActivity::class.java)
                    "rain" -> Intent(this, RainActivity::class.java)
                    "meditation" -> Intent(this, MeditationActivity::class.java)
                    else -> null
                }
                if (intent != null) {
                    intent.putExtra("selectedCycles", selectedCycles)
                    intent.putExtra("exerciseType", previousActivity) // Pass exercise type
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

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = when (previousActivity) {
                "FocusActivity" -> Intent(this, FocusActivity::class.java)
                "SleepActivity" -> Intent(this, SleepActivity::class.java)
                "DestressActivity" -> Intent(this, DestressActivity::class.java)
                else -> Intent(this, DashBoardActivity::class.java)
            }
            intent.putExtra("previousActivity", previousActivity) // Pass the activity back
            startActivity(intent)
            finish()
        }

    }
}
