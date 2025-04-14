package com.anxietystressselfmanagement

import TriggerDetail
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

// Data class definitions (can be in separate files)


class Dashboard2ViewModel : ViewModel() {

    private val TAG = "Dashboard2ViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var currentRangeType = "Last 7 Days"

    // --- LiveData Definitions ---
    private val _triggersData = MutableLiveData<List<TriggerDetail>>()
    // MODIFIED: Use SignDetail for signs data
    private val _signsData = MutableLiveData<List<SignDetail>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()
    private val _dateRange = MutableLiveData<Triple<String, Calendar, Calendar>>()

    val triggersData: LiveData<List<TriggerDetail>> = _triggersData
    // MODIFIED: Expose LiveData<List<SignDetail>>
    val signsData: LiveData<List<SignDetail>> = _signsData
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String?> = _errorMessage
    val dateRange: LiveData<Triple<String, Calendar, Calendar>> = _dateRange
    // --- End LiveData Definitions ---

    init {
        internalSetDateRange(7) // Set initial default range internally
        Log.d(TAG, "ViewModel initialized.")
    }

    private fun internalSetDateRange(days: Int) {
        endCalendar.time = Calendar.getInstance().time
        startCalendar.time = endCalendar.time
        startCalendar.add(Calendar.DAY_OF_YEAR, -(days - 1))
        // Ensure time part is cleared for consistent day boundaries
        clearTime(startCalendar)
        clearTime(endCalendar)
        currentRangeType = when (days) {
            7 -> "Last 7 Days"
            14 -> "Last 14 Days"
            30 -> "Last 30 Days"
            else -> "Custom Range"
        }
    }

    // Helper to clear time part of Calendar
    private fun clearTime(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun initializeDateRange(context: Context) {
        Log.d(TAG, "initializeDateRange called.")
        val savedRange = DateRangeManager.getDateRange(context)
        if (savedRange != null) {
            currentRangeType = savedRange.rangeType
            startCalendar.time = savedRange.startDate
            endCalendar.time = savedRange.endDate
            // Ensure time is cleared on loaded dates too
            clearTime(startCalendar)
            clearTime(endCalendar)
            Log.d(TAG, "Loaded saved range: $currentRangeType from ${startCalendar.time} to ${endCalendar.time}")
        } else {
            internalSetDateRange(7) // Reset to default if nothing loaded
            Log.d(TAG, "No saved range found, using default: $currentRangeType")
        }
        // Post the initial range value
        _dateRange.value = Triple(currentRangeType, startCalendar.clone() as Calendar, endCalendar.clone() as Calendar)
    }

    fun setDateRange(days: Int) {
        internalSetDateRange(days)
        Log.d(TAG, "setDateRange called for $days days. New range: $currentRangeType")
        _dateRange.value = Triple(currentRangeType, startCalendar.clone() as Calendar, endCalendar.clone() as Calendar)
    }

    fun setCustomDateRange(start: Calendar, end: Calendar) {
        startCalendar.time = start.time
        endCalendar.time = end.time
        // Ensure time part is cleared for custom range too
        clearTime(startCalendar)
        clearTime(endCalendar)
        currentRangeType = "Custom Range"
        Log.d(TAG, "setCustomDateRange called. New range: $currentRangeType from ${startCalendar.time} to ${endCalendar.time}")
        _dateRange.value = Triple(currentRangeType, startCalendar.clone() as Calendar, endCalendar.clone() as Calendar)
    }

    fun saveDateRange(context: Context) {
        _dateRange.value?.let { (type, start, end) ->
            Log.d(TAG, "Saving date range: $type from ${start.time} to ${end.time}")
            DateRangeManager.saveDateRange(context, type, start, end)
        } ?: Log.w(TAG, "Attempted to save date range before it was initialized.")
    }

    fun fetchDataForCurrentRange(context: Context) {
        val rangeInfo = _dateRange.value
        if (rangeInfo == null) {
            Log.e(TAG, "fetchDataForCurrentRange called before dateRange is initialized!")
            _errorMessage.value = "Date range not set."
            // Initialize range if called too early? Or rely on UI flow.
            // initializeDateRange(context) // Potentially initialize here if needed
            return
        }

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in, cannot fetch data.")
            _errorMessage.value = "User not logged in."
            return
        }

        saveDateRange(context) // Save the range being fetched

        Log.d(TAG, "Starting data fetch for range: ${rangeInfo.first}")
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "Launching async fetch tasks...")
                // Pass range explicitly to async functions to avoid potential race conditions with LiveData updates
                val rangeStartDate = rangeInfo.second.clone() as Calendar
                val rangeEndDate = rangeInfo.third.clone() as Calendar

