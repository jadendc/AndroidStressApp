package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DestressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destress)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
            finish()
        }

        val backgroundGif: ImageView = findViewById(R.id.backgroundGif)
        Glide.with(this)
            .asGif()
            .load(R.raw.skygif)
            .into(backgroundGif)

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, MusicChoiceActivity::class.java)
            intent.putExtra("previousActivity", "DestressActivity")
            startActivity(intent)
            finish()
        }
    }
}
