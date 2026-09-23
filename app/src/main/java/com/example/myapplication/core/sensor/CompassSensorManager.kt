package com.example.myapplication.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuthFlow = MutableStateFlow(0f)
    val azimuthFlow: StateFlow<Float> = _azimuthFlow.asStateFlow()

    private val _accuracyFlow = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracyFlow: StateFlow<Int> = _accuracyFlow.asStateFlow()

    private val _isMagneticInterference = MutableStateFlow(false)
    val isMagneticInterference: StateFlow<Boolean> = _isMagneticInterference.asStateFlow()

    private val rawRotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAcc = FloatArray(3)
    private val lastMag = FloatArray(3)
    private var hasAcc = false
    private var hasMag = false
    private var smoothedAzimuth = 0f
    private var isFirstReading = true

    fun start() {
        isFirstReading = true
        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        isFirstReading = true
    }

    private var sinSum = 0.0
    private var cosSum = 0.0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rawRotationMatrix, event.values)
                computeAzimuthFromMatrix(rawRotationMatrix)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAcc, 0, 3)
                hasAcc = true
                tryComputeFromAccMag()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMag, 0, 3)
                hasMag = true
                val magnitude = Math.sqrt(
                    (event.values[0] * event.values[0] +
                     event.values[1] * event.values[1] +
                     event.values[2] * event.values[2]).toDouble()
                )
                _isMagneticInterference.value = magnitude > 75.0 || magnitude < 20.0
                tryComputeFromAccMag()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracyFlow.value = accuracy
    }

    private fun tryComputeFromAccMag() {
        if (hasAcc && hasMag) {
            if (SensorManager.getRotationMatrix(rawRotationMatrix, null, lastAcc, lastMag)) {
                computeAzimuthFromMatrix(rawRotationMatrix)
            }
        }
    }

    private fun computeAzimuthFromMatrix(matrix: FloatArray) {
        val isFlat = kotlin.math.abs(matrix[8]) > 0.65f
        val azimuth = if (isFlat) {
            SensorManager.getOrientation(matrix, orientationAngles)
            var az = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (az < 0f) az += 360f
            az
        } else {
            SensorManager.remapCoordinateSystem(
                matrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedMatrix
            )
            SensorManager.getOrientation(remappedMatrix, orientationAngles)
            var az = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (az < 0f) az += 360f
            az
        }
        applyStabilization(azimuth)
    }

    private fun applyStabilization(newAzimuth: Float) {
        val rad = Math.toRadians(newAzimuth.toDouble())
        val curSin = kotlin.math.sin(rad)
        val curCos = kotlin.math.cos(rad)

        if (isFirstReading) {
            sinSum = curSin
            cosSum = curCos
            smoothedAzimuth = newAzimuth
            _azimuthFlow.value = newAzimuth
            isFirstReading = false
            return
        }

        var diff = newAzimuth - smoothedAzimuth
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f

        if (abs(diff) < 0.5f) {
            return
        }

        val alpha = if (abs(diff) > 20f) 0.35 else 0.12
        sinSum = alpha * curSin + (1.0 - alpha) * sinSum
        cosSum = alpha * curCos + (1.0 - alpha) * cosSum

        var smoothed = Math.toDegrees(kotlin.math.atan2(sinSum, cosSum)).toFloat()
        if (smoothed < 0f) smoothed += 360f

        smoothedAzimuth = smoothed
        _azimuthFlow.value = smoothed
    }
}
