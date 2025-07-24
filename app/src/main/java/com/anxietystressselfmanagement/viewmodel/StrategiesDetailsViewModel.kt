package com.anxietystressselfmanagement.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.anxietystressselfmanagement.model.StrategyCardEntry
import com.anxietystressselfmanagement.repository.StrategiesDetailsRepository
import com.google.firebase.auth.FirebaseAuth

class StrategiesDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _entries = mutableStateListOf<StrategyCardEntry>()
    val entries: List<StrategyCardEntry>
        get() = _entries

    private var lastStartMillis: Long = 0
    private var lastEndMillis: Long = 0

    fun fetchStrategiesBetween(startMillis: Long, endMillis: Long) {
        lastStartMillis = startMillis
        lastEndMillis = endMillis

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        StrategiesDetailsRepository.getStrategiesBetween(userId, startMillis, endMillis) { result ->
            _entries.clear()
            _entries.addAll(result)
        }
    }

    fun reloadEntries() {
        if (lastStartMillis != 0L && lastEndMillis != 0L) {
            fetchStrategiesBetween(lastStartMillis, lastEndMillis)
        }
    }

    fun updateRatingForEntry(
        date: String,
        newRating: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        StrategiesDetailsRepository.updateRating(
            date = date,
            rating = newRating,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}