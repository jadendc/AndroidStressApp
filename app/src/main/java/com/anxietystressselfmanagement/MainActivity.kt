package com.anxietystressselfmanagement

import com.bumptech.glide.Glide
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContentView(R.layout.activity_main)

        val backgroundGif = findViewById<ImageView>(R.id.backgroundGif)

        Glide.with(this)
            .asGif()
            .load(R.raw.signinbackground)
            .into(backgroundGif)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Find views
        editTextEmail = findViewById(R.id.editTextTextEmailAddress)
        editTextPassword = findViewById(R.id.editTextTextPassword)

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
    }

    private fun loginUser() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

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

                    // Transition to DashboardActivity if user is authenticated
                    firebaseAuth.currentUser?.let {
                        val intent = Intent(this, DashBoardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish() // Finish MainActivity
                    } ?: run {
                        Log.e("MainActivity", "Login success but user is null.")
                        Toast.makeText(this, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show()
                    }
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
}