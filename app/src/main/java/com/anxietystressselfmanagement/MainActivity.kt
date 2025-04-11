package com.anxietystressselfmanagement

import com.bumptech.glide.Glide
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var rememberMeCheckbox: CheckBox
    private val PREFS_NAME = "login_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        val backgroundGif = findViewById<ImageView>(R.id.backgroundGif)

        Glide.with(this)
            .asGif()
            .load(R.raw.signinbackground)
            .into(backgroundGif)

        // Find views
        editTextEmail = findViewById(R.id.editTextTextEmailAddress)
        editTextPassword = findViewById(R.id.editTextTextPassword)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckBox)

        // Check if user is already logged in with "Remember Me" enabled
        checkAutoLogin()

        // Set up login button listener
        val buttonLogin = findViewById<Button>(R.id.login)
        buttonLogin.setOnClickListener {
            loginUser()
        }

        // Set up signup button listener
        val signUpButton = findViewById<Button>(R.id.signup)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Set up forgot password listener
        val resetButton = findViewById<TextView>(R.id.forgot)
        resetButton.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Load saved preferences for Remember Me
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("email", "")
        val rememberMe = sharedPrefs.getBoolean("remember_me", false)

        if (rememberMe && savedEmail!!.isNotEmpty()) {
            editTextEmail.setText(savedEmail)
            rememberMeCheckbox.isChecked = true
        }
    }

    private fun checkAutoLogin() {
        // Check if user is already authenticated AND remember_me is true
        val currentUser = firebaseAuth.currentUser
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = sharedPrefs.getBoolean("remember_me", false)

        if (currentUser != null && rememberMe) {
            // User is already logged in and wants to be remembered
            navigateToHome()
        }
    }

    private fun loginUser() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val rememberMe = rememberMeCheckbox.isChecked

        // Input validation
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }

        // Sign in with Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Login successful for email: $email")

                    // Save Remember Me preference
                    saveLoginPreferences(email, rememberMe)

                    // Navigate to HomeActivity
                    navigateToHome()
                } else {
                    Log.d("MainActivity", "Login failed for email: $email", task.exception)
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveLoginPreferences(email: String, rememberMe: Boolean) {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        if (rememberMe) {
            // Save email and remember_me flag
            editor.putString("email", email)
            editor.putBoolean("remember_me", true)
        } else {
            // Clear preferences if Remember Me is not checked
            editor.clear()
        }

        editor.apply()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}