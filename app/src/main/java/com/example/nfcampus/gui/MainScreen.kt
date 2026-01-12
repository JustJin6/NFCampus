package com.example.nfcampus.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.gui.about.PrivacyPolicyScreen
import com.example.nfcampus.gui.about.TermsOfServiceScreen
import com.example.nfcampus.gui.access_NFC.NFCTroubleshootingScreen
import com.example.nfcampus.gui.account_security.ChangeEmailPasswordScreen
import com.example.nfcampus.gui.account_security.EmailChangeScreen
import com.example.nfcampus.gui.account_security.PasswordChangeScreen
import com.example.nfcampus.viewmodel.EmailChangeViewModel
import com.example.nfcampus.viewmodel.PasswordChangeViewModel
import com.example.nfcampus.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object NFCTroubleshooting : Screen("nfc_troubleshooting", "NFC Troubleshooting", null)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            Screen.NFCTroubleshooting -> "Settings"
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
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                        else -> null
                    }
                },
                actions = {
                    // Show logout button only on main screens
                    when (currentScreen) {
                        Screen.Home, Screen.Profile, Screen.Settings -> {
                            IconButton(onClick = {
                                FirebaseAuth.getInstance().signOut()
                                onLogout()
                            }) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout")
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
                        emailChangeViewModel.resetState()
                        currentScreen = Screen.ChangeEmailPassword
                    }
                )
                Screen.ChangePassword -> PasswordChangeScreen(
                    viewModel = passwordChangeViewModel,
                    onSuccess = {
                        passwordChangeViewModel.resetState()
                        currentScreen = Screen.ChangeEmailPassword
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
    val currentUserEmail = auth.currentUser?.email
    val currentUser = remember { mutableStateOf<com.example.nfcampus.model.User?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch current user data from Firestore
    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail != null) {
            coroutineScope.launch {
                currentUser.value = userRepository.getUserByEmail(currentUserEmail)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                Text("Your Information", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow("Name", currentUser.value?.fullName ?: "Loading...")
                InfoRow("Student ID", currentUser.value?.studentId ?: "Loading...")
                InfoRow("Email", currentUser.value?.email ?: "Loading...")
                InfoRow("Major", currentUser.value?.major ?: "Loading...")
                InfoRow("Card UID", currentUser.value?.nfcUid ?: "Not registered")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions (optional - can remove)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Quick Actions", style = MaterialTheme.typography.headlineSmall)

            ActionButton(
                text = "Refresh Data",
                icon = Icons.Default.Refresh,
                onClick = { /* Refresh user data */ }
            )
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
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f, fill = false),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}