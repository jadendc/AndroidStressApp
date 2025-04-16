package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _signUpState = MutableLiveData<SignUpState>()
    val signUpState: LiveData<SignUpState> = _signUpState

    init {
        _signUpState.value = SignUpState.Idle
    }

    fun signUp(firstName: String, lastName: String, email: String, password: String) {
        _signUpState.value = SignUpState.Loading

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userID = firebaseAuth.currentUser?.uid
                    if (userID != null) {
                        saveUserData(userID, firstName, lastName, email)
                    } else {
                        _signUpState.value = SignUpState.Error("Sign up failed: User ID is null")
                    }
                } else {
                    _signUpState.value = SignUpState.Error(
                        task.exception?.message ?: "Sign up failed"
                    )
                }
            }
    }

    private fun saveUserData(userID: String, firstName: String, lastName: String, email: String) {
        val user = hashMapOf(
            "first name" to firstName,
            "last name" to lastName,
            "email" to email
        )

        firestore.collection("users").document(userID)
            .set(user)
            .addOnSuccessListener {
                _signUpState.value = SignUpState.Success
            }
            .addOnFailureListener { e ->
                _signUpState.value = SignUpState.Error("Failed to save user data: ${e.message}")
            }
    }

    sealed class SignUpState {
        object Idle : SignUpState()
        object Loading : SignUpState()
        object Success : SignUpState()
        data class Error(val message: String) : SignUpState()
    }
}