package com.example.myapplication.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.core.location.LocationPresets
import com.example.myapplication.core.model.AppSettings
import com.example.myapplication.core.prayer.PrayerTimeCalculator
import java.time.LocalDate

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val defaultCity = LocationPresets.defaultCity
            val schedule = PrayerTimeCalculator.calculate(
                date = LocalDate.now(),
                latitude = defaultCity.latitude,
                longitude = defaultCity.longitude,
                altitudeMeters = defaultCity.altitudeMeters,
                timeZoneHours = defaultCity.timeZoneOffsetHours
            )
            AzanAlarmScheduler.scheduleAllPrayers(context, schedule, AppSettings())
        }
    }
}
