package com.example.adaptive

import com.example.core.Complex
import com.example.core.ComplexVector

/**
 * Adaptive Recursive Least Squares (RLS) FIR filter for rapid convergence channel equalization.
 * Employs exponential forgetting factor (lambda) and Sherman-Morrison matrix update.
 *
 * @property numTaps Number of filter taps.
 * @property lambda Forgetting factor (typically 0.98 to 1.0).
 * @property delta Initial regularization constant for inverse covariance matrix.
 */
class AdaptiveRLS(
    val numTaps: Int,
    var lambda: Float = 0.99f,
    val delta: Float = 100.0f
) {
    init {
        require(numTaps > 0) { "numTaps must be positive." }
        require(lambda in 0.5f..1.0f) { "lambda must be between 0.5 and 1.0." }
    }

    val weightsRe = FloatArray(numTaps)
    val weightsIm = FloatArray(numTaps)
    private val pMatrixRe = FloatArray(numTaps * numTaps)
    private val pMatrixIm = FloatArray(numTaps * numTaps)
    private val delayRe = FloatArray(numTaps)
    private val delayIm = FloatArray(numTaps)
    private var headIdx = 0

    private val piRe = FloatArray(numTaps)
    private val piIm = FloatArray(numTaps)
    private val kRe = FloatArray(numTaps)
    private val kIm = FloatArray(numTaps)

    init {
        reset()
    }

    /**
     * Processes a single input sample [input] against desired reference [desired].
     * Returns filter output and error.
     */
    fun processSample(input: Complex, desired: Complex): Pair<Complex, Complex> {
        delayRe[headIdx] = input.re
        delayIm[headIdx] = input.im

        var outRe = 0f
        var outIm = 0f
        var idx = headIdx
        for (i in 0 until numTaps) {
            val wRe = weightsRe[i]
            val wIm = weightsIm[i]
            val xRe = delayRe[idx]
            val xIm = delayIm[idx]
            outRe += wRe * xRe + wIm * xIm
            outIm += wRe * xIm - wIm * xRe
            idx--
            if (idx < 0) idx = numTaps - 1
        }

        val errRe = desired.re - outRe
        val errIm = desired.im - outIm

        // pi = P * u*
        for (i in 0 until numTaps) {
            var sumRe = 0f
            var sumIm = 0f
            var xIdx = headIdx
            for (j in 0 until numTaps) {
                val pIdx = i * numTaps + j
                val uRe = delayRe[xIdx]
                val uIm = -delayIm[xIdx] // conjugate
                sumRe += pMatrixRe[pIdx] * uRe - pMatrixIm[pIdx] * uIm
                sumIm += pMatrixRe[pIdx] * uIm + pMatrixIm[pIdx] * uRe
                xIdx--
                if (xIdx < 0) xIdx = numTaps - 1
            }
            piRe[i] = sumRe
            piIm[i] = sumIm
        }

        // gamma = lambda + u^T * pi
        var gammaRe = lambda
        var gammaIm = 0f
        var xIdx = headIdx
        for (i in 0 until numTaps) {
            val uRe = delayRe[xIdx]
            val uIm = delayIm[xIdx]
            gammaRe += uRe * piRe[i] - uIm * piIm[i]
            gammaIm += uRe * piIm[i] + uIm * piRe[i]
            xIdx--
            if (xIdx < 0) xIdx = numTaps - 1
        }
        val gammaMagSq = gammaRe * gammaRe + gammaIm * gammaIm
        val gammaInvRe = if (gammaMagSq > 1e-12f) gammaRe / gammaMagSq else 1f
        val gammaInvIm = if (gammaMagSq > 1e-12f) -gammaIm / gammaMagSq else 0f

        // k = pi / gamma
        for (i in 0 until numTaps) {
            kRe[i] = piRe[i] * gammaInvRe - piIm[i] * gammaInvIm
            kIm[i] = piRe[i] * gammaInvIm + piIm[i] * gammaInvRe
        }

        // w = w + k * e
        for (i in 0 until numTaps) {
            weightsRe[i] += kRe[i] * errRe - kIm[i] * errIm
            weightsIm[i] += kRe[i] * errIm + kIm[i] * errRe
        }

        // Update P matrix: P = (P - k * pi^H) / lambda
        val invLambda = 1.0f / lambda
        for (i in 0 until numTaps) {
            for (j in 0 until numTaps) {
                val pIdx = i * numTaps + j
                val kPiRe = kRe[i] * piRe[j] + kIm[i] * piIm[j]
                val kPiIm = kIm[i] * piRe[j] - kRe[i] * piIm[j]
                pMatrixRe[pIdx] = (pMatrixRe[pIdx] - kPiRe) * invLambda
                pMatrixIm[pIdx] = (pMatrixIm[pIdx] - kPiIm) * invLambda
            }
        }

        headIdx = (headIdx + 1) % numTaps
        return Pair(Complex(outRe, outIm), Complex(errRe, errIm))
    }

    /**
     * Resets filter state and initializes P matrix to delta * I.
     */
    fun reset() {
        weightsRe.fill(0f)
        weightsIm.fill(0f)
        delayRe.fill(0f)
        delayIm.fill(0f)
        headIdx = 0
        pMatrixRe.fill(0f)
        pMatrixIm.fill(0f)
        for (i in 0 until numTaps) {
            pMatrixRe[i * numTaps + i] = delta
        }
    }
}
