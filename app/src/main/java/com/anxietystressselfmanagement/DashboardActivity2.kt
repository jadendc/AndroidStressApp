package com.anxietystressselfmanagement

import TriggerDetail
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

class DashboardActivity2 : AppCompatActivity() {

    private val TAG = "DashboardActivity2"

    // UI Components
    private lateinit var pieChart2: PieChart // Triggers
    private lateinit var pieChart3: PieChart // Signs
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
    // Optional TextViews for details
    // private lateinit var triggerDetailsTextView: TextView
    // private lateinit var signDetailsTextView: TextView // Add this ID to XML if using

    private val dashboard2ViewModel: Dashboard2ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board2)
        Log.d(TAG, "onCreate called")

        initializeViews()
        setupToolbar()
        setupButtonListeners()
        setupRangeSpinner()
        observeViewModel()

        dashboard2ViewModel.initializeDateRange(this) // Load saved/default range and trigger first fetch
        Log.d(TAG, "onCreate finished")
    }

    // onResume remains the same

    private fun initializeViews() {
        Log.d(TAG, "Initializing views...")
        try {
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
            loadingIndicator = findViewById(R.id.loadingIndicator)
            chartsContainer = findViewById(R.id.chartsContainer)
            // Initialize optional details TextViews
            // triggerDetailsTextView = findViewById(R.id.triggerDetailsTextView)
            // signDetailsTextView = findViewById(R.id.signDetailsTextView) // Add ID to XML

            setupChartDefaults() // Setup chart defaults *after* finding views
            Log.d(TAG, "Views initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeViews: ${e.message}", e)
            Toast.makeText(this, "Error initializing dashboard view", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupChartDefaults() {
        Log.d(TAG, "Setting up chart defaults and listeners...")
        // Common Pie Chart Settings Function
        fun configurePieChart(chart: PieChart, noDataText: String) {
            chart.setNoDataText(noDataText)
            chart.setNoDataTextColor(Color.WHITE)
            chart.description.isEnabled = false
            chart.setDrawHoleEnabled(false)
            chart.setUsePercentValues(true)
            chart.setEntryLabelColor(Color.WHITE)
            chart.setEntryLabelTextSize(12f)
            chart.legend.textColor = Color.WHITE
            chart.legend.textSize = 14f
            chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            chart.legend.orientation = Legend.LegendOrientation.VERTICAL
            chart.legend.setDrawInside(false)
            chart.legend.isWordWrapEnabled = true
        }

        // Configure Triggers Chart (pieChart2)
        configurePieChart(pieChart2, "Select a date range")
        pieChart2.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                handleChartSelection(e, true) // true indicates Trigger chart
            }
            override fun onNothingSelected() { clearTriggerDetails() }
        })

        // Configure Signs Chart (pieChart3)
        configurePieChart(pieChart3, "Select a date range")
        // MODIFIED: Add listener to Signs chart
        pieChart3.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                handleChartSelection(e, false) // false indicates Sign chart
            }
            override fun onNothingSelected() { clearSignDetails() }
        })

        Log.d(TAG, "Chart defaults and listeners set.")
    }

    // Central handler for chart selections
    private fun handleChartSelection(e: Entry?, isTriggerChart: Boolean) {
        if (e == null || e !is PieEntry) {
            Log.w(TAG, "Invalid selection event or entry type.")
            if (isTriggerChart) clearTriggerDetails() else clearSignDetails()
            return
        }

        val pieEntry = e
        Log.d(TAG, "Chart value selected: ${pieEntry.label}, Chart: ${if (isTriggerChart) "Triggers" else "Signs"}")

        // Data should be List<String>
        val specificItems = (pieEntry.data as? List<*>)?.filterIsInstance<String>()

        if (specificItems != null) {
            if (isTriggerChart) {
                Log.d(TAG, "Displaying Trigger details for ${pieEntry.label} with ${specificItems.size} items.")
                displayTriggerDetails(pieEntry.label, specificItems)
            } else {
                Log.d(TAG, "Displaying Sign details for ${pieEntry.label} with ${specificItems.size} items.")
                displaySignDetails(pieEntry.label, specificItems) // Call new function for signs
            }
        } else {
            Log.w(TAG, "Selected pie entry data is not List<String> or null.")
            if (isTriggerChart) clearTriggerDetails() else clearSignDetails()
        }
    }

    // setupToolbar, onOptionsItemSelected, setupButtonListeners, setupRangeSpinner,
    // showDatePicker, updateDateButtonText remain the same as previous response...

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish(); return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupButtonListeners() {
        Log.d(TAG, "Setting up button listeners...")
        continueButton.setOnClickListener {
            Log.d(TAG, "Continue button clicked.")
            navigateTo(DashboardActivity3::class.java) // Ensure DashboardActivity3 exists
        }
        startDateButton.setOnClickListener { Log.d(TAG, "Start date button clicked."); showDatePicker(true) }
        endDateButton.setOnClickListener { Log.d(TAG, "End date button clicked."); showDatePicker(false) }
        applyButton.setOnClickListener {
            Log.d(TAG, "Apply custom range button clicked.")
            val (isValid, errorMessage) = dashboard2ViewModel.validateDateRange()
            if (isValid) {
                Log.d(TAG, "Custom range is valid. Fetching data.")
                dashboard2ViewModel.fetchDataForCurrentRange(this)
            } else {
                Log.w(TAG, "Custom range validation failed: $errorMessage")
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
        Log.d(TAG, "Button listeners set.")
    }

    private fun setupRangeSpinner() {
        Log.d(TAG, "Setting up range spinner...")
        val ranges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Custom Range")
        dashboard2ViewModel.dateRange.value?.let { rangeSpinner.text = it.first }
        rangeSpinner.setOnClickListener {
            Log.d(TAG, "Range spinner clicked.")
            val currentSelection = dashboard2ViewModel.dateRange.value?.first ?: ranges[0]
            val checkedItem = ranges.indexOf(currentSelection).coerceAtLeast(0)
            AlertDialog.Builder(this)
                .setTitle("Select Date Range")
                .setSingleChoiceItems(ranges, checkedItem) { dialog, which ->
                    val selectedRange = ranges[which]
                    Log.d(TAG, "Range selected: $selectedRange")
                    rangeSpinner.text = selectedRange
                    dateRangeLayout.visibility = if (selectedRange == "Custom Range") View.VISIBLE else View.GONE
                    if (selectedRange != "Custom Range") {
                        when (selectedRange) {
                            "Last 7 Days" -> dashboard2ViewModel.setDateRange(7)
                            "Last 14 Days" -> dashboard2ViewModel.setDateRange(14)
                            "Last 30 Days" -> dashboard2ViewModel.setDateRange(30)
                        }
                    } else {
                        updateDateButtonText()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
        Log.d(TAG, "Range spinner setup complete.")
    }

    private fun showDatePicker(isStartDate: Boolean) {
        dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val calendarToShow = if (isStartDate) startCal else endCal
            DatePickerDialog(this, { _, year, month, day ->
                val newCalendar = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
                val currentStart = dashboard2ViewModel.dateRange.value!!.second
                val currentEnd = dashboard2ViewModel.dateRange.value!!.third
                if (isStartDate) {
                    Log.d(TAG, "Setting custom START date to: ${newCalendar.time}")
                    dashboard2ViewModel.setCustomDateRange(newCalendar, currentEnd)
                } else {
                    Log.d(TAG, "Setting custom END date to: ${newCalendar.time}")
                    dashboard2ViewModel.setCustomDateRange(currentStart, newCalendar)
                }
            }, calendarToShow.get(Calendar.YEAR), calendarToShow.get(Calendar.MONTH), calendarToShow.get(Calendar.DAY_OF_MONTH)).show()
        } ?: run { Log.e(TAG, "Cannot show DatePicker, dateRange in ViewModel is null."); Toast.makeText(this, "Date range not initialized.", Toast.LENGTH_SHORT).show() }
    }

    private fun updateDateButtonText() {
        dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            startDateButton.text = dateFormat.format(startCal.time)
            endDateButton.text = dateFormat.format(endCal.time)
        }
    }


    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers...")

        dashboard2ViewModel.dateRange.observe(this, Observer { (rangeType, startCal, endCal) ->
            Log.i(TAG, "Observer: Date Range Updated: $rangeType from ${startCal.time} to ${endCal.time}")
            rangeSpinner.text = rangeType
            dateRangeLayout.visibility = if (rangeType == "Custom Range") View.VISIBLE else View.GONE
            updateDateButtonText()
            Log.d(TAG, "Observer: Triggering data fetch due to date range change.")
            dashboard2ViewModel.fetchDataForCurrentRange(this)
            clearTriggerDetails()
            clearSignDetails() // Clear sign details too
        })

        dashboard2ViewModel.triggersData.observe(this, Observer { triggerDetailsList ->
            Log.i(TAG, "Observer: Triggers data received: ${triggerDetailsList.size} categories")
            // Use a generic setup function now
            setupDetailedPieChart(pieChart2, triggerDetailsList, "Triggers")
        })

        // MODIFIED: Observe List<SignDetail>
        dashboard2ViewModel.signsData.observe(this, Observer { signDetailsList ->
            Log.i(TAG, "Observer: Signs data received: ${signDetailsList.size} categories")
            // Use the same generic setup function for signs
            setupDetailedPieChart(pieChart3, signDetailsList, "Signs")
        })

        dashboard2ViewModel.isLoading.observe(this, Observer { isLoading ->
            Log.i(TAG, "Observer: Loading state changed: $isLoading")
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            chartsContainer.alpha = if (isLoading) 0.3f else 1.0f
        })

        dashboard2ViewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                Log.e(TAG, "Observer: Error received: $message")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                dashboard2ViewModel.clearErrorMessage()
            }
        })
        Log.d(TAG, "ViewModel observers set.")
    }

    /** Generic setup function for Pie Charts with detailed data. */
    // MODIFIED: Generic setup function
    private fun <T> setupDetailedPieChart(chart: PieChart, dataList: List<T>, label: String) {
        Log.d(TAG, "Setting up detailed pie chart for '$label' with ${dataList.size} entries.")
        val entries = mutableListOf<PieEntry>()

        dataList.forEach { item ->
            // Use reflection or interfaces/abstract classes for a truly generic approach
            // For now, use 'when' based on the label to access correct fields
            var count = 0
            var category = ""
            var specificItems: List<String>? = null

            when (item) {
                is TriggerDetail -> {
                    count = item.count
                    category = item.category
                    specificItems = item.specificTriggers
                }
                is SignDetail -> {
                    count = item.count
                    category = item.category
                    specificItems = item.specificSigns
                }
                else -> {
                    Log.w(TAG, "Unknown data type in setupDetailedPieChart: ${item!!::class.simpleName}")
                    return@forEach // Skip this item
                }
            }

            if (count > 0) {
                // Store the list of specific items in the PieEntry's data field
                entries.add(PieEntry(count.toFloat(), category, specificItems))
            }
        }

        // Update the correct title TextView based on the label
        val titleView = if (label == "Triggers") triggersTextView else signsTextView
        updateChartTitle(titleView, label)

        if (entries.isEmpty()) {
            Log.d(TAG, "No entries to display for '$label'.")
            chart.data = null
            chart.setNoDataText("No $label data recorded\nin this period")
            chart.invalidate()
            return
        }

        // Define color palettes (can be different for each chart)
        val colors = if (label == "Triggers") {
            listOf( "#B39DDB", "#FFE082", "#FFF59D", "#FFAB91", "#A5D6A7", "#81D4FA", "#F48FB1").map { Color.parseColor(it) }
        } else { // Signs colors
            listOf( "#F48FB1", "#81D4FA", "#A5D6A7", "#FFF59D", "#FFAB91", "#FFE082", "#B39DDB").map { Color.parseColor(it) }
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = entries.indices.map { colors[it % colors.size] }
        dataSet.valueFormatter = PercentFormatter(chart)
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        dataSet.sliceSpace = 2f
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.4f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // Position values outside

        val pieData = PieData(dataSet)
        chart.data = pieData
        chart.setDrawEntryLabels(false) // Labels on slices (redundant with legend)
        chart.highlightValues(null) // Clear previous selections
        chart.animateY(1000)
        chart.invalidate()
        Log.d(TAG, "Detailed pie chart ('$label') updated and invalidated.")
    }

    /** Helper to update chart titles consistently */
    private fun updateChartTitle(textView: TextView, label: String) {
        dashboard2ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val startDate = dateFormat.format(startCal.time)
            val endDate = dateFormat.format(endCal.time)
            val dateStr = if (startDate == endDate) startDate else "$startDate - $endDate"
            textView.text = "$label ($dateStr)"
        } ?: run { textView.text = label }
    }

    /** Displays the breakdown of specific triggers. */
    private fun displayTriggerDetails(category: String, specificTriggers: List<String>) {
        val counts = specificTriggers.groupingBy { it }.eachCount()
        val detailsText = buildString {
            append("Trigger Details for '$category':\n")
            if (counts.isNotEmpty()) {
                counts.entries.sortedByDescending { it.value }.forEach { (trigger, count) ->
                    append("  • ${trigger.ifEmpty { "Unspecified" }}: $count\n")
                }
            } else { append("  (No specific details)") }
        }.trimEnd()

        // --- Show in Toast ---
        Toast.makeText(this, detailsText, Toast.LENGTH_LONG).show()
        Log.d(TAG,"Displayed Trigger details via Toast.")
        // --- Or Update TextView ---
        // try { triggerDetailsTextView.text = detailsText; triggerDetailsTextView.visibility = View.VISIBLE ... } catch ...
    }

    /** Clears the trigger details display. */
    private fun clearTriggerDetails() {
        Log.d(TAG,"Clearing trigger details.")
        // if (::triggerDetailsTextView.isInitialized) { triggerDetailsTextView.text = ""; triggerDetailsTextView.visibility = View.GONE }
    }

    // --- NEW Functions for Sign Details ---

    /** Displays the breakdown of specific signs/symptoms. */
    private fun displaySignDetails(category: String, specificSigns: List<String>) {
        val counts = specificSigns.groupingBy { it }.eachCount()
        val detailsText = buildString {
            append("Sign Details for '$category':\n") // Clarify it's Sign details
            if (counts.isNotEmpty()) {
                counts.entries.sortedByDescending { it.value }.forEach { (sign, count) ->
                    append("  • ${sign.ifEmpty { "Unspecified" }}: $count\n")
                }
            } else { append("  (No specific details)") }
        }.trimEnd()

        // --- Show in Toast ---
        Toast.makeText(this, detailsText, Toast.LENGTH_LONG).show()
        Log.d(TAG,"Displayed Sign details via Toast.")
        // --- Or Update TextView ---
        // try { signDetailsTextView.text = detailsText; signDetailsTextView.visibility = View.VISIBLE ... } catch ...
    }

    /** Clears the sign details display. */
    private fun clearSignDetails() {
        Log.d(TAG,"Clearing sign details.")
        // if (::signDetailsTextView.isInitialized) { signDetailsTextView.text = ""; signDetailsTextView.visibility = View.GONE }
    }
    // --- End NEW Sign Functions ---


    /** Navigate to another activity */
    private fun navigateTo(destinationClass: Class<*>) {
        startActivity(Intent(this, destinationClass))
    }
}