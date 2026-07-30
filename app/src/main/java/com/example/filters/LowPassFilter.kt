package com.example.filters

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin

/**
 * FIR Low-Pass Filter with Hamming windowed-sinc tap design and zero-allocation block filtering.
 *
 * @property numTaps Number of FIR filter taps (must be odd).
 * @property cutoffHz Cutoff frequency in Hertz.
 * @property sampleRateHz Sample rate in Hertz.
 */
class LowPassFilter(
    val numTaps: Int,
    val cutoffHz: Float,
    val sampleRateHz: Float
) {
    init {
        require(numTaps > 0 && numTaps % 2 == 1) { "numTaps must be positive odd integer." }
        require(cutoffHz > 0f && cutoffHz < sampleRateHz / 2f) { "Cutoff must be between 0 and Nyquist." }
    }

    val taps: FloatArray = FloatArray(numTaps)
    private val delayLineRe = FloatArray(numTaps)
    private val delayLineIm = FloatArray(numTaps)
    private var delayIdx = 0

    init {
        val fc = cutoffHz / sampleRateHz
        val center = (numTaps - 1) / 2
        var sum = 0f
        for (i in 0 until numTaps) {
            val n = i - center
            val valSinc = if (n == 0) {
                2f * fc
            } else {
                (sin(2.0 * Math.PI * fc * n) / (Math.PI * n)).toFloat()
            }
            // Hamming window
            val win = (0.54 - 0.46 * cos(2.0 * Math.PI * i / (numTaps - 1))).toFloat()
            taps[i] = valSinc * win
            sum += taps[i]
        }
        // Normalize unity gain at DC
        if (sum != 0f) {
            for (i in 0 until numTaps) {
                taps[i] /= sum
            }
        }
    }

    /**
     * Filters complex I/Q samples in-place.
     */
    fun filterInPlace(buffer: ComplexVector) {
        val len = buffer.size
        for (i in 0 until len) {
            val i2 = i * 2
            val re = buffer.data[i2]
            val im = buffer.data[i2 + 1]

            delayLineRe[delayIdx] = re
            delayLineIm[delayIdx] = im

            var accRe = 0f
            var accIm = 0f
            var idx = delayIdx
            for (t in 0 until numTaps) {
                val coeff = taps[t]
                accRe += delayLineRe[idx] * coeff
                accIm += delayLineIm[idx] * coeff
                idx--
                if (idx < 0) idx = numTaps - 1
            }

            buffer.data[i2] = accRe
            buffer.data[i2 + 1] = accIm

            delayIdx = (delayIdx + 1) % numTaps
        }
    }

    /**
     * Clears internal delay line state.
     */
    fun reset() {
        delayLineRe.fill(0f)
        delayLineIm.fill(0f)
        delayIdx = 0
    }
}
