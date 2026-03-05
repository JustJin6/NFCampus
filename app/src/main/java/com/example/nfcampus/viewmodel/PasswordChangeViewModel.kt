package com.example.nfcampus.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PasswordChangeViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val state: StateFlow<PasswordChangeState> = _state.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            _state.value = PasswordChangeState.Loading

            val credential = EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    _state.value = PasswordChangeState.Success("Password changed successfully")
                                } else {
                                    _state.value = PasswordChangeState.Error(
                                        updateTask.exception?.message ?: "Failed to update password"
                                    )
                                }
                            }
                    } else {
                        _state.value = PasswordChangeState.Error(
                            reauthTask.exception?.message ?: "Authentication failed"
                        )
                    }
                }
        } else {
            _state.value = PasswordChangeState.Error("No user logged in")
        }
    }

    fun resetState() {
        _state.value = PasswordChangeState.Idle
    }
}

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    data class Success(val message: String) : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}