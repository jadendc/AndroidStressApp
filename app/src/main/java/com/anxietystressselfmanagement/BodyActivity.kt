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

        // Optional: Set the content dynamically
        bodyBulletPoints.text = """
            • Difficulty Breathing
            • Fatigue
            • Headaches
            • High Blood Pressure
            • Palpitations
            • Skin Irritation
        """.trimIndent()

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, DashBoardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
