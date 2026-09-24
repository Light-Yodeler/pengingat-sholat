package com.example.myapplication.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.core.model.AlertType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AzanBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.myapplication.ACTION_PRAYER_ALARM"
        const val ACTION_STOP_AZAN = "com.example.myapplication.ACTION_STOP_AZAN"

        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_ALERT_TYPE = "EXTRA_ALERT_TYPE"
        const val EXTRA_RAW_RES_ID = "EXTRA_RAW_RES_ID"

        private const val CHANNEL_ID = "noor_waktu_azan_channel_v1"
        private const val NOTIFICATION_ID = 1001

        private val _isAzanPlaying = MutableStateFlow(false)
        val isAzanPlaying: StateFlow<Boolean> = _isAzanPlaying.asStateFlow()

        private val _currentPlayingPrayer = MutableStateFlow<String?>(null)
        val currentPlayingPrayer: StateFlow<String?> = _currentPlayingPrayer.asStateFlow()

        private var activePlayer: MediaPlayer? = null
        private var wakeLock: PowerManager.WakeLock? = null

        private fun stopActivePlayerOnly() {
            _isAzanPlaying.value = false
            _currentPlayingPrayer.value = null
            try {
                if (activePlayer?.isPlaying == true) {
                    activePlayer?.stop()
                }
                activePlayer?.release()
            } catch (_: Exception) {
            } finally {
                activePlayer = null
                try {
                    if (wakeLock?.isHeld == true) {
                        wakeLock?.release()
                    }
                } catch (_: Exception) {
                }
                wakeLock = null
            }
        }

        fun stopActiveAzan(context: Context) {
            stopActivePlayerOnly()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(NOTIFICATION_ID)
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
            val defaultRes = if (prayerName.contains("Subuh", ignoreCase = true)) R.raw.azan_fajr_nafea else R.raw.azan_mekah
            val rawResId = intent.getIntExtra(EXTRA_RAW_RES_ID, defaultRes)
            val finalResId = if (rawResId == 0) defaultRes else rawResId

            val alertType = try {
                AlertType.valueOf(alertTypeStr)
            } catch (_: Exception) {
                AlertType.AZAN
            }

            android.util.Log.i("NoorWaktuAzan", "onReceive: prayer=$prayerName, alertType=$alertType, resId=$finalResId")

            if (alertType == AlertType.OFF) return

            createNotificationChannel(context)
            showNotification(context, prayerName, alertType)

            if (alertType == AlertType.AZAN) {
                playAzanAudio(context, finalResId, prayerName)
            } else if (alertType == AlertType.SILENT) {
                triggerSilentVibration(context)
            }
        }
    }

    private fun playAzanAudio(context: Context, rawResId: Int, prayerName: String) {
        stopActivePlayerOnly()

        try {
            _isAzanPlaying.value = true
            _currentPlayingPrayer.value = prayerName
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoorWaktu:AzanWakeLock")?.apply {
                acquire(4 * 60 * 1000L) // 4 minutes max for Azan duration
            }

            val player = MediaPlayer().apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(attrs)
                val afd = context.resources.openRawResourceFd(rawResId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
            }
            player.isLooping = false
            player.setOnCompletionListener {
                stopActiveAzan(context)
            }
            player.start()
            activePlayer = player
            android.util.Log.i("NoorWaktuAzan", "MediaPlayer started playing successfully")
        } catch (e: Exception) {
            android.util.Log.e("NoorWaktuAzan", "Failed to play azan audio", e)
            activePlayer = null
            _isAzanPlaying.value = false
            _currentPlayingPrayer.value = null
        }
    }



    private fun triggerSilentVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = longArrayOf(0, 500, 300, 500, 300, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val attrs = VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_ALARM)
                            .build()
                        vibrator.vibrate(effect, attrs)
                    } else {
                        vibrator.vibrate(effect)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun showNotification(context: Context, prayerName: String, alertType: AlertType) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AzanBroadcastReceiver::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deleteIntent = Intent(context, AzanBroadcastReceiver::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Waktu Salat $prayerName Telah Tiba"
        val message = if (alertType == AlertType.AZAN) {
            "Hayya 'alas-shalah, mari tunaikan ibadah salat tepat waktu."
        } else {
            "Pengingat waktu $prayerName (Mode Senyap)."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setOngoing(alertType == AlertType.AZAN)

        if (alertType == AlertType.AZAN) {
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Hentikan Azan",
                stopPendingIntent
            )
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NOTIFICATION_ID, builder.build())
        android.util.Log.i("NoorWaktuAzan", "Notification successfully posted for $prayerName")
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Waktu Azan & Salat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi suara azan dan waktu salat otomatis"
                enableVibration(true)
                setSound(null, null)
            }
            nm.createNotificationChannel(channel)
        }
    }
}
