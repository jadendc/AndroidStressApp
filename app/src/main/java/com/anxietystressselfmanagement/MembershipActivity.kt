package com.anxietystressselfmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.anxietystressselfmanagement.ui.activities.HomeActivity

class MembershipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership) // Ensure activity_sotd.xml exists
        // Find the button by its ID
        val button2 = findViewById<Button>(R.id.monthlySubscription)
        val button3 = findViewById<Button>(R.id.threeMonthSubscription)
        val button4 = findViewById<Button>(R.id.annualSubscription)
        val button5 = findViewById<Button>(R.id.continueToDetails)


        val backButton = findViewById<ImageView>(R.id.backButton)

        // Set a click listener to navigate back to MainActivity
        backButton.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()

        }
        button2.setOnClickListener {
            val intent1 = Intent(this, MembershipDetailsActivity::class.java)
            startActivity(intent1)
        }
        button3.setOnClickListener {

            val intent2 = Intent(this, MembershipDetailsActivity::class.java)
            startActivity(intent2)
        }
        button4.setOnClickListener {

            val intent3 = Intent(this, MembershipDetailsActivity::class.java)
            startActivity(intent3)
        }
        button5.setOnClickListener {

            val intent4 = Intent(this, MembershipDetailsActivity::class.java)
            startActivity(intent4)

            }
        }
}