                val triggersDeferred = async { fetchTriggersDataAsync(currentUserId, rangeStartDate, rangeEndDate) }
                // MODIFIED: Expect List<SignDetail> from fetchSignsDataAsync
                val signsDeferred = async { fetchSignsDataAsync(currentUserId, rangeStartDate, rangeEndDate) }

                val triggersResult = triggersDeferred.await()
                val signsResult = signsDeferred.await() // Now List<SignDetail>
                Log.d(TAG, "Async fetch tasks completed.")

                withContext(Dispatchers.Main) {
                    _triggersData.value = triggersResult
                    // MODIFIED: Assign List<SignDetail> to _signsData
                    _signsData.value = signsResult
                    Log.d(TAG, "LiveData updated. Triggers: ${triggersResult.size}, Signs: ${signsResult.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching dashboard data", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error fetching data: ${e.localizedMessage}"
                    _triggersData.value = emptyList()
                    _signsData.value = emptyList() // Clear signs data too
                }
            } finally {
                Log.d(TAG, "Data fetch process finished.")
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    /** Fetch triggers data asynchronously, returning detailed information. */
    private suspend fun fetchTriggersDataAsync(userId: String, rangeStartCal: Calendar, rangeEndCal: Calendar): List<TriggerDetail> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting async fetch for Triggers")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val triggersAggregator = mutableMapOf<String, MutableList<String>>()
            val tempCalendar = rangeStartCal.clone() as Calendar

            while (!tempCalendar.after(rangeEndCal)) {
                val dateKey = dateFormat.format(tempCalendar.time)
                // Log.v(TAG, "Fetching triggers for date: $dateKey") // Verbose
                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey).get().await()
                    if (document.exists()) {
                        val broadTrigger = document.getString("selectedSOTD")
                        val specificTrigger = document.getString("selectedOption")
                        if (broadTrigger != null) {
                            triggersAggregator.computeIfAbsent(broadTrigger) { mutableListOf() }
                                .add(specificTrigger ?: "Unspecified")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching trigger doc for $dateKey", e)
                }
                tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            Log.d(TAG, "Finished async fetch for Triggers. Aggregated ${triggersAggregator.size} categories.")
            triggersAggregator.map { (category, specificList) ->
                TriggerDetail(category, specificList.size, specificList)
            }
        }

    /** Fetch signs data asynchronously, returning detailed information. */
    // MODIFIED: Fetch specific signs and return List<SignDetail>
    private suspend fun fetchSignsDataAsync(userId: String, rangeStartCal: Calendar, rangeEndCal: Calendar): List<SignDetail> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Starting async fetch for Signs")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // Intermediate aggregator: Map<BroadCategory, MutableList<SpecificSign>>
            val signsAggregator = mutableMapOf<String, MutableList<String>>()
            val tempCalendar = rangeStartCal.clone() as Calendar

            while (!tempCalendar.after(rangeEndCal)) {
                val dateKey = dateFormat.format(tempCalendar.time)
                // Log.v(TAG, "Fetching signs for date: $dateKey") // Verbose
                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey).get().await()

                    if (document.exists()) {
                        // Get the broad category ("Mind", "Body", etc.)
                        val broadSignCategory = document.getString("signsOption")
                        // Get the specific symptom ("Worrying", "Difficulty concentrating", etc.)
                        val specificSignSymptom = document.getString("selectedSymptom")

                        if (broadSignCategory != null && broadSignCategory.isNotBlank()) {
                            // Add the specific symptom to the list for the broad category
                            signsAggregator.computeIfAbsent(broadSignCategory) { mutableListOf() }
                                .add(specificSignSymptom ?: "Unspecified") // Use placeholder if specific is null/missing
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching signs document for $dateKey", e)
                }
                tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            Log.d(TAG, "Finished async fetch for Signs. Aggregated ${signsAggregator.size} categories.")

            // Transform the aggregated data into the final structure List<SignDetail>
            signsAggregator.map { (category, specificList) ->
                SignDetail(
                    category = category,
                    count = specificList.size, // Count is the total number of entries for this category
                    specificSigns = specificList // The list of specific signs/symptoms recorded
                )
            }
        }

    /** Validate the current date range. */
    fun validateDateRange(): Pair<Boolean, String?> {
        _dateRange.value ?: return Pair(false, "Date range not initialized")
        val (_, start, end) = _dateRange.value!!
        if (start.after(end)) return Pair(false, "Start date cannot be after end date")
        // Add other validation if needed (e.g., range length)
        return Pair(true, null)
    }

    /** Clear the error message LiveData. */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}