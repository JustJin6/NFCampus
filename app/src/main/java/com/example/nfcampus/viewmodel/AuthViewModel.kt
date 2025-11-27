package com.example.nfcampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String = "") : AuthState()
    data class Error(val message: String) : AuthState()
    data class RequiresVerification(val email: String) : AuthState()
    data class Verified(val email: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private var lastResendTimestamp = 0L
    private val RESEND_COOLDOWN_MS = 30_000L // 30 sec
    val remainingCooldown = MutableStateFlow(0L)

    fun register(email: String, password: String) {
        _state.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { createTask ->
                if (createTask.isSuccessful) {
                    _state.value = AuthState.RequiresVerification(email)
                    sendVerificationEmail(shouldNotifyState = false)
                } else {
                    _state.value = AuthState.Error(
                        createTask.exception?.localizedMessage ?: "Registration failed"
                    )
                }
            }
    }

    fun login(email: String, password: String) {
        _state.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { loginTask ->
                if (loginTask.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        _state.value = AuthState.RequiresVerification(user.email ?: email)
                        sendVerificationEmail(shouldNotifyState = false)
                    } else {
                        _state.value = AuthState.Error("User not found")
                    }
                } else {
                    val exception = loginTask.exception
                    val errorMessage = exception?.localizedMessage ?: "Login failed"
                    _state.value = AuthState.Error(errorMessage)
                }
            }
    }

    fun confirmLoginAfterVerification(email: String, password: String) {
        _state.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { loginTask ->
                if (loginTask.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        user.reload().addOnCompleteListener { reloadTask ->
                            if (reloadTask.isSuccessful) {
                                _state.value = AuthState.Success("Login successful")
                            } else {
                                _state.value = AuthState.Error("Failed to verify user status")
                            }
                        }
                    } else {
                        _state.value = AuthState.Error("User not found after verification")
                    }
                } else {
                    _state.value = AuthState.Error(
                        loginTask.exception?.localizedMessage ?: "Login failed after verification"
                    )
                }
            }
    }

    private fun sendVerificationEmail(shouldNotifyState: Boolean = false) {
        val user = auth.currentUser ?: run {
            if (shouldNotifyState) _state.value = AuthState.Error("User not signed in")
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastResendTimestamp < RESEND_COOLDOWN_MS) {
            if (shouldNotifyState)
                _state.value = AuthState.Error("Please wait before resending the verification email.")
            return
        }

        lastResendTimestamp = now
        startCooldownTimer()

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (shouldNotifyState)
                        _state.value = AuthState.RequiresVerification(user.email ?: "")
                } else {
                    val msg = task.exception?.localizedMessage ?: "Failed to send verification email"
                    if (msg.contains("blocked all requests", ignoreCase = true)) {
                        _state.value = AuthState.Error("Too many requests. Try again later.")
                    } else {
                        _state.value = AuthState.Error(msg)
                    }
                }
            }
    }

    fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user == null) {
            _state.value = AuthState.Error("No signed-in user.")
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastResendTimestamp < RESEND_COOLDOWN_MS) {
            _state.value = AuthState.Error("Please wait before resending.")
            return
        }

        lastResendTimestamp = now
        startCooldownTimer()

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = AuthState.RequiresVerification(user.email ?: "")
                } else {
                    val msg = task.exception?.localizedMessage ?: "Failed to send verification email"
                    _state.value = AuthState.Error(msg)
                }
            }
    }

    fun checkIfEmailVerified() {
        val user = auth.currentUser
        if (user == null) {
            _state.value = AuthState.Error("No user to check")
            return
        }

        _state.value = AuthState.Loading
        user.reload().addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                if (user.isEmailVerified) {
                    _state.value = AuthState.Verified(user.email ?: "")
                } else {
                    _state.value = AuthState.Error("Email not verified yet.")
                }
            } else {
                _state.value = AuthState.Error(
                    reloadTask.exception?.localizedMessage ?: "Failed to reload user"
                )
            }
        }
    }

    private fun startCooldownTimer() {
        viewModelScope.launch {
            val end = System.currentTimeMillis() + RESEND_COOLDOWN_MS
            while (System.currentTimeMillis() < end) {
                remainingCooldown.value = end - System.currentTimeMillis()
                delay(1000)
            }
            remainingCooldown.value = 0L
        }
    }

    fun signOut() {
        auth.signOut()
        _state.value = AuthState.Idle
    }
}