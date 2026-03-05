package com.example.nfcampus.gui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

// Define the frame's relative dimensions
private const val FRAME_LEFT_PERCENT = 0.15f
private const val FRAME_RIGHT_PERCENT = 0.85f
private const val FRAME_TOP_PERCENT = 0.20f
private const val FRAME_BOTTOM_PERCENT = 0.80f
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CardScannerScreen(
    onScanComplete: (String, Uri?) -> Unit,
    onCancelled: () -> Unit,
    performOcr: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    // State for loading indicator
    var isLoading by remember { mutableStateOf(false) }
    var previewSize by remember { mutableStateOf(Size.Zero)}
    // Request permission on first composition
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Student Card") },
                navigationIcon = {
                    IconButton(onClick = onCancelled) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black // Set background to black for better overlay visibility
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onSizeChanged { previewSize = Size(it.width.toFloat(), it.height.toFloat()) }
        ) {
            if (cameraPermissionState.status.isGranted) {
                // only show the camera and button if permission is granted
                val imageCapture = remember { ImageCapture.Builder().build() }

                CameraView(
                    imageCapture = imageCapture,
                    lifecycleOwner = lifecycleOwner
                )

                // Camera Overlay to guide the user
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .drawWithContent {
                            drawContent()

                            // Define the cutout rectangle for the card
                            val cutoutRect = Rect(
                                left = size.width * FRAME_LEFT_PERCENT,
                                right = size.width * FRAME_RIGHT_PERCENT,
                                top = size.height * FRAME_TOP_PERCENT,
                                bottom = size.height * FRAME_BOTTOM_PERCENT
                            )

                            // Draw a rounded rectangle cutout
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = cutoutRect.topLeft,
                                size = cutoutRect.size,
                                cornerRadius = CornerRadius(48f, 48f),
                                blendMode = BlendMode.Clear // This makes the cutout transparent
                            )

                            // Draw a border around the cutout
                            drawRoundRect(
                                color = Color.White,
                                topLeft = cutoutRect.topLeft,
                                size = cutoutRect.size,
                                cornerRadius = CornerRadius(48f, 48f),
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                )

                // Instructional text
                Text(
                    text = "Position card inside the frame",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                )

                FloatingActionButton(
                    onClick = {
                        if (!isLoading) { // Prevent multiple clicks
                            isLoading = true
                            takePictureAndCrop(
                                context = context,
                                imageCapture = imageCapture,
                                previewSize = previewSize,
                                onImageSaved = { text, uri ->
                                    isLoading = false
                                    onScanComplete(text, uri)
                                },
                                onError = {
                                    isLoading = false
                                    // Optionally show an error to the user
                                    Log.e("CardScannerScreen", "Image capture failed", it)
                                    onScanComplete("", null) // Complete with no data on error
                                },
                                performOcr = performOcr
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
                }

            } else {
                // Show a message if permission is not granted
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera Permission is required.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }

            // Central loading indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun CameraView(
    imageCapture: ImageCapture,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize()) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Build the Preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // UNBIND all previous use cases before rebinding
                cameraProvider.unbindAll()

                // BIND ONLY the necessary use cases: Preview and ImageCapture
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CardScanner", "CameraX Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

private fun takePictureAndCrop(
    context: Context,
    imageCapture: ImageCapture,
    previewSize: Size,
    onImageSaved: (String, Uri) -> Unit, // Callback will expects text and Uri
    onError: (Exception) -> Unit,
    performOcr: Boolean
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    // --- Cropping Logic ---
                    val rotationDegrees = image.imageInfo.rotationDegrees
                    val bitmap = image.toBitmap()

                    // Rotate the bitmap to be upright
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    // Get the dimensions of the full-resolution, rotated image.
                    val imageWidth = rotatedBitmap.width.toFloat()
                    val imageHeight = rotatedBitmap.height.toFloat()

                    // Get the dimensions of the preview box on the screen.
                    val previewWidth = previewSize.width
                    val previewHeight = previewSize.height

                    // The camera feed is scaled to fit the preview, either by width or height (letterboxed/pillarboxed).
                    // We need to find that scale factor.
                    val scaleX = imageWidth / previewWidth
                    val scaleY = imageHeight / previewHeight

                    // The 'min' scale factor determines how the preview is fitted inside the full image.
                    val scale = minOf(scaleX, scaleY)

                    // With this scale, we can calculate the size of the 'virtual' preview inside the full image.
                    val scaledPreviewWidth = previewWidth * scale
                    val scaledPreviewHeight = previewHeight * scale

                    // We now center this virtual preview within the full image to find its top-left corner.
                    val offsetX = (imageWidth - scaledPreviewWidth) / 2
                    val offsetY = (imageHeight - scaledPreviewHeight) / 2

                    // Now, calculate the crop rectangle *relative to the virtual preview*.
                    val cropLeft = scaledPreviewWidth * FRAME_LEFT_PERCENT
                    val cropTop = scaledPreviewHeight * FRAME_TOP_PERCENT
                    val cropWidth = scaledPreviewWidth * (FRAME_RIGHT_PERCENT - FRAME_LEFT_PERCENT)
                    val cropHeight = scaledPreviewHeight * (FRAME_BOTTOM_PERCENT - FRAME_TOP_PERCENT)

                    // Finally, create the cropped bitmap by adding the offsets.
                    val croppedBitmap = Bitmap.createBitmap(
                        rotatedBitmap,
                        (cropLeft + offsetX).toInt(),
                        (cropTop + offsetY).toInt(),
                        cropWidth.toInt(),
                        cropHeight.toInt()
                    )

                    // --- Save the Cropped Bitmap ---
                    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + "_cropped.jpg"
                    val file = File(context.cacheDir, name)
                    FileOutputStream(file).use { out ->
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                    val savedUri = Uri.fromFile(file)

                    // --- Perform OCR or finish ---
                    if (performOcr) {
                        val ocrImage = InputImage.fromFilePath(context, savedUri)
                        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                            .process(ocrImage)
                            .addOnSuccessListener { visionText -> onImageSaved(visionText.text, savedUri) }
                            .addOnFailureListener { e ->
                                Log.e("CardScannerScreen", "OCR Failed", e)
                                onImageSaved("", savedUri) // Still return the image on OCR fail
                            }
                    } else {
                        onImageSaved("", savedUri)
                    }
                } catch (e: Exception) {
                    onError(e)
                } finally {
                    image.close() // IMPORTANT: Always close the ImageProxy
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}