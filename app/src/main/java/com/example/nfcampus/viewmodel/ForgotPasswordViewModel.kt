package com.example.nfcampus.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ForgotPasswordViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun sendPasswordResetEmail(email: String) {
        _state.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = AuthState.Success("Password reset email sent")
                } else {
                    _state.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Failed to send reset email"
                    )
                }
            }
    }

    fun clearState() {
        _state.value = AuthState.Idle
    }
}