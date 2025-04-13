package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A modernized self-reflection activity using Material Design components
 * and modern architecture patterns.
 */
class SelfReflectActivity : BaseActivity() {
    private val TAG = "SelfReflectActivity"

    // Firebase references
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // UI components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var backButton: ImageView

    // Input fields
    private lateinit var whoInputLayout: TextInputLayout
    private lateinit var whatInputLayout: TextInputLayout
    private lateinit var whenInputLayout: TextInputLayout
    private lateinit var whereInputLayout: TextInputLayout
    private lateinit var whyInputLayout: TextInputLayout

    private lateinit var whoDropdown: AutoCompleteTextView
    private lateinit var whatDropdown: AutoCompleteTextView
    private lateinit var whenDropdown: AutoCompleteTextView
    private lateinit var whereDropdown: AutoCompleteTextView
    private lateinit var whyDropdown: AutoCompleteTextView

    private lateinit var submitButton: MaterialButton

    // Data lists for dropdown menus
    private val whoOptions = listOf(
        "Select a question...",
        "Who inspires you the most and why?",
        "Who do you trust deeply in your life, and what makes them trustworthy?",
        "Who do you want to be in five years? What steps can you take to get there?",
        "Who have you impacted positively recently, and how did it feel?",
        "Who challenges your way of thinking, and how do you respond to them?"
    )

    private val whatOptions = listOf(
        "Select a question...",
        "What are your biggest strengths, and how do you use them?",
        "What are your most significant weaknesses, and how can you improve them?",
        "What makes you feel truly happy and fulfilled?",
        "What is one thing you wish you could change about your daily life?",
        "What are you most proud of accomplishing so far?"
    )

    private val whenOptions = listOf(
        "Select a question...",
        "When was the last time you felt truly proud of yourself, and why?",
        "When do you feel most productive, and how can you replicate that environment?",
        "When was the last time you overcame a significant challenge? What did it teach you?",
        "When do you feel most stressed, and how can you manage it better?",
        "When was the last time you genuinely connected with someone? What made it meaningful?"
    )

    private val whereOptions = listOf(
        "Select a question...",
        "Where do you feel most at peace, and why?",
        "Where do you see yourself in the next year, and how can you get there?",
        "Where do you go when you need to recharge or reflect?",
        "Where have you felt the most challenged, and what did you learn?",
        "Where do you want to travel or explore, and why does it appeal to you?"
    )

    private val whyOptions = listOf(
        "Select a question...",
        "Why do you pursue your current goals? Are they aligned with your values?",
        "Why do you believe certain things about yourself or others?",
        "Why do you feel stuck in any area of your life, and what might help you move forward?",
        "Why are certain people or things important to you?",
        "Why do you react the way you do in difficult situations, and how can you improve?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_self_reflect)

        // Initialize UI components
        initializeViews()

        setupToolbar()

        setupNavigationDrawer()

        setupBackButton()

        // Set up all dropdown menus
        setupDropdownMenus()

        // Set up button click listener
        setupClickListeners()

        // Setup hardware back press handling
        setupBackPressHandling()
    }

    /**
     * Initialize all UI components
     */
    private fun initializeViews() {
        // Toolbar and navigation components
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Find the back button
        backButton = findViewById(R.id.back_button)

        // Input layouts
        whoInputLayout = findViewById(R.id.whoInputLayout)
        whatInputLayout = findViewById(R.id.whatInputLayout)
        whenInputLayout = findViewById(R.id.whenInputLayout)
        whereInputLayout = findViewById(R.id.whereInputLayout)
        whyInputLayout = findViewById(R.id.whyInputLayout)

        // Dropdown fields
        whoDropdown = findViewById(R.id.whoDropdown)
        whatDropdown = findViewById(R.id.whatDropdown)
        whenDropdown = findViewById(R.id.whenDropdown)
        whereDropdown = findViewById(R.id.whereDropdown)
        whyDropdown = findViewById(R.id.whyDropdown)

        // Submit button
        submitButton = findViewById(R.id.selfReflectbut)
    }

