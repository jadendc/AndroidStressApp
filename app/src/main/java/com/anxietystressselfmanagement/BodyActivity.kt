package com.anxietystressselfmanagement

import android.os.Bundle
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
    }
}
