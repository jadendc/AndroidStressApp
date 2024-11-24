package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {


    private lateinit var tvEmail: TextView
    private lateinit var etName: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvEmail = findViewById(R.id.tvEmail)
        etName = findViewById(R.id.etName)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email

        // Display the user's email
        userEmail?.let {
            tvEmail.text = "$it"
        }

        btnBack.setOnClickListener{
            intent = Intent(this,SettingActivity::class.java)
            startActivity(intent)
        }


        // Save changes to Firestore
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isNotEmpty() && userId != null) {
                val userUpdates = mapOf("first name" to newName)
                firestore.collection("users").document(userId).update(userUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}