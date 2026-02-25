package com.example.nfcampus.gui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nfcampus.util.ScannedData
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun UpdateStudentCardScanStep(
    onScanComplete: (ScannedData) -> Unit
) {
    var frontImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var backImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var parsedData by remember { mutableStateOf<ScannedData?>(null) }
    var showScanErrorDialog by remember { mutableStateOf(false) }

    // This state tracks whether we are currently scanning the front or back card.
    var isScanningFront by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Configure the single, unified Document Scanner for both cards.
    val options = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setGalleryImportAllowed(false)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .build()

    val scanner = com.google.mlkit.vision.documentscanner.GmsDocumentScanning.getClient(options)

    // A single launcher to handle results from the Document Scanner.
    val scannerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val scanningResult = com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                val croppedImageUri = scanningResult?.pages?.firstOrNull()?.imageUri

                if (croppedImageUri != null) {
                    if (isScanningFront) {
                        // Logic for FRONT card scan: Perform OCR
                        coroutineScope.launch {
                            try {
                                val image = com.google.mlkit.vision.common.InputImage.fromFilePath(context, croppedImageUri)
                                val textRecognizer = com.google.mlkit.vision.text.TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
                                val visionText = textRecognizer.process(image).await()

                                val validatedData = parseTextToScannedData(
                                    visionText.text,
                                    croppedImageUri,
                                    null
                                )
                                if (validatedData != null) {
                                    parsedData = validatedData
                                    frontImageUri = croppedImageUri
                                } else {
                                    showScanErrorDialog = true
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("UpdateStudentCardScanStep", "OCR failed", e)
                                showScanErrorDialog = true
                            }
                        }
                    } else {
                        // Logic for BACK card scan: Just save the image URI
                        backImageUri = croppedImageUri
                    }
                }
            }
        }
    )

    // Function to start the scanner, setting the correct flag first.
    val startScanner = { isFront: Boolean ->
        isScanningFront = isFront
        scanner.getStartScanIntent(context as android.app.Activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                android.util.Log.e("UpdateStudentCardScanStep", "Failed to start scanner", it)
            }
    }

    if (showScanErrorDialog) {
        AlertDialog(
            onDismissRequest = { showScanErrorDialog = false },
            title = { Text("Scan Unsuccessful") },
            text = { Text("Could not read card details. Please ensure the card is clear, flat, and well-lit.") },
            confirmButton = {
                Button(onClick = { showScanErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Front Card ---
        Text("Scan Front of New Card", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Front Card Scan
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(350.dp)
                .clickable { startScanner(true) },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (frontImageUri != null) {
                    AsyncImage(
                        model = frontImageUri,
                        contentDescription = "Front of New Student Card",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, "Scan", Modifier.size(48.dp))
                        Text("Tap to Scan")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Back Card ---
        Text("Scan Back of New Card", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Back Card Scan
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(350.dp)
                .clickable(enabled = frontImageUri != null) { startScanner(false) },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (frontImageUri == null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (backImageUri != null) {
                    AsyncImage(
                        model = backImageUri,
                        contentDescription = "Back of New Student Card",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, "Scan", Modifier.size(48.dp))
                        Text(if (frontImageUri != null) "Tap to Scan" else "Scan Front First")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Proceed Button ---
        Button(
            onClick = {
                val finalData = parsedData?.copy(backImageUri = backImageUri)
                if (finalData != null) {
                    onScanComplete(finalData)
                }
            },
            enabled = frontImageUri != null && backImageUri != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Verify Card Details")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}