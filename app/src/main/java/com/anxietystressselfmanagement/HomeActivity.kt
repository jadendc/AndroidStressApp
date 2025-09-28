package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.unit.dp


/**
 * Modern Home Activity implementation using MVVM architecture and Material Design components.
 * The Home screen serves as the main entry point for users after logging in.
 */
class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var welcomeMessage: MaterialTextView
    private lateinit var dashboardButton: MaterialButton
    private lateinit var exercisesButton: MaterialButton
    private lateinit var monthlyCalendarButton: MaterialButton
    private lateinit var awarenessButton: MaterialButton
    private lateinit var navigationView: NavigationView
    private lateinit var composeView: ComposeView

    // ViewModel using the by viewModels() delegate
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI components
        initializeViews()

        // Setup navigation components
        setupNavigationDrawer()

        // Setup modern back navigation
        setupBackNavigation()

        // Setup button click listeners
        setupButtonListeners()

        // Observe LiveData from ViewModel
        observeViewModel()

        // Setup Compose integration
        setupComposeUI()

        // Load user data
        homeViewModel.loadUserData()
    }

    /**
     * Initialize UI components
     */
    private fun initializeViews() {
        // Find toolbar and navigation components
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Find text views
        welcomeMessage = findViewById(R.id.home_WelcomeMes)

        // Find buttons
        dashboardButton = findViewById(R.id.home_DashBut)
        exercisesButton = findViewById(R.id.home_ExBut)
        monthlyCalendarButton = findViewById(R.id.home_MonthlyCalBut)
        awarenessButton = findViewById(R.id.home_awarenessbut)

        // Find ComposeView
        composeView = findViewById(R.id.home_compose_view)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Home"
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
     * Setup modern back press handling
     */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    /**
     * Setup button click listeners
     */
    private fun setupButtonListeners() {
        dashboardButton.setOnClickListener {
            navigateTo(DashboardActivity::class.java)
        }

        exercisesButton.setOnClickListener {
            navigateTo(ExercisesActivity::class.java)
        }

        awarenessButton.setOnClickListener {
            navigateTo(SelfReflectActivity::class.java)
        }

        monthlyCalendarButton.setOnClickListener {
            navigateTo(CalendarActivity::class.java)
        }
    }

    /**
     * Observe LiveData from ViewModel
     */
    private fun observeViewModel() {
        homeViewModel.userName.observe(this, Observer { name ->
            if (!name.isNullOrEmpty()) {
                welcomeMessage.text = "Welcome, $name! What would you like to do?"
            } else {
                welcomeMessage.text = "Welcome! What would you like to do?"
            }
        })
    }

    /**
     * Setup Compose inside the existing XML layout
     */
    private fun setupComposeUI() {
        composeView.setContent {
            MaterialTheme {
                val userName by homeViewModel.userName.observeAsState()

                MotivationalCard(userName = userName)
            }
        }
    }

    /**
     * Handle navigation menu item selection
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> refreshCurrentScreen()
            R.id.nav_dashboard -> navigateTo(DashboardActivity::class.java)
            R.id.nav_settings -> navigateTo(SettingActivity::class.java)
            R.id.nav_about -> navigateTo(AboutActivity::class.java)
            R.id.nav_membership -> navigateTo(MembershipActivity::class.java)
            R.id.nav_exercises -> navigateTo(ExercisesActivity::class.java)
            R.id.nav_logout -> logoutUser()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Navigate to another activity
     */
    private fun navigateTo(destinationClass: Class<*>) {
        startActivity(Intent(this, destinationClass))
    }

    /**
     * Refresh the current screen (used when selecting Home while already on Home)
     */
    private fun refreshCurrentScreen() {
        homeViewModel.loadUserData()
    }

    /**
     * Log out the user and navigate to login screen
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

/**
 * Simple motivational card in Compose
 */
@Composable
fun MotivationalCard(userName: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = if (!userName.isNullOrEmpty()) {
                "Keep going, $userName! You're doing great 🎉"
            } else {
                "Stay motivated! 🌟"
            },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
