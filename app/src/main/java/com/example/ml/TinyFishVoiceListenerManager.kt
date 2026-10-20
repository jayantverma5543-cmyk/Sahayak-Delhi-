package com.example.ml

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class TinyFishVoiceListenerManager(
    private val context: Context,
    private val onPanicDetected: (phrase: String) -> Unit
) {
    private val tag = "TinyFishVoiceListener"
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())

    fun startListening() {
        if (isListening) return
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    Log.w(tag, "Speech recognition is not available on this device")
                    return@post
                }

                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                    speechRecognizer?.setRecognitionListener(createListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                Log.i(tag, "TinyFish Voice SOS listener started")
            } catch (e: Exception) {
                Log.e(tag, "Error starting voice recognition", e)
                isListening = false
            }
        }
    }

    fun stopListening() {
        if (!isListening && speechRecognizer == null) return
        mainHandler.post {
            try {
                isListening = false
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                Log.i(tag, "TinyFish Voice SOS listener stopped")
            } catch (e: Exception) {
                Log.e(tag, "Error stopping voice recognition", e)
            }
        }
    }

    fun release() {
        mainHandler.post {
            try {
                stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
                isListening = false
                Log.i(tag, "TinyFish Voice SOS listener released cleanly")
            } catch (e: Exception) {
                Log.e(tag, "Error destroying speech recognizer", e)
            }
        }
    }

    fun providePanicHapticAndAudioFeedback() {
        try {
            // Audio Feedback: Dual high-pitched emergency tone
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 750)

            // Haptic Feedback: Urgent SOS vibration pattern
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 100, 300, 100, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 300, 100, 300, 100, 500), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 300, 100, 300, 100, 500), -1)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Unable to emit haptic/audio feedback", e)
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            // Restart if still marked as listening
            if (isListening) {
                mainHandler.postDelayed({
                    if (isListening) startListening()
                }, 300)
            }
        }

        override fun onError(error: Int) {
            Log.d(tag, "Speech recognizer error code: $error")
            if (isListening) {
                mainHandler.postDelayed({
                    if (isListening) startListening()
                }, 800)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            checkMatches(matches)
            if (isListening) {
                mainHandler.postDelayed({
                    if (isListening) startListening()
                }, 300)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            checkMatches(matches)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}

        private fun checkMatches(matches: List<String>?) {
            if (matches.isNullOrEmpty()) return
            for (candidate in matches) {
                if (TinyFishVoiceSosDetector.detectPanicPhrase(candidate)) {
                    Log.w(tag, "🚨 Panic phrase detected: '$candidate'")
                    providePanicHapticAndAudioFeedback()
                    onPanicDetected(candidate)
                    break
                }
            }
        }
    }
}
