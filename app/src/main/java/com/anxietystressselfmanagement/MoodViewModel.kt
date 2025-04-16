package com.anxietystressselfmanagement

import android.app.Application // Import Application
import androidx.lifecycle.AndroidViewModel // Change to AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
// Remove androidx.lifecycle.ViewModel import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// Change ViewModel to AndroidViewModel to access application context for strings
class MoodViewModel(application: Application) : AndroidViewModel(application) {

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
        // Reset save state to Idle if user changes mood after an error/success
        if (_saveState.value is SaveState.Error || _saveState.value is SaveState.Success) {
            _saveState.value = SaveState.Idle
        }
    }

    fun saveMood(selectedDate: String) {
        val currentUser = auth.currentUser ?: run {
            // Access string resource via application context
            _saveState.value = SaveState.Error(getApplication<Application>().getString(R.string.user_not_signed_in_error))
            return
        }

        val mood = _selectedMood.value ?: 0
        if (mood <= 0) {
            // Access string resource via application context
            _saveState.value = SaveState.Error(getApplication<Application>().getString(R.string.select_mood_error))
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
                _selectedMood.value = 0 // Optionally reset mood after successful save
            }
            .addOnFailureListener { e ->
                // Access string resource via application context, provide default if message is null
                _saveState.value = SaveState.Error(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
            }
    }

    private fun getMoodEmoji(mood: Int): String {
        return when (mood) {
            1 -> "😢"
            2 -> "😔"
            3 -> "😐"
            4 -> "😊"
            5 -> "😁"
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