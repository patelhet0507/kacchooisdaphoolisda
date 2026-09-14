package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEffectsManager private constructor(private val context: Context) {
    private val settingsManager = SettingsManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sampleRate = 44100

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isSoundEnabled(): Boolean = settingsManager.settings.value.soundEffectsEnabled
    private fun getVolume(): Float = settingsManager.settings.value.soundVolume
    private fun isHapticEnabled(): Boolean = settingsManager.settings.value.hapticFeedbackEnabled

    fun vibrate(durationMs: Long = 30L, amplitude: Int = 120) {
        if (!isHapticEnabled()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    private fun playPcm(samples: ShortArray, volumeMultiplier: Float = 1.0f) {
        if (!isSoundEnabled()) return
        val vol = (getVolume() * volumeMultiplier).coerceIn(0f, 1f)
        if (vol <= 0.01f) return

        scope.launch {
            try {
                val scaledSamples = ShortArray(samples.size)
                for (i in samples.indices) {
                    scaledSamples[i] = (samples[i] * vol).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(scaledSamples, 0, scaledSamples.size)
                audioTrack.play()

                // Release after sound finishes
                val durationMs = (samples.size * 1000L) / sampleRate
                kotlinx.coroutines.delay(durationMs + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
            } catch (e: Exception) {
                Log.w("SoundEffectsManager", "Error playing sound: ${e.message}")
            }
        }
    }

    /**
     * Card Deal Sound Effect:
     * Fast card sliding swish off the deck.
     */
    fun playCardDeal() {
        vibrate(20, 80)
        val numSamples = (sampleRate * 0.13).toInt() // 130ms
        val samples = ShortArray(numSamples)
        val rand = Random(42)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            // Exponential envelope attack and decay
            val envelope = if (t < 0.15) (t / 0.15) else exp(-5.0 * (t - 0.15))
            // Filtered white noise with subtle mid-pitch swish
            val noise = (rand.nextDouble() * 2.0 - 1.0)
            val tone = sin(2 * PI * (900 + 400 * (1.0 - t)) * (i.toDouble() / sampleRate))
            val sampleVal = (0.75 * noise + 0.25 * tone) * envelope * 24000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.9f)
    }

    /**
     * Card Play Sound Effect:
     * Crisp, tactile slap/snap of a card landing on the table felt.
     */
    fun playCardPlay() {
        vibrate(35, 140)
        val numSamples = (sampleRate * 0.11).toInt() // 110ms
        val samples = ShortArray(numSamples)
        val rand = Random(123)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            // Rapid attack thud and crisp snap
            val envelope = if (t < 0.05) (t / 0.05) else exp(-12.0 * (t - 0.05))
            val thud = sin(2 * PI * (220.0 * exp(-18.0 * t)) * (i.toDouble() / sampleRate))
            val snap = (rand.nextDouble() * 2.0 - 1.0) * exp(-25.0 * t)
            val sampleVal = (0.6 * thud + 0.4 * snap) * envelope * 28000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 1.0f)
    }

    /**
     * Card Flip / Tap Sound:
     * Short, clean click.
     */
    fun playCardFlip() {
        vibrate(15, 60)
        val numSamples = (sampleRate * 0.06).toInt() // 60ms
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val env = exp(-20.0 * t)
            val tone = sin(2 * PI * (1200 - 600 * t) * (i.toDouble() / sampleRate))
            val sampleVal = tone * env * 20000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.7f)
    }

    /**
     * Trick Win Sound:
     * Melodic 2-tone chime (D5 -> A5) celebrating winning the trick.
     */
    fun playTrickWin() {
        vibrate(45, 160)
        val numSamples = (sampleRate * 0.35).toInt() // 350ms
        val samples = ShortArray(numSamples)
        val split = numSamples / 2

        for (i in 0 until numSamples) {
            val isFirstNote = i < split
            val noteFreq = if (isFirstNote) 587.33 else 880.0 // D5, A5
            val noteTime = if (isFirstNote) i.toDouble() / split else (i - split).toDouble() / (numSamples - split)
            val env = exp(-6.0 * noteTime)
            val harmonic1 = sin(2 * PI * noteFreq * (i.toDouble() / sampleRate))
            val harmonic2 = 0.3 * sin(2 * PI * (noteFreq * 2) * (i.toDouble() / sampleRate))
            val sampleVal = (harmonic1 + harmonic2) * env * 22000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.85f)
    }

    /**
     * Round Victory Fanfare:
     * Ascending 4-chord celebration (C5 -> E5 -> G5 -> C6).
     */
    fun playRoundWin() {
        vibrate(60, 200)
        val numSamples = (sampleRate * 0.65).toInt() // 650ms
        val samples = ShortArray(numSamples)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteLen = numSamples / notes.size

        for (i in 0 until numSamples) {
            val noteIdx = (i / noteLen).coerceIn(0, notes.size - 1)
            val noteFreq = notes[noteIdx]
            val tInNote = (i % noteLen).toDouble() / noteLen
            val env = exp(-4.5 * tInNote)
            val fundamental = sin(2 * PI * noteFreq * (i.toDouble() / sampleRate))
            val overtone = 0.25 * sin(2 * PI * (noteFreq * 2) * (i.toDouble() / sampleRate))
            val sampleVal = (fundamental + overtone) * env * 22000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.95f)
    }

    /**
     * Trump Suit Announcement Sound:
     * Regal brass-like trumpet chime (G5 -> C6).
     */
    fun playTrumpAnnounce() {
        vibrate(30, 110)
        val numSamples = (sampleRate * 0.38).toInt() // 380ms
        val samples = ShortArray(numSamples)
        val split = (numSamples * 0.4).toInt()

        for (i in 0 until numSamples) {
            val isFirst = i < split
            val freq = if (isFirst) 783.99 else 1046.50 // G5, C6
            val noteT = if (isFirst) i.toDouble() / split else (i - split).toDouble() / (numSamples - split)
            val env = exp(-5.0 * noteT)
            val tone = sin(2 * PI * freq * (i.toDouble() / sampleRate)) + 0.3 * sin(2 * PI * (freq * 1.5) * (i.toDouble() / sampleRate))
            val sampleVal = tone * env * 21000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.85f)
    }

    /**
     * Dealer Hook Warning Alert:
     * Urgent double-ping warning dealer when forbidden bid is selected.
     */
    fun playDealerHookAlert() {
        vibrate(50, 180)
        val numSamples = (sampleRate * 0.26).toInt() // 260ms
        val samples = ShortArray(numSamples)
        val split = numSamples / 2

        for (i in 0 until numSamples) {
            val isFirst = i < split
            val freq = if (isFirst) 440.0 else 330.0 // A4, E4
            val tInNote = if (isFirst) i.toDouble() / split else (i - split).toDouble() / (numSamples - split)
            val env = exp(-8.0 * tInNote)
            val tone = sin(2 * PI * freq * (i.toDouble() / sampleRate))
            val sampleVal = tone * env * 24000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.9f)
    }

    /**
     * Button Tap / General UI interaction sound
     */
    fun playButtonTap() {
        vibrate(10, 50)
        val numSamples = (sampleRate * 0.04).toInt() // 40ms
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val env = exp(-25.0 * t)
            val tone = sin(2 * PI * 800.0 * (i.toDouble() / sampleRate))
            val sampleVal = tone * env * 16000
            samples[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples, 0.5f)
    }

    companion object {
        @Volatile
        private var INSTANCE: SoundEffectsManager? = null

        fun getInstance(context: Context): SoundEffectsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundEffectsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
