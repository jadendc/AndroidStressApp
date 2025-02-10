package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StressInduceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_stress_induce)

            // Find the TextViews for displaying the random questions
            val dopamineTextView = findViewById<TextView>(R.id.dopamineTextView)
            val oxytocinTextView = findViewById<TextView>(R.id.oxytocinTextView)
            val serotoninTextView = findViewById<TextView>(R.id.serotoninTextView)
            val adrenalineTextView = findViewById<TextView>(R.id.adrealineTextView)
            Handler(Looper.getMainLooper()).post {


                // Generate random questions for each hormone and set them in the TextViews
                dopamineTextView.text = getRandomQuestion("Dopamine")
                oxytocinTextView.text = getRandomQuestion("Oxytocin")
                serotoninTextView.text = getRandomQuestion("Serotonin")
                adrenalineTextView.text = getRandomQuestion("Adrenaline")
            }
            // Back button to navigate to Dashboard
            val backButton: ImageView = findViewById(R.id.backButton)
            backButton.setOnClickListener {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Method to get a random question for a specific hormone
        private fun getRandomQuestion(hormone: String): String {
            val questionList = questions[hormone]
            return if (questionList != null && questionList.isNotEmpty()) {
                questionList.random()
            } else {
                "No questions available for this hormone."
            }
        }

        // Map of hormones to their questions
        private val questions = mapOf(
            "Dopamine" to listOf(
                "Did you do something today that made you feel motivated or accomplished?",
                "Have you set a small goal you can achieve this week to boost your motivation?",
                "Can you think of a reward that will make tackling a tough task feel more exciting?"
            ),
            "Oxytocin" to listOf(
                "Did you make time to connect with someone you trust today?",
                "Have you shared a positive experience or appreciation with someone recently?",
                "Did you spend a few minutes with someone who brings you joy or comfort?"
            ),
            "Serotonin" to listOf(
                "Have you spent time in natural sunlight today?",
                "Did you eat any mood-boosting foods like nuts, seeds, or whole grains?",
                "Did you take a moment to practice deep breathing or another stress-relieving activity?"
            ),
            "Adrenaline" to listOf(
                "Did you challenge yourself with a new or exciting experience today?",
                "How do you stay calm and focused when things get intense?",
                "Have you given yourself time to unwind after a high-energy day?"
            )
        )
    }
