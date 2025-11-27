package com.example.nfcampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nfcampus.gui.LoginScreen
import com.example.nfcampus.gui.MainScreen
import com.example.nfcampus.gui.RegisterScreen
import com.example.nfcampus.ui.theme.NFCampusTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.*

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

        setContent {
            NFCampusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NFCampusApp()
                }
            }
        }
    }
}

@Composable
fun NFCampusApp() {
    var currentScreen by remember { mutableStateOf(getInitialScreen()) }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "main" },
            onNavigateToRegister = { currentScreen = "register" }
        )
        "register" -> RegisterScreen(
            onRegisterComplete = { currentScreen = "login" },
            onNavigateToLogin = { currentScreen = "login"}
        )
        "main" -> MainScreen(
            onLogout = {
                Firebase.auth.signOut()
                currentScreen = "login"
            }
        )
    }
}

private fun getInitialScreen(): String {
    val currentUser = Firebase.auth.currentUser
    return if (currentUser != null && currentUser.isEmailVerified) "main" else "login"
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NFCampusTheme {
        LoginScreen(
            onLoginSuccess = { },
            onNavigateToRegister = { }
        )
    }
}