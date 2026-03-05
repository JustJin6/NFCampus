package com.example.nfcampus.util

import android.net.Uri

data class ScannedData(
    val fullName: String = "",
    val studentId: String = "",
    val identificationNumber: String = "",
    val major: String = "",
    val intake: String = "",
    val frontImageUri: Uri? = null,
    val backImageUri: Uri? = null
)