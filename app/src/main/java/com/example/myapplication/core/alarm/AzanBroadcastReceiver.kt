package com.example.myapplication.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.core.model.AlertType
import com.example.myapplication.core.prayer.PrayerTimeCalculator
import com.example.myapplication.core.preferences.AppPreferences
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class AzanBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.myapplication.ACTION_PRAYER_ALARM"
        const val ACTION_STOP_AZAN = "com.example.myapplication.ACTION_STOP_AZAN"

        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_ALERT_TYPE = "EXTRA_ALERT_TYPE"
        const val EXTRA_RAW_RES_ID = "EXTRA_RAW_RES_ID"

        val isAzanPlaying: StateFlow<Boolean>
            get() = AzanPlaybackService.isAzanPlaying

        val currentPlayingPrayer: StateFlow<String?>
            get() = AzanPlaybackService.currentPlayingPrayer

        fun stopActiveAzan(context: Context) {
            AzanPlaybackService.stopAzan(context)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_AZAN) {
            stopActiveAzan(context)
            return
        }

        if (intent.action == ACTION_PRAYER_ALARM) {
            val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salat"
            val alertTypeStr = intent.getStringExtra(EXTRA_ALERT_TYPE) ?: AlertType.AZAN.name
            val defaultRes = if (prayerName.contains("Subuh", ignoreCase = true)) {
                R.raw.azan_fajr_nafea
            } else {
                R.raw.azan_mekah
            }
            val rawResId = intent.getIntExtra(EXTRA_RAW_RES_ID, defaultRes)
            val finalResId = if (rawResId == 0) defaultRes else rawResId

            val alertType = try {
                AlertType.valueOf(alertTypeStr)
            } catch (_: Exception) {
                AlertType.AZAN
            }

            android.util.Log.i("AzanBroadcastReceiver", "onReceive: prayer=$prayerName, alertType=$alertType, resId=$finalResId")

            if (alertType != AlertType.OFF) {
                val serviceIntent = Intent(context, AzanPlaybackService::class.java).apply {
                    action = AzanPlaybackService.ACTION_PLAY_AZAN
                    putExtra(AzanPlaybackService.EXTRA_PRAYER_NAME, prayerName)
                    putExtra(AzanPlaybackService.EXTRA_ALERT_TYPE, alertType.name)
                    putExtra(AzanPlaybackService.EXTRA_RAW_RES_ID, finalResId)
                }

                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    android.util.Log.e("AzanBroadcastReceiver", "Failed to start AzanPlaybackService", e)
                }
            }

            // Always reschedule all prayers so tomorrow's alarms and upcoming prayers are continuously set
            autoRescheduleUpcomingPrayers(context)
        }
    }

    private fun autoRescheduleUpcomingPrayers(context: Context) {
        try {
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
            android.util.Log.i("AzanBroadcastReceiver", "Auto-rescheduled all prayer alarms successfully")
        } catch (e: Exception) {
            android.util.Log.e("AzanBroadcastReceiver", "Failed to auto-reschedule prayers", e)
        }
    }
}
