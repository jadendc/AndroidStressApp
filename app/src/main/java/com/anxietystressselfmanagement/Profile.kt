package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.anxietystressselfmanagement.ui.activities.AboutActivity
import com.anxietystressselfmanagement.ui.activities.HomeActivity
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
                    navigateToActivity(SettingActivity::class.java, finishCurrent = true)
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
     * Show confirmation dialog before deleting user's associated data (logs).
     * This follows modern Material Design alert dialog pattern.
     */
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Associated Data") // Updated title
            .setMessage("This will permanently delete ALL your associated log data (daily entries, symptoms, triggers, etc.). Your profile information (name, email) will remain. This action cannot be undone.") // Updated message
            .setPositiveButton("Delete Data") { _, _ -> // Updated button text
                // Call ViewModel to handle deletion
                profileViewModel.deleteUserData()
            }
            .setNegativeButton("Cancel", null)  // Null listener automatically dismisses
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /**
     * Shows a dialog asking the user if they want to sign out after data deletion.
     */
    private fun showSignOutPromptDialog() {
        AlertDialog.Builder(this)
            .setTitle("Data Deleted")
            .setMessage("Associated log data deleted successfully. Do you want to sign out now?")
            .setPositiveButton("Sign Out") { dialog, _ ->
                logoutUser() // Call the existing logout function
                dialog.dismiss()
            }
            .setNegativeButton("Stay Signed In") { dialog, _ ->
                dialog.dismiss() // Just close the dialog
            }
            .setCancelable(false) // Force a choice
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
            // Only update if different to prevent cursor jumps
            if (etName.text.toString() != name) {
                etName.setText(name ?: "")
            }
        }

        // Observe loading state for save button
        profileViewModel.isLoading.observe(this) { isLoading ->
            btnSave.isEnabled = !isLoading
            btnSave.text = if (isLoading) "Saving..." else "Save Changes"
        }

        // Observe success message for save operation
        profileViewModel.successMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearMessages()
            }
        }

        profileViewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                profileViewModel.clearMessages()
            } else {
            }
        }

        // Observe deletion state
        profileViewModel.isDeleting.observe(this) { isDeleting ->
            btnDeleteData.isEnabled = !isDeleting
            btnDeleteData.text = if (isDeleting) "Deleting..." else "Delete My Data"
            // Optionally disable other buttons during deletion
            btnSave.isEnabled = !isDeleting
            btnBack.isEnabled = !isDeleting
        }

        // Observe deletion success
        profileViewModel.deleteSuccess.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show() // Show success toast
                profileViewModel.clearAllMessages() // Clear the message in ViewModel

                showSignOutPromptDialog()
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
        var navigateToClass: Class<*>? = null
        when (item.itemId) {
            R.id.nav_dashboard -> navigateToClass = DashboardActivity::class.java
            R.id.nav_settings -> navigateToClass = SettingActivity::class.java
            R.id.nav_home -> navigateToClass = HomeActivity::class.java
            R.id.nav_membership -> navigateToClass = MembershipActivity::class.java
            R.id.nav_about -> navigateToClass = AboutActivity::class.java
            R.id.nav_logout -> logoutUser()
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        // Perform navigation after drawer closes smoothly
        navigateToClass?.let {
            // Use postDelayed or Handler if needed for smoother transition after drawer closes
            navigateToActivity(it)
        }

        return true // Return true even if logoutUser was called directly
    }

    /**
     * Navigate to another activity, optionally finishing the current one.
     * Clears task by default for main navigation items.
     */
    private fun navigateToActivity(cls: Class<*>, finishCurrent: Boolean = false, clearTask: Boolean = false) {
        val intent = Intent(this, cls)
        if (clearTask) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        if (finishCurrent) {
            finish()
        }
    }


    /**
     * Log out user and navigate to login screen
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        // Use navigateToActivity to go to MainActivity and clear the task stack
        navigateToActivity(MainActivity::class.java, finishCurrent = true, clearTask = true)
    }
}