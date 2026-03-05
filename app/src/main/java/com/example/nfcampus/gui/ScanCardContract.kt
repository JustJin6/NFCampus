package com.example.nfcampus.gui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class ScanCardContract : ActivityResultContract<Unit, Pair<String?, Uri?>?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, ScannerActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<String?, Uri?>? {
        // Checks the result code.
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        // Uses the constant keys defined in ScannerActivity.
        val scannedText = intent?.getStringExtra(ScannerActivity.EXTRA_SCANNED_TEXT)
        val imageUri = intent?.getParcelableExtra<Uri>(ScannerActivity.EXTRA_IMAGE_URI)

        return Pair(scannedText, imageUri)
    }
}