    /**
     * Setup toolbar without back button
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.title = "Self-Reflection"
    }

    /**
     * Setup our custom back button
     */
    private fun setupBackButton() {
        backButton.setOnClickListener {
            // Navigate back to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    /**
     * Set up the navigation drawer
     */
    private fun setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation view item clicks
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                // Add other menu item handling as needed
            }

            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    /**
     * Set up all dropdown menus with adapters and listeners
     */
    private fun setupDropdownMenus() {
        setupDropdown(whoDropdown, whoOptions)
        setupDropdown(whatDropdown, whatOptions)
        setupDropdown(whenDropdown, whenOptions)
        setupDropdown(whereDropdown, whereOptions)
        setupDropdown(whyDropdown, whyOptions)
    }

    /**
     * Configure a single dropdown with adapter and listener
     */
    private fun setupDropdown(dropdown: AutoCompleteTextView, options: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, options)

        // Set dropdown appearance
        adapter.setDropDownViewResource(R.layout.dropdown_item)

        dropdown.setAdapter(adapter)

        // Set dropdown popup properties
        dropdown.dropDownWidth = ViewGroup.LayoutParams.MATCH_PARENT
        dropdown.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT

        // Set initial value
        dropdown.setText(options[0], false)

        // Handle appearance based on selection
        dropdown.setOnItemClickListener { _, _, position, _ ->
            dropdown.setTextColor(
                if (position == 0) getColor(R.color.hint_text_color)
                else getColor(R.color.white)
            )
        }
    }

    /**
     * Set up click listeners for buttons
     */
    private fun setupClickListeners() {
        // Save button click listener
        submitButton.setOnClickListener {
            if (validateInputs()) {
                saveReflection()
            }
        }
    }

    /**
     * Set up modern back press handling
     */
    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    startActivity(Intent(this@SelfReflectActivity, HomeActivity::class.java))
                    finish()
                }
            }
        })
    }

    /**
     * Validate all input fields
     * @return true if all fields have valid selections
     */
    private fun validateInputs(): Boolean {
        var isValid = true

        // Check that each dropdown has a selection other than the first item
        if (whoDropdown.text.toString() == whoOptions[0]) {
            whoInputLayout.error = "Please select a 'Who' question"
            isValid = false
        } else {
            whoInputLayout.error = null
        }

        if (whatDropdown.text.toString() == whatOptions[0]) {
            whatInputLayout.error = "Please select a 'What' question"
            isValid = false
        } else {
            whatInputLayout.error = null
        }

        if (whenDropdown.text.toString() == whenOptions[0]) {
            whenInputLayout.error = "Please select a 'When' question"
            isValid = false
        } else {
            whenInputLayout.error = null
        }

        if (whereDropdown.text.toString() == whereOptions[0]) {
            whereInputLayout.error = "Please select a 'Where' question"
            isValid = false
        } else {
            whereInputLayout.error = null
        }

        if (whyDropdown.text.toString() == whyOptions[0]) {
            whyInputLayout.error = "Please select a 'Why' question"
            isValid = false
        } else {
            whyInputLayout.error = null
        }

        return isValid
    }

    /**
     * Save the reflection data to Firestore using Kotlin coroutines
     * for improved async handling
     */
    private fun saveReflection() {
        // Show loading state
        submitButton.isEnabled = false
        submitButton.text = "Saving..."

        // Get the current user ID
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("User not logged in!")
            submitButton.isEnabled = true
            submitButton.text = "Save Reflection"
            return
        }

        val userId = currentUser.uid

        // Generate a unique document ID based on the current date
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormatter.format(Date())

        // Create a reflection data map
        val reflectionData = mapOf(
            "who" to whoDropdown.text.toString(),
            "what" to whatDropdown.text.toString(),
            "when" to whenDropdown.text.toString(),
            "where" to whereDropdown.text.toString(),
            "why" to whyDropdown.text.toString(),
            "timestamp" to Date(),
            "date" to currentDate
        )

        lifecycleScope.launch {
            try {
                // Save the reflection data to Firestore
                firestore.collection("users")
                    .document(userId)
                    .collection("selfReflects")
                    .document(currentDate)
                    .set(reflectionData, SetOptions.merge())
                    .await()

                // Show success message and navigate back
                showSuccess("Reflection saved successfully!")
                clearForm()

                // Navigate back to home screen
                startActivity(Intent(this@SelfReflectActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                // Handle errors
                Log.e(TAG, "Error saving reflection: ${e.message}", e)
                showError("Failed to save reflection: ${e.message}")

                // Reset button state
                submitButton.isEnabled = true
                submitButton.text = "Save Reflection"
            }
        }
    }

    /**
     * Clear all form fields
     */
    private fun clearForm() {
        whoDropdown.setText(whoOptions[0], false)
        whatDropdown.setText(whatOptions[0], false)
        whenDropdown.setText(whenOptions[0], false)
        whereDropdown.setText(whereOptions[0], false)
        whyDropdown.setText(whyOptions[0], false)

        // Clear any errors
        whoInputLayout.error = null
        whatInputLayout.error = null
        whenInputLayout.error = null
        whereInputLayout.error = null
        whyInputLayout.error = null
    }

    /**
     * Show a success message using Snackbar
     */
    private fun showSuccess(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setBackgroundTint(getColor(R.color.success_color))
            .setTextColor(getColor(R.color.white))
            .show()
    }

    /**
     * Show an error message using Snackbar
     */
    private fun showError(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setBackgroundTint(getColor(R.color.error_color))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}