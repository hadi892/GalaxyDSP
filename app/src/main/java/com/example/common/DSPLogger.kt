package com.example.common

import android.util.Log

/**
 * Structured logging utility for DSP real-time telemetry, pipeline events, and error tracing.
 */
object DSPLogger {
    private const val DEFAULT_TAG = "GalaxyDSP"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, "[DEBUG] $message")
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, "[INFO] $message")
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, "[WARN] $message", throwable)
        } else {
            Log.w(tag, "[WARN] $message")
        }
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, "[ERROR] $message", throwable)
        } else {
            Log.e(tag, "[ERROR] $message")
        }
    }
}
