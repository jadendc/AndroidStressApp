package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyLogActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var activityInput: EditText
    private lateinit var triggerSpinner: Spinner
    private lateinit var signSpinner: Spinner
    private lateinit var strategiesSpinner: Spinner
    private lateinit var bodySpinner: Spinner
    private lateinit var mindSpinner: Spinner
    private lateinit var emotionSpinner: Spinner
    private lateinit var behaviorSpinner: Spinner
    private lateinit var bodyTextView: TextView
    private lateinit var behaviorTextView: TextView
    private lateinit var mindTextView:TextView
    private lateinit var emotionTextView: TextView



    private lateinit var submitButton: Button
    private lateinit var auth: FirebaseAuth

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_log)
        auth = FirebaseAuth.getInstance()

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        activityInput = findViewById(R.id.activityInput)
        triggerSpinner = findViewById(R.id.triggerSpinner)
        signSpinner = findViewById(R.id.signSpinner)
        bodySpinner = findViewById(R.id.bodySpinner)
        strategiesSpinner = findViewById(R.id.strategiesSpinner)
        mindSpinner = findViewById(R.id.mindSpinner)
        emotionSpinner = findViewById(R.id.emotionSpinner)
        behaviorSpinner = findViewById(R.id.behaviorSpinner)
        submitButton = findViewById(R.id.submitButton)

         bodyTextView = findViewById(R.id.bodyInput)
         behaviorTextView = findViewById(R.id.behaviorInput)
         mindTextView = findViewById(R.id.mindInput)
         emotionTextView = findViewById(R.id.emotionInput)


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
                R.id.nav_dashboard -> navigateTo(HomeActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingActivity::class.java)
                R.id.nav_about -> navigateTo(AboutActivity::class.java)
                R.id.nav_logout -> logOut()
                R.id.nav_membership -> navigateTo(MembershipActivity::class.java)
            }
            drawerLayout.closeDrawers()
            true
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Populate spinners with sample data
        populateSpinners()

        // Handle submit button click
        submitButton.setOnClickListener {
            saveDailyLog()
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Handle fetch button click
        /*fetchButton.setOnClickListener {
            navigateTo(LogListActivity::class.java) // Navigate to a screen showing saved logs
        }*/
    }

    private fun populateSpinners() {
        val triggers = listOf("Work", "Family", "Health", "Other")
        val signs = listOf("Headache", "Fatigue", "Tension", "Other")
        val strategies = listOf("Meditation", "Exercise", "Talking to Someone", "Other")
        val body = listOf("Headaches", "Skin Irritation", "High Blood Pressure", "Fatigue", "Palpitations", "Difficulty Breathing", "Custom")
        val mind = listOf("Worrying", "Muddled Thinking", "Impaired Judgement", "Indecision", "Difficulty Concentrating", "Custom")
        val emotion = listOf("Fear", "Irritability", "Depression", "Apathy", "Alienation", "Loss of Confidence", "Custom")
        val behavior = listOf("Addiction", "Less Appetite", "Less Sex Drive", "Insomnia", "Restlessness", "Accident Prone", "Custom")
        setSpinnerAdapter(triggerSpinner, triggers)
        setSpinnerAdapter(signSpinner, signs)
        setSpinnerAdapter(strategiesSpinner, strategies)
        setSpinnerAdapter(bodySpinner, body)
        setSpinnerAdapter(mindSpinner, mind)
        setSpinnerAdapter(emotionSpinner, emotion)
        setSpinnerAdapter(behaviorSpinner, behavior)
    }

    private fun setSpinnerAdapter(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun saveDailyLog() {
        // Collect data from inputs
        val activity = activityInput.text.toString().trim()
        val trigger = triggerSpinner.selectedItem.toString()
        val sign = signSpinner.selectedItem.toString()
        val strategy = strategiesSpinner.selectedItem.toString()
        val body = bodySpinner.selectedItem.toString()
        val mind = mindSpinner.selectedItem.toString()
        val emotion = emotionSpinner.selectedItem.toString()
        val behavior = behaviorSpinner.selectedItem.toString()
        val bodyText = bodyTextView.text.toString().trim()
        val behaviorText = behaviorTextView.text.toString().trim()
        val mindText = mindTextView.text.toString().trim()
        val emotionText = emotionTextView.text.toString().trim()

        // Validate required fields
        if (activity.isEmpty()) {
            Toast.makeText(this, "Please fill in the activity field", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        // Generate a unique document ID based on the current date
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormatter.format(Date())

        // Create a log object
        val logData = mapOf(
            "activity" to activity,
            "trigger" to trigger,
            "sign" to sign,
            "strategy" to strategy,
            "body" to body,
            "mind" to mind,
            "emotion" to emotion,
            "behavior" to behavior,
            "date" to currentDate,
            "bodyText" to bodyText,
            "mindText" to mindText,
            "emotionText" to emotionText,
            "behaviorText" to behaviorText
        )

        // Save the log under the user's document in Firestore
        firestore.collection("users")
            .document(userId) // Unique user ID
            .collection("dailyLogs") // Sub-collection for logs
            .document(currentDate) // Unique document for the date
            .set(logData, SetOptions.merge()) // Use merge to avoid overwriting existing fields
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
        bodySpinner.setSelection(0)
        mindSpinner.setSelection(0)
        emotionSpinner.setSelection(0)
        behaviorSpinner.setSelection(0)
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
