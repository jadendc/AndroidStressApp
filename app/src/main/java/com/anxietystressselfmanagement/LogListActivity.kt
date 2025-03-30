package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class LogListActivity : BaseActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel any running coroutines
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_list)

        auth = FirebaseAuth.getInstance()

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Retrieve the selected date from the Intent
        val selectedDate = intent.getStringExtra("selectedDate") ?: "No date provided"

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("Auth", "User ID: ${currentUser.uid}")
            fetchDailyLogForDate(currentUser.uid, selectedDate)
        } else {
            Log.e("Auth", "No user is logged in.")
            // Optionally redirect to the login screen
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Set up the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> navigateTo(DashboardActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingActivity::class.java)
                R.id.nav_about -> navigateTo(AboutActivity::class.java)
                R.id.nav_logout -> logOut()
                R.id.nav_membership -> navigateTo(MembershipActivity::class.java)
            }

            drawerLayout.closeDrawers()
            true
        }
    }

    private fun fetchDailyLogForDate(userId: String, date: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("dailyLogs")
            .document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val activity = document.getString("activity") ?: "No activity info logged"
                    val behavior = document.getString("behavior") ?: "No behavior info logged"
                    val behaviorText = document.getString("behaviorText") ?: "No written behavior info logged"
                    val body = document.getString("body") ?: "No body info logged"
                    val bodyText = document.getString("bodyText") ?: "No written body info logged"
                    val emotion = document.getString("emotion") ?: "No emotion info logged"
                    val emotionText = document.getString("emotionText") ?: "No written emotion info logged"
                    val feeling = document.getString("feeling") ?: "No feeling info logged"
                    val mind = document.getString("mind") ?: "No mind info logged"
                    val mindText = document.getString("mindText") ?: "No written mind info logged"
                    val sign = document.getString("sign") ?: "No sign info logged"
                    val strategy = document.getString("strategy") ?: "No strategy info logged"
                    val trigger = document.getString("trigger") ?: "No trigger info logged"


                    coroutineScope.launch(Dispatchers.Main) {
                        findViewById<TextView>(R.id.activityResult).text = activity
                        findViewById<TextView>(R.id.behaviorResult).text = behavior
//                        findViewById<TextView>(R.id.behaviorOutput).text = behaviorText
                        findViewById<TextView>(R.id.triggerResult).text = trigger
                        findViewById<TextView>(R.id.signResult).text = sign
                        findViewById<TextView>(R.id.strategiesResult).text = strategy
                        findViewById<TextView>(R.id.bodyResult).text = body
//                        findViewById<TextView>(R.id.bodyOutput).text = bodyText
                        findViewById<TextView>(R.id.mindResult).text = mind
//                        findViewById<TextView>(R.id.mindOutput).text = mindText
                        findViewById<TextView>(R.id.emotionResult).text = emotion
//                        findViewById<TextView>(R.id.emotionOutput).text = emotionText
                        findViewById<TextView>(R.id.feelingResult).text = feeling
//                        findViewById<TextView>(R.id.journalEntry).text = journal
                    }
                } else {
                    Log.e("Firestore", "No log found for the date: $date")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching daily log", e)
            }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun logOut() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

