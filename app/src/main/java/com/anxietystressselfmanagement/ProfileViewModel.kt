package com.anxietystressselfmanagement

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userEmail = MutableLiveData<String?>()
    private val _userName = MutableLiveData<String?>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _successMessage = MutableLiveData<String>()
    private val _errorMessage = MutableLiveData<String>()
    private val _isDeleting = MutableLiveData<Boolean>()
    private val _deleteSuccess = MutableLiveData<String>()
    private val _deleteError = MutableLiveData<String>()

    val userEmail: LiveData<String?> = _userEmail
    val userName: LiveData<String?> = _userName
    val isLoading: LiveData<Boolean> = _isLoading
    val successMessage: LiveData<String> = _successMessage
    val errorMessage: LiveData<String> = _errorMessage
    val isDeleting: LiveData<Boolean> = _isDeleting
    val deleteSuccess: LiveData<String> = _deleteSuccess
    val deleteError: LiveData<String> = _deleteError

    fun loadUserData() {
        val userId = auth.currentUser?.uid
        val email = auth.currentUser?.email

        _userEmail.value = email

        if (userId != null) {
            _userName.value = null
            _isLoading.value = true

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("first name")
                        if (_userName.value != firstName) {
                            _userName.value = firstName
                        }
                    } else {
                        if (_userName.value != null) {
                            _userName.value = null
                        }
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = "Failed to load profile: ${exception.message}"
                    _isLoading.value = false
                    if (_userName.value != null) {
                        _userName.value = null
                    }
                }
        } else {
            _userEmail.value = "Not logged in"
            _userName.value = null
        }
    }

    fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            _isLoading.value = true
            _errorMessage.value = ""

            val userUpdates = mapOf("first name" to newName)

            firestore.collection("users").document(userId)
                .set(userUpdates, SetOptions.merge())
                .addOnSuccessListener {
                    _successMessage.value = "Profile updated successfully"
                    if (_userName.value != newName) {
                        _userName.value = newName
                    }
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

    fun deleteUserData() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _deleteError.value = "User not authenticated"
            return
        }

        _isDeleting.value = true
        _deleteError.value = ""

        val userRef = firestore.collection("users").document(userId)

        userRef.collection("dailyLogs").get()
            .addOnSuccessListener { logsSnapshot ->
                if (logsSnapshot.isEmpty) {
                    // No logs to delete, operation is trivially successful
                    _deleteSuccess.value = "No associated data (logs) found to delete."
                    _isDeleting.value = false
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()
                logsSnapshot.documents.forEach { batch.delete(it.reference) }

                batch.commit()
                    .addOnSuccessListener {
                        // *** Main document (userRef) is NOT deleted ***
                        _deleteSuccess.value = "Associated data (logs) deleted successfully."
                        // User profile remains, no need to clear local state like name/email
                        _isDeleting.value = false
                    }
                    .addOnFailureListener { e ->
                        _deleteError.value = "Error deleting associated data: ${e.message}"
                        _isDeleting.value = false
                    }
            }
            .addOnFailureListener { e ->
                // Failure to query logs is an error itself
                Log.w("ProfileViewModel", "Could not query dailyLogs: ${e.message}")
                _deleteError.value = "Error accessing associated data to delete: ${e.message}"
                _isDeleting.value = false
            }
    }

    fun clearMessages() {
        _successMessage.value = ""
        _errorMessage.value = ""
    }

    fun clearAllMessages() {
        clearMessages()
        _deleteSuccess.value = ""
        _deleteError.value = ""
    }
}