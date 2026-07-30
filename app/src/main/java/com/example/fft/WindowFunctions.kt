package com.example.fft

import com.example.core.ComplexVector
import kotlin.math.cos

/**
 * Window functions for spectral leakage suppression prior to FFT analysis.
 */
enum class WindowType {
    RECTANGULAR,
    HANN,
    HAMMING,
    BLACKMAN,
    FLATTOP
}

/**
 * Generator and applier for DSP window functions.
 */
object WindowFunctions {

    /**
     * Generates a window array of length [size] for the given [type].
     */
    fun generate(type: WindowType, size: Int): FloatArray {
        val window = FloatArray(size)
        when (type) {
            WindowType.RECTANGULAR -> {
                window.fill(1f)
            }
            WindowType.HANN -> {
                for (i in 0 until size) {
                    window[i] = (0.5 - 0.5 * cos(2.0 * Math.PI * i / (size - 1))).toFloat()
                }
            }
            WindowType.HAMMING -> {
                for (i in 0 until size) {
                    window[i] = (0.54 - 0.46 * cos(2.0 * Math.PI * i / (size - 1))).toFloat()
                }
            }
            WindowType.BLACKMAN -> {
                for (i in 0 until size) {
                    val a0 = 0.42
                    val a1 = 0.5
                    val a2 = 0.08
                    window[i] = (a0 - a1 * cos(2.0 * Math.PI * i / (size - 1)) + a2 * cos(4.0 * Math.PI * i / (size - 1))).toFloat()
                }
            }
            WindowType.FLATTOP -> {
                val a0 = 0.21557895
                val a1 = 0.41663158
                val a2 = 0.277263158
                val a3 = 0.083578947
                val a4 = 0.006947368
                for (i in 0 until size) {
                    val angle = 2.0 * Math.PI * i / (size - 1)
                    window[i] = (a0 - a1 * cos(angle) + a2 * cos(2 * angle) - a3 * cos(3 * angle) + a4 * cos(4 * angle)).toFloat()
                }
            }
        }
        return window
    }

    /**
     * Applies the [window] weights in-place to the complex vector [buffer].
     */
    fun applyWindowInPlace(buffer: ComplexVector, window: FloatArray) {
        require(buffer.size == window.size) { "Buffer and window lengths must match." }
        for (i in 0 until buffer.size) {
            val idx = i * 2
            val w = window[i]
            buffer.data[idx] *= w
            buffer.data[idx + 1] *= w
        }
    }

    /**
     * Computes the equivalent noise bandwidth (ENBW) of a given window array.
     */
    fun computeEnbw(window: FloatArray): Float {
        var sumW = 0f
        var sumW2 = 0f
        for (w in window) {
            sumW += w
            sumW2 += w * w
        }
        if (sumW == 0f) return 1f
        return (window.size * sumW2) / (sumW * sumW)
    }
}
