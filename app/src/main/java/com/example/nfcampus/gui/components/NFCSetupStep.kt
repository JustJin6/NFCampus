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
fun NFCSetupStep(
    onSetupComplete: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Local state
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

    // Check if HCE is already active (from previous session)
    LaunchedEffect(activity) {
        activity?.let {
            val storedUid = it.getStoredCampusCardUID()
            if (storedUid != null && it.isHCEModeActive) {
                nfcUid = storedUid
                nfcDetected = true
                hceEnabled = true
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

    // Auto-redirect after successful scan
    LaunchedEffect(nfcDetected) {
        if (nfcDetected && nfcUid != null) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            if (countdown == 0) {
                onSetupComplete(nfcUid!!)
                onNavigateToLogin()
            }
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
            if (nfcDetected) {
                "Card Registered Successfully"
            } else {
                "Campus Card Setup"
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            when {
                !nfcAvailable -> "NFC is not available on this device"
                !nfcEnabled -> "NFC is disabled"
                nfcDetected -> "Your campus card has been registered successfully"
                isScanning -> "Place your campus card on the back of your phone"
                else -> "Tap 'Start' to begin"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = if (!nfcAvailable || !nfcEnabled) MaterialTheme.colorScheme.error
            else if (hceEnabled) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        when {
            !nfcAvailable -> {
                Button(
                    onClick = {
                        nfcDetected = true
                        nfcUid = "04:A3:B2:C1"
                        hceEnabled = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Use Demo Mode")
                }
            }

            !nfcEnabled -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Enable NFC")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            nfcDetected = true
                            nfcUid = "04:A3:B2:C1"
                            hceEnabled = true
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Demo Mode")
                    }
                }
            }

            !nfcDetected && !isScanning -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { isScanning = true },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Start")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            nfcDetected = true
                            nfcUid = "04:A3:B2:C1"
                            hceEnabled = true
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Demo Mode")
                    }
                }
            }

            !nfcDetected && isScanning -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 4.dp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Scanning...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            isScanning = false
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Cancel")
                    }
                }
            }

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
                            "Back to login in $countdown...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Button(
                            onClick = {
                                nfcUid?.let {
                                    onSetupComplete(it)
                                    onNavigateToLogin()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                        ) {
                            Text("Back to login")
                        }
                    }
                }
            }
        }
    }
}