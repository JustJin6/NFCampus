package com.example.nfcampus.gui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.gui.components.UpdateStudentCardScanStep
import com.example.nfcampus.gui.components.UpdateRegistrationFormStep
import com.example.nfcampus.gui.components.UpdateNFCSetupStep
import com.example.nfcampus.model.User
import com.example.nfcampus.repository.UserRepository
import com.example.nfcampus.util.ScannedData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UpdateScreen(
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onUpdateComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUserUid = auth.currentUser?.uid
    val userRepository = remember { UserRepository() }
    val coroutineScope = rememberCoroutineScope()

    var scannedData by remember { mutableStateOf(ScannedData()) }
    var existingUserData by remember { mutableStateOf<User?>(null) }
    var showIncompleteScanDialog by remember { mutableStateOf(false) }
    var showVerificationError by remember { mutableStateOf(false) }

    // Load existing user data
    LaunchedEffect(currentUserUid) {
        if (currentUserUid != null) {
            existingUserData = userRepository.getUserByUid(currentUserUid)
        }
    }

    if (showIncompleteScanDialog) {
        IncompleteScanDialog { showIncompleteScanDialog = false }
    }

    Scaffold { _ ->
        Column {
            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Scan Card", "Verify", "NFC").forEachIndexed { index, step ->
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
                0 -> UpdateStudentCardScanStep(
                    onScanComplete = { data ->
                        if (data.isComplete()) {
                            scannedData = data
                            onStepChange(currentStep + 1)
                        } else {
                            showIncompleteScanDialog = true
                        }
                    }
                )
                1 -> UpdateRegistrationFormStep(
                    scannedData = scannedData,
                    existingUserData = existingUserData,
                    onVerificationComplete = {
                        // Check if identification number matches
                        if (scannedData.identificationNumber == existingUserData?.identificationNumber) {
                            onStepChange(currentStep + 1)
                        } else {
                            showVerificationError = true
                        }
                    },
                    onBack = { onStepChange(currentStep - 1) }
                )
                2 -> UpdateNFCSetupStep(
                    onUpdateComplete = { newNfcUid ->
                        // Update NFC UID in Firestore
                        coroutineScope.launch {
                            currentUserUid?.let { uid ->
                                existingUserData?.let { user ->
                                    val updatedUser = user.copy(
                                        nfcUid = newNfcUid,
                                        fullName = scannedData.fullName,
                                        studentId = scannedData.studentId,
                                        identificationNumber = scannedData.identificationNumber,
                                        major = scannedData.major,
                                        intake = scannedData.intake,
                                        frontImageUri = scannedData.frontImageUri?.toString(),
                                        backImageUri = scannedData.backImageUri?.toString()
                                    )
                                    userRepository.saveUser(updatedUser)
                                    onUpdateComplete()
                                }
                            }
                        }
                    },
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }

    if (showVerificationError) {
        AlertDialog(
            onDismissRequest = { showVerificationError = false },
            title = { Text("Verification Failed") },
            text = { Text("The scanned card does not match your account. Please scan the correct card.") },
            confirmButton = {
                Button(onClick = {
                    showVerificationError = false
                    onStepChange(0) // Go back to scan step
                }) {
                    Text("Try Again")
                }
            }
        )
    }
}

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