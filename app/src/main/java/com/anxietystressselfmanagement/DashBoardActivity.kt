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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class DashBoardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var welcomeTextView: TextView
    private lateinit var streakTextView: TextView
    private lateinit var verySad: Button
    private lateinit var sad: Button
    private lateinit var meh: Button
    private lateinit var happy: Button
    private lateinit var veryHappy: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var streakManager: StreakManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView)
        streakTextView = findViewById(R.id.streakTextView)
        verySad = findViewById(R.id.verySadButton)
        sad = findViewById(R.id.sadButton)
        meh = findViewById(R.id.mehButton)
        happy = findViewById(R.id.happyButton)
        veryHappy = findViewById(R.id.veryHappyButton)

        // Display user welcome message
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val userName = currentUser.displayName
            if (!userName.isNullOrEmpty()) {
                welcomeTextView.text = "Welcome, $userName!"
            } else {
                val userId = currentUser.uid
                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("first name")
                        welcomeTextView.text = "Welcome, $name!"
                    }
                }.addOnFailureListener {
                    welcomeTextView.text = "Error retrieving user data."
                }
            }

            // Initialize StreakManager and display current streak
            streakManager = StreakManager(db, currentUser.uid)
            updateStreakDisplay()
        } else {
            welcomeTextView.text = "Welcome, Guest!"
        }

        val test: Button = findViewById(R.id.button3)

        test.setOnClickListener {
            intent = Intent(this, PsychSighActivity::class.java)
            startActivity(intent)
        }

        // Emotion buttons click listeners
        verySad.setOnClickListener { Toast.makeText(this, "Why so very sad?", Toast.LENGTH_SHORT).show() }
        sad.setOnClickListener { Toast.makeText(this, "Why so sad?", Toast.LENGTH_SHORT).show() }
        meh.setOnClickListener { Toast.makeText(this, "Feeling meh?", Toast.LENGTH_SHORT).show() }
        happy.setOnClickListener { Toast.makeText(this, "Glad you're happy!", Toast.LENGTH_SHORT).show() }
        veryHappy.setOnClickListener { Toast.makeText(this, "You're very happy!", Toast.LENGTH_SHORT).show() }

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> startActivity(Intent(this, DashBoardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                R.id.nav_settings -> startActivity(Intent(this, SettingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
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

    override fun onResume() {
        super.onResume()
        // Update streak display each time the activity is resumed
        updateStreakDisplay()
    }

    private fun updateStreakDisplay() {
        streakManager.updateLoginStreak { currentStreak ->
            streakTextView.text = "Streak: $currentStreak Days!"
        }
    }
}

class StreakManager(private val db: FirebaseFirestore, private val userId: String) {

    private val streakDocumentRef = db.collection("streaks").document(userId)

    // Function to update the login streak
    fun updateLoginStreak(callback: (Int) -> Unit) {
        streakDocumentRef.get().addOnSuccessListener { document ->
            val today = Calendar.getInstance()
            val lastLoginDate = document.getTimestamp("lastLoginDate")?.toDate()
            val currentStreak = document.getLong("streakCount")?.toInt() ?: 0

            val isSameDay = lastLoginDate?.let {
                val lastLoginCalendar = Calendar.getInstance().apply { time = it }
                today.get(Calendar.YEAR) == lastLoginCalendar.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == lastLoginCalendar.get(Calendar.DAY_OF_YEAR)
            } ?: false

            var newStreakCount = currentStreak

            if (isSameDay) {
                Log.d("StreakManager", "Login on the same day, streak unchanged.")
            } else {
                val isConsecutiveDay = lastLoginDate?.let {
                    val lastLoginCalendar = Calendar.getInstance().apply { time = it }
                    today.get(Calendar.DAY_OF_YEAR) - lastLoginCalendar.get(Calendar.DAY_OF_YEAR) == 1 ||
                            (today.get(Calendar.DAY_OF_YEAR) == 1 && lastLoginCalendar.get(Calendar.DAY_OF_YEAR) == lastLoginCalendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                } ?: false

                newStreakCount = if (isConsecutiveDay) currentStreak + 1 else 1
                Log.d("StreakManager", "Streak updated to: $newStreakCount")
            }

            streakDocumentRef.set(mapOf(
                "streakCount" to newStreakCount,
                "lastLoginDate" to today.time
            )).addOnSuccessListener {
                callback(newStreakCount)
                Log.d("StreakManager", "Streak successfully updated in Firestore.")
            }.addOnFailureListener {
                Log.e("StreakManager", "Failed to update streak in Firestore", it)
            }
        }.addOnFailureListener {
            Log.e("StreakManager", "Error fetching streak document", it)
            callback(0) // Return 0 streak if there's an error
        }
    }
}