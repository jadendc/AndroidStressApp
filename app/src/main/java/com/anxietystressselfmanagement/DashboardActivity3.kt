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

class DashboardActivity3 : AppCompatActivity() {

    private val TAG = "DashboardActivity3"

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

    private val dashboard3ViewModel: Dashboard3ViewModel by viewModels()

    private var isInitialLoad = true

    // Prioritized Pastel Colors
    private val pastelColors: List<Int> by lazy {
        listOf(
            // 10 base pastel colors
            "#F4B6C2", "#A6E1D9", "#F6D1C1", "#E3A7D4", "#C8D8A9",
            "#A7C7E7", "#FFFACD", "#C3B1E1", "#FFDAC1", "#B2F0B2"
        ).mapNotNull { hexString ->
            try { Color.parseColor(hexString) } catch (e: Exception) {
                Log.e(TAG, "Invalid pastel color string: '$hexString'", e); null }
        }
    }

    // Extensive list (Fallback colors, ~40) - same as before
    private val extensiveColors: List<Int> by lazy {
        listOf(
            "#FF6F61", "#6B5B95", "#88B04B", "#F7CAC9", "#92A8D1", "#955251", "#B565A7", "#009B77",
            "#DD4124", "#45B8AC", "#EFC050", "#5B5EA6", "#9B1B30", "#D65076", "#4B3832", "#854442",
            "#1E8449", "#3C3C3C", "#F39C12", "#16A085", "#27AE60", "#2980B9", "#8E44AD", "#2C3E50",
            "#E74C3C", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
        ).mapNotNull { hexString ->
            try { Color.parseColor(hexString) } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid extensive color string: '$hexString'", e); null }
        }
    }

    private val prioritizedColors: List<Int> by lazy {
        val combined = mutableListOf<Int>()
        combined.addAll(pastelColors) // Add the 10 pastels first
        // Add extensive colors only if they aren't already in the pastel list
        combined.addAll(extensiveColors.filter { it !in pastelColors })
        if (combined.isEmpty()) { // Fallback if all parsing failed
            Log.e(TAG, "CRITICAL: prioritizedColors list is empty! Using default gray.")
            combined.add(Color.GRAY)
        }
        combined // Return the final combined list
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board3)

        initializeViews()
        setupToolbar()
        setupButtonListeners()
        setupRangeSpinner()
        observeViewModel()

        // Check combined list (optional logging)
        Log.d(TAG, "Prioritized colors loaded. Count: ${prioritizedColors.size}")
        if (prioritizedColors.size <= 10) {
            Log.w(TAG, "Warning: Prioritized colors list only contains pastels. Extensive colors might have failed parsing or were duplicates.")
        }

