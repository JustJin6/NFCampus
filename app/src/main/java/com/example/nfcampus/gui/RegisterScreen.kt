package com.example.nfcampus.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.model.User
import com.example.nfcampus.repository.UserRepository
import com.example.nfcampus.gui.components.StudentCardScanStep
import com.example.nfcampus.gui.components.RegistrationFormStep
import com.example.nfcampus.gui.components.NFCSetupStep
import com.example.nfcampus.util.ScannedData
import com.example.nfcampus.viewmodel.AuthViewModel
import com.example.nfcampus.dialog.RegistrationVerificationDialog
import kotlinx.coroutines.launch

// Helper function to check if ScannedData is complete
fun ScannedData.isComplete(): Boolean {
    return fullName != "Not Found" &&
            studentId != "Not Found" &&
            identificationNumber != "Not Found" &&
            major != "Not Found" &&
            intake != "Not Found"
}

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

    var currentStep by remember { mutableStateOf(0) }
    var scannedData by remember { mutableStateOf(ScannedData()) }
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
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
                    onFormComplete = { email, password ->
                        userEmail = email
                        userPassword = password
                    },
                    onVerificationRequired = { email ->
                        userEmail = email
                        showVerificationDialog = true
                    }
                )
                2 -> NFCSetupStep(
                    onSetupComplete = { nfcUid ->
                        // Create and save user to Firestore
                        coroutineScope.launch {
                            val user = User(
                                studentId = scannedData.studentId,
                                email = userEmail,
                                password = userPassword,
                                fullName = scannedData.fullName,
                                identificationNumber = scannedData.identificationNumber,
                                major = scannedData.major,
                                intake = scannedData.intake,
                                nfcUid = nfcUid,
                                isVerified = true
                            )
                            userRepository.saveUser(user)
                            authViewModel.signOut()
                            onRegisterComplete()
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
            onVerified = {
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