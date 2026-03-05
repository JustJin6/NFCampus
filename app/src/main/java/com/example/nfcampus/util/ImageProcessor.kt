package com.example.nfcampus.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Converts a given image URI into a high-contrast grayscale image,
 *
 * @param context The application context.
 * @param originalUri The URI of the original image to process.
 * @return The URI of the newly created grayscale image.
 */
fun processImageForOcr(context: Context, originalUri: Uri): Uri {
    // 1. Decode the original image URI into a Bitmap
    val inputStream = context.contentResolver.openInputStream(originalUri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    val width = originalBitmap.width
    val height = originalBitmap.height
    val pixels = IntArray(width * height)
    originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    // 2. Apply a Grayscale filter
    for (i in pixels.indices) {
        val p = pixels[i]
        val r = p.red
        val g = p.green
        val b = p.blue

        // Use the standard luminosity method for grayscaling
        val gray = (r * 0.299 + g * 0.587 + b * 0.114).toInt()

        // Create a new pixel with the same gray value for R, G, and B
        pixels[i] = Color.rgb(gray, gray, gray)
    }

    // 3. Create a new Bitmap with the grayscaled pixels
    val processedBitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

    // 4. Save the new Bitmap to a temporary file and get its URI
    val file = File(context.cacheDir, "processed_ocr_image_${System.currentTimeMillis()}.jpg")
    val fileOutputStream = FileOutputStream(file)
    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
    fileOutputStream.flush()
    fileOutputStream.close()

    return Uri.fromFile(file)
}