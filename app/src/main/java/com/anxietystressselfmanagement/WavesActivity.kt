package com.anxietystressselfmanagement

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class WavesActivity : AppCompatActivity() {
    private var isMuted = false // Track mute state
    private var mediaPlayer: MediaPlayer? = null
    private var animationCount = 0 // Track animation cycles completed
    private var playCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waves) // XML layout for WavesActivity

        // Get number of cycles from Intent
        val cycles = intent.getIntExtra("selectedCycles", 1)

        // Initialize MediaPlayer for Waves sound
        mediaPlayer = MediaPlayer.create(this, R.raw.wavessound) // Replace with the correct sound file
        playSoundRepeatedly(cycles)

        // Back button
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MusicChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Sound button
        val soundButton: ImageView = findViewById(R.id.soundButton)
        soundButton.setOnClickListener {
            toggleSound(soundButton)
        }

        // Start the hollow square animation
        setupBreathCycleAnimation(cycles)
    }

    private fun setupBreathCycleAnimation(cycles: Int) {
        // References to views
        val movingCircle: ImageView = findViewById(R.id.movingCircle)
        val hollowSquare: View = findViewById(R.id.hollowSquare)
        val textBreatheTop: TextView = findViewById(R.id.textBreatheTop)
        val textHoldRight: TextView = findViewById(R.id.textHoldRight)
        val textBreatheBottom: TextView = findViewById(R.id.textBreatheBottom)
        val textHoldLeft: TextView = findViewById(R.id.textHoldLeft)

        // Square dimensions
        hollowSquare.post {
            val squareStartX = hollowSquare.x
            val squareStartY = hollowSquare.y
            val squareSize = hollowSquare.width.toFloat() // Assuming square shape

            // Animate circle along the square
            val animator = ValueAnimator.ofFloat(0f, 4f) // 4 edges of the square
            animator.duration = 16000 // 8 seconds for one full cycle
            animator.repeatCount = ValueAnimator.INFINITE

            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                val x: Float
                val y: Float

                when {
                    value < 1f -> { // Top edge (0 to 1)
                        x = squareStartX + value * squareSize
                        y = squareStartY
                        highlightText(textBreatheTop, listOf(textHoldRight, textBreatheBottom, textHoldLeft))
                    }
                    value < 2f -> { // Right edge (1 to 2)
                        x = squareStartX + squareSize
                        y = squareStartY + (value - 1f) * squareSize
                        highlightText(textHoldRight, listOf(textBreatheTop, textBreatheBottom, textHoldLeft))
                    }
                    value < 3f -> { // Bottom edge (2 to 3)
                        x = squareStartX + squareSize - (value - 2f) * squareSize
                        y = squareStartY + squareSize
                        highlightText(textBreatheBottom, listOf(textBreatheTop, textHoldRight, textHoldLeft))
                    }
                    else -> { // Left edge (3 to 4)
                        x = squareStartX
                        y = squareStartY + squareSize - (value - 3f) * squareSize
                        highlightText(textHoldLeft, listOf(textBreatheTop, textHoldRight, textBreatheBottom))
                    }
                }

                movingCircle.x = x - (movingCircle.width / 2)
                movingCircle.y = y - (movingCircle.height / 2)
            }

            // Add listener to handle animation cycles
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {
                    animationCount++
                    if (animationCount >= cycles) {
                        animator.cancel() // Stop the animation after the desired number of cycles
                    }
                }
            })

            animator.start()
        }
    }

    private fun highlightText(toHighlight: TextView, toDim: List<TextView>) {
        toHighlight.setTextColor(getColor(R.color.white)) // Highlighted text color
        toDim.forEach { it.setTextColor(getColor(R.color.grey)) } // Dimmed text color
    }

    private fun playSoundRepeatedly(cycles: Int) {
        mediaPlayer?.setOnCompletionListener {
            playCount++
            if (playCount < cycles) {
                mediaPlayer?.start()
            }
        }
        mediaPlayer?.start()
    }

    private fun toggleSound(soundButton: ImageView) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (isMuted) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            soundButton.setImageResource(R.drawable.sound) // Replace with your "sound on" icon
        } else {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            soundButton.setImageResource(R.drawable.mute) // Replace with your "sound off" icon
        }
        isMuted = !isMuted
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
