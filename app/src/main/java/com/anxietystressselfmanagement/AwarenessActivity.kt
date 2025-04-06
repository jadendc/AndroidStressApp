package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AwarenessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_awareness)


        val behaviorButton: Button = findViewById(R.id.behaviorButton)
        val feelingButton: Button = findViewById(R.id.feelingButton)
        val mindButton: Button = findViewById(R.id.mindButton)
        val bodyButton: Button = findViewById(R.id.bodyButton)


        behaviorButton.setOnClickListener {
            startActivity(Intent(this,BehaviorActivity::class.java))
            finish()
        }

        feelingButton.setOnClickListener {
            startActivity(Intent(this,FeelingsActivity::class.java))
            finish()
        }

        mindButton.setOnClickListener {
            startActivity(Intent(this,MindActivity::class.java))
            finish()
        }

        bodyButton.setOnClickListener {
            startActivity(Intent(this,BodyActivity::class.java))
            finish()
        }

            val backButton: ImageView = findViewById(R.id.backButton)
            backButton.setOnClickListener {
                val intent = Intent(this, SOTD::class.java)
                startActivity(intent)
                finish()
            }
        }
    }