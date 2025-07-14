package com.anxietystressselfmanagement.viewmodel

import android.app.Application
import android.content.Context
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
class StrategiesActionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StrategyRepository

    var selectedStrategy by mutableStateOf<String?>(null)
    var selectedAction by mutableStateOf<String?>(null)
    var parsedMap by mutableStateOf<Map<String,List<ActionDescription>>>(emptyMap())
    var selectedRating by mutableStateOf(0)

    val strategies: List<String>
        get() =  parsedMap.keys.toList()

    val actions: List<String>
        get() = parsedMap[selectedStrategy]?.map { it.action } ?: emptyList()

    init {
        loadData(application.applicationContext)
    }

    private fun loadData(context: Context) {
        parsedMap = repository.loadStrategies(context)
    }

    // Call StrategyRepo to save strategies and actions selected
    fun saveStrategyAndAction(
        data: StrategyAction,
        rating: Int,
        date: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.saveStrategyAndAction(date, rating, data, onSuccess, onFailure)
    }
}