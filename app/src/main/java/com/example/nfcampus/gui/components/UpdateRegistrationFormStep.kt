package com.example.nfcampus.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nfcampus.gui.isComplete
import com.example.nfcampus.model.User
import com.example.nfcampus.util.ScannedData

@Composable
fun UpdateRegistrationFormStep(
    scannedData: ScannedData,
    existingUserData: User?,
    onVerificationComplete: () -> Unit,
    onBack: () -> Unit
) {
    var verificationMatch by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Automatically verify if scanned data matches existing user
    LaunchedEffect(scannedData, existingUserData) {
        if (scannedData.isComplete() && existingUserData != null) {
            // Verify student identification number matches
            scannedData.identificationNumber == existingUserData.identificationNumber

            if (!verificationMatch) {
                errorMessage = "Scanned card does not match your account. Please scan the correct card."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Verify Card Details",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Please verify that the scanned card details match your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (errorMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Scanned card details (read-only)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Scanned Card Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                DetailRow("Full Name", scannedData.fullName)
                DetailRow("Student ID", scannedData.studentId)
                DetailRow("Identification Number", scannedData.identificationNumber)
                DetailRow("Major/Course", scannedData.major)
                DetailRow("Intake", scannedData.intake)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verification confirmation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (verificationMatch) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    if (verificationMatch) "Card Verified Successfully"
                    else "Verification Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (verificationMatch) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    if (verificationMatch) "Scanned card matches your account. You can proceed to NFC setup."
                    else "Please ensure the scanned card belongs to your account.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Proceed button
        Button(
            onClick = onVerificationComplete,
            modifier = Modifier.fillMaxWidth(),
            enabled = verificationMatch
        ) {
            Text("Proceed to NFC")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}