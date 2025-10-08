package com.anxietystressselfmanagement.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import androidx.drawerlayout.widget.DrawerLayout
import com.anxietystressselfmanagement.DashboardActivity
import com.anxietystressselfmanagement.MainActivity
import com.anxietystressselfmanagement.MembershipActivity
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.ui.activities.SettingActivity
import com.anxietystressselfmanagement.ui.components.AboutText
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AboutActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

        // Find views from XML
        drawerLayout = findViewById(R.id.saveButton)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        // Set toolbar
        setSupportActionBar(toolbar)

        // Setup ActionBarDrawerToggle
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

        // Handle navigation clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> navigateTo(DashboardActivity::class.java)
                R.id.nav_home -> navigateTo(HomeActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingActivity::class.java)
                R.id.nav_about -> navigateTo(AboutActivity::class.java)
                R.id.nav_membership -> navigateTo(MembershipActivity::class.java)
                R.id.nav_logout -> {
                    auth.signOut()
                    navigateTo(MainActivity::class.java)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Compose integration for the text box
        val composeView: ComposeView = findViewById(R.id.aboutComposeView)
        composeView.setContent {
            AboutText()
        }
    }

    // Helper function to navigate and clear back stack
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}