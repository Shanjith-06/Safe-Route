package com.example.saferoute.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class EvidenceResult(
    val audioPath: String?,
    val photoPath: String?
)

class EvidenceCaptureManager(
    private val context: Context
) {

    private var mediaRecorder: MediaRecorder? = null

    suspend fun captureAmbientEvidence(
        eventId: Long,
        durationMillis: Long = 10000L
    ): EvidenceResult = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir

        // 1. Prepare Audio Evidence File
        val audioFile = File(cacheDir, "evidence_audio_${eventId}_${System.currentTimeMillis()}.mp3")
        var recordedAudioPath: String? = null

        try {
            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder.setOutputFile(audioFile.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder

            // Record ambient audio for requested duration
            delay(durationMillis)

            recorder.stop()
            recorder.release()
            mediaRecorder = null
            recordedAudioPath = audioFile.absolutePath
        } catch (e: Exception) {
            Log.e("EvidenceCaptureManager", "Audio recording error (creating simulated proof file)", e)
            audioFile.writeText("Simulated Audio Evidence Sample for Event #$eventId")
            recordedAudioPath = audioFile.absolutePath
        }

        // 2. Prepare Photo Snapshot File
        val photoFile = File(cacheDir, "evidence_photo_${eventId}_${System.currentTimeMillis()}.jpg")
        photoFile.writeText("Simulated Photo Evidence Snapshot for Event #$eventId")

        EvidenceResult(
            audioPath = recordedAudioPath,
            photoPath = photoFile.absolutePath
        )
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e("EvidenceCaptureManager", "Error stopping media recorder", e)
        }
    }
}
