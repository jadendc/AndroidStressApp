package com.anxietystressselfmanagement

import android.util.Log
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
import java.util.*

class StrategyActionDetailViewModel : ViewModel() {

    companion object {
        private const val TAG = "StratActDetailVM"
        private const val STRATEGY_FIELD = "7strategies" // Confirm exact Firestore field name
        private const val ACTION_FIELD = "7actions"     // Confirm exact Firestore field name
        private const val RATING_FIELD ="effectivenessRating"
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _detailsList = MutableLiveData<List<StrategyActionEntry>>()
    val detailsList: LiveData<List<StrategyActionEntry>> = _detailsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchStrategyActionDetails(startDateMillis: Long, endDateMillis: Long) {
        Log.d(TAG, "fetchStrategyActionDetails called. Start: $startDateMillis, End: $endDateMillis")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.e(TAG, "User not logged in.")
                _errorMessage.value = "User not logged in."
                _isLoading.value = false
                return@launch
            }
            Log.d(TAG, "Fetching for User ID: $userId")

            val fetchedDetails = mutableListOf<StrategyActionEntry>()
            val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
            val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis }

            if (startCal.after(endCal)) {
                Log.e(TAG, "Start date is after end date.")
                _errorMessage.value = "Invalid date range."
                _isLoading.value = false
                return@launch
            }

            // Date format for Firestore document keys (YYYY-MM-DD)
            val firestoreDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // Date format for display in the list
            val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            Log.d(TAG, "Date range: ${firestoreDateFormat.format(startCal.time)} to ${firestoreDateFormat.format(endCal.time)}")

            try {
                withContext(Dispatchers.IO) {
                    val tempCalendar = Calendar.getInstance().apply { time = startCal.time }

                    while (!tempCalendar.after(endCal)) {
                        val dateKey = firestoreDateFormat.format(tempCalendar.time)
                        Log.d(TAG, "Checking date key: $dateKey") // Log each date being checked

                        try {
                            val docRef = firestore.collection("users").document(userId)
                                .collection("dailyLogs").document(dateKey)

                            val document = docRef.get().await()

                            if (document.exists()) {
                                Log.d(TAG, "Document exists for $dateKey") // Log if doc found
                                val strategy = document.getString(STRATEGY_FIELD)
                                val action = document.getString(ACTION_FIELD)
                                val rating = document.getLong(RATING_FIELD)?.toInt()
                                Log.d(TAG, " -> Strategy: '$strategy', Action: '$action', Rate of Effectiveness: '$rating' ") // Log retrieved values

                                // Check if values are not null or blank before adding
                                if (!strategy.isNullOrBlank() || !action.isNullOrBlank()) {
                                    val displayDate = displayDateFormat.format(tempCalendar.time)
                                    // Use original calendar time for sorting later if needed
                                    val entryTime = tempCalendar.timeInMillis
                                    fetchedDetails.add(StrategyActionEntry(displayDate, strategy, action, rating))
                                    Log.d(TAG, " -> Added entry for $displayDate")
                                } else {
                                    Log.d(TAG, " -> Skipping $dateKey (both strategy and action are blank/null)")
                                }
                            } else {
                                Log.d(TAG, "Document does NOT exist for $dateKey") // Log if doc not found
                            }
                        } catch (e: Exception) {
                            // Log Firestore errors for specific dates but continue loop
                            Log.e(TAG, "Error fetching details for date $dateKey", e)
                        }
                        tempCalendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                    }
                }

                Log.d(TAG, "Finished fetching. Found ${fetchedDetails.size} potential entries.")

                // Sort by date descending (using the display date string, requires parsing back)
                val sortedDetails = fetchedDetails.sortedByDescending {
                    try { displayDateFormat.parse(it.date)?.time ?: 0L } catch (e: Exception) { 0L }
                }

                Log.d(TAG, "Updating LiveData with ${sortedDetails.size} entries.")
                _detailsList.postValue(sortedDetails) // Use postValue if called from background, value if main

            } catch (e: Exception) {
                Log.e(TAG, "Outer error fetching details: ${e.message}", e)
                _errorMessage.postValue("Failed to load details: ${e.message}") // Use postValue
            } finally {
                _isLoading.postValue(false) // Use postValue
            }
        }
    }
}