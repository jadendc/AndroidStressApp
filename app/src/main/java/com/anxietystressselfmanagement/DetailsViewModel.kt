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
import java.util.Calendar
import java.util.Locale

class DetailsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _detailsList = MutableLiveData<List<Pair<String, String>>>() // Pair of (Specific Item, Date)
    val detailsList: LiveData<List<Pair<String, String>>> = _detailsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchDetails(category: String, type: String, startDateMillis: Long, endDateMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _errorMessage.value = "User not logged in."
                _isLoading.value = false
                _detailsList.value = emptyList() // Clear previous data
                return@launch
            }

            val fetchedDetails = mutableListOf<Pair<String, String>>()
            val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
            val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            Log.d("DetailViewModel", "Fetching details for Type: '$type', Category: '$category' from $startDateMillis to $endDateMillis")

            try {
                withContext(Dispatchers.IO) {
                    val tempCalendar = Calendar.getInstance().apply { time = startCal.time }

                    while (!tempCalendar.after(endCal)) {
                        val dateKey = dateFormat.format(tempCalendar.time)
                        try {
                            val docRef = firestore.collection("users").document(userId)
                                .collection("dailyLogs").document(dateKey)

                            val document = docRef.get().await()

                            if (document.exists()) {
                                var specificDetail: String? = null

                                if (type == "Trigger") {
                                    // --- Trigger Logic (Existing - Seems OK) ---
                                    val triggerCategoryValue = document.getString("selectedSOTD")
                                    if (triggerCategoryValue == category) {
                                        // Assuming specific trigger detail is in "selectedOption" based on your previous code
                                        specificDetail = document.getString("selectedOption")
                                        Log.d("DetailViewModel", "Date: $dateKey, Trigger Match: '$category', Detail: '$specificDetail'")
                                    }
                                } else if (type == "Sign") {
                                    // --- Sign Logic (NEW) ---
                                    // Determine the correct field based on the category passed ("Body", "Mind", etc.)
                                    val fieldToCheck = when (category) {
                                        "Body" -> "bodySymptom"
                                        "Mind" -> "mindSymptom"
                                        "Feelings" -> "feelingSymptom"
                                        "Behavior" -> "behaviorSymptom"
                                        else -> null // Unknown category
                                    }

                                    if (fieldToCheck != null && document.contains(fieldToCheck)) {
                                        // Get the specific detail from the correct field
                                        specificDetail = document.getString(fieldToCheck)
                                        Log.d("DetailViewModel", "Date: $dateKey, Sign Category: '$category', Field: '$fieldToCheck', Detail: '$specificDetail'")
                                    } else if (fieldToCheck == null) {
                                        Log.w("DetailViewModel", "Unknown category '$category' for type 'Sign'")
                                    }
                                }

                                // Add to list if a detail was found
                                if (!specificDetail.isNullOrBlank()) {
                                    fetchedDetails.add(Pair(specificDetail, dateKey))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DetailViewModel", "Error processing document for date $dateKey", e)
                            // Optionally propagate a non-fatal error or just log
                        }
                        tempCalendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                    }
                }
                // Sort by date descending AFTER collecting all details
                _detailsList.value = fetchedDetails.sortedByDescending { it.second }
                Log.d("DetailViewModel", "Fetched ${fetchedDetails.size} details total.")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error fetching details main block: ${e.message}", e)
                _errorMessage.value = "Failed to load details: ${e.message}"
                _detailsList.value = emptyList() // Clear data on error
            } finally {
                _isLoading.value = false
            }
        }
    }
}