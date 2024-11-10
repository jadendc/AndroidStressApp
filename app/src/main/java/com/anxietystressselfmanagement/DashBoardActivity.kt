package com.anxietystressselfmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class DashBoardActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var welcomeTextView: TextView
    private lateinit var verySad: Button
    private lateinit var sad: Button
    private lateinit var meh:Button
    private lateinit var happy: Button
    private lateinit var veryHappy: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        welcomeTextView = findViewById(R.id.welcomeTextView)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        verySad = findViewById(R.id.verySadButton)
        sad = findViewById(R.id.sadButton)
        meh = findViewById(R.id.mehButton)
        happy = findViewById(R.id.happyButton)
        veryHappy = findViewById(R.id.veryHappyButton)


        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser != null) {

            val userName = currentUser.displayName
            if (!userName.isNullOrEmpty()) {
                welcomeTextView.text = "Welcome, $userName!"
            } else {
                // Option 2: Retrieve user name from Firestore
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
        } else {
            welcomeTextView.text = "Welcome, Guest!"
        }


        verySad.setOnClickListener {
            Toast.makeText(this,"Why so very sad?", Toast.LENGTH_SHORT).show()
        }

        sad.setOnClickListener{
            Toast.makeText(this,"Why so sad?", Toast.LENGTH_SHORT).show()
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
                R.id.nav_dashboard -> {
                    val intent = Intent(this, DashBoardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_settings -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_logout -> {
                        auth.signOut()

                        // Redirect to LoginActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                }
            }
            // Close the drawer
            drawerLayout.closeDrawers()
            true
        }
    }
}
