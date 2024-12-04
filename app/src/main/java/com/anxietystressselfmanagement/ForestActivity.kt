package com.anxietystressselfmanagement

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ForestActivity : AppCompatActivity() {
    private var isMuted = false // Track mute state
    private var mediaPlayer: MediaPlayer? = null
    private var playCount = 0 // Track playback cycles
    private var handler: Handler? = null // Handler for managing GIF playback
    private var isActivityDestroyed = false // Track whether the Activity is destroyed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forest)

        val exerciseType = intent.getStringExtra("exerciseType")
        if (exerciseType == null) {
            Log.e("ForestActivity", "Exercise type is missing. Intent extras: ${intent.extras}")
            Toast.makeText(this, "An error occurred. Please restart the app.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("ForestActivity", "Received exerciseType: $exerciseType")

        val cycles = intent.getIntExtra("selectedCycles", 1)

        // Initialize MediaPlayer for Forest sound
        mediaPlayer = MediaPlayer.create(this, R.raw.forestsound)
        playSoundRepeatedly(cycles)

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            resetGifPlayback() // Clear GIF and handler tasks
            val intent = Intent(this, MusicChoiceActivity::class.java)
            intent.putExtra("previousActivity", exerciseType) // Pass exercise type back
            startActivity(intent)
            finish()
        }

        // Sound button functionality
        val soundButton: ImageView = findViewById(R.id.soundButton)
        soundButton.setOnClickListener {
            toggleSound(soundButton)
        }

        // Display the Forest GIF as background
        val backgroundGifView: ImageView = findViewById(R.id.backgroundGif)
        Glide.with(this)
            .asGif()
            .load(R.raw.forestgif) // Replace with Forest GIF resource
            .into(backgroundGifView)

        // Display and control the exercise GIF
        val exerciseGifView: ImageView = findViewById(R.id.exerciseGif)
        val gifDuration = getGifDuration(exerciseType) // Fetch duration for the selected GIF
        playGifForCycles(exerciseGifView, exerciseType, cycles, gifDuration)

        // Resize the exercise GIF
        val layoutParams = exerciseGifView.layoutParams
        layoutParams.width = (layoutParams.width * 1) // Adjust width
        layoutParams.height = (layoutParams.height * 1) // Adjust height
        exerciseGifView.layoutParams = layoutParams
    }

    private fun playGifForCycles(imageView: ImageView, exerciseType: String, cycles: Int, gifDuration: Long) {
        val gifResource = getGifResource(exerciseType)

        // Load the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(gifResource)
            .into(imageView)

        val totalDuration = gifDuration * cycles

        // Use a handler to stop GIF playback after the total duration
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            if (!isActivityDestroyed) { // Ensure the activity is not destroyed before clearing
                Glide.with(this).clear(imageView) // Clears the GIF after the duration
                Toast.makeText(this, "All Done!", Toast.LENGTH_SHORT).show()
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                        it.release() // Release resources
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this, DashBoardActivity::class.java))
                        }, totalDuration+3000)
                    }
                    mediaPlayer = null // Reset the reference
                }
            }
        }, totalDuration)

    }

    private fun getGifResource(exerciseType: String): Int {
        return when (exerciseType) {
            "DestressActivity" -> R.raw.destress_day_gif
            "SleepActivity" -> R.raw.night_time_gif
            "FocusActivity" -> R.raw.focus_time_gif
            else -> R.raw.forestgif // Default GIF for ForestActivity
        }
    }

    private fun getGifDuration(exerciseType: String): Long {
        return when (exerciseType) {
            "DestressActivity" -> 16500L // Duration for Destress GIF (16.5 seconds)
            "SleepActivity" -> 22500L // Duration for Sleep GIF (22.5 seconds)
            "FocusActivity" -> 14500L // Duration for Focus GIF (14.5 seconds)
            else -> 16000L // Default duration (16 seconds)
        }
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

    private fun resetGifPlayback() {
        // Stop the handler from performing any further tasks
        handler?.removeCallbacksAndMessages(null)
        handler = null

        // Clear both GIFs
        val backgroundGifView: ImageView = findViewById(R.id.backgroundGif)
        val exerciseGifView: ImageView = findViewById(R.id.exerciseGif)

        if (!isActivityDestroyed) { // Ensure the activity is not destroyed
            Glide.with(this).clear(backgroundGifView)
            Glide.with(this).clear(exerciseGifView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityDestroyed = true // Mark the activity as destroyed
        mediaPlayer?.release()
        mediaPlayer = null
        resetGifPlayback() // Reset GIF playback when activity is destroyed
    }
}
