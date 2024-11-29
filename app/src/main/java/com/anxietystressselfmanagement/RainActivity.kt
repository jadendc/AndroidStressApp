package com.anxietystressselfmanagement

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RainActivity : AppCompatActivity() {
    private var isMuted = false // Track mute state
    private var mediaPlayer: MediaPlayer? = null
    private var playCount = 0 // Track playback cycles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rain) // XML layout for WavesActivity

        // Get number of cycles from Intent
        val cycles = intent.getIntExtra("selectedCycles", 1)

        // Initialize MediaPlayer for Waves sound
        mediaPlayer = MediaPlayer.create(this, R.raw.rainsound) // Replace with the correct sound file
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
