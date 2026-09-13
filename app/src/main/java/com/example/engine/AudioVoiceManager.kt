package com.example.engine

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AudioVoiceManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun startRecording(): Boolean {
        return try {
            stopPlaying()
            val outputFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recordingStartTime = System.currentTimeMillis()
            _isRecording.value = true
            true
        } catch (e: Exception) {
            Log.e("AudioVoiceManager", "Error starting voice recording", e)
            _isRecording.value = false
            false
        }
    }

    suspend fun stopRecording(): Pair<String, Long>? = withContext(Dispatchers.IO) {
        val recorder = mediaRecorder ?: return@withContext null
        val file = currentRecordingFile ?: return@withContext null
        val duration = System.currentTimeMillis() - recordingStartTime

        try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null
            _isRecording.value = false

            if (!file.exists() || duration < 300) {
                file.delete()
                return@withContext null
            }

            val bytes = FileInputStream(file).use { it.readBytes() }
            val base64Audio = Base64.encodeToString(bytes, Base64.NO_WRAP)
            file.delete()
            Pair(base64Audio, duration)
        } catch (e: Exception) {
            Log.e("AudioVoiceManager", "Error stopping voice recording", e)
            mediaRecorder = null
            _isRecording.value = false
            null
        }
    }

    fun playVoiceAudio(base64Audio: String, onCompletion: () -> Unit = {}) {
        if (base64Audio.isBlank()) return
        try {
            stopPlaying()
            val decodedBytes = Base64.decode(base64Audio, Base64.DEFAULT)
            val tempFile = File(context.cacheDir, "temp_play_${System.currentTimeMillis()}.m4a")
            FileOutputStream(tempFile).use { it.write(decodedBytes) }

            val player = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    tempFile.delete()
                    onCompletion()
                }
                start()
            }
            mediaPlayer = player
            _isPlaying.value = true
        } catch (e: Exception) {
            Log.e("AudioVoiceManager", "Error playing voice audio", e)
            _isPlaying.value = false
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
        } catch (e: Exception) {
            Log.e("AudioVoiceManager", "Error stopping audio player", e)
        }
    }

    fun release() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioVoiceManager", "Error releasing audio resources", e)
        }
    }
}
