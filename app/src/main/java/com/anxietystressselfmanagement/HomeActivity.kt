package com.anxietystressselfmanagement
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity: AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var home_WelcomeMes: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Make sure this points to your home layout

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        home_WelcomeMes = findViewById(R.id.home_WelcomeMes)
        val dashboardButton: Button = findViewById(R.id.home_DashBut)
        val exercisesButton: Button = findViewById(R.id.home_ExBut)
//        val dailyRoadmapButton: Button = findViewById(R.id.home_DailyRoadBut)
        val monthlyCalendarButton: Button = findViewById(R.id.home_MonthlyCalBut)
        val awarenessButton: Button = findViewById(R.id.home_awarenessbut)
//        val learnMoreButton: Button = findViewById(R.id.home_LearnMoreBut)

        // Display user welcome message
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val userName = currentUser.displayName
            if (!userName.isNullOrEmpty()) {
                home_WelcomeMes.text = "Welcome, $userName! What would you like to do?"
            } else {
                val userId = currentUser.uid
                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("first name")
                        home_WelcomeMes.text = "Welcome, $name! What would you like to do?"
                    }
                }.addOnFailureListener {
                    home_WelcomeMes.text = "Welcome! What would you like to do?"
                }
            }
        }

        // Button click handlers
        dashboardButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        exercisesButton.setOnClickListener {
            startActivity(Intent(this, ExercisesActivity::class.java))
        }
        awarenessButton.setOnClickListener {
            startActivity(Intent(this, SelfReflectActivity::class.java))
        }

//        dailyRoadmapButton.setOnClickListener{
//            startActivity(Intent(this, MoodActivity::class.java))
//        }

        monthlyCalendarButton.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
//        learnMoreButton.setOnClickListener {
//            startActivity(Intent(this, AboutActivity::class.java))
//        }

        // Add other button click handlers as needed

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
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

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_home -> startActivity(
                    Intent(this, HomeActivity::class.java)
                )
                R.id.nav_dashboard -> startActivity(
                    Intent(this, DashboardActivity::class.java)
                )

                R.id.nav_settings -> startActivity(
                    Intent(this, SettingActivity::class.java)
                )

                R.id.nav_about -> startActivity(
                    Intent(this, AboutActivity::class.java)
                )

                R.id.nav_membership -> startActivity(
                    Intent(this, MembershipActivity::class.java)
                )

                R.id.nav_exercises -> startActivity(
                    Intent(this, ExercisesActivity::class.java)
                )

                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}