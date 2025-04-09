package com.anxietystressselfmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity3 : AppCompatActivity() {

    private lateinit var pieChart4: PieChart
    private lateinit var pieChart5: PieChart
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board3)

        pieChart4 = findViewById(R.id.pieChart4)
        pieChart5 = findViewById(R.id.pieChart5)
        backButton = findViewById(R.id.dbackButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        backButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity2::class.java)
            startActivity(intent)
            finish()
        }

        fetchStrategiesData()
        fetchActionsData()
    }


    private fun fetchStrategiesData() {
        val userId = auth.currentUser?.uid ?: return

        val strategyCounts = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var daysProcessed = 0

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val strategy = document.getString("7strategies") ?: ""
                    if (strategy.isNotEmpty()) {
                        strategyCounts[strategy] = strategyCounts.getOrDefault(strategy, 0) + 1
                    }
                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart4, strategyCounts, "Strategies")
                    }
                }
                .addOnFailureListener {
                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart4, strategyCounts, "Strategies")
                    }
                }

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun fetchActionsData() {
        val userId = auth.currentUser?.uid ?: return

        val actionCounts = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var daysProcessed = 0

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val action = document.getString("7actions") ?: ""
                    if (action.isNotEmpty()) {
                        actionCounts[action] = actionCounts.getOrDefault(action, 0) + 1
                    }
                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart5, actionCounts, "Actions")
                    }
                }
                .addOnFailureListener {
                    daysProcessed++
                    if (daysProcessed == 7) {
                        setupPieChart(pieChart5, actionCounts, "Actions")
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
            Color.parseColor("#F4B6C2"), // Pastel Blush
            Color.parseColor("#A6E1D9"), // Pastel Aqua
            Color.parseColor("#F6D1C1"), // Pastel Salmon
            Color.parseColor("#E3A7D4"), // Pastel Orchid
            Color.parseColor("#C8D8A9")  // Pastel Olive Green
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
        if (label == "Strategies") {
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val categoryName = e.label
                        Toast.makeText(this@DashboardActivity3,
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

        // Enable word wrapping for legend labels
        legend.isWordWrapEnabled = true
        legend.maxSizePercent = 0.4f  // Adjust this value as needed (40% of chart width)

        // Optional: adjust entry spacing for better readability
        legend.yEntrySpace = 10f
    }

    // Helper method to fetch and display category details
    private fun fetchDetailsForCategory(category: String) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Get data from the last 7 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedSymptoms = mutableListOf<String>()
        var completedQueries = 0

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val strategies = document.getString("7strategies") ?: ""
                    val selectedSymptom = document.getString("selectedSymptom") ?: ""

                    if (strategies == category && selectedSymptom.isNotEmpty()) {
                        selectedSymptoms.add(selectedSymptom)
                    }

                    completedQueries++
                    if (completedQueries == 7) {
                        if (selectedSymptoms.isEmpty()) {
                            Toast.makeText(this,
                                "No symptoms found for $category",
                                Toast.LENGTH_LONG).show()
                        } else {
                            // Format options for display
                            val uniqueSymptoms = selectedSymptoms.toSet()
                            val displayText = StringBuilder("$category details:\n")
                            uniqueSymptoms.forEach { symptom ->
                                val count = selectedSymptoms.count { it == symptom }
                                displayText.append("• $symptom ($count)\n")
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
