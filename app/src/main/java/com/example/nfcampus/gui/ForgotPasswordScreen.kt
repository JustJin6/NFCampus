package com.example.nfcampus.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nfcampus.viewmodel.*
import com.example.nfcampus.R
import kotlinx.coroutines.delay
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    forgotPasswordViewModel: ForgotPasswordViewModel,
    onBackToLogin: () -> Unit
) {
    var currentStep by remember { mutableStateOf(ForgotPasswordStep.INPUT_EMAIL) }
    var email by remember { mutableStateOf("") }

    val authState by forgotPasswordViewModel.state.collectAsState()

    /** When success sending email */
    LaunchedEffect(authState) {
        if (authState is AuthState.Success && currentStep == ForgotPasswordStep.INPUT_EMAIL) {
            currentStep = ForgotPasswordStep.EMAIL_SENT
            forgotPasswordViewModel.clearState()
        }
    }

    // Reset the state when entering the screen
    LaunchedEffect(Unit) {
        forgotPasswordViewModel.clearState()
    }

    Scaffold(
        topBar = {
            if (currentStep != ForgotPasswordStep.SUCCESS) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Reset Password",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackToLogin) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to login")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                ForgotPasswordStep.INPUT_EMAIL ->
                    InputEmailStep(
                        email = email,
                        onEmailChange = { email = it },
                        onNext = { forgotPasswordViewModel.sendPasswordResetEmail(email) },
                        authState = authState,
                        modifier = Modifier.fillMaxSize()
                    )

                ForgotPasswordStep.EMAIL_SENT ->
                    EmailSentStep(
                        email = email,
                        onManualConfirmation = {
                            currentStep = ForgotPasswordStep.SUCCESS
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                ForgotPasswordStep.SUCCESS ->
                    SuccessStep(
                        onBackToLogin = onBackToLogin,
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }
    }
}

@Composable
fun InputEmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onNext: () -> Unit,
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    var localError by remember { mutableStateOf("") }
    val loading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                localError = authState.message
            }
            is AuthState.Success -> {
                localError = ""
            }
            else -> {
                localError = ""
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_forgotpassword),
                contentDescription = "Forgot Password Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Forgot Password?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No worries, we'll send you a link to reset your password",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }

        // Form Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (localError.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        localError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    onEmailChange(it)
                    localError = ""
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = localError.isNotEmpty(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    localError = ""
                    onNext()
                },
                enabled = email.isNotBlank() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Sending Reset Link...")
                } else {
                    Text(
                        "Send Reset Link",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun EmailSentStep(
    email: String,
    onManualConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_sentemail),
            contentDescription = "Email Sent",
            tint = Color(0xFF87CEEB),
            modifier = Modifier.size(80.dp)
        )

        Text(
            "Check Your Email",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "We've sent a password reset link to:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                email,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Instructions
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "What to do next:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InstructionStep(number = "1", text = "Check your email inbox (and spam folder).")
            InstructionStep(number = "2", text = "Click the reset link in the email.")
            InstructionStep(number = "3", text = "Set your new password in the browser.")
            InstructionStep(number = "4", text = "Return to this app and confirm below.")
        }

        Spacer(Modifier.height(32.dp))

        // Manual Confirmation Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Already reset your password?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = onManualConfirmation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    "Yes, I've Reset My Password",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun InstructionStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SuccessStep(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var countdown by remember { mutableStateOf(5) }
    var showManualButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Start countdown
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        // If countdown finishes, auto-redirect
        if (countdown == 0) {
            onBackToLogin()
        }
    }

    // Show manual button after 3 seconds if still on this screen
    LaunchedEffect(Unit) {
        delay(3000)
        showManualButton = true
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_check),
            contentDescription = "Password reset successful",
            tint = Color.Green,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Password Reset Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Your password has been successfully reset.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "You can now login with your new password.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        if (countdown > 0) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Redirecting to login in $countdown...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(24.dp))

        // Manual redirect button (shown as fallback)
        if (showManualButton) {
            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue to Login")
            }
        }
    }
}

enum class ForgotPasswordStep {
    INPUT_EMAIL,
    EMAIL_SENT,
    SUCCESS
}