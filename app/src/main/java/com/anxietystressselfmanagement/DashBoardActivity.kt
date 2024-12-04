package com.anxietystressselfmanagement

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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
import java.util.concurrent.TimeUnit


class DashBoardActivity : AppCompatActivity() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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
    private lateinit var monthlyButton: Button
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
        }

        val journalButton: Button = findViewById(R.id.journalButton)
        val psychButton: Button = findViewById(R.id.psychButton)
        val exerciseButton: Button = findViewById(R.id.exerciseButton)
        val monthlyButton: Button = findViewById(R.id.monthlyButton)
        val induceButton: Button = findViewById(R.id.induceButton)

        journalButton.setOnClickListener {
            intent = Intent(this, JournalActivity::class.java)
            startActivity(intent)
        }


        psychButton.setOnClickListener {
            intent = Intent(this, PsychSighActivity::class.java)
            startActivity(intent)
        }

        monthlyButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        psychButton.setOnClickListener {
            val intent = Intent(this, PsychSighActivity::class.java)
            startActivity(intent) }

        exerciseButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
        }

        induceButton.setOnClickListener {
            val intent = Intent(this, StressInduceActivity::class.java)
            startActivity(intent)
        }



        val intentDaily = Intent(this, DailyLogActivity::class.java)
        // Emotion buttons click listeners
        verySad.setOnClickListener {
            addFeelingToLog("😢")
            startActivity(intentDaily)
        }
        sad.setOnClickListener {
            addFeelingToLog("😔")
            startActivity(intentDaily)
        }
        meh.setOnClickListener {
            addFeelingToLog("😐")
            startActivity(intentDaily)
        }
        happy.setOnClickListener {
            addFeelingToLog("😊")
            startActivity(intentDaily)
        }
        veryHappy.setOnClickListener {
            startActivity(intentDaily)
            addFeelingToLog("😁")
        }



        // Setup navigation drawer
        drawerLayout = findViewById(R.id.saveButton)
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
                R.id.nav_dashboard -> startActivity(Intent(this,DashBoardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                R.id.nav_settings -> startActivity(Intent(this, SettingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })

                R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                R.id.nav_settings -> {
                    // Handle settings action
                }

                R.id.nav_exercises -> {
                    //Handle exercises action
                    val intent = Intent(this, ExercisesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }


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

    private fun addFeelingToLog(feeling: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashBoardActivity, "User not logged in!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val userId = currentUser.uid
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormatter.format(Date())
            val updateData = mapOf("feeling" to feeling)

            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("dailyLogs")
                    .document(currentDate)
                    .set(updateData, SetOptions.merge())
                    .await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashBoardActivity, "Feeling updated successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashBoardActivity, "Failed to update feeling: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    class StreakManager(private val db: FirebaseFirestore, private val userId: String) {

        private val streakDocumentRef = db.collection("users")
            .document(userId) // Associate streaks with the specific user
            .collection("streaks") // Use a sub-collection for streaks
            .document("currentStreak") // Single document to track current streak

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
                                (today.get(Calendar.DAY_OF_YEAR) == 1 && lastLoginCalendar.get(
                                    Calendar.DAY_OF_YEAR
                                ) == lastLoginCalendar.getActualMaximum(
                                    Calendar.DAY_OF_YEAR
                                ))
                    } ?: false

                    newStreakCount = if (isConsecutiveDay) currentStreak + 1 else 1
                    Log.d("StreakManager", "Streak updated to: $newStreakCount")
                }

                streakDocumentRef.set(
                    mapOf(
                        "streakCount" to newStreakCount,
                        "lastLoginDate" to today.time
                    )
                ).addOnSuccessListener {
                    callback(newStreakCount)
                    Log.d(
                        "StreakManager",
                        "Streak successfully updated in Firestore"
                    )
                }.addOnFailureListener {
                    Log.e("StreakManager", "Failed to update streak in Firestore", it)
                }
            }.addOnFailureListener {
                Log.e("StreakManager", "Error fetching streak document", it)
                callback(0) // Return 0 streak if there's an error
            }
        }
    }
}