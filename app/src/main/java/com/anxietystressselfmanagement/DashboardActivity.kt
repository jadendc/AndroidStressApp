package com.anxietystressselfmanagement

import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color
import com.github.mikephil.charting.formatter.PercentFormatter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
class DashboardActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var continueButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        continueButton = findViewById(R.id.continueDashboardButton)
        continueButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity2::class.java)
            startActivity(intent)
        }
        fetchInControlData()
        fetchMoodData()
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout) // Use correct ID
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)


        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> drawerLayout.closeDrawers()
                R.id.nav_settings -> startActivity(Intent(this, SettingActivity::class.java))
                R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_membership -> startActivity(Intent(this, MembershipActivity::class.java))
                R.id.nav_exercises -> startActivity(Intent(this, ExercisesActivity::class.java))
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
    private fun fetchInControlData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<String>()
        val entriesMap = mutableMapOf<Int, Float>() // Map position to control value
        var daysProcessed = 0


        for (i in 0..6) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val formattedDate = dateFormat.format(calendar.time)


            dates.add(0, formattedDate)


            val position = i
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val controlLevel = document.getLong("control")?.toFloat() ?: 0f
                    entriesMap[position] = controlLevel
                    daysProcessed++


                    if (daysProcessed == 7) {

                        val entries = (0..6).map { pos ->

                            BarEntry((6-pos).toFloat(), entriesMap[pos] ?: 0f)
                        }


                        setupBarChart(entries, dates)


                        Log.d("DashboardActivity", "Bar chart data: $entriesMap")
                        Log.d("DashboardActivity", "Bar chart dates: $dates")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardActivity", "Error fetching control data for $dateKey", e)
                    daysProcessed++


                    if (daysProcessed == 7) {
                        val entries = (0..6).map { pos ->
                            BarEntry((6-pos).toFloat(), entriesMap[pos] ?: 0f)
                        }
                        setupBarChart(entries, dates)
                    }
                }

            calendar.add(Calendar.DAY_OF_YEAR, -1) //
        }
    }
    private fun fetchMoodData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var daysProcessed = 0

        // Map to count moods
        val moodCounts = mutableMapOf(
            "Excited" to 0,
            "Happy" to 0,
            "Indifferent" to 0,
            "Sad" to 0,
            "Angry" to 0
        )

        for (i in 0..6) {  // Fetch data for the last 7 days starting from today
            val dateKey = dateFormat.format(calendar.time)
            Log.d("DashboardActivity", "Fetching mood for date: $dateKey")

            // Fetch mood data for this specific date
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val mood = document.getString("feeling") ?: ""
                    Log.d("DashboardActivity", "Mood for $dateKey: $mood")

                    when (mood) {
                        "😁" -> moodCounts["Excited"] = moodCounts["Excited"]!! + 1
                        "😊" -> moodCounts["Happy"] = moodCounts["Happy"]!! + 1
                        "😐" -> moodCounts["Indifferent"] = moodCounts["Indifferent"]!! + 1
                        "😔" -> moodCounts["Sad"] = moodCounts["Sad"]!! + 1
                        "😢" -> moodCounts["Angry"] = moodCounts["Angry"]!! + 1
                    }

                    daysProcessed++
                    // After processing all 7 days, update the pie chart
                    if (daysProcessed == 7) {
                        Log.d("DashboardActivity", "Updating pie chart with data: $moodCounts")
                        setupPieChart(moodCounts)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardActivity", "Error fetching mood data for $dateKey", e)
                    daysProcessed++
                    // Still try to update chart if we failed to get some data
                    if (daysProcessed == 7) {
                        Log.d("DashboardActivity", "Updating pie chart after some errors: $moodCounts")
                        setupPieChart(moodCounts)
                    }
                }

            // Move to the previous day
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }




    private fun setupPieChart(moodCounts: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        moodCounts.forEach { (mood, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), mood))
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
        dataSet.setValueFormatter(PercentFormatter(pieChart))
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setDrawHoleEnabled(false)
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()

        // Setup legend
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textColor = Color.BLACK
        legend.textSize = 16f
    }
    private fun setupBarChart(entries: List<BarEntry>, dates: List<String>) {
        val barDataSet = BarDataSet(entries, "In Control")
        barDataSet.color = Color.parseColor("#77dd77")
        val barData = BarData(barDataSet)

        barData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        })

        barChart.data = barData
        barChart.description.isEnabled = false

        // Set x-axis labels
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Set y-axis properties
        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f  // Start from 0
        leftAxis.axisMaximum = 5f  // Max value is 5
        leftAxis.granularity = 1f
        barChart.axisRight.isEnabled = false

        // Ensure all 7 labels are visible
        xAxis.labelCount = 7

        barChart.invalidate()
    }
}