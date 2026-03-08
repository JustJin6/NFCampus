package com.example.nfcampus.gui.access_NFC

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import com.example.nfcampus.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LinkedCardScreen(
    onLogoutAndNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Get stored card UID
    val storedCardUID = remember {
        mutableStateOf(activity?.getStoredCampusCardUID())
    }

    // Get dynamic device name
    val deviceName = remember {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    val isCardActive = storedCardUID.value != null

    // Function to refresh
    val refresh: () -> Unit = {
        storedCardUID.value = activity?.getStoredCampusCardUID()
    }

    // Dialog states
    var showTerminateDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Success dialog auto-dismiss
    LaunchedEffect(showSuccessDialog) {
        if (showSuccessDialog) {
            delay(3000) // Show for 3 seconds
            showSuccessDialog = false
            onLogoutAndNavigateToLogin()
        }
    }

    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Card Information Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Linked Card",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { refresh() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card UID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Card UID",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            storedCardUID.value ?: "Not linked",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Device Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Device Name",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            deviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Status",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isCardActive) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isCardActive) "Active" else "Inactive",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCardActive) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Unlink Card Section (Danger Zone)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Unlink Card",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Remove the current card link from this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showTerminateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(MaterialTheme.colorScheme.error)
                        )
                    ) {
                        Text("Terminate Linked Card")
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Help Text
            Text(
                "Note: After unlinking, you can re-link your card through the NFC setup process.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Custom Terminate Confirmation Dialog
        if (showTerminateDialog) {
            Dialog(onDismissRequest = { showTerminateDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Warning Icon
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Terminate Card",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Removing your linked card will also delete your account and all associated data.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Are you sure you want to continue?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // NO Button
                            OutlinedButton(
                                onClick = { showTerminateDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    "NO",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // YES Button
                            Button(
                                onClick = {
                                    Log.d("TerminateDebug", "1. Start Termination Process")
                                    showTerminateDialog = false
                                    val auth = FirebaseAuth.getInstance()
                                    val currentUser = auth.currentUser
                                    val userId = currentUser?.uid
                                    val db = FirebaseFirestore.getInstance()

                                    if (userId != null && currentUser != null) {
                                        showSuccessDialog = true

                                        // 1. Delete the ENTRIES sub-collection in activity_logs
                                        Log.d("TerminateDebug", "2. Fetching logs from activity_logs/$userId/entries")
                                        db.collection("activity_logs")
                                            .document(userId)
                                            .collection("entries")
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                val batch = db.batch()
                                                for (doc in snapshot) {
                                                    batch.delete(doc.reference)
                                                }

                                                // 2. Commit logs, then delete the userId document in activity_logs, then users
                                                batch.commit().addOnCompleteListener {
                                                    Log.d("TerminateDebug", "3. Logs wiped. Deleting activity_logs doc and user profile.")

                                                    // Delete the parent doc in activity_logs
                                                    db.collection("activity_logs").document(userId).delete()

                                                    // Delete the main user profile
                                                    db.collection("users").document(userId).delete()
                                                        .addOnCompleteListener {
                                                            Log.d("TerminateDebug", "4. Firestore cleaned. Deleting Auth.")

                                                            // 3. Delete Auth Account
                                                            currentUser.delete().addOnCompleteListener { task ->
                                                                // 4. SharedPreference/Local Cleanup
                                                                activity?.let { act ->
                                                                    act.getSharedPreferences("nfcampus_prefs", Context.MODE_PRIVATE)
                                                                        .edit { clear() }
                                                                    act.disableHCEMode()
                                                                }
                                                                storedCardUID.value = null

                                                                if (task.isSuccessful) {
                                                                    Log.d("TerminateDebug", "5. FULL SUCCESS")
                                                                    // Navigation is handled by the showSuccessDialog LaunchedEffect
                                                                    onLogoutAndNavigateToLogin()
                                                                } else {
                                                                    Log.e("TerminateDebug", "Auth Delete Failed: ${task.exception?.message}")
                                                                    auth.signOut()
                                                                    onLogoutAndNavigateToLogin()
                                                                }
                                                            }
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("TerminateDebug", "Firestore Failed: ${e.message}")
                                                auth.signOut()
                                                onLogoutAndNavigateToLogin()
                                            }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(
                                    "YES",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Success Icon
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Linked card and account removed successfully",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Redirecting to login...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}