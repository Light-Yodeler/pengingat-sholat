package com.example.myapplication.core.qibla

import kotlin.math.*

data class QiblaResult(
    val azimuthDegrees: Double,
    val distanceKm: Double,
    val formattedCompassDirection: String,
    val formattedDistance: String
)

object QiblaCalculator {

    // Ka'bah coordinates in Makkah
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculate(userLat: Double, userLng: Double): QiblaResult {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val deltaLng = Math.toRadians(KAABA_LNG - userLng)

        // Spherical trigonometry for initial bearing from user position to Kaaba
        val y = sin(deltaLng)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(deltaLng)
        var azimuth = Math.toDegrees(atan2(y, x))
        if (azimuth < 0.0) {
            azimuth += 360.0
        }

        // Great circle distance
        val dLat = lat2 - lat1
        val a = sin(dLat / 2.0).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLng / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        val distance = EARTH_RADIUS_KM * c

        val directionLabel = getCompassDirectionLabel(azimuth)
        val formattedDist = String.format("%,d km", distance.roundToInt()).replace(',', '.')

        return QiblaResult(
            azimuthDegrees = azimuth,
            distanceKm = distance,
            formattedCompassDirection = directionLabel,
            formattedDistance = formattedDist
        )
    }

    private fun getCompassDirectionLabel(degrees: Double): String {
        val normalized = (degrees % 360.0 + 360.0) % 360.0
        return when {
            normalized in 22.5..67.5 -> "Timur Laut"
            normalized in 67.5..112.5 -> "Timur"
            normalized in 112.5..157.5 -> "Tenggara"
            normalized in 157.5..202.5 -> "Selatan"
            normalized in 202.5..247.5 -> "Barat Daya"
            normalized in 247.5..292.5 -> "Barat"
            normalized in 292.5..337.5 -> "Barat Laut"
            else -> "Utara"
        }
    }
}
