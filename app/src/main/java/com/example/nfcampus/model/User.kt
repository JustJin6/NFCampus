package com.example.nfcampus.model

data class User(
    val studentId: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val identificationNumber: String = "",
    val major: String = "",
    val intake: String = "",
    val nfcUid: String? = null,
    val isVerified: Boolean = false
)