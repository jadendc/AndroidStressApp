package com.anxietystressselfmanagement

import android.os.Bundle
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class ToggleButton : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_choice)

        // Find all toggle buttons by their IDs
        val toggleRain: ToggleButton = findViewById(R.id.toggleRain)
        val toggleWaves: ToggleButton = findViewById(R.id.toggleWaves)
        val toggleFire: ToggleButton = findViewById(R.id.toggleFire)
        val toggleForest: ToggleButton = findViewById(R.id.toggleForest)
        val toggleMeditation: ToggleButton = findViewById(R.id.toggleMeditation)
        val toggleBirds: ToggleButton = findViewById(R.id.toggleBirds)

        val toggleButtons = listOf(toggleRain, toggleWaves, toggleFire, toggleForest, toggleMeditation, toggleBirds)

        // Set click listeners for each toggle button
        for (button in toggleButtons) {
            button.setOnClickListener {
                // When a button is clicked, turn off all other buttons
                toggleButtons.forEach { otherButton ->
                    if (otherButton != button) {
                        otherButton.isChecked = false // Uncheck other buttons
                    }
                }
            }
        }
    }
}



