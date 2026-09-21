package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class SoundEffectsHelper(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val vibrator = context.getSystemService(Vibrator::class.java)

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.US
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(0.95f)
            }
        }
    }

    fun playScannerSound() {
        vibrate(80)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 44100
                val durationMs = 350
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 450.0 + (1200.0 * progress) + (80.0 * sin(2.0 * Math.PI * 25.0 * progress))
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val envelope = sin(Math.PI * progress)
                    buffer[i] = (sin(angle) * 16000 * envelope).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
            } catch (_: Exception) {
            }
        }
    }

    fun playSignalChime(isUp: Boolean) {
        vibrate(150)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 44100
                val durationMs = 400
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                val baseFreq = if (isUp) 660.0 else 440.0
                val secondFreq = if (isUp) 880.0 else 330.0

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = if (progress < 0.5) baseFreq else secondFreq
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val envelope = 1.0 - progress
                    buffer[i] = (sin(angle) * 18000 * envelope).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
            } catch (_: Exception) {
            }
        }
    }

    fun playAudioBase64(base64Data: String, mimeType: String = "", onFallbackText: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            var track: AudioTrack? = null
            try {
                val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
                if (audioBytes.isEmpty()) {
                    if (onFallbackText.isNotEmpty()) {
                        speakText(onFallbackText)
                    }
                    return@launch
                }

                // Check if audio data has a RIFF WAV container header
                val isWav = audioBytes.size > 12 &&
                        audioBytes[0] == 'R'.code.toByte() &&
                        audioBytes[1] == 'I'.code.toByte() &&
                        audioBytes[2] == 'F'.code.toByte() &&
                        audioBytes[3] == 'F'.code.toByte()

                val pcmBytes: ByteArray
                val sampleRate: Int

                if (isWav) {
                    sampleRate = ((audioBytes[24].toInt() and 0xFF) or
                            ((audioBytes[25].toInt() and 0xFF) shl 8) or
                            ((audioBytes[26].toInt() and 0xFF) shl 16) or
                            ((audioBytes[27].toInt() and 0xFF) shl 24)).let { if (it > 0) it else 24000 }
                    val headerOffset = 44.coerceAtMost(audioBytes.size)
                    pcmBytes = audioBytes.copyOfRange(headerOffset, audioBytes.size)
                } else {
                    // Gemini TTS generates 24000 Hz 16-bit linear PCM audio
                    sampleRate = 24000
                    pcmBytes = audioBytes
                }

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmBytes.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmBytes, 0, pcmBytes.size)
                track.play()

                val durationMs = (pcmBytes.size.toLong() * 1000L) / (sampleRate * 2L)
                delay(durationMs + 200L)
            } catch (_: Exception) {
                if (onFallbackText.isNotEmpty()) {
                    speakText(onFallbackText)
                }
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun speakText(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SignalSpeech_${System.currentTimeMillis()}")
        }
    }

    fun vibrate(ms: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(ms)
            }
        } catch (_: Exception) {
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}
