package com.example.myapplication.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myapplication.core.location.LocationPresets
import com.example.myapplication.core.model.AppSettings
import com.example.myapplication.core.prayer.PrayerTimeCalculator
import com.example.myapplication.core.preferences.AppPreferences
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
        private const val ACTION_HTC_QUICKBOOT = "com.htc.intent.action.QUICKBOOT_POWERON"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        android.util.Log.i("BootReceiver", "Received broadcast action: $action")

        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_QUICKBOOT_POWERON,
            ACTION_HTC_QUICKBOOT
        )

        if (validActions.contains(action)) {
            val targetContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (context.isDeviceProtectedStorage) context else context.createDeviceProtectedStorageContext()
            } else {
                context
            }

            try {
                val prefs = AppPreferences(targetContext)
                val savedCity = try {
                    prefs.loadLocation()
                } catch (_: Exception) {
                    LocationPresets.defaultCity
                }
                val savedSettings = try {
                    prefs.loadSettings()
                } catch (_: Exception) {
                    AppSettings()
                }

                val schedule = PrayerTimeCalculator.calculate(
                    date = LocalDate.now(),
                    latitude = savedCity.latitude,
                    longitude = savedCity.longitude,
                    altitudeMeters = savedCity.altitudeMeters,
                    timeZoneHours = savedCity.timeZoneOffsetHours
                )
                AzanAlarmScheduler.scheduleAllPrayers(targetContext, schedule, savedSettings)
                android.util.Log.i("BootReceiver", "All prayer alarms restored successfully on $action for ${savedCity.name}")
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "Failed to restore alarms on action $action", e)
                try {
                    val defaultCity = LocationPresets.defaultCity
                    val defaultSettings = AppSettings()
                    val schedule = PrayerTimeCalculator.calculate(
                        date = LocalDate.now(),
                        latitude = defaultCity.latitude,
                        longitude = defaultCity.longitude,
                        altitudeMeters = defaultCity.altitudeMeters,
                        timeZoneHours = defaultCity.timeZoneOffsetHours
                    )
                    AzanAlarmScheduler.scheduleAllPrayers(targetContext, schedule, defaultSettings)
                    android.util.Log.i("BootReceiver", "Restored alarms with fallback presets")
                } catch (_: Exception) {
                }
            }
        }
    }
}
