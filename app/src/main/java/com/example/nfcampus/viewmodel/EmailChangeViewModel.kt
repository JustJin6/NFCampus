package com.example.nfcampus.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmailChangeViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<EmailChangeState>(EmailChangeState.Idle)
    val state: StateFlow<EmailChangeState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<EmailChangeUIState>(EmailChangeUIState.Initial)
    val uiState: StateFlow<EmailChangeUIState> = _uiState.asStateFlow()

    fun reauthenticate(currentPassword: String) {
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            _state.value = EmailChangeState.Loading

            val credential = EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = EmailChangeUIState.Reauthenticated
                        _state.value = EmailChangeState.Idle
                    } else {
                        _state.value = EmailChangeState.Error(
                            task.exception?.message ?: "Authentication failed"
                        )
                    }
                }
        } else {
            _state.value = EmailChangeState.Error("No user logged in")
        }
    }

    fun updateEmail(newEmail: String) {
        val user = auth.currentUser

        if (user != null) {
            _state.value = EmailChangeState.Loading

            user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email verification sent successfully
                        _uiState.value = EmailChangeUIState.VerificationSent
                        _state.value = EmailChangeState.Idle
                    } else {
                        _state.value = EmailChangeState.Error(
                            task.exception?.message ?: "Failed to send verification email"
                        )
                    }
                }
        } else {
            _state.value = EmailChangeState.Error("No user logged in")
        }
    }

    fun resetState() {
        _state.value = EmailChangeState.Idle
        _uiState.value = EmailChangeUIState.Initial
    }

    fun setUIState(state: EmailChangeUIState) {
        _uiState.value = state
    }
}

sealed class EmailChangeState {
    object Idle : EmailChangeState()
    object Loading : EmailChangeState()
    data class Success(val message: String) : EmailChangeState()
    data class Error(val message: String) : EmailChangeState()
}

sealed class EmailChangeUIState {
    object Initial : EmailChangeUIState()  // Show reauthentication form
    object Reauthenticated : EmailChangeUIState()  // Show new email form
    object VerificationSent : EmailChangeUIState()  // Show verification sent message
}