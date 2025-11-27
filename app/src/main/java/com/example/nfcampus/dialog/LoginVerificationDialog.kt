package com.example.nfcampus.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nfcampus.viewmodel.AuthState
import com.example.nfcampus.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginVerificationDialog(
    email: String,
    viewModel: AuthViewModel,
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val remainingCooldown by viewModel.remainingCooldown.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(screenWidth * 0.9f)
                .heightIn(min = screenHeight * 0.6f, max = screenHeight * 0.8f)
                .padding(8.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 10.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "2-Step Verification",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Warning Icon
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(70.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Message
                Text(
                    text = "This extra step shows it's really you trying to sign in.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = email,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "A verification link has been sent to your email. Please check your inbox and click the verification link to continue. Don't forget to check your spam folder. Once done, tap “I've verified.”",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Feedback
                when (state) {
                    is AuthState.Loading -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Checking verification...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                            )
                        }
                    }
                    is AuthState.Error -> {
                        Text(
                            text = (state as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            textAlign = TextAlign.Center
                        )
                    }
                    is AuthState.Verified -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Email verified successfully!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Redirecting...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {}
                }

                Spacer(Modifier.height(28.dp))

                // Buttons: I've Verified, Resend, Cancel
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Check Verification Button
                    Button(
                        onClick = { viewModel.checkIfEmailVerified() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = state !is AuthState.Loading
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("I've Verified", fontSize = 15.sp)
                    }

                    // Resend with cooldown countdown
                    OutlinedButton(
                        onClick = { viewModel.resendVerificationEmail() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = remainingCooldown <= 0 && state !is AuthState.Loading
                    ) {
                        if (remainingCooldown > 0) {
                            Text(
                                text = "Resend in ${remainingCooldown / 1000}s",
                                fontSize = 15.sp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(10.dp))
                            Text("Resend Email", fontSize = 15.sp)
                        }
                    }

                    /*// Cancel Button
                    OutlinedButton(
                        onClick = {
                            viewModel.signOut()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Cancel", fontSize = 15.sp)
                    }*/
                }
            }
        }
    }

    // React to verified state
    LaunchedEffect(state) {
        if (state is AuthState.Verified) {
            delay(1200) // Show success message for 1.2 seconds
            onVerified()
        }
    }
}