package com.example.myapplication.core.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
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

class AzanPlaybackService : Service() {

    companion object {
        const val ACTION_PLAY_AZAN = "com.example.myapplication.ACTION_PLAY_AZAN"
        const val ACTION_STOP_AZAN = "com.example.myapplication.ACTION_STOP_AZAN"

        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_ALERT_TYPE = "EXTRA_ALERT_TYPE"
        const val EXTRA_RAW_RES_ID = "EXTRA_RAW_RES_ID"

        private const val CHANNEL_ID = "noor_waktu_azan_service_channel_v2"
        private const val NOTIFICATION_ID = 2001

        private val _isAzanPlaying = MutableStateFlow(false)
        val isAzanPlaying: StateFlow<Boolean> = _isAzanPlaying.asStateFlow()

        private val _currentPlayingPrayer = MutableStateFlow<String?>(null)
        val currentPlayingPrayer: StateFlow<String?> = _currentPlayingPrayer.asStateFlow()

        fun stopAzan(context: Context) {
            val intent = Intent(context, AzanPlaybackService::class.java).apply {
                action = ACTION_STOP_AZAN
            }
            context.startService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var playStartTime: Long = 0L
    private var volumeKeyReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_STOP_AZAN -> {
                handleStopAzan()
                return START_NOT_STICKY
            }
            ACTION_PLAY_AZAN -> {
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

                startForegroundWithNotification(prayerName, alertType)

                if (alertType == AlertType.AZAN) {
                    startPlayingAudio(finalResId, prayerName)
                } else if (alertType == AlertType.SILENT) {
                    triggerVibrationOnly()
                } else {
                    handleStopAzan()
                }
            }
            else -> {
                handleStopAzan()
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(prayerName: String, alertType: AlertType) {
        val notification = buildForegroundNotification(prayerName, alertType)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                }
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("AzanPlaybackService", "Error starting foreground service", e)
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startPlayingAudio(rawResId: Int, prayerName: String) {
        stopPlayerResources()

        try {
            playStartTime = System.currentTimeMillis()
            registerVolumeKeyObserver()

            _isAzanPlaying.value = true
            _currentPlayingPrayer.value = prayerName

            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoorWaktu:AzanPlaybackWakeLock")?.apply {
                acquire(4 * 60 * 1000L) // 4 minutes max
            }

            try {
                @Suppress("DEPRECATION")
                val screenLock = pm?.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "NoorWaktu:ScreenWakeLock"
                )
                screenLock?.acquire(30 * 1000L) // Turn on screen for 30s
            } catch (_: Exception) {
            }

            val prefs = com.example.myapplication.core.preferences.AppPreferences(this)
            val settings = try { prefs.loadSettings() } catch (_: Exception) { null }
            val volumeRatio = ((settings?.azanVolumePercent ?: 85) / 100f).coerceIn(0.1f, 1f)

            val player = MediaPlayer().apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(attrs)
                val afd = resources.openRawResourceFd(rawResId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setVolume(volumeRatio, volumeRatio)
            }

            player.isLooping = false
            player.setOnCompletionListener {
                android.util.Log.i("AzanPlaybackService", "Azan finished playing naturally")
                handleStopAzan()
            }
            player.setOnErrorListener { _, what, extra ->
                android.util.Log.e("AzanPlaybackService", "MediaPlayer error: what=$what, extra=$extra")
                handleStopAzan()
                true
            }

            player.start()
            mediaPlayer = player
            android.util.Log.i("AzanPlaybackService", "MediaPlayer started playing azan for $prayerName")
        } catch (e: Exception) {
            android.util.Log.e("AzanPlaybackService", "Failed to start media player", e)
            handleStopAzan()
        }
    }

    private fun triggerVibrationOnly() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
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
        // For silent vibration, complete service after 4 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            handleStopAzan()
        }, 4000L)
    }

    private fun registerVolumeKeyObserver() {
        unregisterVolumeKeyObserver()
        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                        if (System.currentTimeMillis() - playStartTime > 600L) {
                            android.util.Log.i("AzanPlaybackService", "Volume key pressed, stopping azan!")
                            handleStopAzan()
                        }
                    }
                }
            }
            volumeKeyReceiver = receiver
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            android.util.Log.e("AzanPlaybackService", "Failed to register volume key observer", e)
        }
    }

    private fun unregisterVolumeKeyObserver() {
        try {
            volumeKeyReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {
        } finally {
            volumeKeyReceiver = null
        }
    }

    private fun stopPlayerResources() {
        _isAzanPlaying.value = false
        _currentPlayingPrayer.value = null
        unregisterVolumeKeyObserver()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
            } catch (_: Exception) {
            }
            wakeLock = null
        }
    }

    private fun handleStopAzan() {
        stopPlayerResources()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopPlayerResources()
        super.onDestroy()
    }

    private fun buildForegroundNotification(prayerName: String, alertType: AlertType): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AzanPlaybackService::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Waktu Salat $prayerName Telah Tiba"
        val message = if (alertType == AlertType.AZAN) {
            "Hayya 'alas-shalah, mari tunaikan ibadah salat tepat waktu."
        } else {
            "Pengingat waktu $prayerName (Mode Senyap)."
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(alertType == AlertType.AZAN)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .setFullScreenIntent(openPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (alertType == AlertType.AZAN) {
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Hentikan Azan",
                stopPendingIntent
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Waktu Azan & Salat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi suara azan dan waktu salat otomatis di latar belakang"
                enableVibration(true)
                setSound(null, null)
            }
            nm.createNotificationChannel(channel)
        }
    }
}
