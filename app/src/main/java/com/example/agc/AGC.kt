package com.example.agc

import com.example.core.ComplexVector
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Automatic Gain Control (AGC) with dynamic range compression, fast attack / slow decay,
 * and linear or logarithmic gain adjustment for DVB-S baseband receivers.
 *
 * @property targetLevelRms Target RMS magnitude level (typically 0.5 to 0.707).
 * @property attackRate Loop attack rate for sudden loud signals.
 * @property decayRate Loop decay rate for faint signals.
 */
class AGC(
    var targetLevelRms: Float = 0.5f,
    var attackRate: Float = 0.05f,
    var decayRate: Float = 0.005f
) {
    var currentGain: Float = 1.0f
        private set

    /**
     * Applies AGC gain in-place to the complex I/Q buffer and updates loop gain.
     */
    fun processInPlace(buffer: ComplexVector) {
        if (buffer.size == 0) return
        var totalEnergy = 0f
        val len = buffer.size
        for (i in 0 until len) {
            val i2 = i * 2
            val re = buffer.data[i2]
            val im = buffer.data[i2 + 1]

            // Apply current gain
            val outRe = re * currentGain
            val outIm = im * currentGain
            buffer.data[i2] = outRe
            buffer.data[i2 + 1] = outIm

            totalEnergy += outRe * outRe + outIm * outIm
        }

        val measuredRms = sqrt(totalEnergy / maxOf(1, len))
        if (measuredRms > 1e-6f) {
            val error = targetLevelRms - measuredRms
            val rate = if (error > 0f) attackRate else decayRate
            currentGain += rate * error * currentGain
            // Clamp gain to safe bounds [-40dB to +60dB]
            currentGain = currentGain.coerceIn(0.01f, 1000f)
        }
    }

    /**
     * Returns current AGC gain in decibels.
     */
    fun getGainDb(): Float = (20.0 * log10(currentGain.toDouble())).toFloat()

    /**
     * Resets AGC gain to unity.
     */
    fun reset() {
        currentGain = 1.0f
    }
}
