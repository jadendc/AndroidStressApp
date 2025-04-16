package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var resetButton: MaterialButton
    private lateinit var backButton: MaterialButton
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]

        initializeViews()
        setupListeners()
        observeViewModel()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        emailLayout = findViewById(R.id.emailLayout)
        resetButton = findViewById(R.id.resetButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupListeners() {
        emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                emailLayout.error = if (s.toString().isNotEmpty()) null else "Email is required"
            }
        })

        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (validateEmail(email)) {
                viewModel.sendPasswordResetEmail(email)
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.resetPasswordState.observe(this) { state ->
            when (state) {
                is ForgotPasswordViewModel.ResetPasswordState.Loading -> {
                    resetButton.isEnabled = false
                    resetButton.text = "Sending..."
                }
                is ForgotPasswordViewModel.ResetPasswordState.Success -> {
                    resetButton.isEnabled = true
                    resetButton.text = "Reset"
                    Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
                is ForgotPasswordViewModel.ResetPasswordState.Error -> {
                    resetButton.isEnabled = true
                    resetButton.text = "Reset"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    resetButton.isEnabled = true
                    resetButton.text = "Reset"
                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            emailLayout.error = "Please enter your email"
            return false
        }

        return true
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}