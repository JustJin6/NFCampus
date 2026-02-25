package com.example.nfcampus.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.MainActivity
import kotlinx.coroutines.delay

@Composable
fun UpdateNFCSetupStep(
    onUpdateComplete: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    var nfcDetected by remember { mutableStateOf(false) }
    var nfcUid by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }
    var hceEnabled by remember { mutableStateOf(false) }

    // Check NFC availability
    val nfcAdapter = remember { android.nfc.NfcAdapter.getDefaultAdapter(context) }
    val nfcAvailable = nfcAdapter != null
    val nfcEnabled = nfcAdapter?.isEnabled == true

    // Enable / Disable NFC for the app
    LaunchedEffect(isScanning) {
        if (isScanning && activity != null) {
            activity.enableNFCForApp()
        } else if (activity != null) {
            activity.disableNFCForApp()
        }
    }

    // Setup NFC callback
    LaunchedEffect(activity) {
        activity?.setNFCCallback { uid ->
            if (!nfcDetected && isScanning) {
                nfcUid = uid
                nfcDetected = true
                isScanning = false

                // Store UID and enable HCE
                activity.storeCampusCardUID(uid)
                activity.enableHCEMode()
                hceEnabled = true
            }
        }
    }

    // Auto-redirect after successful scan
    LaunchedEffect(nfcDetected) {
        if (nfcDetected && nfcUid != null) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (countdown == 0) {
                nfcUid?.let { onUpdateComplete(it) }
            }
        }
    }

    // Clean up
    DisposableEffect(activity) {
        onDispose {
            activity?.clearNFCCallback()
            activity?.disableNFCForApp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (nfcDetected) Icons.Default.CheckCircle else Icons.Default.Nfc,
            contentDescription = if (nfcDetected) "Success" else "NFC",
            modifier = Modifier.size(120.dp),
            tint = when {
                nfcDetected -> MaterialTheme.colorScheme.primary
                nfcAvailable && nfcEnabled -> MaterialTheme.colorScheme.primary
                else -> Color.Gray
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            if (nfcDetected) "New Card Linked Successfully" else "Scan New NFC Card",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            when {
                !nfcAvailable -> "NFC is not available on this device"
                !nfcEnabled -> "NFC is disabled"
                nfcDetected -> "Your new campus card has been linked successfully"
                isScanning -> "Place your new campus card on the back of your phone"
                else -> "Tap 'Start' to begin scanning your new card"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = if (!nfcAvailable || !nfcEnabled) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        when {
            nfcDetected -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (countdown > 0) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Completing update in $countdown...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Button(
                            onClick = {
                                nfcUid?.let { onUpdateComplete(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                        ) {
                            Text("Complete Update")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                        ) {
                            Text("Back to Settings")
                        }
                    }
                }
            }
        }
    }
}