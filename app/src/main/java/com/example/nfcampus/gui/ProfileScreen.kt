package com.example.nfcampus.gui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nfcampus.model.User
import com.example.nfcampus.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen() {
    val userRepository = remember { UserRepository() }
    val auth = FirebaseAuth.getInstance()
    val currentUserUid = auth.currentUser?.uid
    val currentUser = remember { mutableStateOf<User?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFrontCardVisible by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserUid) {
        if (currentUserUid != null) {
            coroutineScope.launch {
                try {
                    currentUser.value = userRepository.getUserByUid(currentUserUid)
                } catch (e: Exception) {
                    errorMessage = "Failed to load user data."
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
            errorMessage = "No user is currently logged in."
        }
    }

    Scaffold { _ ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            currentUser.value?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "My Student Card",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Determine which URI to display
                    val imageUriToShow = if (isFrontCardVisible) {
                        user.frontImageUri
                    } else {
                        user.backImageUri
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .aspectRatio(0.63f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        if (imageUriToShow != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUriToShow.toUri())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = if (isFrontCardVisible) "Front of Student ID Card" else "Back of Student ID Card",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Image not available")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Switch to toggle between front and back images
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isFrontCardVisible,
                            onCheckedChange = { isFrontCardVisible = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Front", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}