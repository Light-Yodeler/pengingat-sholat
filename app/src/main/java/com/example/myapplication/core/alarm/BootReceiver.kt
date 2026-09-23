package com.example.myapplication.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.core.preferences.AppPreferences
import com.example.myapplication.core.prayer.PrayerTimeCalculator
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val prefs = AppPreferences(context)
            val savedCity = prefs.loadLocation()
            val savedSettings = prefs.loadSettings()
            val schedule = PrayerTimeCalculator.calculate(
                date = LocalDate.now(),
                latitude = savedCity.latitude,
                longitude = savedCity.longitude,
                altitudeMeters = savedCity.altitudeMeters,
                timeZoneHours = savedCity.timeZoneOffsetHours
            )
            AzanAlarmScheduler.scheduleAllPrayers(context, schedule, savedSettings)
        }
    }
}
