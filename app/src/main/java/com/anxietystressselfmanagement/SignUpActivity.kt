package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var reenterPasswordInput: EditText

    private lateinit var firstNameLayout: TextInputLayout
    private lateinit var lastNameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var reenterPasswordLayout: TextInputLayout

    private lateinit var backButton: Button
    private lateinit var doneButton: Button
    private lateinit var logoImage: ImageView
    private lateinit var titleText: TextView

    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        initializeViews()
        setupTextChangeListeners()
        setupButtonListeners()
        observeViewModel()
    }

    private fun initializeViews() {
        // Input fields
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        reenterPasswordInput = findViewById(R.id.reenterPasswordInput)

        // TextInputLayouts
        firstNameLayout = findViewById(R.id.firstNameLayout)
        lastNameLayout = findViewById(R.id.lastNameLayout)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        reenterPasswordLayout = findViewById(R.id.reenterPasswordLayout)

        // Buttons and other views
        backButton = findViewById(R.id.backButton)
        doneButton = findViewById(R.id.doneButton)
        logoImage = findViewById(R.id.logoImage)
        titleText = findViewById(R.id.titleText)
    }

    private fun setupTextChangeListeners() {
        firstNameInput.addTextChangedListener(createTextWatcher(firstNameLayout, "First name is required"))
        lastNameInput.addTextChangedListener(createTextWatcher(lastNameLayout, "Last name is required"))
        emailInput.addTextChangedListener(createTextWatcher(emailLayout, "Email is required"))
        passwordInput.addTextChangedListener(createTextWatcher(passwordLayout, "Password is required"))
        reenterPasswordInput.addTextChangedListener(createTextWatcher(reenterPasswordLayout, "Please confirm your password"))
    }

    private fun createTextWatcher(inputLayout: TextInputLayout, errorMessage: String): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                inputLayout.error = if (s.toString().isNotEmpty()) null else errorMessage
            }
        }
    }

    private fun setupButtonListeners() {
        doneButton.setOnClickListener {
            if (validateInputs()) {
                signUpUser()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.signUpState.observe(this) { state ->
            when (state) {
                is SignUpViewModel.SignUpState.Loading -> {
                    doneButton.isEnabled = false
                    doneButton.text = "Creating account..."
                }
                is SignUpViewModel.SignUpState.Success -> {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is SignUpViewModel.SignUpState.Error -> {
                    doneButton.isEnabled = true
                    doneButton.text = "Done"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    doneButton.isEnabled = true
                    doneButton.text = "Done"
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val reenterPassword = reenterPasswordInput.text.toString()

        // Check if fields are empty
        if (firstName.isEmpty()) {
            firstNameLayout.error = "First name is required"
            isValid = false
        } else {
            firstNameLayout.error = null
        }

        if (lastName.isEmpty()) {
            lastNameLayout.error = "Last name is required"
            isValid = false
        } else {
            lastNameLayout.error = null
        }

        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        if (reenterPassword.isEmpty()) {
            reenterPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else {
            reenterPasswordLayout.error = null
        }

        // Check if passwords match
        if (password != reenterPassword) {
            reenterPasswordLayout.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun signUpUser() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        viewModel.signUp(firstName, lastName, email, password)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}