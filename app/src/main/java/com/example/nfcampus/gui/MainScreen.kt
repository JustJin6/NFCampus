package com.example.nfcampus.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.gui.about.PrivacyPolicyScreen
import com.example.nfcampus.gui.about.TermsOfServiceScreen
import com.example.nfcampus.gui.access_NFC.LinkedCardScreen
import com.example.nfcampus.gui.access_NFC.NFCTroubleshootingScreen
import com.example.nfcampus.gui.account_security.ChangeEmailPasswordScreen
import com.example.nfcampus.gui.account_security.EmailChangeScreen
import com.example.nfcampus.gui.account_security.PasswordChangeScreen
import com.example.nfcampus.viewmodel.EmailChangeViewModel
import com.example.nfcampus.viewmodel.PasswordChangeViewModel
import com.example.nfcampus.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.example.nfcampus.gui.components.generateQRCodeBitmap
import com.example.nfcampus.model.User
import com.example.nfcampus.repository.ActivityLogRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object NFCTroubleshooting : Screen("nfc_troubleshooting", "NFC Troubleshooting", null)
    object LinkedCard : Screen("linked_card", "Linked Card", null)
    object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", null)
    object TermsOfService : Screen("terms_of_service", "Terms of Service", null)
    object ChangeEmailPassword : Screen("change_email_password", "Change Email & Password", null)
    object ChangeEmail : Screen("change_email", "Change Email", null)
    object ChangePassword : Screen("change_password", "Change Password", null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val emailChangeViewModel = remember { EmailChangeViewModel() }
    val passwordChangeViewModel = remember { PasswordChangeViewModel() }

    val logRepository = remember { ActivityLogRepository() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            Screen.NFCTroubleshooting -> "Settings"
                            Screen.LinkedCard -> "Settings"
                            Screen.PrivacyPolicy -> "Settings"
                            Screen.TermsOfService -> "Settings"
                            Screen.ChangeEmailPassword -> "Settings"
                            Screen.ChangeEmail -> "Change Email"
                            Screen.ChangePassword -> "Change Password"
                            else -> "NFCampus"
                        }
                    )
                },
                navigationIcon = {
                    when (currentScreen) {
                        Screen.NFCTroubleshooting,
                        Screen.LinkedCard,
                        Screen.PrivacyPolicy,
                        Screen.TermsOfService,
                        Screen.ChangeEmailPassword,
                        Screen.ChangeEmail,
                        Screen.ChangePassword -> {
                            IconButton(onClick = {
                                when (currentScreen) {
                                    Screen.ChangeEmail -> {
                                        emailChangeViewModel.resetState()
                                        currentScreen = Screen.ChangeEmailPassword
                                    }
                                    Screen.ChangePassword -> {
                                        passwordChangeViewModel.resetState()
                                        currentScreen = Screen.ChangeEmailPassword
                                    }
                                    else -> currentScreen = Screen.Settings
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                        else -> null
                    }
                },
                actions = {
                    // Show logout button only on main screens
                    when (currentScreen) {
                        Screen.Home, Screen.Profile, Screen.Settings -> {
                            // Get an instance of the repository here for the logout action
                            val logRepository = remember { ActivityLogRepository() }
                            val coroutineScope = rememberCoroutineScope()

                            IconButton(onClick = {
                                coroutineScope.launch {
                                    // Get the user ID BEFORE signing out
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        logRepository.addLog(userId, "User Logged Out")
                                    }

                                    // after that, sign out and navigate
                                    FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                            }
                        }
                        else -> {}
                    }
                }
            )
        },
        bottomBar = {
            // Show bottom bar only on main screens
            when (currentScreen) {
                Screen.Home, Screen.Profile, Screen.Settings -> {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Screen.Home.icon!!, contentDescription = Screen.Home.title) },
                            label = { Text(Screen.Home.title) },
                            selected = currentScreen == Screen.Home,
                            onClick = { currentScreen = Screen.Home }
                        )
                        NavigationBarItem(
                            icon = { Icon(Screen.Profile.icon!!, contentDescription = Screen.Profile.title) },
                            label = { Text(Screen.Profile.title) },
                            selected = currentScreen == Screen.Profile,
                            onClick = { currentScreen = Screen.Profile }
                        )
                        NavigationBarItem(
                            icon = { Icon(Screen.Settings.icon!!, contentDescription = Screen.Settings.title) },
                            label = { Text(Screen.Settings.title) },
                            selected = currentScreen == Screen.Settings,
                            onClick = { currentScreen = Screen.Settings }
                        )
                    }
                }
                else -> {}
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeContent()
                Screen.Profile -> ProfileScreen()
                Screen.Settings -> SettingsScreen(
                    onNavigateToChangeEmailPassword = {
                        // Reset viewmodel states when navigating
                        emailChangeViewModel.resetState()
                        passwordChangeViewModel.resetState()
                        currentScreen = Screen.ChangeEmailPassword
                    },
                    onNavigateToNFCTroubleshooting = { currentScreen = Screen.NFCTroubleshooting },
                    onNavigateToLinkedCard = { currentScreen = Screen.LinkedCard },
                    onNavigateToPrivacyPolicy = { currentScreen = Screen.PrivacyPolicy },
                    onNavigateToTermsOfService = { currentScreen = Screen.TermsOfService }
                )
                Screen.ChangeEmailPassword -> ChangeEmailPasswordScreen(
                    onChangeEmailClick = {
                        emailChangeViewModel.resetState()
                        currentScreen = Screen.ChangeEmail
                    },
                    onChangePasswordClick = {
                        passwordChangeViewModel.resetState()
                        currentScreen = Screen.ChangePassword
                    }
                )
                Screen.ChangeEmail -> EmailChangeScreen(
                    viewModel = emailChangeViewModel,
                    onSuccess = {
                        coroutineScope.launch {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                logRepository.addLog(userId, "Email Address Changed")
                            }

                            emailChangeViewModel.resetState()
                            currentScreen = Screen.ChangeEmailPassword
                        }
                    }
                )
                Screen.ChangePassword -> PasswordChangeScreen(
                    viewModel = passwordChangeViewModel,
                    onSuccess = {
                        coroutineScope.launch {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                logRepository.addLog(userId, "Password Changed")
                            }

                            passwordChangeViewModel.resetState()
                            currentScreen = Screen.ChangeEmailPassword
                        }
                    }
                )
                Screen.LinkedCard -> LinkedCardScreen(
                    onLogoutAndNavigateToLogin = {
                        Firebase.auth.signOut()
                        onLogout()
                    }
                )
                Screen.NFCTroubleshooting -> NFCTroubleshootingScreen()
                Screen.PrivacyPolicy -> PrivacyPolicyScreen()
                Screen.TermsOfService -> TermsOfServiceScreen()
            }
        }
    }
}

