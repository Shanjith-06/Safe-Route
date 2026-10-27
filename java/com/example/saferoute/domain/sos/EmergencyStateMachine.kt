package com.example.saferoute.domain.sos

import com.example.saferoute.domain.model.EmergencyEvent
import com.example.saferoute.domain.model.TriggerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SosState {
    IDLE,
    PREPARING,          // Countdown active (e.g. 5s -> 0s)
    SOS_ACTIVE,         // SOS officially triggered
    TRACKING,           // Transmitting live GPS
    EVIDENCE_RECORDING, // Recording 10s audio + photo snapshot
    ALERT_SENT,         // SMS dispatched to trusted contacts
    CANCELLED,          // Cancelled by user during countdown or active alert
    COMPLETED,          // Emergency resolved safely
    FAILED              // Dispatch failed
}

data class EmergencyUiState(
    val sosState: SosState = SosState.IDLE,
    val triggerType: TriggerType = TriggerType.MANUAL_SOS,
    val countdownSeconds: Int = 5,
    val currentLat: Double = 37.7749,
    val currentLng: Double = -122.4194,
    val batteryPercentage: Int = 88,
    val activeEventId: Long = 0L,
    val contactsNotifiedCount: Int = 0,
    val audioPath: String? = null,
    val photoPath: String? = null,
    val isDemoMode: Boolean = true,
    val statusText: String = "All systems normal"
)

class EmergencyStateMachine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun triggerSos(
        triggerType: TriggerType,
        lat: Double = 37.7749,
        lng: Double = -122.4194,
        battery: Int = 88,
        isDemoMode: Boolean = true,
        onCountdownFinished: suspend (TriggerType, Double, Double, Int) -> Long = { _, _, _, _ -> 101L }
    ) {
        if (_uiState.value.sosState != SosState.IDLE && _uiState.value.sosState != SosState.CANCELLED && _uiState.value.sosState != SosState.COMPLETED) {
            return
        }

        _uiState.value = EmergencyUiState(
            sosState = SosState.PREPARING,
            triggerType = triggerType,
            countdownSeconds = 5,
            currentLat = lat,
            currentLng = lng,
            batteryPercentage = battery,
            isDemoMode = isDemoMode,
            statusText = "Emergency Triggered via ${triggerType.name}. Countdown started!"
        )

        countdownJob?.cancel()
        countdownJob = scope.launch {
            for (sec in 5 downTo 1) {
                _uiState.value = _uiState.value.copy(countdownSeconds = sec)
                delay(1000L)
            }

            // Countdown completed -> Transition to SOS_ACTIVE
            _uiState.value = _uiState.value.copy(
                sosState = SosState.SOS_ACTIVE,
                countdownSeconds = 0,
                statusText = "SOS ACTIVE! Transmitting emergency dispatch..."
            )

            val eventId = onCountdownFinished(triggerType, lat, lng, battery)
            _uiState.value = _uiState.value.copy(activeEventId = eventId)
        }
    }

    fun cancelSos() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            sosState = SosState.CANCELLED,
            statusText = "Emergency alert cancelled by user."
        )
    }

    fun updateProgress(
        newState: SosState,
        contactsNotified: Int = _uiState.value.contactsNotifiedCount,
        audioPath: String? = _uiState.value.audioPath,
        photoPath: String? = _uiState.value.photoPath,
        statusText: String = _uiState.value.statusText
    ) {
        _uiState.value = _uiState.value.copy(
            sosState = newState,
            contactsNotifiedCount = contactsNotified,
            audioPath = audioPath,
            photoPath = photoPath,
            statusText = statusText
        )
    }

    fun resolveSos() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            sosState = SosState.COMPLETED,
            statusText = "Emergency resolved. You are marked as safe."
        )
    }

    fun resetToIdle() {
        countdownJob?.cancel()
        _uiState.value = EmergencyUiState()
    }
}
