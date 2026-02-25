package com.example.nfcampus.gui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

fun generateQRCodeBitmap(data: String): ImageBitmap {
    val size = 1020 // Define the size of the QR code image

    // Set up QR code hints
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
        EncodeHintType.MARGIN to 1 // Set a small margin
    )

    val bitMatrix = QRCodeWriter().encode(
        data,
        BarcodeFormat.QR_CODE,
        size,
        size,
        hints
    )

    // Create a Bitmap from the BitMatrix
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }

    // Convert the Android Bitmap to a Compose ImageBitmap
    return bitmap.asImageBitmap()
}