@Composable
fun HomeContent() {
    val userRepository = remember { UserRepository() }
    val auth = FirebaseAuth.getInstance()
    val firebaseEmail = FirebaseAuth.getInstance().currentUser?.email
    val currentUserUid = auth.currentUser?.uid
    val currentUser = remember { mutableStateOf<User?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var lastGeneratedTime by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }

    suspend fun generateNewQRCode(user: User?) {
        isRefreshing = true
        delay(500)
        // This IP address allows the app to connect to the IP address of the computer it is running on.
        // IP needs to manually update depending on locations/Wi-Fi
        val baseUrl = "http://172.18.55.113:3000" //192.168.0.133 || 172.18.55.150

        // Get the user's unique Firebase UID.
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {

            val uniqueTimestamp = System.currentTimeMillis()

            // Construct the URL with the user's ID as a query parameter
            val urlWithParams = baseUrl.toUri()
                .buildUpon()
                .appendQueryParameter("userId", userId)
                .appendQueryParameter("t", uniqueTimestamp.toString())
                .build()
                .toString()

            // Encode the generated URL into the QR code
            qrCodeBitmap = generateQRCodeBitmap(urlWithParams)

            val sdf = SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault())
            lastGeneratedTime = "Last generated: ${sdf.format(Date())}"
        }

        isRefreshing = false
    }

    // Fetch current user data from Firestore
    LaunchedEffect(currentUserUid) { // Trigger effect when UID is available
        if (currentUserUid != null) {
            coroutineScope.launch {
                currentUser.value = userRepository.getUserByUid(currentUserUid)

                // Generate the initial QR code once user data is loaded
                if (currentUser.value != null) {
                    launch { generateNewQRCode(currentUser.value) }
                }

                if (firebaseEmail != null && firebaseEmail != currentUser.value?.email) {
                    userRepository.updateUserEmail(currentUserUid, firebaseEmail)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            "Welcome Back!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // User Info Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Your Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow("Name", currentUser.value?.fullName ?: "Loading...")
                InfoRow("Student ID", currentUser.value?.studentId ?: "Loading...")
                InfoRow("Email", currentUser.value?.email ?: "Loading...")
                InfoRow("Major", currentUser.value?.major ?: "Loading...")
                InfoRow("Card UID", currentUser.value?.nfcUid ?: "Not registered")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- QR Code Section ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Code Image
            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap!!,
                    contentDescription = "User Activity QR Code",
                    modifier = Modifier.size(200.dp)
                )
            } else {
                // Placeholder while the QR code is generated
                CircularProgressIndicator(modifier = Modifier.size(200.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Last generated timestamp
                Text(
                    text = lastGeneratedTime,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Refresh Button
                IconButton(
                    onClick = {
                        // Don't do anything if it's already refreshing
                        if (!isRefreshing) {
                            coroutineScope.launch {
                                generateNewQRCode(currentUser.value)
                            }
                        }
                    },
                ) {
                    // Show loading indicator or icon based on state
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh QR Code",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f, fill = false),
            textAlign = TextAlign.End
        )
    }
}