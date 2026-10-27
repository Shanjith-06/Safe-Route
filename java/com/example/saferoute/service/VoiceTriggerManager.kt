package com.example.saferoute.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceTriggerManager(
    private val context: Context,
    private val onVoiceTriggered: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("VoiceTriggerManager", "Speech recognition unavailable on device")
            return
        }

        if (isListening) return

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        isListening = false
                        // Restart listener if active
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.forEach { phrase ->
                            val lower = phrase.lowercase()
                            if (lower.contains("saferoute") || lower.contains("help") || lower.contains("emergency") || lower.contains("sos")) {
                                onVoiceTriggered(phrase)
                                return
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e("VoiceTriggerManager", "Failed to start speech recognizer", e)
            isListening = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
        } catch (e: Exception) {
            Log.e("VoiceTriggerManager", "Error stopping speech recognizer", e)
        }
    }

    fun simulateVoiceTrigger(phrase: String = "Help SafeRoute SOS!") {
        onVoiceTriggered(phrase)
    }
}
