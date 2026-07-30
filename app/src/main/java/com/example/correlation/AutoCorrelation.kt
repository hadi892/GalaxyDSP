package com.example.correlation

import com.example.core.Complex
import com.example.core.ComplexVector
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Computes autocorrelation of complex I/Q signal buffers over specified lag offsets.
 * Essential for carrier frequency offset (CFO) estimation and symbol timing synchronization.
 */
class AutoCorrelation {

    /**
     * Computes the normalized autocorrelation coefficient at a specific [lag].
     *
     * @param buffer Input complex signal vector.
     * @param lag Sample delay offset.
     * @return Complex autocorrelation value normalized by signal energy.
     */
    fun computeAtLag(buffer: ComplexVector, lag: Int): Complex {
        require(lag >= 0 && lag < buffer.size) { "Lag must be within buffer bounds." }
        if (lag == 0) return Complex.ONE
        var sumRe = 0f
        var sumIm = 0f
        var energy = 0f
        val limit = buffer.size - lag
        for (i in 0 until limit) {
            val aRe = buffer.getRe(i + lag)
            val aIm = buffer.getIm(i + lag)
            val bRe = buffer.getRe(i)
            val bIm = buffer.getIm(i)

            // a(i+lag) * b*(i)
            sumRe += aRe * bRe + aIm * bIm
            sumIm += aIm * bRe - aRe * bIm
            energy += bRe * bRe + bIm * bIm
        }
        if (energy <= 1e-12f) return Complex.ZERO
        return Complex(sumRe / energy, sumIm / energy)
    }

    /**
     * Estimates Carrier Frequency Offset (CFO) in Hertz using delayed autocorrelation (Schmidl-Cox method).
     *
     * @param buffer Received signal buffer containing repeated preamble symbols.
     * @param symbolLengthSamples Length of the repeated symbol in samples.
     * @param sampleRateHz System sample rate in Hertz.
     * @return Estimated CFO in Hertz.
     */
    fun estimateCfoHz(
        buffer: ComplexVector,
        symbolLengthSamples: Int,
        sampleRateHz: Float
    ): Float {
        val corr = computeAtLag(buffer, symbolLengthSamples)
        val phaseAngle = atan2(corr.im, corr.re)
        // Delta_f = phaseAngle / (2 * PI * T_d) where T_d = symbolLengthSamples / sampleRate
        val td = symbolLengthSamples / sampleRateHz
        if (td <= 1e-12f) return 0f
        return (phaseAngle / (2.0 * Math.PI * td)).toFloat()
    }
}
