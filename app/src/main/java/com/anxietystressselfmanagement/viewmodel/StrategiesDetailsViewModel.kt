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

    fun fetchStrategiesBetween(startMillis: Long, endMillis: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        StrategiesDetailsRepository.getStrategiesBetween(userId, startMillis, endMillis) { result ->
            _entries.clear()
            _entries.addAll(result)
        }
    }

}