package com.example.nfcampus.gui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nfcampus.viewmodel.AuthState
import com.example.nfcampus.viewmodel.AuthViewModel
import com.example.nfcampus.viewmodel.ForgotPasswordViewModel
import com.example.nfcampus.dialog.LoginVerificationDialog
import com.example.nfcampus.R
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val authViewModel = remember { AuthViewModel() }
    val forgotPasswordViewModel = remember { ForgotPasswordViewModel() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var userEmailForVerification by remember { mutableStateOf("") }
    var storedPassword by remember { mutableStateOf("") }
    var showForgotPasswordScreen by remember { mutableStateOf(false) }

    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                isLoading = false
                showVerificationDialog = false
                onLoginSuccess()
            }
            is AuthState.Verified -> {
                isLoading = true
                authViewModel.confirmLoginAfterVerification(userEmailForVerification, storedPassword)
            }
            is AuthState.RequiresVerification -> {
                isLoading = false
                showVerificationDialog = true
                userEmailForVerification = (authState as AuthState.RequiresVerification).email
            }
            is AuthState.Error -> {
                isLoading = false
                val errorState = authState as AuthState.Error
                errorMessage = errorState.message
            }
            is AuthState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }

    // Forgot Password Flow
    if (showForgotPasswordScreen) {
        ForgotPasswordScreen(
            forgotPasswordViewModel = forgotPasswordViewModel,
            onBackToLogin = {
                showForgotPasswordScreen = false
                forgotPasswordViewModel.clearState()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_nfcampus),
            contentDescription = "NFCampus Logo",
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(50.dp))
                .padding(bottom = 8.dp)
        )

        Text(
            text = "NFCampus",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = errorMessage.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage.isNotEmpty()
        )

        // Forgot Password TextButton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    showForgotPasswordScreen = true
                }
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = ""
                userEmailForVerification = email
                storedPassword = password
                authViewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logging in...")
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register here")
        }
    }

    // Email Verification Dialog for login flow
    if (showVerificationDialog) {
        LoginVerificationDialog(
            email = userEmailForVerification,
            viewModel = authViewModel,
            onVerified = {
                // This will be handled by AuthState.Verified above
            },
            onDismiss = {
                showVerificationDialog = false
                isLoading = false
                authViewModel.signOut()
            }
        )
    }
}