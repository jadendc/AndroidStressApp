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
            val intent = Intent(this, DashboardActivity::class.java)
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

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
        }

        val pastelColors = listOf(
            Color.parseColor("#77dd77"), // Pastel Green
            Color.parseColor("#a2c2e0"), // Pastel Blue
            Color.parseColor("#fdfd96"), // Pastel Yellow
            Color.parseColor("#d3d3d3"), // Pastel Gray
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
        chart.invalidate()

        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textColor = Color.BLACK
        legend.textSize = 16f

        // Add these lines to enable word wrapping
        legend.isWordWrapEnabled = true
        legend.maxSizePercent = 0.4f  // Adjust this value as needed (40% of chart width)

        // Optional: adjust entry spacing for better readability
        legend.yEntrySpace = 10f
    }
}
