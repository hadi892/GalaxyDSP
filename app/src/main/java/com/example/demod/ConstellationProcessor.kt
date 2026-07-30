package com.example.demod

import com.example.core.Complex
import com.example.core.ComplexVector
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Constellation Processor for computing Error Vector Magnitude (EVM),
 * constellation cluster variance, and correcting static quadrant rotation.
 */
class ConstellationProcessor {

    /**
     * Computes the Error Vector Magnitude (EVM) in RMS percentage for QPSK symbols.
     */
    fun computeEvmRmsPercent(symbols: ComplexVector): Float {
        if (symbols.size == 0) return 0f
        var sumErrorSq = 0f
        var sumRefSq = 0f
        val idealMag = 0.70710678f // 1/sqrt(2)

        for (i in 0 until symbols.size) {
            val re = symbols.getRe(i)
            val im = symbols.getIm(i)

            val idealRe = if (re >= 0f) idealMag else -idealMag
            val idealIm = if (im >= 0f) idealMag else -idealMag

            val errRe = re - idealRe
            val errIm = im - idealIm

            sumErrorSq += errRe * errRe + errIm * errIm
            sumRefSq += idealRe * idealRe + idealIm * idealIm
        }
        if (sumRefSq <= 1e-12f) return 0f
        return sqrt(sumErrorSq / sumRefSq) * 100.0f
    }

    /**
     * Computes cluster variance across the four QPSK quadrants.
     */
    fun computeClusterVariance(symbols: ComplexVector): Float {
        if (symbols.size == 0) return 0f
        val idealMag = 0.70710678f
        var sumVar = 0f
        for (i in 0 until symbols.size) {
            val re = symbols.getRe(i)
            val im = symbols.getIm(i)
            val idealRe = if (re >= 0f) idealMag else -idealMag
            val idealIm = if (im >= 0f) idealMag else -idealMag
            val diffRe = re - idealRe
            val diffIm = im - idealIm
            sumVar += diffRe * diffRe + diffIm * diffIm
        }
        return sumVar / symbols.size
    }

    /**
     * Applies static phase rotation to all symbols by [angleRads].
     */
    fun rotateInPlace(symbols: ComplexVector, angleRads: Float) {
        val c = cos(angleRads)
        val s = sin(angleRads)
        for (i in 0 until symbols.size) {
            val idx = i * 2
            val re = symbols.data[idx]
            val im = symbols.data[idx + 1]
            symbols.data[idx] = re * c - im * s
            symbols.data[idx + 1] = re * s + im * c
        }
    }
}
