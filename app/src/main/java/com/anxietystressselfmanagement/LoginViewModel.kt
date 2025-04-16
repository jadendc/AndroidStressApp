package com.anxietystressselfmanagement

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val PREFS_NAME = "login_prefs"

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    init {
        _loginState.value = LoginState.Idle
    }

    fun login(email: String, password: String, rememberMe: Boolean, context: Context) {
        _loginState.value = LoginState.Loading

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveLoginPreferences(email, rememberMe, context)
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error(
                        task.exception?.message ?: "Authentication failed"
                    )
                }
            }
    }

    private fun saveLoginPreferences(email: String, rememberMe: Boolean, context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        if (rememberMe) {
            editor.putString("email", email)
            editor.putBoolean("remember_me", true)
        } else {
            editor.clear()
        }

        editor.apply()
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}