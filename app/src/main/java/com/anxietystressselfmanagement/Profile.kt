package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

/**
 * Modern implementation of ProfileActivity using MVVM architecture pattern
 * with ViewModels, LiveData, and Material Design components.
 */
class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var tvEmail: androidx.appcompat.widget.AppCompatTextView
    private lateinit var etName: TextInputEditText
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnDeleteData: MaterialButton
    private lateinit var navigationView: NavigationView

    // ViewModel
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize ViewModel
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialize UI components
        initViews()

        // Setup navigation drawer
        setupNavigationDrawer()

        // Setup modern back press handling
        setupBackPressHandling()

        // Setup UI listeners
        setupListeners()

        // Observe LiveData from ViewModel
        observeViewModel()

        // Load user data
        profileViewModel.loadUserData()
    }

    /**
     * Initialize all UI components from layout
     */
    private fun initViews() {
        // Find views
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        tvEmail = findViewById(R.id.tvEmail)
        etName = findViewById(R.id.etName)
        nameInputLayout = findViewById(R.id.nameInputLayout)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnDeleteData = findViewById(R.id.btnDeleteData)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
    }

    /**
     * Setup modern back press handling using OnBackPressedCallback
     * This replaces the deprecated onBackPressed() method
     */
    private fun setupBackPressHandling() {
        // Register a callback for when the back button is pressed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if drawer is open first
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Navigate back to SettingActivity
                    val intent = Intent(this@ProfileActivity, SettingActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    /**
     * Setup the Navigation Drawer
     */
    private fun setupNavigationDrawer() {
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

        navigationView.setNavigationItemSelectedListener(this)
    }

    /**
     * Setup click listeners and other event handlers
     */
    private fun setupListeners() {
        // Save button click listener
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()

            if (newName.isEmpty()) {
                nameInputLayout.error = "Name cannot be empty"
                return@setOnClickListener
            } else {
                nameInputLayout.error = null
            }

            // Show loading indicator
            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            // Update profile in ViewModel
            profileViewModel.updateUserName(newName)
        }

        // Back button click listener - uses the same logic as onBackPressed
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Delete data button click listener
        btnDeleteData.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    /**
     * Show confirmation dialog before deleting user data
     * This follows modern Material Design alert dialog pattern
     */
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete All My Data")
            .setMessage("This will permanently delete ALL your personal data, including logs, preferences, and profile information. You will have to sign back in after. This action cannot be undone.")
            .setPositiveButton("Delete Everything") { _, _ ->
                // Call ViewModel to handle deletion
                profileViewModel.deleteUserData()
            }
            .setNegativeButton("Cancel", null)  // Null listener automatically dismisses
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /**
     * Observe LiveData from ViewModel to update UI accordingly
     */
    private fun observeViewModel() {
        // Observe user email
        profileViewModel.userEmail.observe(this) { email ->
            tvEmail.text = email ?: "Email not available"
        }

        // Observe user name
        profileViewModel.userName.observe(this) { name ->
            if (!name.isNullOrEmpty() && etName.text.toString() != name) {
                etName.setText(name)
            }
        }

        // Observe loading state
        profileViewModel.isLoading.observe(this) { isLoading ->
            btnSave.isEnabled = !isLoading
            btnSave.text = if (isLoading) "Saving..." else "Save Changes"
        }

        // Observe success message
        profileViewModel.successMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearMessages() // Clear message after showing
            }
        }

        // Observe error message
        profileViewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                nameInputLayout.error = message
                profileViewModel.clearMessages() // Clear message after showing
            } else {
                nameInputLayout.error = null
            }
        }

        // Observe deletion state
        profileViewModel.isDeleting.observe(this) { isDeleting ->
            btnDeleteData.isEnabled = !isDeleting
            btnDeleteData.text = if (isDeleting) "Deleting..." else "Delete My Data"
        }

        // Observe deletion success
        profileViewModel.deleteSuccess.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                profileViewModel.clearAllMessages()

                // Log out user after successful deletion
                FirebaseAuth.getInstance().signOut()

                // Return to login/main screen
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // Observe deletion errors
        profileViewModel.deleteError.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                profileViewModel.clearAllMessages()
            }
        }
    }

    /**
     * Handle navigation item selection
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> navigateToActivity(DashboardActivity::class.java)
            R.id.nav_settings -> navigateToActivity(SettingActivity::class.java)
            R.id.nav_home -> navigateToActivity(HomeActivity::class.java)
            R.id.nav_membership -> navigateToActivity(MembershipActivity::class.java)
            R.id.nav_about -> navigateToActivity(AboutActivity::class.java)
            R.id.nav_logout -> logoutUser()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Navigate to another activity with clean back stack
     */
    private fun navigateToActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Log out user and navigate to login screen
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        navigateToActivity(MainActivity::class.java)
    }
}