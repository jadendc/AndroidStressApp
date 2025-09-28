package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.anxietystressselfmanagement.ui.activities.HomeActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

/**
 * ExercisesActivity presents various mindfulness and wellness exercises
 * using a modern, componentized approach with Material Design
 */
class ExercisesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var btnDestress: MaterialButton
    private lateinit var btnSleep: MaterialButton
    private lateinit var btnFocus: MaterialButton
    private lateinit var btnPsychSigh: MaterialButton

    // ViewModel
    private lateinit var viewModel: ExercisesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ExercisesViewModel::class.java]

        // Initialize UI components
        initViews()

        // Setup navigation drawer
        setupNavigationDrawer()

        // Setup modern back press handling
        setupBackPressHandling()

        // Setup UI listeners
        setupListeners()

        // Observe ViewModel events
        observeViewModel()
    }

    /**
     * Initialize all UI components from layout
     */
    private fun initViews() {
        // Find views
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        btnDestress = findViewById(R.id.buttonDestress)
        btnSleep = findViewById(R.id.buttonEaseSleep)
        btnFocus = findViewById(R.id.buttonStrengthenFocus)
        btnPsychSigh = findViewById(R.id.buttonPsychologicalSigh)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Exercises"
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
        toggle.drawerArrowDrawable.color = getColor(android.R.color.white)

        navigationView.setNavigationItemSelectedListener(this)
    }

    /**
     * Setup modern back press handling with OnBackPressedCallback
     */
    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    viewModel.navigateBack()
                }
            }
        })
    }

    /**
     * Setup all button click listeners
     */
    private fun setupListeners() {
        btnDestress.setOnClickListener {
            viewModel.navigateToExercise(ExercisesViewModel.ExerciseDestination.DESTRESS)
        }

        btnSleep.setOnClickListener {
            viewModel.navigateToExercise(ExercisesViewModel.ExerciseDestination.SLEEP)
        }

        btnFocus.setOnClickListener {
            viewModel.navigateToExercise(ExercisesViewModel.ExerciseDestination.FOCUS)
        }

        btnPsychSigh.setOnClickListener {
            viewModel.navigateToExercise(ExercisesViewModel.ExerciseDestination.PSYCH_SIGH)
        }
    }

    /**
     * Observe LiveData from ViewModel
     */
    private fun observeViewModel() {
        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                is ExercisesViewModel.NavigationEvent.NavigateToExercise -> {
                    when (event.destination) {
                        ExercisesViewModel.ExerciseDestination.DESTRESS ->
                            navigateToActivity(DestressActivity::class.java)

                        ExercisesViewModel.ExerciseDestination.SLEEP ->
                            navigateToActivity(SleepActivity::class.java)

                        ExercisesViewModel.ExerciseDestination.FOCUS ->
                            navigateToActivity(FocusActivity::class.java)

                        ExercisesViewModel.ExerciseDestination.PSYCH_SIGH ->
                            navigateToActivity(PsychSighActivity::class.java)
                    }
                }
                is ExercisesViewModel.NavigationEvent.NavigateBack -> {
                    navigateToActivity(HomeActivity::class.java)
                }
            }
        }
    }

    /**
     * Handle navigation menu item selection
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
     * Navigate to specified activity
     */
    private fun <T> navigateToActivity(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        if (activityClass == HomeActivity::class.java) {
            finish() // Only finish this activity when going back to Home
        }
    }

    /**
     * Logout the current user and navigate to login screen
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}