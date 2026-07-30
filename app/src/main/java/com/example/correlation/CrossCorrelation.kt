package com.example.correlation

import com.example.core.Complex
import com.example.core.ComplexVector
import kotlin.math.sqrt

/**
 * Computes sliding normalized cross-correlation between complex I/Q signal buffers
 * and reference sequences for burst detection, frame synchronization, and timing alignment.
 */
class CrossCorrelation {

    /**
     * Cross-correlation peak result.
     *
     * @property lag Sample lag index where maximum correlation occurs.
     * @property peakValue Peak complex correlation value.
     * @property normalizedScore Normalized correlation score in range [0.0..1.0].
     */
    data class PeakResult(
        val lag: Int,
        val peakValue: Complex,
        val normalizedScore: Float
    )

    /**
     * Computes sliding cross-correlation between [signal] and [reference] for lags from 0 up to [maxLag].
     *
     * @param signal Received complex signal buffer.
     * @param reference Known reference template vector.
     * @param maxLag Maximum lag index to evaluate.
     * @return PeakResult containing lag index and score.
     */
    fun findPeakCorrelation(
        signal: ComplexVector,
        reference: ComplexVector,
        maxLag: Int = signal.size - reference.size
    ): PeakResult {
        require(reference.size <= signal.size && maxLag >= 0 && maxLag + reference.size <= signal.size) {
            "Invalid buffer sizes or maxLag for cross-correlation."
        }

        val refEnergy = reference.computeEnergy()
        if (refEnergy <= 1e-12f) {
            return PeakResult(0, Complex.ZERO, 0f)
        }

        var bestLag = 0
        var bestScore = -1f
        var bestVal = Complex.ZERO

        for (lag in 0..maxLag) {
            var sumRe = 0f
            var sumIm = 0f
            var sigEnergy = 0f
            for (i in 0 until reference.size) {
                val sigIdx = lag + i
                val sRe = signal.getRe(sigIdx)
                val sIm = signal.getIm(sigIdx)
                val rRe = reference.getRe(i)
                val rIm = reference.getIm(i)

                // Dot product with conjugate of reference: s(i) * r*(i)
                sumRe += sRe * rRe + sIm * rIm
                sumIm += sIm * rRe - sRe * rIm
                sigEnergy += sRe * sRe + sIm * sIm
            }

            val magSq = sumRe * sumRe + sumIm * sumIm
            val denom = sigEnergy * refEnergy
            val normScore = if (denom > 1e-12f) sqrt(magSq / denom) else 0f

            if (normScore > bestScore) {
                bestScore = normScore
                bestLag = lag
                bestVal = Complex(sumRe, sumIm)
            }
        }

        return PeakResult(bestLag, bestVal, bestScore)
    }

    /**
     * Computes full correlation profile across all lags into [destination] array.
     */
    fun computeProfile(
        signal: ComplexVector,
        reference: ComplexVector,
        destination: FloatArray,
        maxLag: Int = minOf(destination.size - 1, signal.size - reference.size)
    ) {
        val refEnergy = maxOf(1e-9f, reference.computeEnergy())
        for (lag in 0..maxLag) {
            var sumRe = 0f
            var sumIm = 0f
            var sigEnergy = 0f
            for (i in 0 until reference.size) {
                val sigIdx = lag + i
                val sRe = signal.getRe(sigIdx)
                val sIm = signal.getIm(sigIdx)
                val rRe = reference.getRe(i)
                val rIm = reference.getIm(i)
                sumRe += sRe * rRe + sIm * rIm
                sumIm += sIm * rRe - sRe * rIm
                sigEnergy += sRe * sRe + sIm * sIm
            }
            val magSq = sumRe * sumRe + sumIm * sumIm
            val denom = sigEnergy * refEnergy
            destination[lag] = if (denom > 1e-12f) sqrt(magSq / denom) else 0f
        }
    }
}
