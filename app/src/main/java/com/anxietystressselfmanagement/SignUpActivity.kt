package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anxietystressselfmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextReenterPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editTextFirstName = findViewById(R.id.editTextText)
        editTextLastName = findViewById(R.id.editTextText2)
        editTextEmail = findViewById(R.id.editTextTextEmailAddress2)
        editTextPassword = findViewById(R.id.editTextTextPassword2)
        editTextReenterPassword = findViewById(R.id.editTextTextPassword3)

        val buttonSignUp = findViewById<Button>(R.id.done_sign_up)
        buttonSignUp.setOnClickListener {
            signUpUser()
        }

        val backButton: Button = findViewById(R.id.back)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUpUser() {
        val firstName = editTextFirstName.text.toString().trim()
        val lastName = editTextLastName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val reenter = editTextReenterPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || reenter.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != reenter) {
            Toast.makeText(this, "Passwords must match", Toast.LENGTH_LONG).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User successfully signed up
                    val userID = firebaseAuth.currentUser?.uid
                    if (userID != null) {
                        Log.d("SignUpActivity", "First Name: $firstName")
                        val user = hashMapOf(
                            "first name" to firstName,
                            "last name" to lastName,
                            "email" to email
                        )

                        // Save user data to Firestore
                        firestore.collection("users").document(userID)
                            .set(user)
                            .addOnSuccessListener {
                                // Data saved successfully
                                Toast.makeText(this, "User data saved", Toast.LENGTH_SHORT).show()

                                // Transition to Dashboard only after successful signup and saving user data
                                val intent = Intent(this, DashBoardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Signup failed: User ID is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Signup failed
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}