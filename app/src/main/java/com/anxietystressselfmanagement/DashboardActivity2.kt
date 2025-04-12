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

class DashboardActivity2 : AppCompatActivity() {
    private lateinit var pieChart2: PieChart
    private lateinit var pieChart3: PieChart
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var backButton: ImageView
    private lateinit var continueButton: Button
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
        setContentView(R.layout.activity_dash_board2)

        // Initialize views
        pieChart2 = findViewById(R.id.pieChart2)
        pieChart3 = findViewById(R.id.pieChart3)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        backButton = findViewById(R.id.dbackButton)
        continueButton = findViewById(R.id.continueDashboardButton2)
        rangeSpinner = findViewById(R.id.rangeSpinner)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        applyButton = findViewById(R.id.applyRangeButton)
        dateRangeLayout = findViewById(R.id.dateRangeLayout)

//        // Setup navigation drawer
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
//        val navigationView: NavigationView = findViewById(R.id.nav_view)
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//        val toggle = ActionBarDrawerToggle(
//            this,
//            drawerLayout,
//            toolbar,
//            R.string.navigation_drawer_open,
//            R.string.navigation_drawer_close
//        )
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//        toggle.drawerArrowDrawable.color = getColor(R.color.white)
//
//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_dashboard -> drawerLayout.closeDrawers()
//                R.id.nav_settings -> startActivity(Intent(this, SettingActivity::class.java))
//                R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
//                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
//                R.id.nav_membership -> startActivity(Intent(this, MembershipActivity::class.java))
//                R.id.nav_exercises -> startActivity(Intent(this, ExercisesActivity::class.java))
//                R.id.nav_logout -> {
//                    FirebaseAuth.getInstance().signOut()
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
//            }
//            drawerLayout.closeDrawers()
//            true
//        }

        // Date range setup
        setupRangeSpinner()
        setDateRange(7) // Default to 7 days
        updateDateButtonText()
        setupDateButtons()

        backButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        continueButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity3::class.java))
        }

        fetchDataForCurrentRange()
    }

    // -- DATE RANGE CODE (Same as DashboardActivity) --
    private fun setupRangeSpinner() {
        val ranges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "Custom Range")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ranges) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(Color.parseColor("#556874"))
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(Color.BLACK)
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(Color.WHITE)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangeSpinner.adapter = adapter

        rangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRangeType = ranges[position]
                if (currentRangeType == "Custom Range") {
                    dateRangeLayout.visibility = View.VISIBLE
                } else {
                    dateRangeLayout.visibility = View.GONE
                    when (currentRangeType) {
                        "Last 7 Days" -> setDateRange(7)
                        "Last 14 Days" -> setDateRange(14)
                        "Last 30 Days" -> setDateRange(30)
                    }
                    fetchDataForCurrentRange()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
            Toast.makeText(this, "Date range cannot exceed a year", Toast.LENGTH_SHORT).show()
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
        fetchTriggersData()
        fetchSignsData()
    }

    // -- TRIGGERS/SIGNS SPECIFIC CODE --
    private fun fetchTriggersData() {
        val userId = auth.currentUser?.uid ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val triggerCounts = mutableMapOf(
            "Home" to 0, "School" to 0, "Social" to 0,
            "Work" to 0, "Other" to 0
        )

        val diff = endCalendar.timeInMillis - startCalendar.timeInMillis
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        val calendar = Calendar.getInstance().apply { time = endCalendar.time }
        var processed = 0

        for (i in 0 until days) {
            val dateKey = dateFormat.format(calendar.time)
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { doc ->
                    doc.getString("selectedSOTD")?.let { trigger ->
                        triggerCounts[trigger] = triggerCounts[trigger]!! + 1
                    }
                    if (++processed == days) setupPieChart(pieChart2, triggerCounts, "Triggers")
                }
                .addOnFailureListener {
                    if (++processed == days) setupPieChart(pieChart2, triggerCounts, "Triggers")
                }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun fetchSignsData() {
        val userId = auth.currentUser?.uid ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val signCounts = mutableMapOf(
            "Body" to 0, "Mind" to 0, "Feelings" to 0, "Behavior" to 0
        )

        val diff = endCalendar.timeInMillis - startCalendar.timeInMillis
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        val calendar = Calendar.getInstance().apply { time = endCalendar.time }
        var processed = 0

        for (i in 0 until days) {
            val dateKey = dateFormat.format(calendar.time)
            db.collection("users").document(userId)
                .collection("dailyLogs").document(dateKey)
                .get()
                .addOnSuccessListener { doc ->
                    doc.getString("signsOption")?.let { sign ->
                        signCounts[sign] = signCounts[sign]!! + 1
                    }
                    if (++processed == days) setupPieChart(pieChart3, signCounts, "Signs")
                }
                .addOnFailureListener {
                    if (++processed == days) setupPieChart(pieChart3, signCounts, "Signs")
                }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    private fun setupPieChart(chart: PieChart, data: Map<String, Int>, title: String) {
        val entries = data.filter { it.value > 0 }.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(if (entries.isEmpty()) listOf(PieEntry(1f, "No Data")) else entries, "").apply {
            colors = listOf(
                Color.parseColor("#c2c2f0"), Color.parseColor("#ffeb99"),
                Color.parseColor("#fdfd96"), Color.parseColor("#ffb3b3"),
                Color.parseColor("#f7a9a9")
            )
            valueTextColor = Color.BLACK
            valueTextSize = 16f
            setValueFormatter(PercentFormatter(chart))
        }

        chart.apply {
            this.data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            setDrawHoleEnabled(false)
            invalidate()
        }

        // Update title with date range
        findViewById<TextView>(when(title) {
            "Triggers" -> R.id.triggersTextView
            else -> R.id.signsTextView
        }).text = "$title (${SimpleDateFormat("MMM dd", Locale.getDefault())
            .format(startCalendar.time)} - ${
            SimpleDateFormat("MMM dd", Locale.getDefault())
                .format(endCalendar.time)})"
    }
}