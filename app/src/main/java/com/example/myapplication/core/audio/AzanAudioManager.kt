package com.example.myapplication.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.myapplication.R

class AzanAudioManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun playPreview(rawResId: Int = R.raw.azan_mekah, volumePercent: Int = 85, onComplete: () -> Unit) {
        stop()
        try {
            val player = MediaPlayer().apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(attrs)
                val afd = context.resources.openRawResourceFd(rawResId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
            }
            mediaPlayer = player
            val volume = (volumePercent / 100f).coerceIn(0.0f, 1.0f)
            player.setVolume(volume, volume)
            player.setOnCompletionListener {
                stop()
                onComplete()
            }
            player.start()
            isPlaying = true
        } catch (_: Exception) {
            isPlaying = false
            onComplete()
        }
    }

    fun updateVolume(volumePercent: Int) {
        val volume = (volumePercent / 100f).coerceIn(0.0f, 1.0f)
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (_: Exception) {
        }
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }
}

