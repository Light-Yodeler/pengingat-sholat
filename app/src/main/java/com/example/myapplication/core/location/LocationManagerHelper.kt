package com.example.myapplication.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.resume

object LocationManagerHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): CityLocation? {
        if (!hasLocationPermission(context)) return null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        val location: Location? = withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        } ?: withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                fusedClient.lastLocation
                    .addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val finalLocation: Location? = location ?: lm?.let { manager ->
            try {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else if (manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                    manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                } else null
            } catch (_: Exception) {
                null
            }
        }

        if (finalLocation == null) return null

        val lat = finalLocation.latitude
        val lng = finalLocation.longitude
        val alt = if (finalLocation.hasAltitude()) finalLocation.altitude else 25.0

        val tzHours = ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds / 3600.0

        val (name, detail) = reverseGeocode(context, lat, lng)

        return CityLocation(
            id = "gps_current",
            name = name,
            detail = detail,
            latitude = lat,
            longitude = lng,
            altitudeMeters = alt,
            timeZoneOffsetHours = tzHours
        )
    }

    private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("id", "ID"))
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { list ->
                            cont.resume(list)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val cityName = addr.subAdminArea ?: addr.locality ?: addr.adminArea ?: "Lokasi Saya"
                    val subDistrict = addr.subLocality ?: addr.locality ?: ""
                    val detail = if (subDistrict.isNotEmpty() && subDistrict != cityName) {
                        "$subDistrict, $cityName"
                    } else {
                        addr.getAddressLine(0) ?: cityName
                    }
                    Pair(cityName, detail)
                } else {
                    Pair("Lokasi GPS", String.format(Locale.US, "%.4f, %.4f", lat, lng))
                }
            } catch (_: Exception) {
                Pair("Lokasi GPS", String.format(Locale.US, "%.4f, %.4f", lat, lng))
            }
        }
    }
}
