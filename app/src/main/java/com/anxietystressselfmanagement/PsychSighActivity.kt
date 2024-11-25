package com.anxietystressselfmanagement

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class PsychSighActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    private lateinit var button: Button
    private lateinit var lungLeftImageView: ImageView
    private lateinit var lungRightImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psych_sigh)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        button = findViewById(R.id.button2)
        lungLeftImageView = findViewById(R.id.LeftlungImageView)
        lungRightImageView = findViewById(R.id.RightLungImageView)

        // Safely handle AnimatedVectorDrawable
        val animatedDrawableL = lungLeftImageView.drawable as? AnimatedVectorDrawable
        val animatedDrawableR = lungRightImageView.drawable as? AnimatedVectorDrawable

        button.setOnClickListener {
            animatedDrawableL?.start() ?: run {
                // Log or handle missing animation for the left lung
                android.util.Log.e("PsychSighActivity", "Left lung drawable is not animated")
            }
            animatedDrawableR?.start() ?: run {
                // Log or handle missing animation for the right lung
                android.util.Log.e("PsychSighActivity", "Right lung drawable is not animated")
            }
        }

        // Set up DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
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

        // Customize drawer icon color
        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> navigateToActivity(DashBoardActivity::class.java)
                R.id.nav_settings -> navigateToActivity(SettingActivity::class.java)
                R.id.nav_about -> navigateToActivity(AboutActivity::class.java)
                R.id.nav_logout -> {
                    auth.signOut()
                    navigateToActivity(MainActivity::class.java)
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    // Utility function to navigate between activities
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
