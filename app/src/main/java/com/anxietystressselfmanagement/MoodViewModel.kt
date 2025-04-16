package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MoodViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _selectedMood = MutableLiveData<Int>()
    val selectedMood: LiveData<Int> = _selectedMood

    private val _saveState = MutableLiveData<SaveState>()
    val saveState: LiveData<SaveState> = _saveState

    init {
        _selectedMood.value = 0
        _saveState.value = SaveState.Idle
    }

    fun selectMood(mood: Int) {
        _selectedMood.value = mood
    }

    fun saveMood(selectedDate: String) {
        val currentUser = auth.currentUser ?: run {
            _saveState.value = SaveState.Error("User not signed in")
            return
        }

        val mood = _selectedMood.value ?: 0
        if (mood <= 0) {
            _saveState.value = SaveState.Error("Please select a mood first")
            return
        }

        _saveState.value = SaveState.Loading

        val userId = currentUser.uid
        val emojiValue = getMoodEmoji(mood)
        val moodData = hashMapOf("feeling" to emojiValue)

        db.collection("users")
            .document(userId)
            .collection("dailyLogs")
            .document(selectedDate)
            .set(moodData, SetOptions.merge())
            .addOnSuccessListener {
                _saveState.value = SaveState.Success
            }
            .addOnFailureListener { e ->
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
    }

    private fun getMoodEmoji(mood: Int): String {
        return when (mood) {
            1 -> "😢"  // Very sad
            2 -> "😔"  // Sad
            3 -> "😐"  // Neutral
            4 -> "😊"  // Happy
            5 -> "😁"  // Very happy
            else -> ""
        }
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}