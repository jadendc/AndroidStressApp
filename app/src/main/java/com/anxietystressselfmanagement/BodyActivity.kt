package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class BodyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body)

        val bodyBulletPoints = findViewById<TextView>(R.id.body_bullet_points)


        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, AwarenessActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
