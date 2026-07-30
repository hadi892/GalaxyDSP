package com.example.correlation

import com.example.core.ComplexVector

/**
 * Common correlation interface and utility methods for digital signal processing.
 */
interface Correlation {
    /**
     * Computes the normalized peak correlation value between two complex sample vectors.
     */
    fun computeCorrelationMetric(x: ComplexVector, y: ComplexVector): Float

    companion object {
        /**
         * Calculates the Pearson-like complex correlation coefficient magnitude [0.0..1.0].
         */
        fun coefficient(x: ComplexVector, y: ComplexVector): Float {
            require(x.size == y.size && x.size > 0) { "Vectors must have identical positive length" }
            var dotRe = 0.0f
            var dotIm = 0.0f
            var normX = 0.0f
            var normY = 0.0f

            for (i in 0 until x.size) {
                val xr = x.getRe(i)
                val xi = x.getIm(i)
                val yr = y.getRe(i)
                val yi = y.getIm(i)

                // Complex dot product x * conj(y)
                dotRe += (xr * yr + xi * yi)
                dotIm += (xi * yr - xr * yi)

                normX += (xr * xr + xi * xi)
                normY += (yr * yr + yi * yi)
            }

            val denom = Math.sqrt((normX * normY).toDouble()).toFloat()
            if (denom <= 1e-12f) return 0.0f
            val numMag = Math.sqrt((dotRe * dotRe + dotIm * dotIm).toDouble()).toFloat()
            return minOf(1.0f, numMag / denom)
        }
    }
}
