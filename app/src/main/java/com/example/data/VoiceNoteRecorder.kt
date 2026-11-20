package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class VoiceNoteRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var activeFile: File? = null
    private var recordStartMs: Long = 0L

    fun startRecording(): File? {
        stopRecording()
        return try {
            val dir = File(context.filesDir, "voice_notes").apply { mkdirs() }
            val file = File(dir, "note_${System.currentTimeMillis()}.m4a")
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioSamplingRate(44100)
            mr.setAudioEncodingBitRate(96000)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            activeFile = file
            recordStartMs = System.currentTimeMillis()
            file
        } catch (e: Exception) {
            Log.e("VoiceNote", "Failed to start recording", e)
            null
        }
    }

    fun stopRecording(): Pair<File, Int>? {
        val file = activeFile
        val durationSec = ((System.currentTimeMillis() - recordStartMs) / 1000L).toInt().coerceAtLeast(1)
        try {
            recorder?.stop()
        } catch (_: Exception) {}
        try {
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        activeFile = null
        return if (file != null && file.exists()) file to durationSec else null
    }

    fun play(filePath: String, onFinished: () -> Unit) {
        stopPlayback()
        try {
            player = MediaPlayer().apply {
                setDataSource(filePath)
                setOnCompletionListener {
                    stopPlayback()
                    onFinished()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("VoiceNote", "Failed to play voice note", e)
            onFinished()
        }
    }

    fun stopPlayback() {
        try {
            player?.stop()
        } catch (_: Exception) {}
        try {
            player?.release()
        } catch (_: Exception) {}
        player = null
    }
}
