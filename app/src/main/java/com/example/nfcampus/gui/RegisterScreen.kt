package com.example.nfcampus.gui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.nfcampus.model.User
import com.example.nfcampus.repository.UserRepository
import com.example.nfcampus.gui.components.StudentCardScanStep
import com.example.nfcampus.gui.components.RegistrationFormStep
import com.example.nfcampus.gui.components.NFCSetupStep
import com.example.nfcampus.util.ScannedData
import com.example.nfcampus.viewmodel.AuthViewModel
import com.example.nfcampus.dialog.RegistrationVerificationDialog
import com.example.nfcampus.repository.ActivityLogRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// Helper function to check if ScannedData is complete
fun ScannedData.isComplete(): Boolean {
    return fullName != "Not Found" &&
            studentId != "Not Found" &&
            identificationNumber != "Not Found" &&
            major != "Not Found" &&
            intake != "Not Found"
}

@SuppressLint("HardwareIds")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterComplete: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }
    val authViewModel = remember { AuthViewModel() }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(0) }
    var scannedData by remember { mutableStateOf(ScannedData()) }
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var showVerificationDialog by remember { mutableStateOf(false) }

    // State to control the error dialog
    var showIncompleteScanDialog by remember { mutableStateOf(false) }

    if (showIncompleteScanDialog) {
        IncompleteScanDialog { showIncompleteScanDialog = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registration") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) {
                            currentStep--
                        } else {
                            onNavigateToLogin()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Scan Card", "Register", "NFC").forEachIndexed { index, step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (index <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                color = if (index <= currentStep) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = step,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            when (currentStep) {
                0 -> StudentCardScanStep(
                    onScanComplete = { data ->
                        if (data.isComplete()) {
                            scannedData = data
                            currentStep++
                        } else {
                            showIncompleteScanDialog = true
                        }
                    }
                )
                1 -> RegistrationFormStep(
                    scannedData = scannedData,
                    authViewModel = authViewModel,
                    onFormComplete = { email, password, uid ->
                        userEmail = email
                        userPassword = password
                        userId = uid
                    },
                    onVerificationRequired = { email, uid ->
                        userEmail = email
                        userId = uid
                        showVerificationDialog = true
                    }
                )
                2 -> NFCSetupStep(
                    onSetupComplete = { nfcUid ->
                        // Store UID in SharedPreferences
                        val sharedPrefs = context.getSharedPreferences("nfcampus_prefs", Context.MODE_PRIVATE)
                        sharedPrefs.edit {
                            putString("campus_card_uid", nfcUid.replace(":", "").uppercase())
                        }

                        val androidId = android.provider.Settings.Secure.getString(
                            context.contentResolver,
                            android.provider.Settings.Secure.ANDROID_ID
                        )

                        // Create and save user to Firestore
                        coroutineScope.launch {

                            val user = User(
                                uid = userId,
                                deviceId = androidId,
                                studentId = scannedData.studentId,
                                email = userEmail,
                                password = userPassword,
                                fullName = scannedData.fullName,
                                identificationNumber = scannedData.identificationNumber,
                                major = scannedData.major,
                                intake = scannedData.intake,
                                nfcUid = nfcUid,
                                isVerified = true,
                                frontImageUri = scannedData.frontImageUri?.toString(),
                                backImageUri = scannedData.backImageUri?.toString()
                            )

                            Log.d("RegisterDebug", "Saving user: $userId to Firestore")

                            // Use a listener to ensure the data is saved before signing out
                            FirebaseFirestore.getInstance().collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Log.d("RegisterDebug", "User successfully saved to Firestore")

                                    // Now that user is saved, add the log
                                    val logRepository = ActivityLogRepository()
                                    logRepository.addLog(userId, "User Registered")

                                    // and now sign out
                                    authViewModel.signOut()
                                    onRegisterComplete()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("RegisterDebug", "Failed to save user: ${e.message}")

                                }
                        }
                    },
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }

    // Email Verification Dialog for Registration
    if (showVerificationDialog) {
        RegistrationVerificationDialog(
            email = userEmail,
            viewModel = authViewModel,
            uid = userId,
            onVerified = { verifiedUid ->
                // Email verified, proceed to NFC setup
                showVerificationDialog = false
                currentStep = 2
            },
            onDismiss = {
                showVerificationDialog = false
                // Optionally go back to registration form
                currentStep = 1
            }
        )
    }
}

//Error Message when not all card details are present
@Composable
private fun IncompleteScanDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Unsuccessful") },
        text = { Text("Could not read all required details from the card. Please try again in a well-lit area.") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}