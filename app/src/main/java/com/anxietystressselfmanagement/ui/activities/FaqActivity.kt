package com.anxietystressselfmanagement.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.ComposeView
import androidx.drawerlayout.widget.DrawerLayout
import com.anxietystressselfmanagement.DashboardActivity
import com.anxietystressselfmanagement.MainActivity
import com.anxietystressselfmanagement.MembershipActivity
import com.anxietystressselfmanagement.R
import com.anxietystressselfmanagement.ui.activities.SettingActivity
import com.anxietystressselfmanagement.ui.components.FaqText
import com.anxietystressselfmanagement.ui.components.BackButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier

class FaqActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

        // Find views from XML
        drawerLayout = findViewById(R.id.saveButton) // This is your DrawerLayout
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

        // Compose integration for FAQ content with Back button
        val composeView: ComposeView = findViewById(R.id.faqComposeView)
        composeView.setContent {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                FaqText(
                    faqList = listOf(
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "What is HowRU?",
                            "HowRU is a mental wellness and stress management application designed to help users track their mood, manage anxiety, and practice mindfulness through guided activities and personalized insights. The app provides educational tools, breathing exercises, and self-reflection prompts to promote emotional awareness and self-care."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "How does HowRU work?",
                            "HowRU allows you to log your emotions and stress levels daily through an interactive mood tracker. Based on your entries, the app provides personalized coping strategies such as relaxation techniques, journaling prompts, and guided meditations. You can also view trends over time to understand your emotional patterns and identify triggers or improvements."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "Does HowRU replace professional therapy?",
                            "No. HowRU is **not a substitute for professional mental health care**. It is intended as a self-help and wellness tool to support daily stress management and emotional well-being. If you are experiencing severe stress, depression, anxiety, or any mental health crisis, please seek help from a qualified therapist, counselor, or healthcare provider. In case of an emergency, contact local emergency services or a mental health hotline immediately."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "Is my data secure?",
                            "Yes. HowRU prioritizes your privacy and data protection. All personal information and journal entries are securely stored in Google Firebase, which follows industry-standard encryption (AES-256) and secure communication protocols (HTTPS/TLS). Only authenticated users can access their data, and no information is shared with third parties without explicit user consent."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "Who can use HowRU?",
                            "HowRU is designed for users 18 years or older. It is not intended for children or minors under 18. If you are under 18, please use the app only with parental or guardian supervision and consent."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "Can I delete my data?",
                            "Yes. You can permanently delete your account and all associated data at any time. Simply go to Settings > Privacy > Delete My Account, or contact the support email listed in the Privacy Policy. Once deleted, your data cannot be recovered."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "Does HowRU share my information with third parties?",
                            "No. HowRU does not sell or share your personal data with advertisers or third-party analytics providers. Data access is restricted to the app’s secure backend for providing core functionality and improving the user experience."
                        ),
                        com.anxietystressselfmanagement.ui.components.FaqItem(
                            "What should I do if I’m in crisis?",
                            "If you ever feel in danger or are experiencing a mental health crisis, please seek immediate professional help. Contact your local emergency number or a mental health hotline such as the 988 Suicide and Crisis Lifeline (U.S.). HowRU is here to support your wellness journey, but it cannot provide emergency medical or therapeutic care."
                        )
                    )
                )

                // Back button at the end
                BackButton {
                    finish() // Returns to previous screen
                }
            }
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
