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
    private val _isDeleting = MutableLiveData<Boolean>()
    private val _deleteSuccess = MutableLiveData<String>()
    private val _deleteError = MutableLiveData<String>()

    // Public LiveData - exposed to observers
    val userEmail: LiveData<String?> = _userEmail
    val userName: LiveData<String?> = _userName
    val isLoading: LiveData<Boolean> = _isLoading
    val successMessage: LiveData<String> = _successMessage
    val errorMessage: LiveData<String> = _errorMessage
    val isDeleting: LiveData<Boolean> = _isDeleting
    val deleteSuccess: LiveData<String> = _deleteSuccess
    val deleteError: LiveData<String> = _deleteError

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
     * Delete all user data from Firestore
     * This performs a cascading delete of all user-related collections
     */
    fun deleteUserData() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _deleteError.value = "User not authenticated"
            return
        }

        _isDeleting.value = true

        // Reference to the user document
        val userRef = firestore.collection("users").document(userId)

        // 1. First, get all dailyLogs documents
        userRef.collection("dailyLogs").get()
            .addOnSuccessListener { logsSnapshot ->
                // Create batch for efficient deletes
                val batch = firestore.batch()

                // Add all daily logs to batch delete
                for (document in logsSnapshot.documents) {
                    batch.delete(document.reference)
                }

                // 2. Execute the batch delete
                batch.commit()
                    .addOnSuccessListener {
                        // 3. Delete the user document itself
                        userRef.delete()
                            .addOnSuccessListener {
                                _deleteSuccess.value = "Your data has been completely deleted"
                                _isDeleting.value = false
                            }
                            .addOnFailureListener { e ->
                                _deleteError.value = "Error deleting user profile: ${e.message}"
                                _isDeleting.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        _deleteError.value = "Error deleting logs: ${e.message}"
                        _isDeleting.value = false
                    }
            }
            .addOnFailureListener { e ->
                _deleteError.value = "Error retrieving data to delete: ${e.message}"
                _isDeleting.value = false
            }
    }

    /**
     * Clear any one-time messages to prevent showing them again on configuration changes
     */
    fun clearMessages() {
        _successMessage.value = ""
        _errorMessage.value = ""
    }

    /**
     * Clear all messages including deletion-related ones
     */
    fun clearAllMessages() {
        _successMessage.value = ""
        _errorMessage.value = ""
        _deleteSuccess.value = ""
        _deleteError.value = ""
    }
}