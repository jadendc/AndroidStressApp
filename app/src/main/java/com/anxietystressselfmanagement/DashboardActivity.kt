package com.anxietystressselfmanagement

import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color
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
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        barChart = findViewById(R.id.barChart)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetchInControlData()

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
        val entries = mutableListOf<BarEntry>()

        // Start from today and go back 5 days
        for (i in 0..4) { // From today (0) to 4 days ago
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val formattedDate = dateFormat.format(calendar.time)

            // Add the formatted date to the dates list
            dates.add(formattedDate)

            // Fetch the data for this day
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    // If data exists, add the control value; otherwise, use 0f
                    val controlLevel = document.getLong("control")?.toFloat() ?: 0f
                    entries.add(BarEntry(entries.size.toFloat(), controlLevel))

                    // If all 5 days have been processed, update the chart
                    if (entries.size == 5) {
                        setupBarChart(entries, dates)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardActivity", "Error fetching control data", e)
                }

            calendar.add(Calendar.DAY_OF_YEAR, -1) // Move to the previous day
        }
    }




    private fun setupBarChart(entries: List<BarEntry>, dates: List<String>) {
        val barDataSet = BarDataSet(entries, "In Control")
        barDataSet.color = Color.BLUE
        val barData = BarData(barDataSet)

        barData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() // Remove decimal points
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
        leftAxis.axisMinimum = 1f
        leftAxis.axisMaximum = 5f
        leftAxis.granularity = 1f
        barChart.axisRight.isEnabled = false

        barChart.invalidate()
    }
}