package com.anxietystressselfmanagement

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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
 * Modern implementation of DashboardActivity2 using MVVM architecture pattern
 * with ViewModels, LiveData, and Material Design components.
 */
class DashboardActivity2 : AppCompatActivity() {

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

        // Skip refresh during initial loading (already handled in onCreate)
        if (isInitialLoad) return

        // Check for date range changes when returning to this activity
        dashboard2ViewModel.loadSavedDateRange(this)
    }

    /**
     * Initialize all UI components from layout
     */
    private fun initializeViews() {
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

        // Make sure the initial text is visible
        rangeSpinner.text = dashboard2ViewModel.dateRange.value?.first ?: "Last 7 Days"

        // Add dropdown icon if missing
        if (rangeSpinner.icon == null) {
            rangeSpinner.setIconResource(R.drawable.ic_calendar)
            rangeSpinner.iconGravity = MaterialButton.ICON_GRAVITY_START
        }

        // Use AlertDialog instead of PopupMenu for better styling control
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
        // Observe date range changes
        dashboard2ViewModel.dateRange.observe(this, Observer { (rangeType, startCal, endCal) ->
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

        // Observe loading state
        dashboard2ViewModel.isLoading.observe(this, Observer { isLoading ->
            // You could show a progress indicator here
        })

        // Observe error messages
        dashboard2ViewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                dashboard2ViewModel.clearErrorMessage()
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

        // If no data, add an empty entry
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
        }

        val pastelColors = listOf(
            Color.parseColor("#c2c2f0"), // Pastel Purple
            Color.parseColor("#ffeb99"), // Pastel Yellow
            Color.parseColor("#fdfd96"), // Lighter Yellow
            Color.parseColor("#ffb3b3"), // Pastel Red
            Color.parseColor("#f7a9a9")  // Darker Pastel Red
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

        // Setup legend
        val legend = chart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.textColor = Color.BLACK
        legend.textSize = 16f

        // Update title with date range
        dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val startDate = dateFormat.format(startCal.time)
            val endDate = dateFormat.format(endCal.time)

            // Find and update the appropriate title TextView
            val titleView = when (label) {
                "Triggers" -> triggersTextView
                else -> signsTextView
            }
            titleView.text = "$label ($startDate - $endDate)"
        }
    }

    /**
     * Navigate to another activity
     */
    private fun navigateTo(destinationClass: Class<*>) {
        startActivity(Intent(this, destinationClass))
    }
}