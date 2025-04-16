package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodActivity : AppCompatActivity() {

    private lateinit var verySadButton: MaterialButton
    private lateinit var sadButton: MaterialButton
    private lateinit var neutralButton: MaterialButton
    private lateinit var happyButton: MaterialButton
    private lateinit var veryHappyButton: MaterialButton
    private lateinit var continueButton: MaterialButton
    private lateinit var backButton: ImageView

    private lateinit var viewModel: MoodViewModel
    private var selectedDate: String = ""
    private val moodButtons = mutableListOf<MaterialButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        // Get selected date from intent or use today's date
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MoodViewModel::class.java]

        // Initialize views
        initializeViews()

        // Set up observers
        setupObservers()

        // Set up listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        verySadButton = findViewById(R.id.verySadButton)
        sadButton = findViewById(R.id.sadButton)
        neutralButton = findViewById(R.id.neutralButton)
        happyButton = findViewById(R.id.happyButton)
        veryHappyButton = findViewById(R.id.veryHappyButton)
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)

        // Add all mood buttons to the list for easier management
        moodButtons.apply {
            add(verySadButton)
            add(sadButton)
            add(neutralButton)
            add(happyButton)
            add(veryHappyButton)
        }
    }

    private fun setupObservers() {
        // Observe selected mood
        viewModel.selectedMood.observe(this) { mood ->
            updateSelectedMoodUI(mood)
        }

        // Observe save state
        viewModel.saveState.observe(this) { state ->
            when (state) {
                is MoodViewModel.SaveState.Loading -> {
                    continueButton.isEnabled = false
                    continueButton.text = "Saving..."
                }
                is MoodViewModel.SaveState.Success -> {
                    Toast.makeText(this, "Mood saved successfully!", Toast.LENGTH_SHORT).show()
                    navigateToInControlActivity()
                }
                is MoodViewModel.SaveState.Error -> {
                    continueButton.isEnabled = true
                    continueButton.text = "Continue"
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {
                    continueButton.isEnabled = true
                    continueButton.text = "Continue"
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Mood selection buttons
        verySadButton.setOnClickListener { viewModel.selectMood(1) }
        sadButton.setOnClickListener { viewModel.selectMood(2) }
        neutralButton.setOnClickListener { viewModel.selectMood(3) }
        happyButton.setOnClickListener { viewModel.selectMood(4) }
        veryHappyButton.setOnClickListener { viewModel.selectMood(5) }

        // Continue button
        continueButton.setOnClickListener {
            viewModel.saveMood(selectedDate)
        }

        // Back button
        backButton.setOnClickListener {
            navigateToCalendar()
        }
    }

    private fun updateSelectedMoodUI(mood: Int) {
        // Reset all buttons to their default states
        resetButtonStates()

        // Highlight the selected button if any
        if (mood > 0 && mood <= moodButtons.size) {
            moodButtons[mood - 1].alpha = 0.6f
            moodButtons[mood - 1].strokeWidth = 4
            continueButton.isEnabled = true
        } else {
            continueButton.isEnabled = false
        }
    }

    private fun resetButtonStates() {
        moodButtons.forEach {
            it.alpha = 1.0f
            it.strokeWidth = 1
        }
    }

    private fun navigateToInControlActivity() {
        val intent = Intent(this, InControlActivity::class.java)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
        finish()
    }

    private fun navigateToCalendar() {
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
        finish()
    }
}