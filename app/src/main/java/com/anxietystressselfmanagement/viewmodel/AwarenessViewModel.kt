package com.anxietystressselfmanagement.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.anxietystressselfmanagement.model.AwarenessSigns
import com.anxietystressselfmanagement.model.StrategyAction
import com.anxietystressselfmanagement.model.SymptomAndIcon
import com.anxietystressselfmanagement.repository.AwarenessRepository
import kotlin.collections.get

class AwarenessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AwarenessRepository

    var selectedSign by mutableStateOf<String?>(null)
    var selectedOption by mutableStateOf<String?>(null)
    var parsedMap by mutableStateOf<Map<String, SymptomAndIcon>>(emptyMap())

    val awarenessOptions: List<String>
        get() = parsedMap[selectedSign]?.symptoms ?: emptyList()

    fun getIconFor(sign: String?): String {
        return parsedMap[sign]?.icon ?: ""
    }

    init {
        loadData(application.applicationContext)
    }

    private fun loadData(context: Context) {
        parsedMap = repository.loadAwareness(context)
    }

    // Call StrategyRepo to save strategies and actions selected
    fun saveAwarenessSelection(
        data: AwarenessSigns,
        date: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.saveAwarenessChoice(date, data, onSuccess, onFailure)
    }
}