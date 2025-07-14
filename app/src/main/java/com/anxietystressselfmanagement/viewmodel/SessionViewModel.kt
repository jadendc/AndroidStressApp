package com.anxietystressselfmanagement.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SessionViewModel : ViewModel() {
    var selectedDate by mutableStateOf<String?>(null)
        private set

    fun setDate(date: String) {
        selectedDate = date
    }
}