        dashboard3ViewModel.loadSavedDateRange(this)
        isInitialLoad = false
    }

    override fun onResume() {
        super.onResume()
        if (isInitialLoad) return
        dashboard3ViewModel.loadSavedDateRange(this)
    }

    private fun initializeViews() {
        try {
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
            } catch (e: Exception) { Log.e(TAG, "Error finding loadingIndicator: ${e.message}") }
            try {
                chartsContainer = findViewById(R.id.chartsContainer)
            } catch (e: Exception) { Log.e(TAG, "Error finding chartsContainer: ${e.message}") }

            setupChartDefaults()
        } catch (e: Exception) {
            Log.e(TAG, "FATAL: Error during initializeViews: ${e.message}", e)
            Toast.makeText(this, "Fatal error initializing dashboard layout.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupChartDefaults() {
        pieChart4.setNoDataText("Loading data...")
        pieChart4.setNoDataTextColor(Color.WHITE)
        pieChart4.description.isEnabled = false

        pieChart5.setNoDataText("Loading data...")
        pieChart5.setNoDataTextColor(Color.WHITE)
        pieChart5.description.isEnabled = false
    }

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

    private fun setupButtonListeners() {
        startDateButton.setOnClickListener { showDatePicker(true) }
        endDateButton.setOnClickListener { showDatePicker(false) }
        applyButton.setOnClickListener {
            val (isValid, errorMessage) = dashboard3ViewModel.validateDateRange()
            if (isValid) {
                dashboard3ViewModel.fetchDataForCurrentRange(this)
            } else {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                .setItems(ranges) { _, which ->
                    val selectedRange = ranges[which]
                    rangeSpinner.text = selectedRange
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
                    val currentEndCal = Calendar.getInstance().apply { time = endCal.time }
                    dashboard3ViewModel.setCustomDateRange(newCalendar, currentEndCal)
                } else {
                    val currentStartCal = Calendar.getInstance().apply { time = startCal.time }
                    dashboard3ViewModel.setCustomDateRange(currentStartCal, newCalendar)
                }
                updateDateButtonText()
            }, year, month, day).show()
        }
    }

    private fun updateDateButtonText() {
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            startCal?.time?.let { startDateButton.text = dateFormat.format(it) }
            endCal?.time?.let { endDateButton.text = dateFormat.format(it) }
        }
    }


    private fun observeViewModel() {
        dashboard3ViewModel.dateRange.observe(this, Observer { rangeInfo ->
            rangeInfo ?: return@Observer
            val (rangeType, startCal, endCal) = rangeInfo
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
            updateDateButtonText() // Update buttons whenever range changes
            if (!isInitialLoad) {
                dashboard3ViewModel.fetchDataForCurrentRange(this)
            }
        })

        dashboard3ViewModel.strategiesData.observe(this, Observer { strategyCounts ->
            strategyCounts ?: return@Observer
            setupPieChart(pieChart4, strategyCounts, "Strategies")
        })

        dashboard3ViewModel.actionsData.observe(this, Observer { actionCounts ->
            actionCounts ?: return@Observer
            setupPieChart(pieChart5, actionCounts, "Actions")
        })

        dashboard3ViewModel.isLoading.observe(this, Observer { isLoading ->
            isLoading ?: return@Observer
            try {
                loadingIndicator?.visibility = if (isLoading) View.VISIBLE else View.GONE
                val alphaValue = if (isLoading) 0.3f else 1.0f
                chartsContainer?.alpha = alphaValue
                if (chartsContainer == null) { // Fallback
                    pieChart4.alpha = alphaValue; pieChart5.alpha = alphaValue
                    strategiesTextView.alpha = alphaValue; actionsTextView.alpha = alphaValue
                }
            } catch (e: Exception) { Log.e(TAG, "Error updating loading state UI: ${e.message}", e) }
        })

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
        if (prioritizedColors.isEmpty()) {
            Log.e(TAG, "Cannot setup chart: prioritizedColors list is empty.")
            chart.setNoDataText("Chart color configuration error.")
            chart.setNoDataTextColor(Color.RED)
            chart.data = null
            chart.invalidate()
            return
        }

        val entries = mutableListOf<PieEntry>()
        dataCounts.forEach { (item, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), item))
        }

        if (entries.isEmpty()) {
            chart.setNoDataText("No $label data recorded in this period")
            chart.setNoDataTextColor(Color.WHITE)
            chart.data = null
            chart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = prioritizedColors

        dataSet.setValueFormatter(PercentFormatter(chart))
        if (entries.size <= 10) {
            dataSet.setDrawValues(true)
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.DKGRAY
        } else {
            dataSet.setDrawValues(false)
        }
        dataSet.sliceSpace = 1f

        val data = PieData(dataSet)
        chart.data = data
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setDrawHoleEnabled(false)
        chart.setDrawEntryLabels(false)

        // Legend Configuration
        val legend = chart.legend
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

        chart.invalidate()

        // Update Title
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            startCal?.time?.let { startTime ->
                endCal?.time?.let { endTime ->
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    val startDate = dateFormat.format(startTime)
                    val endDate = dateFormat.format(endTime)
                    val titleView = if (label == "Strategies") strategiesTextView else actionsTextView
                    titleView?.text = "$label ($startDate - $endDate)"
                }
            }
        }
    }
}