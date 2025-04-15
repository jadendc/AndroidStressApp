package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern implementation of DashboardActivity2 using MVVM architecture pattern
 * with ViewModels, LiveData, and Material Design components.
 * Includes interactive pie charts that open the DetailActivity when clicked.
 */
class DashboardActivity2 : AppCompatActivity() {

    private val TAG = "DashboardActivity2"

    // UI Components
    private lateinit var pieChart2: PieChart
    private lateinit var pieChart3: PieChart
    private lateinit var continueButton: MaterialButton
    private lateinit var rangeSpinner: MaterialButton
    private lateinit var startDateButton: MaterialButton
    private lateinit var endDateButton: MaterialButton
    private lateinit var applyButton: MaterialButton
    private lateinit var dateRangeLayout: View
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var triggersTextView: TextView
    private lateinit var signsTextView: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var chartsContainer: View

    // ViewModel using the by viewModels() delegate
    private val dashboard2ViewModel: Dashboard2ViewModel by viewModels()

    // Flag to prevent initial double-loading
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board2)

        // Initialize UI components
        initializeViews()

        // Setup toolbar with back button
        setupToolbar()

        // Setup button click listeners
        setupButtonListeners()

        // Setup range selector
        setupRangeSpinner()

        // Observe LiveData from ViewModel
        observeViewModel()

        // Load saved date range and initial data
        dashboard2ViewModel.loadSavedDateRange(this)
        isInitialLoad = false
    }

    override fun onResume() {
        super.onResume()

        if (isInitialLoad) return

        // Check for date range changes when returning to this activity
        dashboard2ViewModel.loadSavedDateRange(this)
    }

    /**
     * Initialize all UI components from layout
     */
    private fun initializeViews() {
        try {
            // Find views
            pieChart2 = findViewById(R.id.pieChart2)
            pieChart3 = findViewById(R.id.pieChart3)
            continueButton = findViewById(R.id.continueDashboardButton2)
            rangeSpinner = findViewById(R.id.rangeSpinner)
            startDateButton = findViewById(R.id.startDateButton)
            endDateButton = findViewById(R.id.endDateButton)
            applyButton = findViewById(R.id.applyRangeButton)
            dateRangeLayout = findViewById(R.id.dateRangeLayout)
            toolbar = findViewById(R.id.toolbar)
            triggersTextView = findViewById(R.id.triggersTextView)
            signsTextView = findViewById(R.id.signsTextView)

            try {
                loadingIndicator = findViewById(R.id.loadingIndicator)
                Log.d(TAG, "loadingIndicator found: ${loadingIndicator != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Error finding loadingIndicator: ${e.message}")
            }

            try {
                chartsContainer = findViewById(R.id.chartsContainer)
                Log.d(TAG, "chartsContainer found: ${chartsContainer != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Error finding chartsContainer: ${e.message}")
            }

            // Setup chart defaults
            setupChartDefaults()
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeViews: ${e.message}", e)
        }
    }

    /**
     * Setup default settings for charts
     */
    private fun setupChartDefaults() {
        Log.d(TAG, "Setting up chart defaults")
        listOf(pieChart2, pieChart3).forEach { chart ->
            chart.setNoDataText("Select a date range") // Changed from "Loading data..."
            chart.setNoDataTextColor(Color.WHITE)
            chart.description.isEnabled = false
            chart.setDrawHoleEnabled(false)
            chart.setDrawEntryLabels(false)
            chart.legend.textColor = Color.WHITE
            chart.legend.textSize = 14f // Reverted if needed, or keep 16f from D3 style
            chart.setUsePercentValues(true)
            chart.isRotationEnabled = true
        }
    }

    /**
     * Setup toolbar with back button
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Dashboard 2"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Setup button click listeners
     */
    private fun setupButtonListeners() {
        continueButton.setOnClickListener {
            navigateTo(DashboardActivity3::class.java)
        }

        startDateButton.setOnClickListener {
            showDatePicker(true)
        }

        endDateButton.setOnClickListener {
            showDatePicker(false)
        }

        applyButton.setOnClickListener {
            val (isValid, errorMessage) = dashboard2ViewModel.validateDateRange()
            if (isValid) {
                dashboard2ViewModel.fetchDataForCurrentRange(this)
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

        rangeSpinner.text = dashboard2ViewModel.dateRange.value?.first ?: "Last 7 Days"

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

                    if (selectedRange == "Custom Range") {
                        dateRangeLayout.visibility = View.VISIBLE
                    } else {
                        dateRangeLayout.visibility = View.GONE

                        // Reset date range based on selection and fetch data
                        when (selectedRange) {
                            "Last 7 Days" -> dashboard2ViewModel.setDateRange(7)
                            "Last 14 Days" -> dashboard2ViewModel.setDateRange(14)
                            "Last 30 Days" -> dashboard2ViewModel.setDateRange(30)
                        }

                        dashboard2ViewModel.fetchDataForCurrentRange(this)
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
        dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
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
                    dashboard2ViewModel.setCustomDateRange(newCalendar, endCalendar)
                } else {
                    // Update end date
                    val startCalendar = Calendar.getInstance()
                    startCalendar.time = startCal.time
                    dashboard2ViewModel.setCustomDateRange(startCalendar, newCalendar)
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
        dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            startDateButton.text = dateFormat.format(startCal.time)
            endDateButton.text = dateFormat.format(endCal.time)
        }
    }

    /**
     * Observe LiveData from ViewModel to update UI accordingly
     */
    private fun observeViewModel() {
        dashboard2ViewModel.dateRange.observe(this, Observer { (rangeType, startCal, endCal) ->
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
                dashboard2ViewModel.fetchDataForCurrentRange(this)
            }
        })

        // Observe triggers data
        dashboard2ViewModel.triggersData.observe(this, Observer { triggerCounts ->
            setupPieChart(pieChart2, triggerCounts, "Triggers")
        })

        // Observe signs data
        dashboard2ViewModel.signsData.observe(this, Observer { signCounts ->
            setupPieChart(pieChart3, signCounts, "Signs")
        })

        dashboard2ViewModel.isLoading.observe(this, Observer { isLoading ->
            try {
                if (isLoading) {
                    // Show loading indicator
                    loadingIndicator?.visibility = View.VISIBLE

                    if (chartsContainer != null) {
                        chartsContainer.alpha = 0.3f
                    }
                    else {
                        pieChart2.alpha = 0.3f
                        pieChart3.alpha = 0.3f
                        triggersTextView.alpha = 0.3f
                        signsTextView.alpha = 0.3f
                    }
                } else {
                    // Hide loading indicator
                    loadingIndicator?.visibility = View.GONE

                    if (chartsContainer != null) {
                        chartsContainer.alpha = 1.0f
                    }
                    else {
                        pieChart2.alpha = 1.0f
                        pieChart3.alpha = 1.0f
                        triggersTextView.alpha = 1.0f
                        signsTextView.alpha = 1.0f
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating loading state: ${e.message}", e)
            }
        })

        // Observe error messages
        dashboard2ViewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                dashboard2ViewModel.clearErrorMessage()
            }
        })
    }

    private fun updateChartTitle(label: String, startCal: Calendar?, endCal: Calendar?) {
        val titleView = when (label) {
            "Triggers" -> triggersTextView
            "Signs" -> signsTextView
            else -> {
                Log.w(TAG, "Unknown label '$label' for updating chart title.")
                null
            }
        }

        if (titleView == null) return

        var titleText = label
        if (startCal != null && endCal != null) {
            try {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val startStr = dateFormat.format(startCal.time)
                val endStr = dateFormat.format(endCal.time)

                if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                    startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)) {
                    titleText = "$label ($startStr)"
                } else {
                    titleText = "$label ($startStr - $endStr)"
                }
                Log.d(TAG, "Updating chart title for '$label' to '$titleText'")
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting date range for title '$label': ${e.message}")
                // Keep default title if formatting fails
            }
        } else {
            Log.d(TAG, "Updating chart title for '$label' to default '$label' (no date range)")
        }
        titleView.text = titleText
    }

    private fun setupPieChart(chart: PieChart, dataCounts: Map<String, Int>, label: String) {
        Log.d(TAG, "Setting up chart: $label with data size: ${dataCounts.size}")
        val entries = mutableListOf<PieEntry>()
        var totalCount = 0
        dataCounts.forEach { (item, count) ->
            if (count > 0 && item.isNotBlank()) {
                entries.add(PieEntry(count.toFloat(), item))
                totalCount += count
            } else {
                Log.w(TAG, "Invalid entry skipped for chart '$label': Item='$item', Count=$count")
            }
        }
        Log.d(TAG, "Valid entries for chart '$label': ${entries.size}, Total items: $totalCount")

        if (entries.isEmpty()) {
            Log.d(TAG, "No valid entries for chart: $label. Displaying 'No data'.")
            chart.setNoDataText("No $label data recorded in this period")
            chart.setNoDataTextColor(Color.WHITE)
            chart.data = null
            chart.setOnChartValueSelectedListener(null) // Ensure listener is null if no data
            chart.invalidate()
            updateChartTitle(label, dashboard2ViewModel.dateRange.value?.second, dashboard2ViewModel.dateRange.value?.third)
            return
        }

        // Using colors from DashboardActivity3 as per previous request
        val pastelColors = listOf(
            Color.parseColor("#F4B6C2"),
            Color.parseColor("#A6E1D9"),
            Color.parseColor("#F6D1C1"),
            Color.parseColor("#E3A7D4"),
            Color.parseColor("#C8D8A9")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = entries.indices.map { pastelColors[it % pastelColors.size] }
        dataSet.setValueFormatter(PercentFormatter(chart))
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueTextSize = 16f // Match DashboardActivity3
        // Added selection shift from your last version
        dataSet.selectionShift = 10f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(chart))
        data.setValueTextSize(16f)
        data.setValueTextColor(Color.DKGRAY)

        chart.data = data
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setDrawHoleEnabled(false)
        chart.setDrawEntryLabels(false)
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = true // Keep highlight on tap enabled
        chart.highlightValues(null)

        // Added offset to give legend space on the right
        chart.setExtraOffsets(5f, 5f, 30f, 5f)

        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textColor = Color.WHITE
        legend.textSize = 16f // Match DashboardActivity3
        legend.isWordWrapEnabled = true // Match DashboardActivity3
        legend.maxSizePercent = 0.4f // Match DashboardActivity3
        legend.form = Legend.LegendForm.SQUARE
        legend.formSize = 10f
        legend.xEntrySpace = 10f
        legend.yEntrySpace = 5f

        // *** Add listener logic back here ***
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                // Check if the entry is a PieEntry (it should be)
                if (e is PieEntry) {
                    val selectedCategory = e.label
                    if (selectedCategory.isNullOrBlank()) {
                        Log.w(TAG, "Selected pie slice has null or blank label for chart '$label'.")
                        return
                    }

                    // Determine type based on the 'label' parameter passed to setupPieChart
                    val chartType = if (label == "Triggers") "Trigger" else "Sign"

                    Log.d(TAG, "Selected: $selectedCategory from chart: $chartType")

                    dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
                        val intent = Intent(this@DashboardActivity2, DetailActivity::class.java).apply {
                            putExtra(DetailActivity.EXTRA_CATEGORY, selectedCategory)
                            putExtra(DetailActivity.EXTRA_TYPE, chartType)
                            putExtra(DetailActivity.EXTRA_START_DATE, startCal.timeInMillis)
                            putExtra(DetailActivity.EXTRA_END_DATE, endCal.timeInMillis)
                        }
                        startActivity(intent)
                    } ?: run {
                        Log.e(TAG, "Cannot navigate: Date range is null in ViewModel.")
                        Toast.makeText(this@DashboardActivity2, "Error: Date range not available.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w(TAG, "Selected entry is not a PieEntry: ${e?.javaClass?.simpleName}")
                }
            }

            override fun onNothingSelected() {
                Log.d(TAG, "Chart '$label' value selection cleared.")
                // Optional: Deselect visually if needed, e.g., chart.highlightValues(null)
            }
        })

         chart.animateY(1000)

        chart.invalidate() // Refresh chart with new settings
    }

    /**
     * Navigate to another activity
     */
    private fun navigateTo(destinationClass: Class<*>) {
        startActivity(Intent(this, destinationClass))
    }
}