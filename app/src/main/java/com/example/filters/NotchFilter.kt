package com.example.filters

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin

/**
 * Second-order IIR Biquad Notch Filter for narrow interfering tone rejection.
 *
 * @property notchFreqHz Frequency to suppress in Hertz.
 * @property sampleRateHz Sample rate in Hertz.
 * @property qFactor Quality factor Q of the notch (higher Q = narrower rejection band).
 */
class NotchFilter(
    val notchFreqHz: Float,
    val sampleRateHz: Float,
    val qFactor: Float = 30.0f
) {
    init {
        require(notchFreqHz > 0f && notchFreqHz < sampleRateHz / 2f) {
            "Notch frequency must be between 0 and Nyquist."
        }
        require(qFactor > 0f) { "Q factor must be positive." }
    }

    private val b0: Float
    private val b1: Float
    private val b2: Float
    private val a1: Float
    private val a2: Float

    private var x1Re = 0f
    private var x2Re = 0f
    private var y1Re = 0f
    private var y2Re = 0f

    private var x1Im = 0f
    private var x2Im = 0f
    private var y1Im = 0f
    private var y2Im = 0f

    init {
        val omega0 = 2.0 * Math.PI * notchFreqHz / sampleRateHz
        val alpha = sin(omega0) / (2.0 * qFactor)
        val a0 = (1.0 + alpha).toFloat()

        b0 = 1.0f / a0
        b1 = ((-2.0 * cos(omega0)) / a0).toFloat()
        b2 = b0
        a1 = b1
        a2 = ((1.0 - alpha) / a0).toFloat()
    }

    /**
     * Applies notch filter in-place to complex I/Q buffer.
     */
    fun filterInPlace(buffer: ComplexVector) {
        val len = buffer.size
        for (i in 0 until len) {
            val i2 = i * 2
            val xRe = buffer.data[i2]
            val xIm = buffer.data[i2 + 1]

            val yRe = b0 * xRe + b1 * x1Re + b2 * x2Re - a1 * y1Re - a2 * y2Re
            val yIm = b0 * xIm + b1 * x1Im + b2 * x2Im - a1 * y1Im - a2 * y2Im

            x2Re = x1Re
            x1Re = xRe
            y2Re = y1Re
            y1Re = yRe

            x2Im = x1Im
            x1Im = xIm
            y2Im = y1Im
            y1Im = yIm

            buffer.data[i2] = yRe
            buffer.data[i2 + 1] = yIm
        }
    }

    /**
     * Resets filter internal state.
     */
    fun reset() {
        x1Re = 0f; x2Re = 0f; y1Re = 0f; y2Re = 0f
        x1Im = 0f; x2Im = 0f; y1Im = 0f; y2Im = 0f
    }
}
