package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class JournalActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var journalInput: EditText
    private lateinit var saveContinueButton: Button
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        drawerLayout = findViewById(R.id.saveButton) // Ensure this matches the correct ID in your XML
        navView = findViewById(R.id.nav_view)
        journalInput = findViewById(R.id.editTextTextMultiLine)
        saveContinueButton = findViewById(R.id.saveContinueButton)

        // Toolbar setup
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer toggle setup
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = getColor(R.color.white)


        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, DashBoardActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Save button click listener
        saveContinueButton.setOnClickListener {
            val journalText = journalInput.text.toString().trim()
            if (journalText.isNotEmpty()) {
                saveJournalEntry(journalText)
            } else {
                Toast.makeText(this, "Please type something to save", Toast.LENGTH_SHORT).show()
            }
        }
        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        // Navigation view item selection
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> navigateTo(DashBoardActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingActivity::class.java)
                R.id.nav_about -> navigateTo(AboutActivity::class.java)
                R.id.nav_logout -> logOut()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun saveJournalEntry(text: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user ID
        val userId = currentUser.uid

        // Get the current date
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        // Create a journal entry
        val entry = hashMapOf(
            "text" to text,
            "date" to formattedDate
        )

        val updateData = mapOf("text" to text)

        // Save the journal entry under the specific user's collection
        firestore.collection("users")
            .document(userId) // Save under the specific user's document
            .collection("dailyLogs") // Sub-collection for daily logs
            .document(formattedDate) // Use the current date as the document ID
            .set(updateData, SetOptions.merge()) // Add a new document for each entry
            .addOnSuccessListener {
                Toast.makeText(this, "Journal entry saved successfully!", Toast.LENGTH_SHORT).show()
                journalInput.text.clear() // Clear the input field after saving
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save journal entry: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun logOut() {
        auth.signOut()
        navigateTo(MainActivity::class.java)
        finish()
    }
}