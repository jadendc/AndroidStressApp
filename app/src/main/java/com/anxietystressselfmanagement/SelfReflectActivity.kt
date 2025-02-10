package com.anxietystressselfmanagement

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class SelfReflectActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_self_reflect)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SelfReflectPrefs", MODE_PRIVATE)

        val whatSpinner: Spinner = findViewById(R.id.whatSpinner)
        val whoSpinner: Spinner = findViewById(R.id.whoSpinner)
        val whenSpinner: Spinner = findViewById(R.id.whenSpinner)
        val whereSpinner: Spinner = findViewById(R.id.whereSpinner)
        val whySpinner: Spinner = findViewById(R.id.whySpinner)

        val drawerLayout: DrawerLayout = findViewById(R.id.saveButton)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        val whoOptions = listOf("Who inspires you the most and why?", "Who do you trust deeply in your life, and what makes them trustworthy?", "Who do you want to be in five years? What steps can you take to get there?", "Who have you impacted positively recently, and how did it feel?", "Who challenges your way of thinking, and how do you respond to them?")
        val whatOptions = listOf("What are your biggest strengths, and how do you use them?", "What are your most significant weaknesses, and how can you improve them?", "What makes you feel truly happy and fulfilled?", "What is one thing you wish you could change about your daily life?", "What are you most proud of accomplishing so far?")
        val whenOptions = listOf("When was the last time you felt truly proud of yourself, and why?", "When do you feel most productive, and how can you replicate that environment?", "When was the last time you overcame a significant challenge? What did it teach you?", "When do you feel most stressed, and how can you manage it better?", "When was the last time you genuinely connected with someone? What made it meaningful?")
        val whereOptions = listOf("Where do you feel most at peace, and why?", "Where do you see yourself in the next year, and how can you get there?", "Where do you go when you need to recharge or reflect?", "Where have you felt the most challenged, and what did you learn?", "Where do you want to travel or explore, and why does it appeal to you?")
        val whyOptions = listOf("Why do you pursue your current goals? Are they aligned with your values?", "Why do you believe certain things about yourself or others?", "Why do you feel stuck in any area of your life, and what might help you move forward?", "Why are certain people or things important to you?", "Why do you react the way you do in difficult situations, and how can you improve?")

        setupNavigationDrawer(drawerLayout, navigationView, toolbar)

        backButton = findViewById(R.id.backButton)

        setupSpinner(whatSpinner, "whatSelection", whatOptions)
        setupSpinner(whoSpinner, "whoSelection", whoOptions)
        setupSpinner(whenSpinner, "whenSelection", whenOptions)
        setupSpinner(whereSpinner, "whereSelection", whereOptions)
        setupSpinner(whySpinner, "whySelection", whyOptions)

        backButton.setOnClickListener{
            startActivity(Intent(this,DashboardActivity::class.java))
            finish()
        }
    }

    private fun setupSpinner(spinner: Spinner, key: String, options: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, options)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Custom dropdown
        spinner.adapter = adapter

        // Restore the previously selected item
        val savedPosition = sharedPreferences.getInt(key, 0)
        spinner.setSelection(savedPosition)

        // Save the selected item when changed
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                sharedPreferences.edit().putInt(key, position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}