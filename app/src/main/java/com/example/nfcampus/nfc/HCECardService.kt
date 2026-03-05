package com.example.nfcampus.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.nfcampus.util.NotificationHelper

class HCECardService : HostApduService() {

    companion object {
        // Standard AID for PN532
        private val AID = byteArrayOf(
            0xA0.toByte(), 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01
        )

        private val SELECT_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_UNKNOWN = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    private var selected = false

    private fun isHapticEnabled(): Boolean {
        val prefs = getSharedPreferences("nfcampus_prefs", MODE_PRIVATE)
        return prefs.getBoolean("haptic_feedback", true)
    }

    private fun isAccessAlertsEnabled(): Boolean {
        val prefs = getSharedPreferences("nfcampus_prefs", MODE_PRIVATE)
        return prefs.getBoolean("access_alerts", false)
    }

    override fun processCommandApdu(apdu: ByteArray?, extras: Bundle?): ByteArray {
        if (apdu == null || apdu.size < 4) return SW_UNKNOWN

        Log.d("HCE", "Received APDU: ${apdu.joinToString(" ") { "%02X".format(it) }}")

        // -------- SELECT AID Command --------
        if (apdu[0] == 0x00.toByte() &&
            apdu[1] == 0xA4.toByte() &&
            apdu[2] == 0x04.toByte() &&
            apdu[3] == 0x00.toByte()) {

            val aidLength = apdu[4].toInt()
            val aid = if (apdu.size >= 5 + aidLength) {
                apdu.copyOfRange(5, 5 + aidLength)
            } else {
                return SW_UNKNOWN
            }

            selected = aid.contentEquals(AID)

            if (selected) {
                // RETRIEVE STORED UID
                val prefs = getSharedPreferences("nfcampus_prefs", MODE_PRIVATE)
                val uidHex = prefs.getString("campus_card_uid", null)

                Log.d("HCE", "AID Selected. Stored UID: $uidHex")

                // Haptic feedback
                if (isHapticEnabled()) {
                    val vibrator = getSystemService(Vibrator::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }

                // Access alert notification
                if (isAccessAlertsEnabled()) {
                    val granted = uidHex != null
                    NotificationHelper.showAccessNotification(applicationContext, granted)
                }

                if (uidHex != null) {
                    val uidBytes = hexToBytes(uidHex)
                    // Return UID bytes followed by Success Status (90 00)
                    return uidBytes + SELECT_OK
                } else {
                    // If no UID is set, just return OK
                    return SELECT_OK
                }
            } else {
                return SW_UNKNOWN
            }
        }
        return SW_UNKNOWN
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "Deactivated: $reason")
        selected = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("HCE", "HCE Service Started")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HCE", "HCE Service Destroyed")
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(":", "").replace(" ", "").uppercase()
        // If length is odd (bad data), pad it
        val padded = if (clean.length % 2 != 0) "0$clean" else clean

        return ByteArray(padded.length / 2) {
            padded.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
}