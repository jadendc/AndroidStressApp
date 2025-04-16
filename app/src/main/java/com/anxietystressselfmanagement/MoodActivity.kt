package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView // Import TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import java.text.DateFormat
import java.text.ParseException
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
    private lateinit var titleTextView: TextView // Declare TextView for the title

    private lateinit var viewModel: MoodViewModel
    private var selectedDate: String = ""
    private val moodButtons = mutableListOf<MaterialButton>()
    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Reusable format

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            inputDateFormat.format(Date())
        }

        viewModel = ViewModelProvider(this)[MoodViewModel::class.java]

        initializeViews()
        updateTitleText() // Set the title dynamically
        setupObservers()
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
        titleTextView = findViewById(R.id.titleText) // Initialize the TextView

        moodButtons.apply {
            add(verySadButton)
            add(sadButton)
            add(neutralButton)
            add(happyButton)
            add(veryHappyButton)
        }
    }

    private fun updateTitleText() {
        val todayDateString = inputDateFormat.format(Date())
        val isToday = selectedDate == todayDateString

        if (isToday) {
            // Use a string resource for better practice
            titleTextView.text = getString(R.string.mood_prompt_today)
        } else {
            // Format the past date for display
            val outputFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            try {
                val dateObject = inputDateFormat.parse(selectedDate)
                // Use ?.let for null safety on dateObject
                val formattedDisplayDate = dateObject?.let { outputFormat.format(it) } ?: selectedDate // Fallback to original string if parsing fails or returns null
                // Use a formatted string resource
                titleTextView.text = getString(R.string.mood_prompt_past, formattedDisplayDate)
            } catch (e: ParseException) {
                Log.e("MoodActivity", "Error parsing date: $selectedDate", e)
                // Fallback in case of parsing error
                titleTextView.text = getString(R.string.mood_prompt_past, selectedDate)
            }
        }
    }

    private fun setupObservers() {
        viewModel.selectedMood.observe(this) { mood ->
            updateSelectedMoodUI(mood)
        }

        viewModel.saveState.observe(this) { state ->
            when (state) {
                is MoodViewModel.SaveState.Loading -> {
                    continueButton.isEnabled = false
                    continueButton.text = getString(R.string.saving) // Use string resource
                }
                is MoodViewModel.SaveState.Success -> {
                    Toast.makeText(this, getString(R.string.mood_saved_success), Toast.LENGTH_SHORT).show() // Use string resource
                    navigateToInControlActivity()
                }
                is MoodViewModel.SaveState.Error -> {
                    continueButton.isEnabled = true
                    continueButton.text = getString(R.string.continue_button) // Use string resource
                    // Use a formatted string resource for the error
                    Toast.makeText(this, getString(R.string.save_error, state.message), Toast.LENGTH_LONG).show()
                }
                else -> { // Includes Idle state
                    continueButton.isEnabled = viewModel.selectedMood.value ?: 0 > 0 // Enable only if a mood is selected
                    continueButton.text = getString(R.string.continue_button) // Use string resource
                }
            }
        }
    }

    private fun setupClickListeners() {
        verySadButton.setOnClickListener { viewModel.selectMood(1) }
        sadButton.setOnClickListener { viewModel.selectMood(2) }
        neutralButton.setOnClickListener { viewModel.selectMood(3) }
        happyButton.setOnClickListener { viewModel.selectMood(4) }
        veryHappyButton.setOnClickListener { viewModel.selectMood(5) }

        continueButton.setOnClickListener {
            viewModel.saveMood(selectedDate)
        }

        backButton.setOnClickListener {
            navigateToCalendar()
        }
    }

    private fun updateSelectedMoodUI(mood: Int) {
        resetButtonStates()

        if (mood > 0 && mood <= moodButtons.size) {
            moodButtons[mood - 1].alpha = 0.6f
            moodButtons[mood - 1].strokeWidth = 4
            continueButton.isEnabled = true
        } else {
            // Disable continue button if no mood is selected
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