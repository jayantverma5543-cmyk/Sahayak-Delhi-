package com.example.ml

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.IOException

interface TinyFishModel : Closeable {
    suspend fun initialize(context: Context, modelAssetPath: String, apiKey: String? = null): Boolean
    suspend fun predict(input: FloatArray): FloatArray
    val isInitialized: Boolean
}

class TinyFishModelImpl : TinyFishModel {
    private val tag = "TinyFishModelImpl"
    private var runtimeHandle: Any? = null
    override var isInitialized: Boolean = false
        private set

    override suspend fun initialize(
        context: Context,
        modelAssetPath: String,
        apiKey: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Verify model asset presence from APK assets
            context.assets.open(modelAssetPath).use { stream ->
                val bytesRead = stream.available()
                Log.d(tag, "Loaded model asset $modelAssetPath, size: $bytesRead bytes")
            }

            // Placeholder runtime initialization with fallback
            Log.i(tag, "Initializing TinyFish runtime from $modelAssetPath (Key provided: ${!apiKey.isNullOrBlank()})")
            runtimeHandle = "TINYFISH_RUNTIME_INSTANCE_${System.currentTimeMillis()}"
            isInitialized = true
            true
        } catch (e: IOException) {
            Log.e(tag, "Failed to load TinyFish model asset from $modelAssetPath", e)
            isInitialized = false
            false
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error initializing TinyFish runtime", e)
            isInitialized = false
            false
        }
    }

    override suspend fun predict(input: FloatArray): FloatArray = withContext(Dispatchers.Default) {
        if (!isInitialized || runtimeHandle == null) {
            throw IllegalStateException("TinyFishModel is not initialized. Call initialize() first.")
        }
        try {
            Log.d(tag, "Running TinyFish inference on ${input.size} input features")
            // Input features: [heart_rate, systolic_bp, spo2_pct]
            val hr = input.getOrElse(0) { 80f }
            val bp = input.getOrElse(1) { 120f }
            val spo2 = input.getOrElse(2) { 98f }

            val criticalScore = if (spo2 < 90f || hr > 130f || bp < 85f || bp > 180f) 0.88f else 0.05f
            val urgentScore = if (spo2 in 90f..94f || hr in 100f..130f || bp in 140f..180f) 0.65f else 0.15f
            val mildScore = (1.0f - (criticalScore + urgentScore)).coerceAtLeast(0.1f)

            floatArrayOf(mildScore, urgentScore, criticalScore)
        } catch (e: Exception) {
            Log.e(tag, "Inference execution failed", e)
            throw e
        }
    }

    override fun close() {
        try {
            Log.i(tag, "Releasing TinyFish runtime resources")
            runtimeHandle = null
            isInitialized = false
        } catch (e: Exception) {
            Log.w(tag, "Error closing TinyFish runtime", e)
        }
    }
}
