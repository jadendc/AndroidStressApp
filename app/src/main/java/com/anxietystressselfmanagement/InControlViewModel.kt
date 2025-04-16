package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class InControlViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _controlLevel = MutableLiveData<Int>()
    val controlLevel: LiveData<Int> = _controlLevel

    private val _saveState = MutableLiveData<SaveState>()
    val saveState: LiveData<SaveState> = _saveState

    init {
        _controlLevel.value = 0
        _saveState.value = SaveState.Idle
    }

    fun setControlLevel(level: Int) {
        _controlLevel.value = level
    }

    fun saveControlLevel(selectedDate: String) {
        val currentUser = auth.currentUser ?: run {
            _saveState.value = SaveState.Error("User not signed in")
            return
        }

        val level = _controlLevel.value ?: 0
        if (level <= 0) {
            _saveState.value = SaveState.Error("Please select a control level first")
            return
        }

        _saveState.value = SaveState.Loading

        val userId = currentUser.uid
        val controlData = hashMapOf(
            "control" to level,
            "controlLevel" to level
        )

        // Try updating first (in case the document already exists)
        db.collection("users")
            .document(userId)
            .collection("dailyLogs")
            .document(selectedDate)
            .update("control", level, "controlLevel", level)
            .addOnSuccessListener {
                _saveState.value = SaveState.Success
            }
            .addOnFailureListener {
                // If update fails, try setting with merge option
                db.collection("users")
                    .document(userId)
                    .collection("dailyLogs")
                    .document(selectedDate)
                    .set(controlData, SetOptions.merge())
                    .addOnSuccessListener {
                        _saveState.value = SaveState.Success
                    }
                    .addOnFailureListener { e ->
                        _saveState.value = SaveState.Error(e.message ?: "Failed to save control level")
                    }
            }
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}