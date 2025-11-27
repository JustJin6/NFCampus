package com.example.nfcampus.gui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.nfcampus.ui.theme.NFCampusTheme
import kotlin.apply

class ScannerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SCANNED_TEXT = "scanned_text"
        const val EXTRA_IMAGE_URI = "image_uri"
        internal const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val TAG = "ScannerActivity"
        const val EXTRA_PERFORM_OCR = "perform_ocr"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val performOcr = intent.getBooleanExtra(EXTRA_PERFORM_OCR, true) // Default to true
        setContent {
            NFCampusTheme {
                // Use the full-featured CardScannerScreen here
                CardScannerScreen(
                    onScanComplete = { text, uri ->
                        // When the scan is complete, finish with the results
                        finishWithResult(text, uri)
                    },
                    onCancelled = {
                        // If the user presses back, cancel the operation
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    performOcr = performOcr
                )
            }
        }
    }

    private fun finishWithResult(text: String?, uri: Uri?) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_SCANNED_TEXT, text)
            putExtra(EXTRA_IMAGE_URI, uri)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}