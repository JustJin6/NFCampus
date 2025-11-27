package com.example.nfcampus.gui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class CaptureImageContract : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        // This should return a valid Intent. If you are using GmsDocumentScanner,
        // you would create its intent here. Since it's asynchronous, returning an empty
        // Intent and handling logic elsewhere might be your design.
        return Intent()
    }


    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        // The result is handled asynchronously, so we return null here as per the contract.
        return null
    }
}