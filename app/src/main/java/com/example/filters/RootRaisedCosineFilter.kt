package com.example.filters

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Root Raised Cosine (RRC) FIR Filter for DVB-S pulse shaping and matched filtering.
 *
 * @property numTaps Number of FIR taps (must be odd).
 * @property rollOff Roll-off factor (alpha), standard DVB-S uses 0.35.
 * @property samplesPerSymbol Over-sampling ratio (e.g., 2 or 4).
 */
class RootRaisedCosineFilter(
    val numTaps: Int,
    val rollOff: Float = 0.35f,
    val samplesPerSymbol: Int = 2
) {
    init {
        require(numTaps > 0 && numTaps % 2 == 1) { "numTaps must be positive odd integer." }
        require(rollOff in 0.0f..1.0f) { "rollOff must be in range [0.0, 1.0]." }
        require(samplesPerSymbol >= 1) { "samplesPerSymbol must be at least 1." }
    }

    val taps: FloatArray = FloatArray(numTaps)
    private val delayLineRe = FloatArray(numTaps)
    private val delayLineIm = FloatArray(numTaps)
    private var delayIdx = 0

    init {
        val center = (numTaps - 1) / 2
        var sumPower = 0f
        for (i in 0 until numTaps) {
            val t = (i - center).toFloat() / samplesPerSymbol
            taps[i] = computeRrcTap(t, rollOff)
            sumPower += taps[i] * taps[i]
        }
        // Normalize unity energy
        val norm = if (sumPower > 0f) 1.0f / sqrt(sumPower) else 1.0f
        for (i in 0 until numTaps) {
            taps[i] *= norm
        }
    }

    private fun computeRrcTap(t: Float, alpha: Float): Float {
        if (t == 0f) {
            return (1.0f - alpha + (4.0f * alpha / Math.PI)).toFloat()
        }
        val absT = if (t < 0f) -t else t
        if (Math.abs(16.0f * alpha * alpha * t * t - 1.0f) < 1e-6f) {
            val term1 = (1.0f + 2.0f / Math.PI) * sin(Math.PI / (4.0f * alpha))
            val term2 = (1.0f - 2.0f / Math.PI) * cos(Math.PI / (4.0f * alpha))
            return ((alpha / sqrt(2.0f)) * (term1 + term2)).toFloat()
        }
        val numerator = sin(Math.PI * t * (1.0 - alpha)) +
                4.0 * alpha * t * cos(Math.PI * t * (1.0 + alpha))
        val denominator = Math.PI * t * (1.0 - 16.0 * alpha * alpha * t * t)
        return (numerator / denominator).toFloat()
    }

    /**
     * Applies RRC filter in-place to complex I/Q samples.
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
