package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

/**
 * ViewModel for Dashboard screen that handles all data operations and business logic.
 * Uses LiveData for reactive UI updates and Coroutines for asynchronous operations.
 */
class DashboardViewModel : ViewModel() {

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Date range values
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var currentRangeType = "Last 7 Days"

    // MutableLiveData - internal
    private val _barChartData = MutableLiveData<Pair<List<BarEntry>, List<String>>>()
    private val _pieChartData = MutableLiveData<Map<String, Int>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()
    private val _dateRange = MutableLiveData<Triple<String, Calendar, Calendar>>()

    // Public LiveData - exposed to observers
    val barChartData: LiveData<Pair<List<BarEntry>, List<String>>> = _barChartData
    val pieChartData: LiveData<Map<String, Int>> = _pieChartData
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String> = _errorMessage
    val dateRange: LiveData<Triple<String, Calendar, Calendar>> = _dateRange

    /**
     * Initialize with default date range
     */
    init {
        setDateRange(7) // Default to last 7 days
    }

    /**
     * Load saved date range from persistent storage
     * @param context The context to access shared preferences
     */
    fun loadSavedDateRange(context: android.content.Context) {
        val savedRange = DateRangeManager.getDateRange(context)
        if (savedRange != null) {
            currentRangeType = savedRange.rangeType
            startCalendar.time = savedRange.startDate
            endCalendar.time = savedRange.endDate
            _dateRange.value = Triple(currentRangeType, startCalendar, endCalendar)
        }
    }

    /**
     * Set a predefined date range based on number of days
     * @param days Number of days to include in the range
     */
    fun setDateRange(days: Int) {
        // Reset end date to today
        endCalendar.time = Calendar.getInstance().time

        // Set start date to days before end date
        startCalendar.time = endCalendar.time
        startCalendar.add(Calendar.DAY_OF_YEAR, -(days - 1))

        when (days) {
            7 -> currentRangeType = "Last 7 Days"
            14 -> currentRangeType = "Last 14 Days"
            30 -> currentRangeType = "Last 30 Days"
            else -> currentRangeType = "Custom Range"
        }

        _dateRange.value = Triple(currentRangeType, startCalendar, endCalendar)
    }

    /**
     * Set custom date range
     * @param start Start calendar
     * @param end End calendar
     */
    fun setCustomDateRange(start: Calendar, end: Calendar) {
        startCalendar.time = start.time
        endCalendar.time = end.time
        currentRangeType = "Custom Range"
        _dateRange.value = Triple(currentRangeType, startCalendar, endCalendar)
    }

    /**
     * Save current date range to persistent storage
     * @param context The context to access shared preferences
     */
    fun saveDateRange(context: android.content.Context) {
        DateRangeManager.saveDateRange(context, currentRangeType, startCalendar, endCalendar)
    }

    /**
     * Fetch all dashboard data for current date range
     * @param context The context to access shared preferences
     */
    fun fetchDataForCurrentRange(context: android.content.Context) {
        // Save the current range first
        saveDateRange(context)

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Fetch data in parallel
                val inControlDataDeferred = fetchInControlDataAsync()
                val moodDataDeferred = fetchMoodDataAsync()

                // Set the results to LiveData
                _barChartData.value = inControlDataDeferred
                _pieChartData.value = moodDataDeferred
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch in-control data for bar chart asynchronously
     * @return Pair of bar entries and date labels
     */
    private suspend fun fetchInControlDataAsync(): Pair<List<BarEntry>, List<String>> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

            // Calculate days in range
            val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
            val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val daysToProcess = min(daysInRange, 90)

            val dates = ArrayList<String>()
            val entries = ArrayList<BarEntry>()
            val entriesMap = mutableMapOf<Int, Float>()

            // Create a temporary calendar for iteration
            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = endCalendar.time

            for (i in 0 until daysToProcess) {
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCalendar.time)
                val formattedDate = dateFormat.format(tempCalendar.time)

                dates.add(0, formattedDate)

                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey)
                        .get()
                        .await()

                    val controlLevel = document.getLong("control")?.toFloat() ?: 0f
                    entriesMap[i] = controlLevel
                } catch (e: Exception) {
                    // Log error but continue processing other dates
                    entriesMap[i] = 0f
                }

                tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            // Create bar entries in proper order
            for (i in 0 until daysToProcess) {
                entries.add(BarEntry((daysToProcess - 1 - i).toFloat(), entriesMap[i] ?: 0f))
            }

            Pair(entries, dates)
        }

    /**
     * Fetch mood data for pie chart asynchronously
     * @return Map of mood names to counts
     */
    private suspend fun fetchMoodDataAsync(): Map<String, Int> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            // Calculate days in range
            val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
            val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val daysToProcess = min(daysInRange, 90)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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

                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey)
                        .get()
                        .await()

                    val mood = document.getString("feeling") ?: ""

                    when (mood) {
                        "😁" -> moodCounts["Excited"] = moodCounts["Excited"]!! + 1
                        "😊" -> moodCounts["Happy"] = moodCounts["Happy"]!! + 1
                        "😐" -> moodCounts["Indifferent"] = moodCounts["Indifferent"]!! + 1
                        "😔" -> moodCounts["Sad"] = moodCounts["Sad"]!! + 1
                        "😢" -> moodCounts["Angry"] = moodCounts["Angry"]!! + 1
                    }
                } catch (e: Exception) {
                    // Log error but continue processing other dates
                }

                tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            moodCounts
        }

    /**
     * Validate a custom date range
     * @return Pair of (isValid, errorMessage)
     */
    fun validateDateRange(): Pair<Boolean, String?> {
        if (startCalendar.after(endCalendar)) {
            return Pair(false, "Start date cannot be after end date")
        }

        // Calculate days between dates
        val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1

        if (diffInDays > 90) {
            return Pair(false, "Date range cannot exceed 90 days")
        }

        return Pair(true, null)
    }

    /**
     * Clear error messages
     */
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}