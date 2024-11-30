package com.anxietystressselfmanagement

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PsychSighActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    private lateinit var startButton: Button
    private lateinit var lungLeftImageView: ImageView
    private lateinit var lungRightImageView: ImageView
    private lateinit var instructionTextView: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var breathCount = 0
    private val totalBreaths = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psych_sigh)

        // Initialize Views
        startButton = findViewById(R.id.button2)
        lungLeftImageView = findViewById(R.id.LeftlungImageView)
        lungRightImageView = findViewById(R.id.RightLungImageView)
        instructionTextView = findViewById(R.id.instructionTextView)

        startButton.setOnClickListener {
            startPhysiologicalSigh()
        }
        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Set up the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
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
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, DashBoardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_daily -> {
                    val intent = Intent(this, DashBoardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_settings -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_logout -> {
                    auth.signOut()

                    // Redirect to LoginActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }

            // Close the drawer
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun startPhysiologicalSigh() {
        breathCount = 0
        instructionTextView.text = "Prepare to start physiological sigh..."

        // Reset lungs to initial deflated and unfilled state
        resetLungFill(lungLeftImageView)
        resetLungFill(lungRightImageView)
        deflateLungs(lungLeftImageView, lungRightImageView, startScale = 1.0f, endScale = 0.65f, duration = 1000)

        handler.postDelayed({
            performSighCycle()
        }, 1500)
    }

    private fun performSighCycle() {
        if (breathCount >= totalBreaths) {
            instructionTextView.text = "Exercise complete! Well done!"
            return
        }

        // First inhale: Partial fill and expand
        instructionTextView.text = "Inhale deeply (1st, partial)..."
        setLungDrawable(lungLeftImageView, R.drawable.l_lung_expand_animation)
        setLungDrawable(lungRightImageView, R.drawable.r_lung_expand_animation)
        animateLungFill(lungLeftImageView)
        animateLungFill(lungRightImageView)
        expandLungs(lungLeftImageView, lungRightImageView, startScale = 0.65f, endScale = 0.8f, duration = 2000)

        handler.postDelayed({
            // Second inhale: Full fill and expand
            instructionTextView.text = "Inhale again (2nd, full)..."
            animateLungFill(lungLeftImageView)
            animateLungFill(lungRightImageView)
            expandLungs(lungLeftImageView, lungRightImageView, startScale = 0.8f, endScale = 1.0f, duration = 1000)
        }, 2000)

        handler.postDelayed({
            // Exhale: Reverse fill and deflate
            instructionTextView.text = "Exhale slowly..."
            setLungDrawable(lungLeftImageView, R.drawable.l_lung_defill_animation)
            setLungDrawable(lungRightImageView, R.drawable.r_lung_defill_animation)
            reverseLungFill(lungLeftImageView)
            reverseLungFill(lungRightImageView)
            deflateLungs(lungLeftImageView, lungRightImageView, startScale = 1.0f, endScale = 0.65f, duration = 4000)
        }, 3000)

        handler.postDelayed({
            breathCount++
            instructionTextView.text = "Breath ${breathCount + 1}/$totalBreaths"
            performSighCycle()
        }, 8000)
    }

    private fun setLungDrawable(lungImageView: ImageView, drawableResId: Int) {
        lungImageView.setImageDrawable(getDrawable(drawableResId))
    }

    private fun animateLungFill(lungImageView: ImageView) {
        val animatedDrawable = lungImageView.drawable as? AnimatedVectorDrawable
        animatedDrawable?.start()
    }

    private fun reverseLungFill(lungImageView: ImageView) {
        val animatedDrawable = lungImageView.drawable as? AnimatedVectorDrawable
        animatedDrawable?.start()
    }

    private fun resetLungFill(lungImageView: ImageView) {
        val animatedDrawable = lungImageView.drawable as? AnimatedVectorDrawable
        animatedDrawable?.reset()
    }

    private fun expandLungs(
        leftLung: ImageView,
        rightLung: ImageView,
        startScale: Float,
        endScale: Float,
        duration: Long
    ) {
        setLungPivot(leftLung)
        setLungPivot(rightLung)

        val scaleUpLeftX = ObjectAnimator.ofFloat(leftLung, "scaleX", startScale, endScale)
        val scaleUpLeftY = ObjectAnimator.ofFloat(leftLung, "scaleY", startScale, endScale)

        val scaleUpRightX = ObjectAnimator.ofFloat(rightLung, "scaleX", startScale, endScale)
        val scaleUpRightY = ObjectAnimator.ofFloat(rightLung, "scaleY", startScale, endScale)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleUpLeftX, scaleUpLeftY, scaleUpRightX, scaleUpRightY)
        animatorSet.duration = duration
        animatorSet.start()
    }

    private fun deflateLungs(
        leftLung: ImageView,
        rightLung: ImageView,
        startScale: Float,
        endScale: Float,
        duration: Long
    ) {
        setLungPivot(leftLung)
        setLungPivot(rightLung)

        val scaleDownLeftX = ObjectAnimator.ofFloat(leftLung, "scaleX", startScale, endScale)
        val scaleDownLeftY = ObjectAnimator.ofFloat(leftLung, "scaleY", startScale, endScale)

        val scaleDownRightX = ObjectAnimator.ofFloat(rightLung, "scaleX", startScale, endScale)
        val scaleDownRightY = ObjectAnimator.ofFloat(rightLung, "scaleY", startScale, endScale)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleDownLeftX, scaleDownLeftY, scaleDownRightX, scaleDownRightY)
        animatorSet.duration = duration
        animatorSet.start()
    }

    private fun setLungPivot(lungImageView: ImageView) {
        lungImageView.pivotX = lungImageView.width / 2f
        lungImageView.pivotY = lungImageView.height / 2f
    }

}
