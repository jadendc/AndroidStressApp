package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class DailyLogActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var activityInput: EditText
    private lateinit var triggerSpinner: Spinner
    private lateinit var signSpinner: Spinner
    private lateinit var stressLevelSpinner: Spinner
    private lateinit var strategiesSpinner: Spinner
    private lateinit var submitButton: Button

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_log)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        activityInput = findViewById(R.id.activityInput)
        triggerSpinner = findViewById(R.id.triggerSpinner)
        signSpinner = findViewById(R.id.signSpinner)
        strategiesSpinner = findViewById(R.id.strategiesSpinner)
        submitButton = findViewById(R.id.submitButton)



        // Toolbar and navigation drawer setup
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color = getColor(R.color.white)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))

        // Handle navigation menu item selection
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> navigateTo(DashBoardActivity::class.java)
                R.id.nav_daily -> navigateTo(DailyLogActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingActivity::class.java)
                R.id.nav_about -> navigateTo(AboutActivity::class.java)
                R.id.nav_logout -> logOut()
            }
            drawerLayout.closeDrawers()
            true
        }

        // Populate spinners with sample data
        populateSpinners()

        // Handle submit button click
        submitButton.setOnClickListener {
            saveDailyLog()
        }
    }

    private fun populateSpinners() {
        val triggers = listOf("Work", "Family", "Health", "Other")
        val signs = listOf("Headache", "Fatigue", "Tension", "Other")
        val strategies = listOf("Meditation", "Exercise", "Talking to Someone", "Other")

        setSpinnerAdapter(triggerSpinner, triggers)
        setSpinnerAdapter(signSpinner, signs)
        setSpinnerAdapter(strategiesSpinner, strategies)
    }

    private fun setSpinnerAdapter(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun saveDailyLog() {
        val activity = activityInput.text.toString().trim()
        val trigger = triggerSpinner.selectedItem.toString()
        val sign = signSpinner.selectedItem.toString()
        val stressLevel = stressLevelSpinner.selectedItem.toString()
        val strategy = strategiesSpinner.selectedItem.toString()

        if (activity.isEmpty()) {
            Toast.makeText(this, "Please fill in the activity field", Toast.LENGTH_SHORT).show()
            return
        }

        val log = hashMapOf(
            "activity" to activity,
            "trigger" to trigger,
            "sign" to sign,
            "strategy" to strategy,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("dailyLogs")
            .add(log)
            .addOnSuccessListener {
                Toast.makeText(this, "Daily log saved successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save log: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        activityInput.text.clear()
        triggerSpinner.setSelection(0)
        signSpinner.setSelection(0)
        strategiesSpinner.setSelection(0)
    }


    private fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    private fun logOut() {
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        navigateTo(MainActivity::class.java)
        finish()
    }
}