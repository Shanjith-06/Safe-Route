package com.example.saferoute.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private var lastShakeTimestamp: Long = 0L
    private var shakeCount: Int = 0

    private val ACCELERATION_THRESHOLD = 13.0f // m/s^2 force threshold
    private val SHAKE_WINDOW_MILLIS = 1500L
    private val SHAKE_COOLDOWN_MILLIS = 3000L
    private val REQUIRED_SHAKE_COUNT = 3

    fun register(sensorManager: SensorManager): Boolean {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregister(sensorManager: SensorManager) {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate gForce without gravity (~9.81 m/s^2)
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH

        val now = System.currentTimeMillis()

        if (acceleration > ACCELERATION_THRESHOLD) {
            if (now - lastShakeTimestamp < SHAKE_WINDOW_MILLIS) {
                shakeCount++
                if (shakeCount >= REQUIRED_SHAKE_COUNT) {
                    shakeCount = 0
                    lastShakeTimestamp = now + SHAKE_COOLDOWN_MILLIS
                    onShakeDetected()
                }
            } else {
                shakeCount = 1
                lastShakeTimestamp = now
            }
        } else if (now - lastShakeTimestamp > SHAKE_WINDOW_MILLIS) {
            shakeCount = 0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
