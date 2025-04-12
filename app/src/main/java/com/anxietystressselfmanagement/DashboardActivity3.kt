package com.anxietystressselfmanagement

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class DashboardActivity3 : AppCompatActivity() {

    private lateinit var pieChart4: PieChart
    private lateinit var pieChart5: PieChart
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rangeSpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyButton: Button
    private lateinit var dateRangeLayout: LinearLayout

    // Date range values
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var currentRangeType = "Last 7 Days"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board3)

        // Initialize views
        pieChart4 = findViewById(R.id.pieChart4)
        pieChart5 = findViewById(R.id.pieChart5)
        backButton = findViewById(R.id.dbackButton)
        rangeSpinner = findViewById(R.id.rangeSpinner)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        applyButton = findViewById(R.id.applyRangeButton)
        dateRangeLayout = findViewById(R.id.dateRangeLayout)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Date range setup
        setupRangeSpinner()
        setDateRange(7) // Default to 7 days
        updateDateButtonText()
        setupDateButtons()

        backButton.setOnClickListener {
            finish()
        }

        // Setup navigation drawer
        setupNavigationDrawer()

        // Fetch data based on the previously saved date range or use default
        val savedRange = DateRangeManager.getDateRange(this)
        if (savedRange != null) {
            currentRangeType = savedRange.rangeType
            startCalendar.time = savedRange.startDate
            endCalendar.time = savedRange.endDate

            // Update UI to reflect the saved range
            when (currentRangeType) {
                "Last 7 Days" -> rangeSpinner.setSelection(0)
                "Last 14 Days" -> rangeSpinner.setSelection(1)
                "Last 30 Days" -> rangeSpinner.setSelection(2)
                "Custom Range" -> {
                    rangeSpinner.setSelection(3)
                    dateRangeLayout.visibility = View.VISIBLE
                    updateDateButtonText()
                }
            }
        }

        fetchDataForCurrentRange()
    }

    override fun onResume() {
        super.onResume()

        val savedRange = DateRangeManager.getDateRange(this)
        if (savedRange != null) {
            val currentStart = startCalendar.timeInMillis
            val currentEnd = endCalendar.timeInMillis
            val savedStart = savedRange.startDate.time
            val savedEnd = savedRange.endDate.time

            if (currentStart != savedStart || currentEnd != savedEnd || currentRangeType != savedRange.rangeType) {
                currentRangeType = savedRange.rangeType
                startCalendar.time = savedRange.startDate
                endCalendar.time = savedRange.endDate

                // Update UI components
                when (currentRangeType) {
                    "Last 7 Days" -> rangeSpinner.setSelection(0, false)
                    "Last 14 Days" -> rangeSpinner.setSelection(1, false)
                    "Last 30 Days" -> rangeSpinner.setSelection(2, false)
                    "Custom Range" -> {
                        rangeSpinner.setSelection(3, false)
                        dateRangeLayout.visibility = View.VISIBLE
                    }
                }
                updateDateButtonText()

                // Refresh data
                fetchDataForCurrentRange()
            }
        }
    }

    private fun setupNavigationDrawer() {
        try {
            val drawerLayout: DrawerLayout? = findViewById(R.id.drawer_layout)
            val navigationView: NavigationView? = findViewById(R.id.nav_view)
            val toolbar: androidx.appcompat.widget.Toolbar? = findViewById(R.id.toolbar)

            if (drawerLayout != null && navigationView != null && toolbar != null) {
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
                        R.id.nav_dashboard -> {
                            startActivity(Intent(this, DashboardActivity::class.java))
                        }
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
        } catch (e: Exception) {
            Log.e("DashboardActivity3", "Error setting up navigation drawer: ${e.message}")
        }
    }

    private fun setupRangeSpinner() {
        val ranges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Custom Range")

        // Create a custom ArrayAdapter with explicit text colors
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
        startDateButton.setOnClickListener { showDatePicker(startCalendar) { updateDateButtonText() } }
        endDateButton.setOnClickListener { showDatePicker(endCalendar) { updateDateButtonText() } }
        applyButton.setOnClickListener { if (validateDateRange()) fetchDataForCurrentRange() }
    }

    private fun validateDateRange(): Boolean {
        if (startCalendar.after(endCalendar)) {
            Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
            return false
        }
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
        if (diffInDays > 90) {
            Toast.makeText(this, "Date range cannot exceed 90 days", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showDatePicker(calendar: Calendar, onDateSet: () -> Unit) {
        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(y, m, d)
            onDateSet()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        startDateButton.text = dateFormat.format(startCalendar.time)
        endDateButton.text = dateFormat.format(endCalendar.time)
    }

    private fun setDateRange(days: Int) {
        endCalendar.time = Calendar.getInstance().time
        startCalendar.time = endCalendar.time
        startCalendar.add(Calendar.DAY_OF_YEAR, -(days - 1))
        updateDateButtonText()
    }

    private fun fetchDataForCurrentRange() {
        DateRangeManager.saveDateRange(this, currentRangeType, startCalendar, endCalendar)

        fetchStrategiesData()
        fetchActionsData()
    }

    private fun fetchStrategiesData() {
        val userId = auth.currentUser?.uid ?: return

        val strategyCounts = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Calculate date range
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
        val daysToProcess = min(daysInRange, 90) // Limit to 90 days

        var daysProcessed = 0
        val tempCalendar = Calendar.getInstance().apply { time = endCalendar.time }

        for (i in 0 until daysToProcess) {
            val dateKey = dateFormat.format(tempCalendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val strategy = document.getString("7strategies") ?: ""
                    if (strategy.isNotEmpty()) {
                        strategyCounts[strategy] = strategyCounts.getOrDefault(strategy, 0) + 1
                    }

                    if (++daysProcessed == daysToProcess) {
                        setupPieChart(pieChart4, strategyCounts, "Strategies")
                    }
                }
                .addOnFailureListener {
                    if (++daysProcessed == daysToProcess) {
                        setupPieChart(pieChart4, strategyCounts, "Strategies")
                    }
                }

            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun fetchActionsData() {
        val userId = auth.currentUser?.uid ?: return

        val actionCounts = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Calculate date range
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
        val daysToProcess = min(daysInRange, 90) // Limit to 90 days

        var daysProcessed = 0
        val tempCalendar = Calendar.getInstance().apply { time = endCalendar.time }

        for (i in 0 until daysToProcess) {
            val dateKey = dateFormat.format(tempCalendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val action = document.getString("7actions") ?: ""
                    if (action.isNotEmpty()) {
                        actionCounts[action] = actionCounts.getOrDefault(action, 0) + 1
                    }

                    if (++daysProcessed == daysToProcess) {
                        setupPieChart(pieChart5, actionCounts, "Actions")
                    }
                }
                .addOnFailureListener {
                    if (++daysProcessed == daysToProcess) {
                        setupPieChart(pieChart5, actionCounts, "Actions")
                    }
                }

            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun setupPieChart(chart: PieChart, dataCounts: Map<String, Int>, label: String) {
        val entries = mutableListOf<PieEntry>()
        dataCounts.forEach { (item, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), item))
        }

        // If no data, add an empty entry
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

        val dataSet = PieDataSet(entries, "").apply {
            colors = pastelColors
            setValueFormatter(PercentFormatter(chart))
            valueTextColor = Color.BLACK
            valueTextSize = 16f
            setDrawValues(true)
        }

        chart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            setDrawHoleEnabled(false)
            setDrawEntryLabels(false)

            // Set up interactive selection for strategies chart
            if (label == "Strategies") {
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                        if (e is PieEntry) {
                            fetchDetailsForCategory(e.label)
                        }
                    }

                    override fun onNothingSelected() {
                        // Do nothing
                    }
                })
            }

            // Configure legend
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textColor = Color.BLACK
                textSize = 16f
                isWordWrapEnabled = true
                maxSizePercent = 0.4f
            }

            invalidate()
        }

        // Update title with date range
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startDate = dateFormat.format(startCalendar.time)
        val endDate = dateFormat.format(endCalendar.time)

        // Find and update the appropriate title TextView
        val titleView = when (label) {
            "Strategies" -> findViewById<TextView>(R.id.strategiesTextView)
            else -> findViewById<TextView>(R.id.actionsTextView)
        }
        titleView.text = "$label ($startDate - $endDate)"
    }

    private fun fetchDetailsForCategory(category: String) {
        Toast.makeText(this, "Loading details for $category", Toast.LENGTH_SHORT).show()

        val userId = auth.currentUser?.uid ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedSymptoms = mutableListOf<String>()

        // Calculate date range
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
        val daysToProcess = min(daysInRange, 90)

        var completedQueries = 0
        val tempCalendar = Calendar.getInstance().apply { time = endCalendar.time }

        for (i in 0 until daysToProcess) {
            val dateKey = dateFormat.format(tempCalendar.time)

            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { document ->
                    val strategies = document.getString("7strategies") ?: ""
                    val selectedSymptom = document.getString("selectedSymptom") ?: ""

                    if (strategies == category && selectedSymptom.isNotEmpty()) {
                        selectedSymptoms.add(selectedSymptom)
                    }

                    if (++completedQueries == daysToProcess) {
                        showSymptomDetails(category, selectedSymptoms)
                    }
                }
                .addOnFailureListener {
                    if (++completedQueries == daysToProcess) {
                        showSymptomDetails(category, selectedSymptoms)
                    }
                }

            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun showSymptomDetails(category: String, symptoms: List<String>) {
        if (symptoms.isEmpty()) {
            Toast.makeText(this, "No symptoms found for $category", Toast.LENGTH_LONG).show()
            return
        }

        // Group and count symptoms
        val symptomCounts = symptoms.groupingBy { it }.eachCount()

        // Build the display message
        val message = StringBuilder("$category details:\n\n")
        symptomCounts.forEach { (symptom, count) ->
            message.append("• $symptom ($count)\n")
        }

        // Show in dialog
        AlertDialog.Builder(this)
            .setTitle("Details for $category")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .create()
            .show()
    }
}