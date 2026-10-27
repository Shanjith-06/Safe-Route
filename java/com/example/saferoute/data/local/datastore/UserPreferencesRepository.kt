package com.example.saferoute.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val isDemoModeEnabled: Boolean = false,
    val isOfflineModeSimulated: Boolean = false,
    val isOnboardingCompleted: Boolean = true,
    val isShakeTriggerEnabled: Boolean = true,
    val isVoiceTriggerEnabled: Boolean = true,
    val isAutoEvidenceCaptureEnabled: Boolean = true,
    val isCustomLocationEnabled: Boolean = false,
    val customLatitude: Double = 12.9716,
    val customLongitude: Double = 77.5946,
    val customLocationName: String = "Bengaluru City Center",
    val sensitivityLevel: String = "MEDIUM",
    val routeDeviationThresholdMeters: Int = 40,
    val unexpectedStopTimeoutSeconds: Int = 60,
    val voiceTriggerPhrase: String = "SafeRoute SOS",
    val userName: String = "Priya Sharma",
    val userPhone: String = "+15550192831",
    val emergencySmsTemplate: String = "SAFE ROUTE EMERGENCY ALERT\n{USER_NAME} may be in danger.\n\nCurrent location:\nhttps://maps.google.com/?q={LATITUDE},{LONGITUDE}\n\nTime:\n{TIME}\n\nBattery:\n{BATTERY}%\n\nPlease contact them immediately.",
    val simulatedSpeedKmH: Double = 35.0,
    val activeEmergencyId: Long = -1L
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val IS_OFFLINE_SIMULATED = booleanPreferencesKey("is_offline_simulated")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_SHAKE_TRIGGER = booleanPreferencesKey("is_shake_trigger")
        val IS_VOICE_TRIGGER = booleanPreferencesKey("is_voice_trigger")
        val IS_AUTO_EVIDENCE = booleanPreferencesKey("is_auto_evidence")
        val IS_CUSTOM_LOCATION = booleanPreferencesKey("is_custom_location")
        val CUSTOM_LATITUDE = doublePreferencesKey("custom_latitude")
        val CUSTOM_LONGITUDE = doublePreferencesKey("custom_longitude")
        val CUSTOM_LOCATION_NAME = stringPreferencesKey("custom_location_name")
        val SENSITIVITY_LEVEL = stringPreferencesKey("sensitivity_level")
        val DEVIATION_THRESHOLD = intPreferencesKey("deviation_threshold")
        val STOP_TIMEOUT = intPreferencesKey("stop_timeout")
        val VOICE_PHRASE = stringPreferencesKey("voice_phrase")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val EMERGENCY_SMS_TEMPLATE = stringPreferencesKey("emergency_sms_template")
        val SIMULATED_SPEED_KMH = doublePreferencesKey("simulated_speed_kmh")
        val ACTIVE_EMERGENCY_ID = longPreferencesKey("active_emergency_id")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                isDemoModeEnabled = preferences[PreferencesKeys.IS_DEMO_MODE] ?: false,
                isOfflineModeSimulated = preferences[PreferencesKeys.IS_OFFLINE_SIMULATED] ?: false,
                isOnboardingCompleted = preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: true,
                isShakeTriggerEnabled = preferences[PreferencesKeys.IS_SHAKE_TRIGGER] ?: true,
                isVoiceTriggerEnabled = preferences[PreferencesKeys.IS_VOICE_TRIGGER] ?: true,
                isAutoEvidenceCaptureEnabled = preferences[PreferencesKeys.IS_AUTO_EVIDENCE] ?: true,
                isCustomLocationEnabled = preferences[PreferencesKeys.IS_CUSTOM_LOCATION] ?: false,
                customLatitude = preferences[PreferencesKeys.CUSTOM_LATITUDE] ?: 12.9716,
                customLongitude = preferences[PreferencesKeys.CUSTOM_LONGITUDE] ?: 77.5946,
                customLocationName = preferences[PreferencesKeys.CUSTOM_LOCATION_NAME] ?: "Bengaluru City Center",
                sensitivityLevel = preferences[PreferencesKeys.SENSITIVITY_LEVEL] ?: "MEDIUM",
                routeDeviationThresholdMeters = preferences[PreferencesKeys.DEVIATION_THRESHOLD] ?: 40,
                unexpectedStopTimeoutSeconds = preferences[PreferencesKeys.STOP_TIMEOUT] ?: 60,
                voiceTriggerPhrase = preferences[PreferencesKeys.VOICE_PHRASE] ?: "SafeRoute SOS",
                userName = preferences[PreferencesKeys.USER_NAME] ?: "Priya Sharma",
                userPhone = preferences[PreferencesKeys.USER_PHONE] ?: "+15550192831",
                emergencySmsTemplate = preferences[PreferencesKeys.EMERGENCY_SMS_TEMPLATE]
                    ?: "SAFE ROUTE EMERGENCY ALERT\n{USER_NAME} may be in danger.\n\nCurrent location:\nhttps://maps.google.com/?q={LATITUDE},{LONGITUDE}\n\nTime:\n{TIME}\n\nBattery:\n{BATTERY}%\n\nPlease contact them immediately.",
                simulatedSpeedKmH = preferences[PreferencesKeys.SIMULATED_SPEED_KMH] ?: 35.0,
                activeEmergencyId = preferences[PreferencesKeys.ACTIVE_EMERGENCY_ID] ?: -1L
            )
        }

    suspend fun setDemoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DEMO_MODE] = enabled
        }
    }

    suspend fun setOfflineModeSimulated(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_OFFLINE_SIMULATED] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setShakeTrigger(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SHAKE_TRIGGER] = enabled
        }
    }

    suspend fun setVoiceTrigger(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_VOICE_TRIGGER] = enabled
        }
    }

    suspend fun setAutoEvidenceCapture(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUTO_EVIDENCE] = enabled
        }
    }

    suspend fun setSensitivityLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SENSITIVITY_LEVEL] = level
        }
    }

    suspend fun setDeviationThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVIATION_THRESHOLD] = threshold
        }
    }

    suspend fun setStopTimeout(timeoutSeconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STOP_TIMEOUT] = timeoutSeconds
        }
    }

    suspend fun setVoicePhrase(phrase: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOICE_PHRASE] = phrase
        }
    }

    suspend fun updateUserProfile(name: String, phone: String, template: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_PHONE] = phone
            preferences[PreferencesKeys.EMERGENCY_SMS_TEMPLATE] = template
        }
    }

    suspend fun setActiveEmergencyId(eventId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_EMERGENCY_ID] = eventId
        }
    }

    suspend fun clearActiveEmergencyId() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_EMERGENCY_ID] = -1L
        }
    }

    suspend fun setCustomLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_CUSTOM_LOCATION] = enabled
        }
    }

    suspend fun setCustomLocation(lat: Double, lng: Double, name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_LATITUDE] = lat
            preferences[PreferencesKeys.CUSTOM_LONGITUDE] = lng
            preferences[PreferencesKeys.CUSTOM_LOCATION_NAME] = name
        }
    }
}
