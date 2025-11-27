package com.example.nfcampus.gui.components

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun String.toTitleCase(): String {
    if (this.isBlank()) return ""
    return this.lowercase().split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

fun parseTextToScannedData(text: String, frontUri: Uri?, backUri: Uri?): ScannedData? {
    val lines = text.lines().filter { it.isNotBlank() }.toMutableList()

    fun findAndRemoveIcNumber(): String {
        val icRegex = Regex("^\\d{12}$")
        var foundValue = ""
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next().trim().replace('O', '0').replace('o', '0')
            if (icRegex.matches(line)) {
                foundValue = line
                iterator.remove()
                break
            }
        }
        return foundValue
    }

    fun findAndRemove(pattern: String): String {
        val regex = Regex(pattern)
        var foundValue = ""
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next().trim()
            if (regex.matches(line)) {
                foundValue = line
                iterator.remove()
                break
            }
        }
        return foundValue
    }

    val icNumber = findAndRemoveIcNumber()
    val studentId = findAndRemove("^[A-Z]{2,}[0-9]{2,}\$")
    val intake = findAndRemove("^(January|February|March|April|May|June|July|August|September|October|November|December)\\s\\d{4}\$")

    if (studentId.isBlank() || icNumber.isBlank()) {
        Log.e("Parser", "Validation Failed: Could not find Student ID or IC Number.")
        return null
    }

    var major = ""
    val majorStartIndex = lines.indexOfFirst {
        val trimmed = it.trim()
        trimmed.contains("BSc", ignoreCase = true) ||
                trimmed.contains("Bachelor", ignoreCase = true) ||
                trimmed.contains("Diploma", ignoreCase = true)
    }
    if (majorStartIndex != -1) {
        val firstLine = lines[majorStartIndex]
        major = firstLine
        val nextLineIndex = majorStartIndex + 1
        if (nextLineIndex < lines.size && lines[nextLineIndex].trim().startsWith("(")) {
            val secondLine = lines[nextLineIndex]
            major = "$firstLine $secondLine"
            lines.removeAt(nextLineIndex)
            lines.removeAt(majorStartIndex)
        } else {
            lines.removeAt(majorStartIndex)
        }
    }

    val rawFullName = lines.firstOrNull { line ->
        val trimmedLine = line.trim()
        val normalizedLine = trimmedLine.replace("\\s".toRegex(), "").lowercase()
        val nameRegex = "^([A-Z'’]+\\s){1,4}[A-Z'’]+$".toRegex(RegexOption.IGNORE_CASE)
        nameRegex.matches(trimmedLine) &&
                !normalizedLine.contains("college") &&
                !normalizedLine.contains("campus") &&
                !normalizedLine.contains("peninsula") &&
                !normalizedLine.contains("Col Leg B") &&
                !normalizedLine.contains("COL LEG B") &&
                !normalizedLine.contains("Col Lbg E")
    }?.trim() ?: ""

    val fullName = rawFullName.toTitleCase()

    return ScannedData(
        fullName = fullName.ifEmpty { "Not Found" },
        studentId = studentId.ifEmpty { "Not Found" },
        identificationNumber = icNumber.ifEmpty { "Not Found" },
        major = major.trim().ifEmpty { "Not Found" },
        intake = intake.ifEmpty { "Not Found" },
        frontImageUri = frontUri,
        backImageUri = backUri
    )
}

@Composable
fun StudentCardScanStep(
    onScanComplete: (ScannedData) -> Unit
) {
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }
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

    val scanner = GmsDocumentScanning.getClient(options)

    // A single launcher to handle results from the Document Scanner.
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                val croppedImageUri = scanningResult?.pages?.firstOrNull()?.imageUri

                if (croppedImageUri != null) {
                    if (isScanningFront) {
                        // Logic for FRONT card scan: Perform OCR
                        coroutineScope.launch {
                            try {
                                val image = InputImage.fromFilePath(context, croppedImageUri)
                                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                val visionText = textRecognizer.process(image).await()

                                val validatedData =
                                    parseTextToScannedData(
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
                                Log.e("StudentCardScanStep", "OCR failed", e)
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
        scanner.getStartScanIntent(context as Activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Log.e("StudentCardScanStep", "Failed to start scanner", it)
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
        Text(
            "Scan Front of Card",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Front Card Scan
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(350.dp)
                //.fillMaxWidth()
                //.height(200.dp)
                .clickable {
                    startScanner(true)
                },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (frontImageUri != null) {
                    AsyncImage(
                        model = frontImageUri,
                        contentDescription = "Front of Student Card",
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
        Text("Scan Back of Card", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Back Card Scan
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(350.dp)
                //.fillMaxWidth()
                //.height(200.dp)
                .clickable(enabled = frontImageUri != null) {
                    startScanner(false)
                },
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
                        contentDescription = "Back of Student Card",
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
            Text("Proceed to Register")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}