package com.example.filters

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin

/**
 * FIR Band-Pass Filter designed via difference of two low-pass windowed-sinc prototypes.
 *
 * @property numTaps Number of FIR taps (must be odd).
 * @property lowCutoffHz Lower cutoff frequency in Hertz.
 * @property highCutoffHz Upper cutoff frequency in Hertz.
 * @property sampleRateHz Sample rate in Hertz.
 */
class BandPassFilter(
    val numTaps: Int,
    val lowCutoffHz: Float,
    val highCutoffHz: Float,
    val sampleRateHz: Float
) {
    init {
        require(numTaps > 0 && numTaps % 2 == 1) { "numTaps must be positive odd integer." }
        require(lowCutoffHz > 0f && lowCutoffHz < highCutoffHz && highCutoffHz < sampleRateHz / 2f) {
            "Cutoffs must satisfy 0 < lowCutoffHz < highCutoffHz < Nyquist."
        }
    }

    val taps: FloatArray = FloatArray(numTaps)
    private val delayLineRe = FloatArray(numTaps)
    private val delayLineIm = FloatArray(numTaps)
    private var delayIdx = 0

    init {
        val fcLow = lowCutoffHz / sampleRateHz
        val fcHigh = highCutoffHz / sampleRateHz
        val center = (numTaps - 1) / 2

        val lpLow = FloatArray(numTaps)
        val lpHigh = FloatArray(numTaps)
        var sumLow = 0f
        var sumHigh = 0f

        for (i in 0 until numTaps) {
            val n = i - center
            val sincLow = if (n == 0) 2f * fcLow else (sin(2.0 * Math.PI * fcLow * n) / (Math.PI * n)).toFloat()
            val sincHigh = if (n == 0) 2f * fcHigh else (sin(2.0 * Math.PI * fcHigh * n) / (Math.PI * n)).toFloat()
            val win = (0.54 - 0.46 * cos(2.0 * Math.PI * i / (numTaps - 1))).toFloat()

            lpLow[i] = sincLow * win
            lpHigh[i] = sincHigh * win
            sumLow += lpLow[i]
            sumHigh += lpHigh[i]
        }

        if (sumLow != 0f) for (i in 0 until numTaps) lpLow[i] /= sumLow
        if (sumHigh != 0f) for (i in 0 until numTaps) lpHigh[i] /= sumHigh

        for (i in 0 until numTaps) {
            taps[i] = lpHigh[i] - lpLow[i]
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
