package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ViewModel for Profile screen that handles business logic and data operations
 * Uses LiveData for reactive UI updates and MVVM architecture pattern
 */
class ProfileViewModel : ViewModel() {

    // Private Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // MutableLiveData - internal
    private val _userEmail = MutableLiveData<String?>()
    private val _userName = MutableLiveData<String?>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _successMessage = MutableLiveData<String>()
    private val _errorMessage = MutableLiveData<String>()

    // Public LiveData - exposed to observers
    val userEmail: LiveData<String?> = _userEmail
    val userName: LiveData<String?> = _userName
    val isLoading: LiveData<Boolean> = _isLoading
    val successMessage: LiveData<String> = _successMessage
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Load user data from Firestore
     */
    fun loadUserData() {
        val userId = auth.currentUser?.uid
        val email = auth.currentUser?.email

        // Set user email
        _userEmail.value = email

        // If user is authenticated, fetch their data
        if (userId != null) {
            _isLoading.value = true

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Load and set user's name
                        val firstName = document.getString("first name")
                        _userName.value = firstName
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = "Failed to load profile: ${exception.message}"
                    _isLoading.value = false
                }
        }
    }

    /**
     * Update user's name in Firestore
     * @param newName The new name to save
     */
    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            _isLoading.value = true
            _errorMessage.value = ""

            val userUpdates = mapOf("first name" to newName)

            firestore.collection("users").document(userId).update(userUpdates)
                .addOnSuccessListener {
                    _successMessage.value = "Profile updated successfully"
                    _userName.value = newName
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = "Failed to update profile: ${exception.message}"
                    _isLoading.value = false
                }
        } else {
            _errorMessage.value = "You must be logged in to update profile"
        }
    }

    /**
     * Clear any one-time messages to prevent showing them again on configuration changes
     */
    fun clearMessages() {
        _successMessage.value = ""
        _errorMessage.value = ""
    }
}