package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.util.Log
import android.widget.Toast
import com.anxietystressselfmanagement.ui.activities.AboutActivity
import com.anxietystressselfmanagement.ui.activities.HomeActivity

class SettingActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var navigationView: NavigationView

    private lateinit var logOutButton: Button
    private lateinit var profileButton: Button
    private lateinit var notificationButton: Button
    private lateinit var faqButton: Button

    companion object {
        private const val TAG = "SettingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

        initializeViews()

        setupNavigationDrawer()

        setupButtonListeners()
    }

    /**
     * Initialize all view components
     */
    private fun initializeViews() {
        try {
            drawerLayout = findViewById(R.id.drawer_layout)
            navigationView = findViewById(R.id.nav_view)

            toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            logOutButton = findViewById(R.id.logOutButton)
            profileButton = findViewById(R.id.profileButton)
            notificationButton = findViewById(R.id.profileButton4)

            faqButton = findViewById(R.id.profileButton3)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Set up the navigation drawer with proper toggle
     */
    private fun setupNavigationDrawer() {
        try {
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
                handleNavigationItemSelected(menuItem.itemId)
                // Close the drawer
                drawerLayout.closeDrawers()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up navigation drawer: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Set up click listeners for all buttons
     */
    private fun setupButtonListeners() {
        logOutButton.setOnClickListener {
            logOutUser()
        }

        profileButton.setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        notificationButton.setOnClickListener {
            navigateTo(NotificationsActivity::class.java)
        }

        faqButton.setOnClickListener {
            // If we implement FAQ then add this line, otherwise keep the Toast message
            // navigateTo(FaqActivity::class.java)

            // Or show a dialog/toast if not implemented yet
             Toast.makeText(this, "FAQ feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handle navigation item selection
     * @param itemId The ID of the selected menu item
     */
    private fun handleNavigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_dashboard -> navigateWithClearStack(DashboardActivity::class.java)
            R.id.nav_settings -> navigateWithClearStack(SettingActivity::class.java)
            R.id.nav_home -> navigateWithClearStack(HomeActivity::class.java)
            R.id.nav_membership -> navigateWithClearStack(MembershipActivity::class.java)
            R.id.nav_about -> navigateWithClearStack(AboutActivity::class.java)
            R.id.nav_logout -> logOutUser()
        }
    }

    /**
     * Helper method to navigate to another activity
     * @param activityClass The class of the activity to navigate to
     */
    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    /**
     * Helper method to navigate with clearing the back stack
     * @param activityClass The class of the activity to navigate to
     */
    private fun navigateWithClearStack(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Log out the current user and navigate to the main screen
     */
    private fun logOutUser() {
        auth.signOut()
        navigateWithClearStack(MainActivity::class.java)
    }
}