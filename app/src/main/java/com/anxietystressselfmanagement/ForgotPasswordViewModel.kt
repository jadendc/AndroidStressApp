package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _resetPasswordState = MutableLiveData<ResetPasswordState>()
    val resetPasswordState: LiveData<ResetPasswordState> = _resetPasswordState

    init {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    fun sendPasswordResetEmail(email: String) {
        _resetPasswordState.value = ResetPasswordState.Loading

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetPasswordState.value = ResetPasswordState.Success
                } else {
                    _resetPasswordState.value = ResetPasswordState.Error(
                        task.exception?.message ?: "Unable to send reset mail"
                    )
                }
            }
    }

    sealed class ResetPasswordState {
        object Idle : ResetPasswordState()
        object Loading : ResetPasswordState()
        object Success : ResetPasswordState()
        data class Error(val message: String) : ResetPasswordState()
    }
}