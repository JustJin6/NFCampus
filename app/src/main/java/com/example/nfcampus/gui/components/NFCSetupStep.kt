package com.example.nfcampus.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NFCSetupStep(
    onSetupComplete: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var nfcDetected by remember { mutableStateOf(false) }
    var nfcUid by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Nfc,
            contentDescription = "NFC",
            modifier = Modifier.size(120.dp),
            tint = if (nfcDetected) MaterialTheme.colorScheme.primary else Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "NFC Card Setup",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            if (nfcDetected) {
                "Card registered successfully!"
            } else {
                "Place your student card on the back of your phone"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!nfcDetected) {
            Button(
                onClick = {
                    nfcDetected = true
                    nfcUid = "04:A3:B2:C1:89:45:80"
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Simulate NFC Detection (Demo)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Make sure NFC is enabled on your device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Show detected UID
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NFC Card UID:", style = MaterialTheme.typography.labelMedium)
                    Text(
                        nfcUid ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Call onSetupComplete when user confirms
                    nfcUid?.let { uid ->
                        onSetupComplete(uid)
                    }
                    onNavigateToLogin()
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Continue to Login")
            }
        }
    }
}