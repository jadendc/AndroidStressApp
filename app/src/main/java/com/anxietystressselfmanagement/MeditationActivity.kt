package com.anxietystressselfmanagement

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer

class MeditationActivity : AppCompatActivity() {
    private var isMuted = false // Track mute state
    private var mediaPlayer: MediaPlayer? = null
    private var playCount = 0 // Track the number of times the sound has played

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation)

        // Get the number of cycles from the intent
        val cycles = intent.getIntExtra("selectedCycles", 1) // Default to 1 if not provided

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.meditationmusic)
        playSoundRepeatedly(cycles) // Play the sound the specified number of times

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Navigate back to the main menu
            val intent = Intent(this, MusicChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Sound button functionality
        val soundButton: ImageView = findViewById(R.id.soundButton)
        soundButton.setOnClickListener {
            // Toggle mute/unmute
            toggleSound(soundButton)
        }
    }

    private fun playSoundRepeatedly(cycles: Int) {
        // Play the sound and repeat based on the number of cycles
        mediaPlayer?.setOnCompletionListener {
            playCount++
            if (playCount < cycles) {
                mediaPlayer?.start() // Restart the sound
            }
        }
        mediaPlayer?.start() // Start the first playback
    }

    private fun toggleSound(soundButton: ImageView) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (isMuted) {
            // Unmute
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            soundButton.setImageResource(R.drawable.sound) // Replace with your "sound on" icon
        } else {
            // Mute
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            soundButton.setImageResource(R.drawable.mute) // Replace with your "sound off" icon
        }

        isMuted = !isMuted // Toggle mute state
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
