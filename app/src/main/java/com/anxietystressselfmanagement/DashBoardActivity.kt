package com.anxietystressselfmanagement


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anxietystressselfmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashBoardActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        supportActionBar?.hide()

        // Find the TextView in the layout
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)

        // Get the current user's ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if user is logged in
        if (userId != null) {
            // Reference to Firestore
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            // Retrieve the user details from Firestore
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get the first name from Firestore and display it in the welcome message
                        val firstName = document.getString("first name") ?: "User"
                        welcomeTextView.text = "Welcome, $firstName"
                    } else {
                        // If no document exists, show an error
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors with a Toast
                    Toast.makeText(
                        this,
                        "Error retrieving user data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // If no user is logged in, show an error message
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }
}