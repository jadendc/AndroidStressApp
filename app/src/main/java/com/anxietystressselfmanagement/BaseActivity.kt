package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        if (auth.currentUser == null) {
            redirectToLogin()
        }
    }

    protected fun setupNavigationDrawer(drawerLayout: DrawerLayout, navigationView: NavigationView, toolbar: androidx.appcompat.widget.Toolbar) {
        this.drawerLayout = drawerLayout

        // Set up the toolbar
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
            menuItem.isChecked = true // Highlight the selected item
            when (menuItem.itemId) {
                R.id.nav_dashboard -> navigateTo(DashBoardActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingActivity::class.java)
                R.id.nav_about -> navigateTo(AboutActivity::class.java)
                R.id.nav_logout -> {
                    auth.signOut()
                    redirectToLogin()
                }
            }

            // Close the drawer
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateTo(activityClass: Class<*>) {
        if (this::class.java != activityClass) {
            val intent = Intent(this, activityClass)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
