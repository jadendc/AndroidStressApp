package com.anxietystressselfmanagement
import android.widget.TextView
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelfReflectActivity : BaseActivity() {
    private val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backButton: ImageView
    private lateinit var whoSpinner: Spinner
    private lateinit var whatSpinner: Spinner
    private lateinit var whenSpinner: Spinner
    private lateinit var whereSpinner: Spinner
    private lateinit var whySpinner: Spinner


    private lateinit var submitButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_self_reflect)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SelfReflectPrefs", MODE_PRIVATE)

        whoSpinner = findViewById(R.id.whoSpinner)
        whatSpinner = findViewById(R.id.whatSpinner)
        whenSpinner = findViewById(R.id.whenSpinner)
        whereSpinner = findViewById(R.id.whereSpinner)
        whySpinner = findViewById(R.id.whySpinner)
        submitButton = findViewById(R.id.selfReflectbut)

        val drawerLayout: DrawerLayout = findViewById(R.id.saveButton)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        val whoOptions = listOf("Select an option... ") + listOf("Who inspires you the most and why?"
            , "Who do you trust deeply in your life, and what makes them trustworthy?"
            , "Who do you want to be in five years? What steps can you take to get there?"
            , "Who have you impacted positively recently, and how did it feel?"
            , "Who challenges your way of thinking, and how do you respond to them?")
        val whatOptions = listOf("Select an option... ") + listOf("What are your biggest strengths, and how do you use them?",
            "What are your most significant weaknesses, and how can you improve them?",
            "What makes you feel truly happy and fulfilled?",
            "What is one thing you wish you could change about your daily life?",
            "What are you most proud of accomplishing so far?")
        val whenOptions = listOf("Select an option... ") + listOf("When was the last time you felt truly proud of yourself, and why?",
            "When do you feel most productive, and how can you replicate that environment?",
            "When was the last time you overcame a significant challenge? What did it teach you?",
            "When do you feel most stressed, and how can you manage it better?", "When was the last time you genuinely connected with someone? What made it meaningful?")
        val whereOptions = listOf("Select an option... ") + listOf("Where do you feel most at peace, and why?",
            "Where do you see yourself in the next year, and how can you get there?",
            "Where do you go when you need to recharge or reflect?",
            "Where have you felt the most challenged, and what did you learn?",
            "Where do you want to travel or explore, and why does it appeal to you?")
        val whyOptions = listOf("Select an option... ") + listOf("Why do you pursue your current goals? Are they aligned with your values?",
            "Why do you believe certain things about yourself or others?",
            "Why do you feel stuck in any area of your life, and what might help you move forward?",
            "Why are certain people or things important to you?",
            "Why do you react the way you do in difficult situations, and how can you improve?")

        setupNavigationDrawer(drawerLayout, navigationView, toolbar)

        backButton = findViewById(R.id.backButton)

        setupSpinner(whatSpinner, "whatSelection", whatOptions)
        setupSpinner(whoSpinner, "whoSelection", whoOptions)
        setupSpinner(whenSpinner, "whenSelection", whenOptions)
        setupSpinner(whereSpinner, "whereSelection", whereOptions)
        setupSpinner(whySpinner, "whySelection", whyOptions)

        backButton.setOnClickListener{
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }
        submitButton.setOnClickListener {
            saveSelfReflect()
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSpinner(spinner: Spinner, key: String, options: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_dropdown_item, options)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Custom dropdown
        spinner.adapter = adapter

        // Restore the previously selected item
//        val savedPosition = sharedPreferences.getInt(key, 0)
//        spinner.setSelection(savedPosition)

        // Save the selected item when changed
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(resources.getColor(R.color.white))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    private fun saveSelfReflect() {
        // Collect data from inputs
        val who = whoSpinner.selectedItem.toString()
        val what = whatSpinner.selectedItem.toString()
        val whenn = whenSpinner.selectedItem.toString()
        val where = whereSpinner.selectedItem.toString()
        val why = whySpinner.selectedItem.toString()





        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        // Generate a unique document ID based on the current date
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormatter.format(Date())

        // Create a log object
        val logData = mapOf(
            "who" to who,
            "what" to what,
            "when" to whenn,
            "where" to where,
            "why" to why,

            )

        // Save the log under the user's document in Firestore
        firestore.collection("users")
            .document(userId) // Unique user ID
            .collection("selfReflects") // Sub-collection self reflect
            .document(currentDate) // Unique document for the date
            .set(logData, SetOptions.merge()) // Use merge to avoid overwriting existing fields
            .addOnSuccessListener {
                Toast.makeText(this, "Self reflect saved successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save self reflect: ${e.message}", Toast.LENGTH_SHORT).show()
            }



    }
    private fun clearForm() {
        whoSpinner.setSelection(0)
        whatSpinner.setSelection(0)
        whenSpinner.setSelection(0)
        whereSpinner.setSelection(0)
        whySpinner.setSelection(0)
    }

}