package com.anxietystressselfmanagement

import com.github.mikephil.charting.formatter.ValueFormatter
import android.app.DatePickerDialog
import android.graphics.Color
import com.github.mikephil.charting.formatter.PercentFormatter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class DashboardActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var continueButton: Button
    private lateinit var rangeSpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyButton: Button
    private lateinit var dateRangeLayout: LinearLayout

    // Date range values
    private val calendar = Calendar.getInstance()
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var currentRangeType = "Last 7 Days"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        // Initialize views
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        continueButton = findViewById(R.id.continueDashboardButton)
        rangeSpinner = findViewById(R.id.rangeSpinner)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        applyButton = findViewById(R.id.applyRangeButton)
        dateRangeLayout = findViewById(R.id.dateRangeLayout)

        // Configure date range spinner
        setupRangeSpinner()

        // Set initial dates
        setDateRange(7) // Default to 7 days
        updateDateButtonText()

        // Setup date picker buttons
        setupDateButtons()

        continueButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity2::class.java)
            startActivity(intent)
        }

        // Initial data fetch
        fetchDataForCurrentRange()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
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

    private fun setupRangeSpinner() {
        val ranges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Custom Range")

        // Create a completely custom ArrayAdapter with explicit text colors
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ranges) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(Color.WHITE)
                textView.textSize = 16f
                textView.setPadding(16, 16, 16, 16)
                textView.setBackgroundColor(Color.parseColor("#556874"))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(Color.BLACK)
                textView.textSize = 16f
                textView.setPadding(16, 16, 16, 16)
                textView.setBackgroundColor(Color.WHITE)
                return view
            }
        }

        // Set the adapter's dropdown view resource
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangeSpinner.adapter = adapter

        rangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRangeType = ranges[position]

                // Show/hide custom date range inputs
                if (currentRangeType == "Custom Range") {
                    dateRangeLayout.visibility = View.VISIBLE
                } else {
                    dateRangeLayout.visibility = View.GONE

                    // Reset date range based on selection and fetch data
                    when (currentRangeType) {
                        "Last 7 Days" -> setDateRange(7)
                        "Last 14 Days" -> setDateRange(14)
                        "Last 30 Days" -> setDateRange(30)
                    }

                    fetchDataForCurrentRange()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupDateButtons() {
        startDateButton.setOnClickListener {
            showDatePicker(startCalendar) {
                updateDateButtonText()
                // Don't automatically fetch data for custom range until Apply is clicked
            }
        }

        endDateButton.setOnClickListener {
            showDatePicker(endCalendar) {
                updateDateButtonText()
                // Don't automatically fetch data for custom range until Apply is clicked
            }
        }

        applyButton.setOnClickListener {
            if (validateDateRange()) {
                fetchDataForCurrentRange()
            }
        }
    }

    private fun validateDateRange(): Boolean {
        if (startCalendar.after(endCalendar)) {
            Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
            return false
        }

        // Calculate days between dates
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        if (diffInDays > 90) {
            Toast.makeText(this, "Date range cannot exceed 90 days", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun showDatePicker(calendar: Calendar, onDateSet: () -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSet()
        }, year, month, day).show()
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        startDateButton.text = dateFormat.format(startCalendar.time)
        endDateButton.text = dateFormat.format(endCalendar.time)
    }

    private fun setDateRange(days: Int) {
        // Reset end date to today
        endCalendar.time = Calendar.getInstance().time

        // Set start date to days before end date
        startCalendar.time = endCalendar.time
        startCalendar.add(Calendar.DAY_OF_YEAR, -(days - 1))

        updateDateButtonText()
    }

    private fun fetchDataForCurrentRange() {
        DateRangeManager.saveDateRange(this, currentRangeType, startCalendar, endCalendar)

        fetchInControlData()
        fetchMoodData()
    }

    private fun fetchInControlData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        // How many days are we displaying
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        // 90 day cap for queries.
        val daysToProcess = min(daysInRange, 90)

        val dates = ArrayList<String>()
        val entriesMap = mutableMapOf<Int, Float>()
        var daysProcessed = 0

        // Create a temporary calendar for iteration
        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = endCalendar.time

        for (i in 0 until daysToProcess) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCalendar.time)
            val formattedDate = dateFormat.format(tempCalendar.time)

            dates.add(0, formattedDate)

            val position = i
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val controlLevel = document.getLong("control")?.toFloat() ?: 0f
                    entriesMap[position] = controlLevel
                    daysProcessed++

                    if (daysProcessed == daysToProcess) {
                        val entries = (0 until daysToProcess).map { pos ->
                            BarEntry((daysToProcess - 1 - pos).toFloat(), entriesMap[pos] ?: 0f)
                        }

                        setupBarChart(entries, dates)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardActivity", "Error fetching control data for $dateKey", e)
                    daysProcessed++

                    if (daysProcessed == daysToProcess) {
                        val entries = (0 until daysToProcess).map { pos ->
                            BarEntry((daysToProcess - 1 - pos).toFloat(), entriesMap[pos] ?: 0f)
                        }
                        setupBarChart(entries, dates)
                    }
                }

            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun fetchMoodData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        val daysToProcess = min(daysInRange, 90)

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

        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = endCalendar.time

        for (i in 0 until daysToProcess) {
            val dateKey = dateFormat.format(tempCalendar.time)

            // Fetch mood data for this specific date
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val mood = document.getString("feeling") ?: ""

                    when (mood) {
                        "😁" -> moodCounts["Excited"] = moodCounts["Excited"]!! + 1
                        "😊" -> moodCounts["Happy"] = moodCounts["Happy"]!! + 1
                        "😐" -> moodCounts["Indifferent"] = moodCounts["Indifferent"]!! + 1
                        "😔" -> moodCounts["Sad"] = moodCounts["Sad"]!! + 1
                        "😢" -> moodCounts["Angry"] = moodCounts["Angry"]!! + 1
                    }

                    daysProcessed++
                    // After processing all days, update the pie chart
                    if (daysProcessed == daysToProcess) {
                        setupPieChart(moodCounts)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardActivity", "Error fetching mood data for $dateKey", e)
                    daysProcessed++
                    // Still try to update chart if we failed to get some data
                    if (daysProcessed == daysToProcess) {
                        setupPieChart(moodCounts)
                    }
                }

            // Move to the previous day
            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
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

        // Update feelings title to include date range
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startDate = dateFormat.format(startCalendar.time)
        val endDate = dateFormat.format(endCalendar.time)

        val feelingsTextView = findViewById<TextView>(R.id.textView16)
        feelingsTextView.text = "Feelings ($startDate - $endDate)"
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

        // Adjust label count based on number of dates
        if (dates.size <= 14) {
            xAxis.labelCount = dates.size
        } else {
            // For larger date ranges, show fewer labels to avoid overcrowding
            xAxis.labelCount = 7
        }

        // Update chart title to reflect the date range
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startDate = dateFormat.format(startCalendar.time)
        val endDate = dateFormat.format(endCalendar.time)

        // Find and update the title TextView
        val titleTextView = findViewById<TextView>(R.id.triggersTextView)
        titleTextView.text = "In Control ($startDate - $endDate)"

        barChart.invalidate()
    }
}