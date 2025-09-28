package com.anxietystressselfmanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anxietystressselfmanagement.ui.activities.HomeActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var backgroundGif: ImageView
    private lateinit var loginViewModel: LoginViewModel

    private val PREFS_NAME = "login_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        initializeViews()
        setupUI()
        setupObservers()
        checkAutoLogin()
        loadSavedCredentials()
    }

    private fun initializeViews() {
        backgroundGif = findViewById(R.id.backgroundGif)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckBox)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signupButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
    }

    private fun setupUI() {
        Glide.with(this)
            .asGif()
            .load(R.raw.signinbackground)
            .into(backgroundGif)

        emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                emailLayout.error = if (s.toString().isNotEmpty()) null else "Email is required"
            }
        })

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                passwordLayout.error = if (s.toString().isNotEmpty()) null else "Password is required"
            }
        })

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val rememberMe = rememberMeCheckbox.isChecked

            if (validateInputs(email, password)) {
                loginViewModel.login(email, password, rememberMe, this)
            }
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setupObservers() {
        loginViewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    loginButton.isEnabled = false
                    loginButton.text = "Logging in..."
                }
                is LoginViewModel.LoginState.Success -> {
                    navigateToHome()
                }
                is LoginViewModel.LoginState.Error -> {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                }
            }
        }
    }

    private fun checkAutoLogin() {
        val currentUser = firebaseAuth.currentUser
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = sharedPrefs.getBoolean("remember_me", false)

        if (currentUser != null && rememberMe) {
            navigateToHome()
        }
    }

    private fun loadSavedCredentials() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("email", "")
        val rememberMe = sharedPrefs.getBoolean("remember_me", false)

        if (rememberMe && !savedEmail.isNullOrEmpty()) {
            emailInput.setText(savedEmail)
            rememberMeCheckbox.isChecked = true
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

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

        return isValid
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}