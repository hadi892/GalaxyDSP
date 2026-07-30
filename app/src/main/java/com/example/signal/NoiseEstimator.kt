package com.example.signal

import com.example.core.ComplexVector
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Estimates noise standard deviation and variance from an I/Q sample block
 * using Robust Median Absolute Deviation (MAD), which is resilient to signal outliers and bursts.
 */
class NoiseEstimator {

    /**
     * Estimates the noise standard deviation (sigma) of a complex I/Q stream.
     * Assumes Gaussian noise distribution where MAD ~ 0.6745 * sigma.
     *
     * @param buffer Complex I/Q buffer to estimate noise from.
     * @return Estimated noise standard deviation.
     */
    fun estimateSigma(buffer: ComplexVector): Float {
        if (buffer.size == 0) return 0f
        val absValues = FloatArray(buffer.size * 2)
        for (i in 0 until buffer.size) {
            absValues[i * 2] = abs(buffer.getRe(i))
            absValues[i * 2 + 1] = abs(buffer.getIm(i))
        }
        absValues.sort()
        val median = if (absValues.size % 2 == 0) {
            (absValues[absValues.size / 2 - 1] + absValues[absValues.size / 2]) * 0.5f
        } else {
            absValues[absValues.size / 2]
        }
        // For normal distribution, standard deviation = 1.4826 * MAD
        return median * 1.4826f
    }

    /**
     * Estimates the Signal-to-Noise Ratio (SNR) in dB given signal plus noise energy and estimated noise sigma.
     */
    fun estimateSnrDb(totalPower: Float, noiseSigma: Float): Float {
        val noisePower = noiseSigma * noiseSigma * 2f // Two quadrature dimensions
        if (noisePower <= 1e-12f || totalPower <= noisePower) return 0f
        val signalPower = totalPower - noisePower
        return (10.0 * Math.log10((signalPower / noisePower).toDouble())).toFloat()
    }
}
