package com.example.nfcampus.gui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun SettingsScreen(
    onNavigateToChangeEmailPassword: () -> Unit,
    onNavigateToNFCTroubleshooting: () -> Unit,
    onNavigateToLinkedCard: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermsOfService: () -> Unit
) {
    val context = LocalContext.current
    val preferences = context.getSharedPreferences("nfcampus_prefs", Context.MODE_PRIVATE)

    // Load initial values from preferences
    var hapticFeedbackEnabled by remember { mutableStateOf(preferences.getBoolean("haptic_feedback", true)) }
    var accessAlertsEnabled by remember { mutableStateOf(preferences.getBoolean("access_alerts", false)) }

    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Account & Security Section
            SectionTitle(title = "Account & Security")

            SettingsItemWithAction(
                title = "Change Email Address & Password",
                subtitle = "Change your email / password anytime",
                icon = Icons.Default.Email,
                onClick = { onNavigateToChangeEmailPassword() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Divider between sections
            Spacer(modifier = Modifier.height(24.dp))

            // Access & NFC Section
            SectionTitle(title = "Access & NFC")

            SettingsToggle(
                title = "Haptic Feedback",
                subtitle = "Vibrate on successful tap",
                icon = Icons.Default.Vibration,
                checked = hapticFeedbackEnabled,
                onCheckedChange = {
                    hapticFeedbackEnabled = it
                    preferences.edit { putBoolean("haptic_feedback", it) }
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SettingsItemWithAction(
                title = "NFC Troubleshooting",
                subtitle = "Guide on how to use NFC correctly",
                icon = Icons.AutoMirrored.Filled.Help,
                onClick = { onNavigateToNFCTroubleshooting() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SettingsItemWithAction(
                title = "Linked Card",
                subtitle = "View, update or unlink your NFC card",
                icon = Icons.Default.CreditCard,
                onClick = { onNavigateToLinkedCard() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Divider between sections
            Spacer(modifier = Modifier.height(24.dp))

            // Notifications Section
            SectionTitle(title = "Notifications")

            SettingsToggle(
                title = "Access Alerts",
                subtitle = "Notify when access is granted or denied",
                icon = Icons.Default.Security,
                checked = accessAlertsEnabled,
                onCheckedChange = {
                    accessAlertsEnabled = it
                    preferences.edit { putBoolean("access_alerts", it) }
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Divider between sections
            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SectionTitle(title = "About")

            SettingsItemWithAction(
                title = "Privacy Policy",
                subtitle = null,
                icon = Icons.Default.PrivacyTip,
                onClick = { onNavigateToPrivacyPolicy() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SettingsItemWithAction(
                title = "Terms of Service",
                subtitle = null,
                icon = Icons.Default.Description,
                onClick = { onNavigateToTermsOfService() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                thickness = 1.0.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // App Version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Version 0.2.1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )

    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        thickness = 1.0.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun SettingsItemWithAction(
    title: String,
    subtitle: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Switch
                )
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}