package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlarmAudioController(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var loopJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null

    fun startLoopingAlarm(ringtoneId: String, volume: Float) {
        stopAlarm()
        val safeVol = volume.coerceIn(0.1f, 1.0f)

        val uri = when (ringtoneId) {
            "beacon" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            "pulse" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val startedPlayer = try {
            if (uri != null) {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = true
                    setVolume(safeVol, safeVol)
                    prepare()
                    start()
                }.also { mediaPlayer = it }
                true
            } else false
        } catch (e: Exception) {
            Log.w("AlarmAudio", "Falling back to ToneGenerator loop: ${e.message}")
            false
        }

        loopJob = scope.launch {
            if (!startedPlayer) {
                val volInt = (safeVol * 100).toInt().coerceIn(20, 100)
                toneGenerator = try {
                    ToneGenerator(AudioManager.STREAM_ALARM, volInt)
                } catch (_: Exception) {
                    null
                }
            }
            while (isActive) {
                triggerShortVibration()
                if (!startedPlayer) {
                    val tone = when (ringtoneId) {
                        "beacon" -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                        "pulse" -> ToneGenerator.TONE_PROP_BEEP2
                        "horizon" -> ToneGenerator.TONE_SUP_RINGTONE
                        else -> ToneGenerator.TONE_CDMA_HIGH_L
                    }
                    try {
                        toneGenerator?.startTone(tone, 550)
                    } catch (_: Exception) {}
                }
                delay(1200L)
            }
        }
    }

    fun previewTone(ringtoneId: String, volume: Float) {
        scope.launch {
            startLoopingAlarm(ringtoneId, volume)
            delay(1800L)
            stopAlarm()
        }
    }

    fun stopAlarm() {
        loopJob?.cancel()
        loopJob = null
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {}
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
        toneGenerator = null
    }

    private fun triggerShortVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(350L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(350L)
                }
            }
        } catch (_: Exception) {}
    }
}
