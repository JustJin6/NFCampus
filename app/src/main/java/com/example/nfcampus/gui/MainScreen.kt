package com.example.nfcampus.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFCampus") },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Welcome to NFCampus!",
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
                    Text("Student Information", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow("Name", currentUser.value?.fullName ?: "Loading...")
                    InfoRow("Student ID", currentUser.value?.studentId ?: "Loading...")
                    InfoRow("Email", currentUser.value?.email ?: "Loading...")
                    InfoRow("Major", currentUser.value?.major ?: "Loading...")
                    InfoRow("NFC UID", currentUser.value?.nfcUid ?: "Not registered")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionButton(
                    text = "Home",
                    icon = Icons.Default.Home,
                    onClick = { /* Handle home */ }
                )

                ActionButton(
                    text = "Profile",
                    icon = Icons.Default.Person,
                    onClick = { /* Handle profile */ }
                )

                ActionButton(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = { /* Handle settings */ }
                )
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
fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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