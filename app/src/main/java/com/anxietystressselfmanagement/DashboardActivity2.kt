package com.anxietystressselfmanagement

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity2 : AppCompatActivity() {
    private lateinit var pieChart2: PieChart
    private lateinit var pieChart3: PieChart
    private lateinit var backButton: ImageView
    private lateinit var continueButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board2)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        pieChart2 = findViewById(R.id.pieChart2)
        pieChart3 = findViewById(R.id.pieChart3)
        backButton = findViewById(R.id.dbackButton)
        continueButton = findViewById(R.id.continueDashboardButton2)

        // Fix: Set up back button to go to DashboardActivity instead of AwarenessActivity
        backButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        continueButton.setOnClickListener {
            // Navigate to the next screen based on your app flow
            val intent = Intent(this, DashboardActivity3::class.java)
            startActivity(intent)
            finish()
        }

        // Fetch and display data
        fetchTriggersData()
        fetchSignsData()
    }

    private fun fetchTriggersData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Map to track trigger counts
        val triggerCounts = mutableMapOf(
            "Home" to 0,
            "School" to 0,
            "Social" to 0,
            "Work" to 0,
            "Other" to 0
        )

        // Fetch data from the last 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var daysProcessed = 0

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    // Get the selected SOTD (Situation of the Day)
                    val selectedSOTD = document.getString("selectedSOTD") ?: ""

                    // Update counts based on what was recorded
                    if (selectedSOTD.isNotEmpty()) {
                        triggerCounts[selectedSOTD] = triggerCounts[selectedSOTD]!! + 1
                    }

                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart2, triggerCounts, "Triggers")
                    }
                }
                .addOnFailureListener {
                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart2, triggerCounts, "Triggers")
                    }
                }

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun fetchSignsData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Map to track signs counts based on options from AwarenessActivity
        val signCounts = mutableMapOf(
            "Body" to 0,
            "Mind" to 0,
            "Feelings" to 0,
            "Behavior" to 0
        )

        // Fetch data from the last 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var daysProcessed = 0

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    // Get the selected signs option from AwarenessActivity
                    val signsOption = document.getString("signsOption") ?: ""

                    // Update counts based on what was recorded
                    if (signsOption.isNotEmpty() && signCounts.containsKey(signsOption)) {
                        signCounts[signsOption] = signCounts[signsOption]!! + 1
                    }

                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart3, signCounts, "Signs")
                    }
                }
                .addOnFailureListener {
                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart3, signCounts, "Signs")
                    }
                }

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun setupPieChart(chart: PieChart, dataCounts: Map<String, Int>, label: String) {
        val entries = mutableListOf<PieEntry>()
        dataCounts.forEach { (item, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), item))
        }

        // If no data, add an empty entry to show empty chart
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
        }

        val pastelColors = listOf(
            Color.parseColor("#c2c2f0"), // Pastel Lavender
            Color.parseColor("#ffeb99"), // Pastel Lemon
            Color.parseColor("#fdfd96"), // Pastel Yellow
            Color.parseColor("#ffb3b3"), // Pastel Pink
            Color.parseColor("#f7a9a9")  // Pastel Red
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = pastelColors
        dataSet.setValueFormatter(PercentFormatter(chart))
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        chart.data = data
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setDrawHoleEnabled(false)
        chart.setDrawEntryLabels(false)

        // Set up OnChartValueSelectedListener with the correct implementation
        if (label == "Triggers") {
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val categoryName = e.label
                        Toast.makeText(this@DashboardActivity2,
                            "Loading details for $categoryName",
                            Toast.LENGTH_SHORT).show()

                        // Fetch the data for the selected category
                        fetchDetailsForCategory(categoryName)
                    }
                }

                override fun onNothingSelected() {
                    // Do nothing when nothing is selected
                }
            })
        }

        chart.invalidate()

        // Setup legend
        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textColor = Color.BLACK
        legend.textSize = 16f
    }

    // Helper method to fetch and display category details
    private fun fetchDetailsForCategory(category: String) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Get data from the last 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedOptions = mutableListOf<String>()
        var completedQueries = 0

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val selectedSOTD = document.getString("selectedSOTD") ?: ""
                    val selectedOption = document.getString("selectedOption") ?: ""

                    if (selectedSOTD == category && selectedOption.isNotEmpty()) {
                        selectedOptions.add(selectedOption)
                    }

                    completedQueries++
                    if (completedQueries == 7) {
                        if (selectedOptions.isEmpty()) {
                            Toast.makeText(this,
                                "No options found for $category",
                                Toast.LENGTH_LONG).show()
                        } else {
                            // Format options for display
                            val uniqueOptions = selectedOptions.toSet()
                            val displayText = StringBuilder("$category details:\n")
                            uniqueOptions.forEach { option ->
                                val count = selectedOptions.count { it == option }
                                displayText.append("• $option ($count)\n")
                            }

                            // Show in a custom dialog
                            val dialog = AlertDialog.Builder(this)
                                .setTitle("Details for $category")
                                .setMessage(displayText.toString())
                                .setPositiveButton("OK", null)
                                .create()
                            dialog.show()
                        }
                    }
                }
                .addOnFailureListener {
                    completedQueries++
                    if (completedQueries == 7) {
                        Toast.makeText(this,
                            "Error loading details for $category",
                            Toast.LENGTH_SHORT).show()
                    }
                }

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }
}