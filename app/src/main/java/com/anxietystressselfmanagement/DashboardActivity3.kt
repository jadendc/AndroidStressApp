package com.anxietystressselfmanagement

// Import Intent
import android.content.Intent
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
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

    // --- UI Components ---
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
    // Add the new button variable
    private lateinit var viewDetailsButton: MaterialButton
    // --- End UI Components ---

    private val dashboard3ViewModel: Dashboard3ViewModel by viewModels()

    private var isInitialLoad = true

    // --- Color Definitions ---
    private val pastelColors: List<Int> by lazy {
        listOf(
            "#F4B6C2", "#A6E1D9", "#F6D1C1", "#E3A7D4", "#C8D8A9",
            "#A7C7E7", "#FFFACD", "#C3B1E1", "#FFDAC1", "#B2F0B2"
        ).mapNotNull { hexString ->
            try { Color.parseColor(hexString) } catch (e: Exception) {
                Log.e(TAG, "Invalid pastel color string: '$hexString'", e); null }
        }
    }

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
        combined.addAll(pastelColors)
        combined.addAll(extensiveColors.filter { it !in pastelColors })
        if (combined.isEmpty()) {
            Log.e(TAG, "CRITICAL: prioritizedColors list is empty! Using default gray.")
            combined.add(Color.GRAY)
        }
        combined
    }
    // --- End Color Definitions ---


    // --- Activity Lifecycle Methods ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board3)

        // Initialization order is important
        initializeViews() // Find all views first
        setupToolbar()
        setupButtonListeners() // Setup listeners after views are found
        setupRangeSpinner()
        observeViewModel()

        Log.d(TAG, "Prioritized colors loaded. Count: ${prioritizedColors.size}")
        if (prioritizedColors.isEmpty()) {
            Log.e(TAG, "CRITICAL: prioritizedColors list is empty after parsing! Check hex codes.")
            Toast.makeText(this, "Error loading chart colors", Toast.LENGTH_LONG).show()
        }

        dashboard3ViewModel.loadSavedDateRange(this)
        isInitialLoad = false
    }

    override fun onResume() {
        super.onResume()
        if (isInitialLoad) return
        // Reloading saved range might trigger data fetch via observer if range didn't change
        // Or call fetch explicitly if needed: dashboard3ViewModel.fetchDataForCurrentRange(this)
        dashboard3ViewModel.loadSavedDateRange(this)
    }
    // --- End Activity Lifecycle Methods ---


    // --- View Initialization and Setup ---
    private fun initializeViews() {
        try {
            // Charts
            pieChart4 = findViewById(R.id.pieChart4)
            pieChart5 = findViewById(R.id.pieChart5)
            // Date Range Components
            rangeSpinner = findViewById(R.id.rangeSpinner)
            startDateButton = findViewById(R.id.startDateButton)
            endDateButton = findViewById(R.id.endDateButton)
            applyButton = findViewById(R.id.applyRangeButton)
            dateRangeLayout = findViewById(R.id.dateRangeLayout)
            // Toolbar and Titles
            toolbar = findViewById(R.id.toolbar)
            strategiesTextView = findViewById(R.id.strategiesTextView)
            actionsTextView = findViewById(R.id.actionsTextView)
            // New Details Button
            viewDetailsButton = findViewById(R.id.viewDetailsButton) // Initialize the new button

            // Optional Components (Loading/Container)
            try { loadingIndicator = findViewById(R.id.loadingIndicator) }
            catch (e: Exception) { Log.e(TAG, "Error finding loadingIndicator: ${e.message}") }
            try { chartsContainer = findViewById(R.id.chartsContainer) }
            catch (e: Exception) { Log.e(TAG, "Error finding chartsContainer: ${e.message}") }

            setupChartDefaults()

        } catch (e: Exception) {
            Log.e(TAG, "FATAL: Error during initializeViews: ${e.message}", e)
            Toast.makeText(this, "Fatal error initializing dashboard layout.", Toast.LENGTH_LONG).show()
            // Consider finishing the activity if core components fail
            // finish()
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
        // Handle presses on the action bar items
        if (item.itemId == android.R.id.home) {
            finish() // Navigate back to previous activity
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupButtonListeners() {
        // Date Picker Buttons
        startDateButton.setOnClickListener { showDatePicker(true) }
        endDateButton.setOnClickListener { showDatePicker(false) }

        // Apply Custom Range Button
        applyButton.setOnClickListener {
            val (isValid, errorMessage) = dashboard3ViewModel.validateDateRange()
            if (isValid) {
                dashboard3ViewModel.fetchDataForCurrentRange(this)
            } else {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // View Details Button
        viewDetailsButton.setOnClickListener {
            dashboard3ViewModel.dateRange.value?.let { (_, startCal, _) ->
                if (startCal != null) {
                    // Convert startCal to formatted date string
                    val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startCal.time)

                    // Launch StrategiesActions activity
                    val intent = StrategiesActions.newIntent(this, selectedDate)
                    startActivity(intent)
                } else {
                    Log.e(TAG, "Start calendar is null.")
                    Toast.makeText(this, "Cannot determine selected date.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Log.e(TAG, "dateRange LiveData is null.")
                Toast.makeText(this, "Cannot determine date range.", Toast.LENGTH_SHORT).show()
            }
        }
        // --- End Details Button Listener ---
    }

    private fun setupRangeSpinner() {
        val ranges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Custom Range")
        // Set initial text based on ViewModel's current state if available
        dashboard3ViewModel.dateRange.value?.let { rangeSpinner.text = it.first }

        if (rangeSpinner.icon == null) {
            rangeSpinner.setIconResource(R.drawable.ic_calendar)
            rangeSpinner.iconGravity = MaterialButton.ICON_GRAVITY_START
        }

        rangeSpinner.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select Date Range")
                .setItems(ranges) { _, which ->
                    val selectedRange = ranges[which]
                    rangeSpinner.text = selectedRange // Update button text immediately

                    if (selectedRange == "Custom Range") {
                        dateRangeLayout.visibility = View.VISIBLE
                        // Dates should be set via date picker and apply button
                    } else {
                        dateRangeLayout.visibility = View.GONE
                        // Set range in ViewModel, which will trigger observer to fetch data
                        when (selectedRange) {
                            "Last 7 Days" -> dashboard3ViewModel.setDateRange(7)
                            "Last 14 Days" -> dashboard3ViewModel.setDateRange(14)
                            "Last 30 Days" -> dashboard3ViewModel.setDateRange(30)
                        }
                        // Data fetch now happens via the dateRange observer
                    }
                }
                .create()
                .show()
        }
    }
    // --- End View Initialization and Setup ---


    // --- Date Handling ---
    private fun showDatePicker(isStartDate: Boolean) {
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            // Use non-null start/end calendar for picker default, fallback if needed
            val calendar = (if (isStartDate) startCal else endCal) ?: Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)

                // Get current non-null calendars for setting the range
                val currentStartCal = Calendar.getInstance().apply { time = startCal?.time ?: Date() }
                val currentEndCal = Calendar.getInstance().apply { time = endCal?.time ?: Date() }

                if (isStartDate) {
                    dashboard3ViewModel.setCustomDateRange(newCalendar, currentEndCal)
                } else {
                    dashboard3ViewModel.setCustomDateRange(currentStartCal, newCalendar)
                }
                // updateDateButtonText() // Observer will handle button text updates
            }, year, month, day).show()
        }
    }

    private fun updateDateButtonText() {
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            // Check for null before formatting
            startCal?.time?.let { startDateButton.text = dateFormat.format(it) } ?: Log.w(TAG, "Start Calendar is null in updateDateButtonText")
            endCal?.time?.let { endDateButton.text = dateFormat.format(it) } ?: Log.w(TAG, "End Calendar is null in updateDateButtonText")
        }
    }
    // --- End Date Handling ---


    // --- ViewModel Observation ---
    private fun observeViewModel() {
        dashboard3ViewModel.dateRange.observe(this, Observer { rangeInfo ->
            rangeInfo ?: return@Observer
            val (rangeType, _, _) = rangeInfo // Only need type here directly
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
            updateDateButtonText() // Update button text whenever range changes
            if (!isInitialLoad) {
                // Trigger data fetch only after initial load is complete
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
    // --- End ViewModel Observation ---


    // --- Chart Setup ---
    private fun setupPieChart(chart: PieChart, dataCounts: Map<String, Int>, label: String) {
        if (prioritizedColors.isEmpty()) {
            Log.e(TAG, "Cannot setup chart: prioritizedColors list is empty.")
            chart.setNoDataText("Chart color config error.")
            chart.setNoDataTextColor(Color.RED)
            chart.data = null; chart.invalidate(); return
        }

        val entries = mutableListOf<PieEntry>()
        dataCounts.forEach { (item, count) ->
            if (count > 0) entries.add(PieEntry(count.toFloat(), item))
        }

        if (entries.isEmpty()) {
            chart.setNoDataText("No $label data recorded") // Shorter text
            chart.setNoDataTextColor(Color.WHITE)
            chart.data = null; chart.invalidate(); return
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = prioritizedColors
        dataSet.setValueFormatter(PercentFormatter(chart))

        // Conditional Value Drawing
        if (entries.size <= 10) {
            dataSet.setDrawValues(true)
            dataSet.valueTextSize = 14f
            dataSet.valueTextColor = Color.DKGRAY
            dataSet.setValueTypeface(Typeface.DEFAULT_BOLD)
        } else {
            dataSet.setDrawValues(false)
        }
        dataSet.sliceSpace = 1f

        val data = PieData(dataSet)
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.DKGRAY)

        chart.data = data
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setDrawHoleEnabled(false)
        chart.setDrawEntryLabels(false) // Keep labels off slices

        // Legend Configuration (Horizontal Bottom for both)
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

        // Add animation
        chart.animateY(1000)

        chart.invalidate() // Refresh chart drawing

        // Update Title
        dashboard3ViewModel.dateRange.value?.let { (_, startCal, endCal) ->
            startCal?.time?.let { startTime ->
                endCal?.time?.let { endTime ->
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault()) // Short format for title
                    val startDate = dateFormat.format(startTime)
                    val endDate = dateFormat.format(endTime)
                    val titleView = if (label == "Strategies") strategiesTextView else actionsTextView
                    titleView?.text = "$label ($startDate - $endDate)"
                }
            }
        }
    }
}