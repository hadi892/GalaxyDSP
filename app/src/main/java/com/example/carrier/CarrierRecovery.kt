package com.example.carrier

import com.example.core.Complex
import com.example.core.ComplexVector
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Carrier Recovery loop using Costas Loop / Decision-Directed PLL for QPSK and BPSK carrier frequency and phase tracking.
 *
 * @property dampingFactor Loop damping factor zeta (typically 0.707 for critical damping).
 * @property normalizedBandwidth Normalized loop noise bandwidth B_L * T_s (typically 0.01 to 0.05).
 */
class CarrierRecovery(
    val dampingFactor: Float = 0.707f,
    val normalizedBandwidth: Float = 0.01f
) {
    private var phase = 0f
    private var frequency = 0f

    private val alpha: Float
    private val beta: Float

    init {
        // Compute second-order PLL loop gains alpha (proportional) and beta (integral)
        val denom = (1.0f + 2.0f * dampingFactor * normalizedBandwidth + normalizedBandwidth * normalizedBandwidth)
        alpha = (4.0f * dampingFactor * normalizedBandwidth / denom)
        beta = (4.0f * normalizedBandwidth * normalizedBandwidth / denom)
    }

    /**
     * Processes a block of I/Q samples in-place, removing carrier frequency and phase offset.
     *
     * @param buffer Input/output complex baseband symbols.
     * @return Average phase error over the block in radians.
     */
    fun processBlockInPlace(buffer: ComplexVector): Float {
        var totalError = 0f
        val len = buffer.size
        for (i in 0 until len) {
            val i2 = i * 2
            val re = buffer.data[i2]
            val im = buffer.data[i2 + 1]

            // Rotate by negative phase estimate
            val c = cos(phase)
            val s = sin(phase)
            val rotRe = re * c + im * s
            val rotIm = im * c - re * s
            buffer.data[i2] = rotRe
            buffer.data[i2 + 1] = rotIm

            // QPSK Costas loop phase error detector: e = sign(I)*Q - sign(Q)*I
            val signI = if (rotRe >= 0f) 1f else -1f
            val signQ = if (rotIm >= 0f) 1f else -1f
            val error = signI * rotIm - signQ * rotRe

            // Loop filter update
            frequency += beta * error
            phase += frequency + alpha * error

            // Wrap phase to [-PI, PI]
            while (phase > Math.PI.toFloat()) phase -= (2.0 * Math.PI).toFloat()
            while (phase < -Math.PI.toFloat()) phase += (2.0 * Math.PI).toFloat()

            totalError += Math.abs(error)
        }
        return totalError / maxOf(1, len)
    }

    /**
     * Gets current estimated carrier frequency offset normalized to sample rate.
     */
    fun getNormalizedFrequencyOffset(): Float = frequency / (2.0f * Math.PI.toFloat())

    /**
     * Resets carrier loop state.
     */
    fun reset() {
        phase = 0f
        frequency = 0f
    }
}
