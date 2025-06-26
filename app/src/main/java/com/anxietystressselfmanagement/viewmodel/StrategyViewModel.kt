package com.anxietystressselfmanagement.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.anxietystressselfmanagement.repository.StrategyRepository
import com.anxietystressselfmanagement.model.ActionDescription
import com.anxietystressselfmanagement.model.StrategyAction

/**
 * ViewModel for managing strategy and action selections.
 * Holds the selected values and filtered action lists,
 * and provides functionality to save data to a database.
 */
class StrategyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StrategyRepository

    var selectedStrategy by mutableStateOf<String?>(null)
    var selectedAction by mutableStateOf<String?>(null)
    var parsedMap by mutableStateOf<Map<String,List<ActionDescription>>>(emptyMap())

    val strategies: List<String>
        get() =  parsedMap.keys.toList()

    val actions: List<String>
        get() = parsedMap[selectedStrategy]?.map { it.action } ?: emptyList()

    init {
        loadData(application.applicationContext)
    }

    private fun loadData(context: Context) {
        parsedMap = StrategyRepository.loadStrategies(context)
    }

    // Call StrategyRepo to save strategies and actions selected
    fun saveStrategyAndAction(
        data: StrategyAction,
        date: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.saveStrategyAndAction(date, data, onSuccess, onFailure)
    }
}