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
import androidx.appcompat.app.AppCompatActivity

class PsychSighActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var backButton: ImageView
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
        backButton = findViewById(R.id.backButton)
        lungLeftImageView = findViewById(R.id.LeftlungImageView)
        lungRightImageView = findViewById(R.id.RightLungImageView)
        instructionTextView = findViewById(R.id.instructionTextView)

        // Back button navigation
        backButton.setOnClickListener {
            navigateToDashboard()
        }

        // Start button functionality
        startButton.setOnClickListener {
            startPhysiologicalSigh()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashBoardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startPhysiologicalSigh() {
        breathCount = 0
        instructionTextView.text = "Prepare to start physiological sigh..."

        // Reset lungs to the initial state
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
            handler.postDelayed({
                navigateToDashboard()
            }, 1000)
            return
        }

        // First inhale: Partial fill
        instructionTextView.text = "Inhale deeply (1st, partial)..."
        animateLungFill(lungLeftImageView, R.drawable.l_lung_expand_animation)
        animateLungFill(lungRightImageView, R.drawable.r_lung_expand_animation)
        expandLungs(lungLeftImageView, lungRightImageView, startScale = 0.65f, endScale = 0.8f, duration = 2000)

        handler.postDelayed({
            instructionTextView.text = "Inhale again (2nd, full)..."
            animateLungFill(lungLeftImageView, R.drawable.l_lung_expand_animation)
            animateLungFill(lungRightImageView, R.drawable.r_lung_expand_animation)
            expandLungs(lungLeftImageView, lungRightImageView, startScale = 0.8f, endScale = 1.0f, duration = 1000)
        }, 2500)

        handler.postDelayed({
            instructionTextView.text = "Exhale slowly..."
            animateLungFill(lungLeftImageView, R.drawable.l_lung_defill_animation)
            animateLungFill(lungRightImageView, R.drawable.r_lung_defill_animation)
            deflateLungs(lungLeftImageView, lungRightImageView, startScale = 1.0f, endScale = 0.65f, duration = 4000)
        }, 6000)

        handler.postDelayed({
            breathCount++
            instructionTextView.text = "Breath ${breathCount + 1}/$totalBreaths"
            performSighCycle()
        }, 10000)
    }

    private fun animateLungFill(lungImageView: ImageView, drawableResId: Int) {
        lungImageView.setImageDrawable(getDrawable(drawableResId))
        val animatedDrawable = lungImageView.drawable as? AnimatedVectorDrawable
        animatedDrawable?.stop()
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
        // Set pivot points for both lungs
        setLungPivot(leftLung)
        setLungPivot(rightLung)

        // Create scale animations for the left lung
        val scaleUpLeftX = ObjectAnimator.ofFloat(leftLung, "scaleX", startScale, endScale)
        val scaleUpLeftY = ObjectAnimator.ofFloat(leftLung, "scaleY", startScale, endScale)

        // Create scale animations for the right lung
        val scaleUpRightX = ObjectAnimator.ofFloat(rightLung, "scaleX", startScale, endScale)
        val scaleUpRightY = ObjectAnimator.ofFloat(rightLung, "scaleY", startScale, endScale)

        // Create and configure the AnimatorSet
        AnimatorSet().apply {
            playTogether(scaleUpLeftX, scaleUpLeftY, scaleUpRightX, scaleUpRightY)
            this.duration = duration // Assign duration here
            start()
        }
    }


    private fun deflateLungs(
        leftLung: ImageView,
        rightLung: ImageView,
        startScale: Float,
        endScale: Float,
        duration: Long
    ) {
        // Declare a mutable variable if modification is needed
        var mutableDuration = duration

        setLungPivot(leftLung)
        setLungPivot(rightLung)

        val scaleDownLeftX = ObjectAnimator.ofFloat(leftLung, "scaleX", startScale, endScale)
        val scaleDownLeftY = ObjectAnimator.ofFloat(leftLung, "scaleY", startScale, endScale)

        val scaleDownRightX = ObjectAnimator.ofFloat(rightLung, "scaleX", startScale, endScale)
        val scaleDownRightY = ObjectAnimator.ofFloat(rightLung, "scaleY", startScale, endScale)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleDownLeftX, scaleDownLeftY, scaleDownRightX, scaleDownRightY)

        // Use the mutable variable instead of directly modifying the parameter
        mutableDuration += 500 // Example adjustment
        animatorSet.duration = mutableDuration
        animatorSet.start()
    }

    private fun setLungPivot(lungImageView: ImageView) {
        lungImageView.pivotX = lungImageView.width / 2f
        lungImageView.pivotY = lungImageView.height / 2f
    }
}
