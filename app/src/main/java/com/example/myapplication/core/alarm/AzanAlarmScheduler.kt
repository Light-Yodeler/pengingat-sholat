package com.example.myapplication.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.core.model.AlertType
import com.example.myapplication.core.model.AppSettings
import com.example.myapplication.core.model.MuazzinList
import com.example.myapplication.core.prayer.PrayerSchedule
import com.example.myapplication.core.prayer.PrayerType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AzanAlarmScheduler {

    private const val REQUEST_CODE_BASE = 2000

    fun scheduleAllPrayers(
        context: Context,
        schedule: PrayerSchedule,
        settings: AppSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val prayers = listOf(
            PrayerType.SUBUH to schedule.subuh,
            PrayerType.DZUHUR to schedule.dzuhur,
            PrayerType.ASAR to schedule.asar,
            PrayerType.MAGHRIB to schedule.maghrib,
            PrayerType.ISYA to schedule.isya
        )

        val selectedSubuhResId = if (settings.selectedSubuhMuazzinId == 2) {
            R.raw.azan_fajr_madinah
        } else {
            R.raw.azan_fajr_nafea
        }

        val selectedRegularResId = if (settings.selectedRegularMuazzinId == 2) {
            R.raw.azan_madinah
        } else {
            R.raw.azan_mekah
        }

        prayers.forEachIndexed { index, (type, prayerTime) ->
            val alertType = settings.prayerAlertTypes[type] ?: AlertType.AZAN
            if (alertType == AlertType.OFF) {
                cancelAlarm(context, index)
                return@forEachIndexed
            }

            val audioResId = if (type == PrayerType.SUBUH) {
                selectedSubuhResId
            } else {
                selectedRegularResId
            }

            val triggerMillis = computeNextTriggerMillis(prayerTime)
            scheduleExactAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = REQUEST_CODE_BASE + index,
                triggerMillis = triggerMillis,
                prayerName = type.displayName,
                alertType = alertType,
                rawResId = audioResId
            )
        }
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerMillis: Long,
        prayerName: String,
        alertType: AlertType,
        rawResId: Int
    ) {
        val intent = Intent(context, AzanBroadcastReceiver::class.java).apply {
            action = AzanBroadcastReceiver.ACTION_PRAYER_ALARM
            putExtra(AzanBroadcastReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AzanBroadcastReceiver.EXTRA_ALERT_TYPE, alertType.name)
            putExtra(AzanBroadcastReceiver.EXTRA_RAW_RES_ID, rawResId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (_: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } catch (_: Exception) {
            }
        }
    }

    fun cancelAllPrayers(context: Context) {
        val count = 5
        for (i in 0 until count) {
            cancelAlarm(context, i)
        }
    }

    private fun cancelAlarm(context: Context, index: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AzanBroadcastReceiver::class.java).apply {
            action = AzanBroadcastReceiver.ACTION_PRAYER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + index,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun computeNextTriggerMillis(prayerTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var targetDateTime = LocalDateTime.of(LocalDate.now(), prayerTime)
        if (targetDateTime.isBefore(now) || targetDateTime.isEqual(now)) {
            // If already passed today, schedule for tomorrow
            targetDateTime = targetDateTime.plusDays(1)
        }
        val zone = ZoneId.systemDefault()
        return targetDateTime.atZone(zone).toInstant().toEpochMilli()
    }
}
