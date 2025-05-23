package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel for Dashboard3 screen that handles strategies and actions data operations
 * Uses LiveData for reactive UI updates and Coroutines for asynchronous operations.
 */
class Dashboard3ViewModel : ViewModel() {

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Date range values
    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance()
    private var currentRangeType = "Last 7 Days"

    // MutableLiveData - internal
    private val _strategiesData = MutableLiveData<Map<String, Int>>()
    private val _actionsData = MutableLiveData<Map<String, Int>>()
    private val _symptomDetails = MutableLiveData<Pair<String, List<String>>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()
    private val _dateRange = MutableLiveData<Triple<String, Calendar, Calendar>>()

    // Public LiveData - exposed to observers
    val strategiesData: LiveData<Map<String, Int>> = _strategiesData
    val actionsData: LiveData<Map<String, Int>> = _actionsData
    val symptomDetails: LiveData<Pair<String, List<String>>> = _symptomDetails
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String> = _errorMessage
    val dateRange: LiveData<Triple<String, Calendar, Calendar>> = _dateRange

    // Mapping dictionaries - same as in StrategiesAndActionsActivity
    private val strategyShortenedLabels = mapOf(
        "Breathing: Deep, slow breaths to calm the mind" to "Breathing",
        "Time Management: Plan and prioritize tasks" to "Time Management",
        "Movement: Walk, stretch, or exercise" to "Movement",
        "Digital Detox: Limit screen time" to "Digital Detox",
        "Social Connection: Talk to friends or family" to "Social Connection",
        "Gratitude: Focus on positive" to "Gratitude",
        "Relaxation: Meditate or listen to music" to "Relaxation",
        "Custom..." to "Custom..."
    )

    private val actionShortenedLabels = mapOf(
        "5-min deep breathing in the morning" to "5-min Breathing",
        "Plan tasks using a list or app" to "Plan Tasks",
        "10-min walk or stretch after lunch" to "10-min Walk",
        "No screens 1 hour before bed" to "No Screens",
        "Call or message a friend" to "Call Friend",
        "Write 3 things you're grateful for" to "Gratitude Writing",
        "Listen to relaxing music or meditation" to "Relax Music",
        "Custom..." to "Custom..."
    )

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
                val strategiesDeferred = fetchStrategiesDataAsync()
                val actionsDeferred = fetchActionsDataAsync()

                // Set the results to LiveData
                _strategiesData.value = strategiesDeferred
                _actionsData.value = actionsDeferred
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Convert full strategy text to shortened version
     */
    private fun getShortenedStrategy(fullStrategy: String): String {
        // Check if it's a custom strategy (not in our mapping)
        return strategyShortenedLabels[fullStrategy] ?:
        // If it's not in the map but starts with "Custom:", extract the custom text
        if (fullStrategy.startsWith("Custom: ")) {
            fullStrategy.substring(8) // Skip "Custom: " prefix
        } else {
            // Return first 15 chars + "..." for any other unmapped strategy
            if (fullStrategy.length > 15) "${fullStrategy.take(15)}..." else fullStrategy
        }
    }

    /**
     * Convert full action text to shortened version
     */
    private fun getShortenedAction(fullAction: String): String {
        // Check if it's a custom action (not in our mapping)
        return actionShortenedLabels[fullAction] ?:
        // If it's not in the map but starts with "Custom:", extract the custom text
        if (fullAction.startsWith("Custom: ")) {
            fullAction.substring(8) // Skip "Custom: " prefix
        } else {
            // Return first 15 chars + "..." for any other unmapped action
            if (fullAction.length > 15) "${fullAction.take(15)}..." else fullAction
        }
    }

    /**
     * Fetch strategies data asynchronously
     * @return Map of strategy names to counts
     */
    private suspend fun fetchStrategiesDataAsync(): Map<String, Int> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            // Calculate days in range
            val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
            val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val daysToProcess = min(daysInRange, 90)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val strategyCounts = mutableMapOf<String, Int>()

            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = endCalendar.time

            for (i in 0 until daysToProcess) {
                val dateKey = dateFormat.format(tempCalendar.time)

                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey)
                        .get()
                        .await()

                    val strategy = document.getString("7strategies") ?: ""
                    if (strategy.isNotEmpty()) {
                        // Convert to shortened version before adding to counts
                        val shortenedStrategy = getShortenedStrategy(strategy)
                        strategyCounts[shortenedStrategy] = strategyCounts.getOrDefault(shortenedStrategy, 0) + 1
                    }
                } catch (e: Exception) {
                    // Log error but continue processing other dates
                }

                tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            strategyCounts
        }

    /**
     * Fetch actions data asynchronously
     * @return Map of action names to counts
     */
    private suspend fun fetchActionsDataAsync(): Map<String, Int> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            // Calculate days in range
            val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
            val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val daysToProcess = min(daysInRange, 90)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val actionCounts = mutableMapOf<String, Int>()

            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = endCalendar.time

            for (i in 0 until daysToProcess) {
                val dateKey = dateFormat.format(tempCalendar.time)

                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey)
                        .get()
                        .await()

                    val action = document.getString("7actions") ?: ""
                    if (action.isNotEmpty()) {
                        // Convert to shortened version before adding to counts
                        val shortenedAction = getShortenedAction(action)
                        actionCounts[shortenedAction] = actionCounts.getOrDefault(shortenedAction, 0) + 1
                    }
                } catch (e: Exception) {
                    // Log error but continue processing other dates
                }

                tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            actionCounts
        }

    /**
     * Fetch details for a specific category (strategy)
     * @param category The strategy category to get details for
     */
    fun fetchDetailsForCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val symptoms = fetchSymptomDetailsAsync(category)
                _symptomDetails.value = Pair(category, symptoms)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch symptom details for a specific strategy asynchronously
     * @param category The strategy category
     * @return List of symptoms associated with the strategy
     */
    private suspend fun fetchSymptomDetailsAsync(category: String): List<String> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedSymptoms = mutableListOf<String>()

            // Calculate date range
            val diffInMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
            val daysInRange = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            val daysToProcess = min(daysInRange, 90)

            val tempCalendar = Calendar.getInstance().apply { time = endCalendar.time }

            for (i in 0 until daysToProcess) {
                val dateKey = dateFormat.format(tempCalendar.time)

                try {
                    val document = firestore.collection("users").document(userId)
                        .collection("dailyLogs").document(dateKey)
                        .get()
                        .await()

                    // Need to match against full strategy text
                    val strategies = document.getString("7strategies") ?: ""

                    // Find the full strategy text that corresponds to this shortened category
                    val fullStrategy = strategyShortenedLabels.entries.find { it.value == category }?.key ?: category

                    val selectedSymptom = document.getString("selectedSymptom") ?: ""

                    if (strategies == fullStrategy && selectedSymptom.isNotEmpty()) {
                        selectedSymptoms.add(selectedSymptom)
                    }
                } catch (e: Exception) {
                    // Continue with next date
                }

                tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            selectedSymptoms
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