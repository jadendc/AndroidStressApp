package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper

/**
 * Modern implementation of DashboardActivity using MVVM architecture pattern
 * with ViewModels, LiveData, and Material Design components.
 */
class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // UI Components
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var continueButton: MaterialButton
    private lateinit var rangeSpinner: MaterialButton
    private lateinit var startDateButton: MaterialButton
    private lateinit var endDateButton: MaterialButton
    private lateinit var applyButton: MaterialButton
    private lateinit var dateRangeLayout: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var triggersTextView: TextView
    private lateinit var feelingsTextView: TextView
    private lateinit var navigationView: NavigationView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var chartsContainer: View

    // ViewModel using the by viewModels() delegate
    private val dashboardViewModel: DashboardViewModel by viewModels()

    // Flag to prevent initial double-loading
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        // Initialize UI components
        initializeViews()

        // Setup navigation components
        setupNavigationDrawer()

        // Setup button click listeners
        setupButtonListeners()

        // Setup range spinner
        setupRangeSpinner()

        // Observe LiveData from ViewModel
        observeViewModel()

        // Load saved date range and initial data
        dashboardViewModel.loadSavedDateRange(this)
        isInitialLoad = false
    }

    override fun onResume() {
        super.onResume()

        // Skip refresh during initial loading (already handled in onCreate)
        if (isInitialLoad) return

        // Check for date range changes when returning to this activity
        dashboardViewModel.loadSavedDateRange(this)
    }

    /**
     * Initialize all UI components from layout
     */
    private fun initializeViews() {
        // Find views
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        continueButton = findViewById(R.id.continueDashboardButton)
        rangeSpinner = findViewById(R.id.rangeSpinner)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        applyButton = findViewById(R.id.applyRangeButton)
        dateRangeLayout = findViewById(R.id.dateRangeLayout)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        triggersTextView = findViewById(R.id.triggersTextView)
        feelingsTextView = findViewById(R.id.textView16)
        navigationView = findViewById(R.id.nav_view)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        chartsContainer = findViewById(R.id.chartsContainer)

        // Setup chart defaults
        setupChartDefaults()

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"
    }

    /**
     * Setup default settings for charts
     */
    private fun setupChartDefaults() {
        // Bar chart defaults
        barChart.setNoDataText("Loading data...")
        barChart.setNoDataTextColor(Color.WHITE)
        barChart.legend.textColor = Color.WHITE
        barChart.legend.textColor = Color.WHITE
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.description.isEnabled = false

        // Pie chart defaults
        pieChart.setNoDataText("Loading data...")
        pieChart.setNoDataTextColor(Color.WHITE)
        pieChart.description.isEnabled = false
    }

    /**
     * Setup the Navigation Drawer
     */
    private fun setupNavigationDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = getColor(R.color.white)

        navigationView.setNavigationItemSelectedListener(this)
    }

    /**
     * Setup button click listeners
     */
    private fun setupButtonListeners() {
        continueButton.setOnClickListener {
            navigateTo(DashboardActivity2::class.java)
        }

        startDateButton.setOnClickListener {
            showDatePicker(true)
        }

        endDateButton.setOnClickListener {
            showDatePicker(false)
        }

        applyButton.setOnClickListener {
            val (isValid, errorMessage) = dashboardViewModel.validateDateRange()
            if (isValid) {
                dashboardViewModel.fetchDataForCurrentRange(this)
            } else {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Setup the range selection dropdown using AlertDialog
     */
    private fun setupRangeSpinner() {
        val ranges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Custom Range")

        rangeSpinner.text = dashboardViewModel.dateRange.value?.first ?: "Last 7 Days"

        if (rangeSpinner.icon == null) {
            rangeSpinner.setIconResource(R.drawable.ic_calendar)
            rangeSpinner.iconGravity = MaterialButton.ICON_GRAVITY_START
        }

        rangeSpinner.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Date Range")
                .setItems(ranges) { dialog, which ->
                    val selectedRange = ranges[which]
                    rangeSpinner.text = selectedRange

                    // Show/hide custom date range inputs
                    if (selectedRange == "Custom Range") {
                        dateRangeLayout.visibility = View.VISIBLE
                    } else {
                        dateRangeLayout.visibility = View.GONE

                        // Reset date range based on selection and fetch data
                        when (selectedRange) {
                            "Last 7 Days" -> dashboardViewModel.setDateRange(7)
                            "Last 14 Days" -> dashboardViewModel.setDateRange(14)
                            "Last 30 Days" -> dashboardViewModel.setDateRange(30)
                        }

                        dashboardViewModel.fetchDataForCurrentRange(this)
                    }
                }
                .create()
                .show()
        }
    }

    /**
     * Show date picker dialog
     * @param isStartDate True if picking start date, false if picking end date
     */
    private fun showDatePicker(isStartDate: Boolean) {
        dashboardViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val calendar = if (isStartDate) startCal else endCal
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)

                if (isStartDate) {
                    // Update start date
                    val endCalendar = Calendar.getInstance()
                    endCalendar.time = endCal.time
                    dashboardViewModel.setCustomDateRange(newCalendar, endCalendar)
                } else {
                    // Update end date
                    val startCalendar = Calendar.getInstance()
                    startCalendar.time = startCal.time
                    dashboardViewModel.setCustomDateRange(startCalendar, newCalendar)
                }

                // Update button text
                updateDateButtonText()
            }, year, month, day).show()
        }
    }

    /**
     * Update date button text based on current range
     */
    private fun updateDateButtonText() {
        dashboardViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            startDateButton.text = dateFormat.format(startCal.time)
            endDateButton.text = dateFormat.format(endCal.time)
        }
    }

    /**
     * Observe LiveData from ViewModel to update UI accordingly
     */
    private fun observeViewModel() {
        // Observe date range changes
        dashboardViewModel.dateRange.observe(this, Observer { (rangeType, startCal, endCal) ->
            // Update UI to reflect date range
            when (rangeType) {
                "Last 7 Days", "Last 14 Days", "Last 30 Days" -> {
                    rangeSpinner.text = rangeType
                    dateRangeLayout.visibility = View.GONE
                }
                "Custom Range" -> {
                    rangeSpinner.text = rangeType
                    dateRangeLayout.visibility = View.VISIBLE
                }
            }

            // Update date buttons
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            startDateButton.text = dateFormat.format(startCal.time)
            endDateButton.text = dateFormat.format(endCal.time)

            // Fetch data if not already loading
            if (!isInitialLoad) {
                dashboardViewModel.fetchDataForCurrentRange(this)
            }
        })

        // Observe bar chart data
        dashboardViewModel.barChartData.observe(this, Observer { (entries, dates) ->
            setupBarChart(entries, dates)
        })

        // Observe pie chart data
        dashboardViewModel.pieChartData.observe(this, Observer { moodCounts ->
            setupPieChart(moodCounts)
        })

        // Observe loading state
        dashboardViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                loadingIndicator.visibility = View.VISIBLE
                chartsContainer.alpha = 0.3f
            } else {
                loadingIndicator.visibility = View.GONE
                chartsContainer.alpha = 1.0f
            }
        })

        // Observe error messages
        dashboardViewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                dashboardViewModel.clearErrorMessage()
            }
        })
    }

    /**
     * Setup bar chart with data
     */
    private fun setupBarChart(entries: List<BarEntry>, dates: List<String>) {
        if (entries.isEmpty() || entries.all { it.y == 0f }) {
            barChart.setNoDataText("No control data recorded in this period")
            barChart.setNoDataTextColor(Color.WHITE)
            barChart.invalidate()
            return
        }

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

        xAxis.textColor = Color.WHITE
        leftAxis.textColor = Color.WHITE

        barDataSet.valueTextColor = Color.WHITE
        barDataSet.valueTextSize = 12f

        xAxis.axisLineColor = Color.WHITE
        leftAxis.axisLineColor = Color.WHITE

        leftAxis.gridColor = Color.parseColor("#50FFFFFF")

        // Adjust label count based on number of dates
        if (dates.size <= 14) {
            xAxis.labelCount = dates.size
        } else {
            xAxis.labelCount = 7
        }

        // Update chart title to reflect the date range
        dashboardViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val startDate = dateFormat.format(startCal.time)
            val endDate = dateFormat.format(endCal.time)
            triggersTextView.text = "In Control ($startDate - $endDate)"
        }
        barChart.animateY(1000)

        barChart.invalidate()
    }

    /**
     * Setup pie chart with data
     */
    private fun setupPieChart(moodCounts: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        moodCounts.forEach { (mood, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), mood))
        }

        // If no data, show a custom message
        if (entries.isEmpty()) {
            pieChart.setNoDataText("No mood data recorded in this period")
            pieChart.setNoDataTextColor(Color.WHITE)
            pieChart.invalidate()
            return
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
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueTextSize = 14f
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD)

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setDrawHoleEnabled(false)
        pieChart.setDrawEntryLabels(false)
        pieChart.animateY(1000)

        // UPDATED LEGEND CONFIGURATION - Changed from vertical right to horizontal bottom
        val legend = pieChart.legend
        legend.textColor = Color.WHITE
        legend.isWordWrapEnabled = true
        legend.formSize = 8f
        legend.textSize = 12f
        legend.xEntrySpace = 6f
        legend.yEntrySpace = 2f
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.setDrawInside(false)
        legend.yOffset = 5f
        legend.xOffset = 0f

        pieChart.invalidate()

        // Update feelings title to include date range
        dashboardViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val startDate = dateFormat.format(startCal.time)
            val endDate = dateFormat.format(endCal.time)
            feelingsTextView.text = "Feelings ($startDate - $endDate)"
        }
    }

    /**
     * Handle navigation item selection
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> { /* Already on dashboard */ }
            R.id.nav_settings -> navigateTo(SettingActivity::class.java)
            R.id.nav_about -> navigateTo(AboutActivity::class.java)
            R.id.nav_home -> {
                DateRangeManager.clearDateRange(this)
                navigateTo(HomeActivity::class.java)
            }
            R.id.nav_membership -> navigateTo(MembershipActivity::class.java)
            R.id.nav_exercises -> navigateTo(ExercisesActivity::class.java)
            R.id.nav_logout -> logoutUser()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Navigate to another activity
     */
    private fun navigateTo(destinationClass: Class<*>) {
        startActivity(Intent(this, destinationClass))
    }

    /**
     * Log out user and navigate to login screen
     */
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}