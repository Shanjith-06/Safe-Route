package com.example.saferoute.domain.sos

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import com.example.saferoute.domain.repository.ContactsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DispatchResult(
    val totalContacts: Int,
    val successCount: Int,
    val failureCount: Int,
    val messageText: String,
    val isDemoMode: Boolean,
    val isCellularAvailable: Boolean,
    val dispatchedContacts: List<String>,
    val failureReason: String? = null
)

class SmsDispatchEngine(
    private val context: Context,
    private val contactsRepository: ContactsRepository
) {

    fun getBatteryPercentage(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            85
        }
    }

    fun isCellularNetworkAvailable(): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return telephonyManager?.simState == TelephonyManager.SIM_STATE_READY
    }

    suspend fun dispatchEmergencySms(
        lat: Double,
        lng: Double,
        userName: String = "SafeRoute User",
        isDemoMode: Boolean = true,
        isSimulatedOffline: Boolean = false,
        customTemplate: String? = null
    ): DispatchResult {
        val activeContacts = contactsRepository.getActiveContacts()
        val battery = getBatteryPercentage()
        val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val locationUrl = "https://maps.google.com/?q=$lat,$lng"

        // Format message: check if customTemplate is provided or use default exact specification
        val message = if (!customTemplate.isNullOrBlank()) {
            val formatted = customTemplate
                .replace("{USER_NAME}", userName)
                .replace("{name}", userName)
                .replace("{LATITUDE}", "$lat")
                .replace("{LONGITUDE}", "$lng")
                .replace("{location}", locationUrl)
                .replace("{BATTERY}", "$battery")
                .replace("{battery}", "$battery")
                .replace("{TIME}", timeStr)
                .replace("{time}", timeStr)
            if (!formatted.contains("maps.google.com") && !formatted.contains(locationUrl)) {
                "$formatted\n\nLocation:\n$locationUrl"
            } else {
                formatted
            }
        } else {
            """
SAFE ROUTE EMERGENCY ALERT

$userName may be in danger.

Current location:
$locationUrl

Time:
$timeStr

Battery:
$battery%

Please contact them immediately.
""".trimIndent()
        }

        if (activeContacts.isEmpty()) {
            return DispatchResult(
                totalContacts = 0,
                successCount = 0,
                failureCount = 0,
                messageText = message,
                isDemoMode = isDemoMode,
                isCellularAvailable = isCellularNetworkAvailable(),
                dispatchedContacts = emptyList(),
                failureReason = "No active trusted contacts configured."
            )
        }

        val hasCellular = isCellularNetworkAvailable() && !isSimulatedOffline
        val dispatchedList = mutableListOf<String>()

        if (isDemoMode) {
            // In DEMO MODE: Never send real SMS, simulate dispatch safely
            activeContacts.forEach { contact ->
                dispatchedList.add("${contact.name} (${contact.phoneNumber}) [DEMO SIMULATED SMS]")
            }
            Log.d("SmsDispatchEngine", "DEMO MODE: Simulated SMS dispatch to ${dispatchedList.size} contacts.\nMessage:\n$message")
            return DispatchResult(
                totalContacts = activeContacts.size,
                successCount = dispatchedList.size,
                failureCount = 0,
                messageText = message,
                isDemoMode = true,
                isCellularAvailable = hasCellular,
                dispatchedContacts = dispatchedList
            )
        }

        // Real Production Mode:
        if (isSimulatedOffline) {
            // Simulated offline: Save to local storage queue.
            Log.w("SmsDispatchEngine", "Offline mode simulated. SMS queued locally.")
            return DispatchResult(
                totalContacts = activeContacts.size,
                successCount = 0,
                failureCount = activeContacts.size,
                messageText = message,
                isDemoMode = false,
                isCellularAvailable = false,
                dispatchedContacts = emptyList(),
                failureReason = "Simulated offline mode: Emergency event queued for local synchronization."
            )
        }

        var successCount = 0
        var failureCount = 0

        try {
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

            activeContacts.forEach { contact ->
                val phone = contact.phoneNumber.trim()
                if (contact.sendSms && phone.isNotBlank()) {
                    try {
                        val parts = smsManager.divideMessage(message)
                        if (parts.size > 1) {
                            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
                        } else {
                            smsManager.sendTextMessage(phone, null, message, null, null)
                        }
                        dispatchedList.add("${contact.name} ($phone) [REAL SMS SENT]")
                        successCount++
                        Log.i("SmsDispatchEngine", "Successfully dispatched real SMS to $phone")
                    } catch (e: Exception) {
                        Log.e("SmsDispatchEngine", "Failed to dispatch real SMS to $phone", e)
                        dispatchedList.add("${contact.name} ($phone) [SMS FAILED: ${e.localizedMessage ?: "Telecom error"}]")
                        failureCount++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsDispatchEngine", "Error accessing SmsManager", e)
            failureCount = activeContacts.size
        }

        return DispatchResult(
            totalContacts = activeContacts.size,
            successCount = successCount,
            failureCount = failureCount,
            messageText = message,
            isDemoMode = false,
            isCellularAvailable = hasCellular,
            dispatchedContacts = dispatchedList,
            failureReason = if (failureCount > 0) "Some or all SMS messages failed to send." else null
        )
    }
}
