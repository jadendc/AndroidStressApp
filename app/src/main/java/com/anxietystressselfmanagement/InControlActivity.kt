package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class InControlActivity : AppCompatActivity(), ControlGaugeView.OnControlLevelSelectedListener {

    private lateinit var continueButton: MaterialButton
    private lateinit var backButton: ImageView
    private lateinit var controlGaugeView: ControlGaugeView

    private lateinit var viewModel: InControlViewModel
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_control)

        // Get selected date from intent or use today's date
        selectedDate = intent.getStringExtra("selectedDate") ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[InControlViewModel::class.java]

        // Initialize views
        initializeViews()

        // Set up observers
        setupObservers()

        // Set up listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)
        controlGaugeView = findViewById(R.id.controlGaugeView)

        continueButton.isEnabled = false
        continueButton.alpha = 0.5f

        controlGaugeView.setOnControlLevelSelectedListener(this)
    }

    private fun setupObservers() {
        // Observe control level changes
        viewModel.controlLevel.observe(this) { level ->
            if (level > 0) {
                controlGaugeView.setControlLevel(level)
                continueButton.apply {
                    isEnabled = true
                    animate().alpha(1f).setDuration(300).start()
                }
            }
        }

        // Observe save state
        viewModel.saveState.observe(this) { state ->
            when (state) {
                is InControlViewModel.SaveState.Loading -> {
                    continueButton.isEnabled = false
                    continueButton.text = "Saving..."
                }
                is InControlViewModel.SaveState.Success -> {
                    Toast.makeText(this, "Control level saved successfully!", Toast.LENGTH_SHORT).show()
                    navigateToSotd()
                }
                is InControlViewModel.SaveState.Error -> {
                    continueButton.isEnabled = true
                    continueButton.text = "Continue"
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {
                    continueButton.isEnabled = viewModel.controlLevel.value ?: 0 > 0
                    continueButton.text = "Continue"
                }
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            navigateToMood()
        }

        continueButton.setOnClickListener {
            viewModel.saveControlLevel(selectedDate)
        }
    }

    override fun onControlLevelSelected(level: Int) {
        viewModel.setControlLevel(level)
    }

    private fun navigateToMood() {
        val intent = Intent(this, MoodActivity::class.java)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
        finish()
    }

    private fun navigateToSotd() {
        val intent = Intent(this, SOTD::class.java)
        intent.putExtra("selectedDate", selectedDate)
        startActivity(intent)
        finish()
    }
}