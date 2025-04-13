package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.app.DatePickerDialog
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
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern implementation of DashboardActivity3 using MVVM architecture pattern
 * with ViewModels, LiveData, and Material Design components.
 * Includes robust error handling and UI state management.
 */
class DashboardActivity3 : AppCompatActivity() {

    private val TAG = "DashboardActivity3"

    // UI Components
    private lateinit var pieChart4: PieChart
    private lateinit var pieChart5: PieChart
    private lateinit var rangeSpinner: MaterialButton
    private lateinit var startDateButton: MaterialButton
    private lateinit var endDateButton: MaterialButton
    private lateinit var applyButton: MaterialButton
    private lateinit var dateRangeLayout: View
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var strategiesTextView: TextView
    private lateinit var actionsTextView: TextView
    private var loadingIndicator: ProgressBar? = null
    private var chartsContainer: View? = null

    // ViewModel using the by viewModels() delegate
    private val dashboard3ViewModel: Dashboard3ViewModel by viewModels()

    // Flag to prevent initial double-loading
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board3)

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
        dashboard3ViewModel.loadSavedDateRange(this)
        isInitialLoad = false
    }

    override fun onResume() {
        super.onResume()

        if (isInitialLoad) return

        dashboard3ViewModel.loadSavedDateRange(this)
    }

    /**
     * Initialize all UI components from layout with robust error handling
     */
    private fun initializeViews() {
        try {
            // Find views - core components
            pieChart4 = findViewById(R.id.pieChart4)
            pieChart5 = findViewById(R.id.pieChart5)
            rangeSpinner = findViewById(R.id.rangeSpinner)
            startDateButton = findViewById(R.id.startDateButton)
            endDateButton = findViewById(R.id.endDateButton)
            applyButton = findViewById(R.id.applyRangeButton)
            dateRangeLayout = findViewById(R.id.dateRangeLayout)
            toolbar = findViewById(R.id.toolbar)
            strategiesTextView = findViewById(R.id.strategiesTextView)
            actionsTextView = findViewById(R.id.actionsTextView)

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

            setupChartDefaults()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            Toast.makeText(this, "Error initializing dashboard. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Setup default settings for charts
     */
    private fun setupChartDefaults() {
        pieChart4.setNoDataText("Loading data...")
        pieChart4.setNoDataTextColor(Color.WHITE)
        pieChart4.description.isEnabled = false

        pieChart5.setNoDataText("Loading data...")
        pieChart5.setNoDataTextColor(Color.WHITE)
        pieChart5.description.isEnabled = false
    }

    /**
     * Setup toolbar with back button
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Dashboard 3"
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
        startDateButton.setOnClickListener {
            showDatePicker(true)
        }

        endDateButton.setOnClickListener {
            showDatePicker(false)
        }

        applyButton.setOnClickListener {
            val (isValid, errorMessage) = dashboard3ViewModel.validateDateRange()
            if (isValid) {
                dashboard3ViewModel.fetchDataForCurrentRange(this)
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

        rangeSpinner.text = dashboard3ViewModel.dateRange.value?.first ?: "Last 7 Days"

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

                        when (selectedRange) {
                            "Last 7 Days" -> dashboard3ViewModel.setDateRange(7)
                            "Last 14 Days" -> dashboard3ViewModel.setDateRange(14)
                            "Last 30 Days" -> dashboard3ViewModel.setDateRange(30)
                        }

                        dashboard3ViewModel.fetchDataForCurrentRange(this)
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
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
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
                    dashboard3ViewModel.setCustomDateRange(newCalendar, endCalendar)
                } else {
                    // Update end date
                    val startCalendar = Calendar.getInstance()
                    startCalendar.time = startCal.time
                    dashboard3ViewModel.setCustomDateRange(startCalendar, newCalendar)
                }

                updateDateButtonText()
            }, year, month, day).show()
        }
    }

    /**
     * Update date button text based on current range
     */
    private fun updateDateButtonText() {
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            startDateButton.text = dateFormat.format(startCal.time)
            endDateButton.text = dateFormat.format(endCal.time)
        }
    }

    /**
     * Observe LiveData from ViewModel to update UI accordingly
     */
    private fun observeViewModel() {
        dashboard3ViewModel.dateRange.observe(this, Observer { (rangeType, startCal, endCal) ->
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
                dashboard3ViewModel.fetchDataForCurrentRange(this)
            }
        })

        // Observe strategies data
        dashboard3ViewModel.strategiesData.observe(this, Observer { strategyCounts ->
            setupPieChart(pieChart4, strategyCounts, "Strategies")
        })

        // Observe actions data
        dashboard3ViewModel.actionsData.observe(this, Observer { actionCounts ->
            setupPieChart(pieChart5, actionCounts, "Actions")
        })

        dashboard3ViewModel.isLoading.observe(this, Observer { isLoading ->
            try {
                if (isLoading) {
                    // Show loading indicator
                    loadingIndicator?.visibility = View.VISIBLE

                    if (chartsContainer != null) {
                        chartsContainer?.alpha = 0.3f
                    } else {
                        // Fallback approach - dim individual components
                        pieChart4.alpha = 0.3f
                        pieChart5.alpha = 0.3f
                        strategiesTextView.alpha = 0.3f
                        actionsTextView.alpha = 0.3f
                    }
                } else {
                    loadingIndicator?.visibility = View.GONE

                    // Restore normal visibility
                    if (chartsContainer != null) {
                        chartsContainer?.alpha = 1.0f
                    } else {
                        pieChart4.alpha = 1.0f
                        pieChart5.alpha = 1.0f
                        strategiesTextView.alpha = 1.0f
                        actionsTextView.alpha = 1.0f
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating loading state: ${e.message}", e)
            }
        })

        // Observe error messages
        dashboard3ViewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                dashboard3ViewModel.clearErrorMessage()
            }
        })
    }

    /**
     * Setup pie chart with data
     */
    private fun setupPieChart(chart: PieChart, dataCounts: Map<String, Int>, label: String) {
        val entries = mutableListOf<PieEntry>()
        dataCounts.forEach { (item, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), item))
        }

        if (entries.isEmpty()) {
            chart.setNoDataText("No $label data recorded in this period")
            chart.setNoDataTextColor(Color.WHITE)
            chart.invalidate()
            return
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
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)

        chart.data = data
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setDrawHoleEnabled(false)
        chart.setDrawEntryLabels(false)
        chart.invalidate()

        // Setup legend
        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textColor = Color.WHITE
        legend.textSize = 16f
        legend.isWordWrapEnabled = true
        legend.maxSizePercent = 0.4f

        // Update title with date range
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val startDate = dateFormat.format(startCal.time)
            val endDate = dateFormat.format(endCal.time)

            // Find and update the appropriate title TextView
            val titleView = when (label) {
                "Strategies" -> strategiesTextView
                else -> actionsTextView
            }
            titleView.text = "$label ($startDate - $endDate)"
        }
    }
}