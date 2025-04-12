package com.anxietystressselfmanagement

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

/**
 * ViewModel for HomeActivity that handles data operations and business logic.
 * Uses Kotlin Coroutines for asynchronous operations and LiveData for reactive UI updates.
 */
class HomeViewModel : ViewModel() {

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Private mutable LiveData
    private val _userName = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()

    // Public immutable LiveData exposed to the UI
    val userName: LiveData<String> = _userName
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Load user data from FirebaseAuth and Firestore
     */
    fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // First check if display name is available from FirebaseAuth
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Check if display name is available
                    if (!currentUser.displayName.isNullOrEmpty()) {
                        _userName.value = currentUser.displayName
                    } else {
                        // If not, fetch from Firestore
                        fetchUserNameFromFirestore(currentUser.uid)
                    }
                } else {
                    // Not logged in
                    _userName.value = ""
                    _errorMessage.value = "User not logged in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch user name from Firestore using coroutines for asynchronous operation
     */
    private suspend fun fetchUserNameFromFirestore(userId: String) {
        try {
            withContext(Dispatchers.IO) {
                val documentSnapshot = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val firstName = documentSnapshot.getString("first name")
                    withContext(Dispatchers.Main) {
                        _userName.value = firstName ?: ""
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _userName.value = ""
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _errorMessage.value = "Failed to fetch user data: ${e.message}"
            }
        }
    }

    /**
     * Clear error messages
     */
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}