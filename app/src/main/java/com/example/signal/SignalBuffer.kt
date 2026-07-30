package com.example.signal

import kotlin.math.sqrt

/**
 * Reusable signal buffer representing a block of real-valued time-domain or frequency-domain samples.
 *
 * @property sampleRateHz Sample rate in Hertz.
 * @property samples Contiguous FloatArray containing the sample data.
 */
class SignalBuffer(
    val sampleRateHz: Float,
    val samples: FloatArray
) {
    val size: Int
        get() = samples.size

    /**
     * Scales all samples in-place by a linear gain factor [gain].
     */
    fun applyGain(gain: Float) {
        for (i in samples.indices) {
            samples[i] *= gain
        }
    }

    /**
     * Computes the total energy (sum of squared values) of the buffer.
     */
    fun computeEnergy(): Float {
        var energy = 0f
        for (value in samples) {
            energy += value * value
        }
        return energy
    }

    /**
     * Computes the Root Mean Square (RMS) value of the buffer.
     */
    fun computeRms(): Float {
        if (samples.isEmpty()) return 0f
        return sqrt(computeEnergy() / samples.size)
    }

    /**
     * Finds the peak absolute sample value in this buffer.
     */
    fun findPeakAbsolute(): Float {
        var peak = 0f
        for (value in samples) {
            val absVal = if (value < 0f) -value else value
            if (absVal > peak) {
                peak = absVal
            }
        }
        return peak
    }

    /**
     * Fills the buffer with zero samples.
     */
    fun clear() {
        samples.fill(0f)
    }

    companion object {
        /**
         * Creates a zeroed SignalBuffer with the specified [size] and [sampleRateHz].
         */
        fun createEmpty(size: Int, sampleRateHz: Float = 1_000_000f): SignalBuffer {
            return SignalBuffer(sampleRateHz, FloatArray(size))
        }
    }
}
