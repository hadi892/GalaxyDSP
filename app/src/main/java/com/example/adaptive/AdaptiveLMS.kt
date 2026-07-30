package com.example.adaptive

import com.example.core.Complex
import com.example.core.ComplexVector

/**
 * Adaptive Least Mean Squares (LMS) FIR filter for channel equalization,
 * noise cancellation, and linear interference suppression.
 *
 * @property numTaps Number of FIR taps.
 * @property stepSize Step size (mu) for weight updates. Must be chosen for stability (0 < mu < 2/sum_power).
 */
class AdaptiveLMS(
    val numTaps: Int,
    var stepSize: Float = 0.01f
) {
    init {
        require(numTaps > 0) { "numTaps must be positive." }
        require(stepSize > 0f) { "stepSize must be positive." }
    }

    val weightsRe = FloatArray(numTaps)
    val weightsIm = FloatArray(numTaps)
    private val delayRe = FloatArray(numTaps)
    private val delayIm = FloatArray(numTaps)
    private var headIdx = 0

    /**
     * Processes a single input sample [input] against a desired reference sample [desired].
     * Returns the filter output and updates weights based on LMS error gradient.
     *
     * @return Pair of (filter output, error = desired - output).
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
            // Complex dot product: w^H * x
            outRe += wRe * xRe + wIm * xIm
            outIm += wRe * xIm - wIm * xRe
            idx--
            if (idx < 0) idx = numTaps - 1
        }

        val errRe = desired.re - outRe
        val errIm = desired.im - outIm

        // Weight update: w_new = w_old + mu * e * x^*
        idx = headIdx
        for (i in 0 until numTaps) {
            val xRe = delayRe[idx]
            val xIm = delayIm[idx]
            weightsRe[i] += stepSize * (errRe * xRe + errIm * xIm)
            weightsIm[i] += stepSize * (errIm * xRe - errRe * xIm)
            idx--
            if (idx < 0) idx = numTaps - 1
        }

        headIdx = (headIdx + 1) % numTaps
        return Pair(Complex(outRe, outIm), Complex(errRe, errIm))
    }

    /**
     * Processes a block of input samples against desired samples in-place.
     */
    fun processBlock(input: ComplexVector, desired: ComplexVector, output: ComplexVector) {
        require(input.size == desired.size && output.size == input.size) {
            "Block size mismatch in AdaptiveLMS."
        }
        for (i in 0 until input.size) {
            val i2 = i * 2
            val inComp = Complex(input.data[i2], input.data[i2 + 1])
            val desComp = Complex(desired.data[i2], desired.data[i2 + 1])
            val (out, _) = processSample(inComp, desComp)
            output.setRaw(i, out.re, out.im)
        }
    }

    /**
     * Resets filter weights and delay line.
     */
    fun reset() {
        weightsRe.fill(0f)
        weightsIm.fill(0f)
        delayRe.fill(0f)
        delayIm.fill(0f)
        headIdx = 0
    }
}
