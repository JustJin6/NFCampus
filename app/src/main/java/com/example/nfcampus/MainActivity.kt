package com.example.nfcampus

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import com.example.nfcampus.gui.LoginScreen
import com.example.nfcampus.gui.MainScreen
import com.example.nfcampus.gui.RegisterScreen
import com.example.nfcampus.ui.theme.NFCampusTheme
import com.example.nfcampus.repository.ActivityLogRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private lateinit var auth: FirebaseAuth
    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private lateinit var preferences: SharedPreferences
    private var cardEmulation: CardEmulation? = null

    var allowEnrollment = false

    // NFC callback
    var onNFCTagDetected: ((String) -> Unit)? = null

    // Track if NFC should be active
    var isNFCEnabledForApp = false
    var isHCEModeActive = false

    // Component name for HCE service
    private lateinit var hceServiceName: ComponentName

    @SuppressLint("HardwareIds")
    private fun syncUidFromFirestoreToPreferences(userId: String) {
        val currentDeviceId = android.provider.Settings.Secure.getString(
            contentResolver, android.provider.Settings.Secure.ANDROID_ID
        )
            FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val registeredDeviceId = document.getString("deviceId")
                    val nfcUid = document.getString("nfcUid")

                    // SECURITY CHECK: Is this the right device?
                    if (registeredDeviceId != null && registeredDeviceId != currentDeviceId) {
                        Log.e("Security", "Device ID mismatch! Forcing logout.")
                        auth.signOut()

                        return@addOnSuccessListener
                    }

                    if (nfcUid != null) {
                        // Store in SharedPreferences
                        preferences.edit {
                            putString("campus_card_uid", nfcUid.replace(":", "").uppercase())
                        }

                        // Enable HCE if UID exists
                        enableHCEMode()
                    } else {
                        disableHCEMode()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching UID: ${e.message}")
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize preferences
        preferences = getSharedPreferences("nfcampus_prefs", MODE_PRIVATE)

        // Get NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Initialize CardEmulation
        nfcAdapter?.let {
            cardEmulation = CardEmulation.getInstance(it)
            hceServiceName = ComponentName(this, "com.example.nfcampus.nfc.HCECardService")
        }

        // Create PendingIntent for NFC foreground dispatch
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Check if have a stored UID and enable HCE
        val storedUid = preferences.getString("campus_card_uid", null)
        if (storedUid != null) {
            enableHCEMode()
        }

        setContent {
            NFCampusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NFCampusApp(onSyncUid = ::syncUidFromFirestoreToPreferences)
                }
            }
        }

        // Handle initial NFC intent
        handleNFCIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1002
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch only when NFC is needed for reading
        if (isHCEModeActive) {
            cardEmulation?.setPreferredService(this, hceServiceName)
        } else if (isNFCEnabledForApp) {
            enableForegroundDispatch()
        }
    }

    override fun onPause() {
        super.onPause()
        // Always disable foreground dispatch when paused
        disableForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNFCIntent(intent)
    }

    private fun handleNFCIntent(intent: Intent) {
        val action = intent.action

        // Use the new API for getting Parcelable
        val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        // Check if it's an NFC intent
        if (action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == NfcAdapter.ACTION_TAG_DISCOVERED) {

            tag?.let {
                val uid = bytesToHex(it.id)
                onNFCTagDetected?.invoke(uid)
                vibrateIfEnabled()

                if (allowEnrollment) {
                    storeCampusCardUID(uid)
                    enableHCEMode()
                    allowEnrollment = false
                }
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(":") { "%02X".format(it) }
    }

    fun storeCampusCardUID(uid: String) {
        // Normalize UID format (remove colons if present, convert to uppercase)
        val normalizedUid = uid.replace(":", "").uppercase()

        // Store the normalized UID using KTX extension
        preferences.edit {
            putString("campus_card_uid", normalizedUid)
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("nfcUid", normalizedUid)
        }

        // Enable HCE mode immediately after storing
        enableHCEMode()
    }

    fun getStoredCampusCardUID(): String? {
        val uid = preferences.getString("campus_card_uid", null)
        return uid?.chunked(2)?.joinToString(":")
    }

    fun enableHCEMode() {
        isHCEModeActive = true
        isNFCEnabledForApp = false
        disableForegroundDispatch()

        // Enable HCE service
        cardEmulation?.setPreferredService(this, hceServiceName)
    }

    fun disableHCEMode() {
        isHCEModeActive = false
        cardEmulation?.unsetPreferredService(this)
    }

    fun enableNFCForApp() {
        // Only enable if don't have HCE active
        if (!isHCEModeActive) {
            isNFCEnabledForApp = true
            enableForegroundDispatch()
        }
    }

    fun disableNFCForApp() {
        isNFCEnabledForApp = false
        disableForegroundDispatch()
    }

    private fun enableForegroundDispatch() {
        if (isHCEModeActive) return // Don't enable if HCE is active
        nfcAdapter?.let { adapter ->
            try {
                val techList = arrayOf(
                    arrayOf("android.nfc.tech.NfcA")
                )

                // Intent filter for NFC
                val intentFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED).apply {
                    try {
                        addDataType("*/*")
                    } catch (e: IntentFilter.MalformedMimeTypeException) {
                        e.printStackTrace()
                    }
                }

                val intentFilters = arrayOf(intentFilter)

                adapter.enableForegroundDispatch(this, nfcPendingIntent, intentFilters, techList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.let { adapter ->
            try {
                adapter.disableForegroundDispatch(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setNFCCallback(callback: (String) -> Unit) {
        onNFCTagDetected = callback
    }

    fun clearNFCCallback() {
        onNFCTagDetected = null
    }

    private fun vibrateIfEnabled() {
        val prefs = getSharedPreferences("nfcampus_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("haptic_feedback", true)) {
            val vibrator = getSystemService(Vibrator::class.java)
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(100)
                }
            }
        }
    }
}

@Composable
fun NFCampusApp(
    onSyncUid: (String) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(getInitialScreen(onSyncUid)) }
    val logRepository = remember { ActivityLogRepository() }

    LaunchedEffect(currentScreen) {
        if (currentScreen == "main") {
            Firebase.auth.currentUser?.uid?.let { userId ->
                logRepository.addLog(userId, "User Logged In")
            }
        }
    }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = {
                currentScreen = "main"
                Firebase.auth.currentUser?.uid?.let { userId ->
                    onSyncUid(userId)
                }
            },
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

private fun getInitialScreen(onSyncUid: (String) -> Unit): String {
    val currentUser = Firebase.auth.currentUser
    return if (currentUser != null && currentUser.isEmailVerified) {
        // Sync UID from Firestore when app starts
        onSyncUid(currentUser.uid)
        "main"
    } else {
        "login"
    }
}