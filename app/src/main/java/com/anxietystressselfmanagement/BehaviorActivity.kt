package com.anxietystressselfmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class BehaviorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body)

        val bodyBulletPoints = findViewById<TextView>(R.id.body_bullet_points)

        // Optional: Set the content dynamically
        bodyBulletPoints.text = """
            • Accident Prone
            • Insomnia
            • Loss of Appetite
            • Loss of Sex Drive
            • More Addiction
            • Restlessness
        """.trimIndent()
    }
}
