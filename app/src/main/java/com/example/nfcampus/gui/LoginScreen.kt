package com.example.nfcampus.gui

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nfcampus.viewmodel.AuthState
import com.example.nfcampus.viewmodel.AuthViewModel
import com.example.nfcampus.viewmodel.ForgotPasswordViewModel
import com.example.nfcampus.dialog.LoginVerificationDialog
import com.example.nfcampus.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    Log.d("DeviceDebug", "My unique ID: $androidId")

    // Access the shared preferences throughout the app
    val prefs = remember { context.getSharedPreferences("nfcampus_prefs", Context.MODE_PRIVATE) }

    // Check if there is NFC card linked?
    val isCardLinked = remember { prefs.getString("campus_card_uid", null) != null }

    // Check if there is an account already registered
    val isDeviceOccupied = isCardLinked

    val authViewModel = remember { AuthViewModel() }
    val forgotPasswordViewModel = remember { ForgotPasswordViewModel() }
    val authState by authViewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var userEmailForVerification by remember { mutableStateOf("") }
    var storedPassword by remember { mutableStateOf("") }
    var showForgotPasswordScreen by remember { mutableStateOf(false) }
    var isHardwareLocked by remember { mutableStateOf(false) }
    var showLockedDialog by remember { mutableStateOf(false) }

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

        // When the screen opens, check if this specific phone is already in use
        LaunchedEffect(Unit) {
            Log.d("DeviceDebug", "Screen Opened. ID: $androidId")
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("deviceId", androidId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        isHardwareLocked = true
                    }
                }
        }

        LaunchedEffect(isHardwareLocked, isDeviceOccupied) {
            Log.d("DeviceDebug", "Security State -> Locked: $isHardwareLocked, Occupied: $isDeviceOccupied")
        }

        //Register Button Logic
        TextButton(onClick = {
            if (isDeviceOccupied || isHardwareLocked) {
                showLockedDialog = true
            } else {
                onNavigateToRegister()
            }
        }) {
            Text("Don't have an account? Register here")
        }

        // Alert Dialog to inform the user
        if (showLockedDialog) {
            AlertDialog(
                onDismissRequest = { showLockedDialog = false },
                title = { Text("Registration Locked") },
                text = {
                    Text(
                        "This device is already associated with an account and an NFC card. " +
                                "To register a new account, the existing account and linked card must " +
                                "be terminated from the Settings menu."
                    )
                },
                confirmButton = {
                    Button(onClick = { showLockedDialog = false }) {
                        Text("OK")
                    }
                }
            )
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