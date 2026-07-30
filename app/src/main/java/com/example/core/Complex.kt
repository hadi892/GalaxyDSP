package com.example.core

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Immutable complex number representation optimized for DSP calculations.
 *
 * @property re Real component of the complex number.
 * @property im Imaginary component of the complex number.
 */
data class Complex(val re: Float, val im: Float) {

    /**
     * Computes the magnitude (euclidean norm) of this complex number: sqrt(re^2 + im^2).
     */
    val magnitude: Float
        get() = hypot(re, im)

    /**
     * Computes the power (magnitude squared) of this complex number: re^2 + im^2.
     * Avoids the square root operation for high-speed energy calculations.
     */
    val power: Float
        get() = re * re + im * im

    /**
     * Computes the phase angle in radians (-PI to PI).
     */
    val phase: Float
        get() = atan2(im, re)

    /**
     * Returns the complex conjugate (re - j*im).
     */
    val conjugate: Complex
        get() = Complex(re, -im)

    /**
     * Adds another complex number to this one.
     */
    operator fun plus(other: Complex): Complex =
        Complex(re + other.re, im + other.im)

    /**
     * Subtracts another complex number from this one.
     */
    operator fun minus(other: Complex): Complex =
        Complex(re - other.re, im - other.im)

    /**
     * Multiplies this complex number by another complex number.
     */
    operator fun times(other: Complex): Complex =
        Complex(re * other.re - im * other.im, re * other.im + im * other.re)

    /**
     * Multiplies this complex number by a real scalar value.
     */
    operator fun times(scalar: Float): Complex =
        Complex(re * scalar, im * scalar)

    /**
     * Divides this complex number by another complex number.
     */
    operator fun div(other: Complex): Complex {
        val denom = other.re * other.re + other.im * other.im
        if (denom == 0f) return ZERO
        return Complex(
            (re * other.re + im * other.im) / denom,
            (im * other.re - re * other.im) / denom
        )
    }

    /**
     * Divides this complex number by a real scalar value.
     */
    operator fun div(scalar: Float): Complex {
        if (scalar == 0f) return ZERO
        return Complex(re / scalar, im / scalar)
    }

    companion object {
        val ZERO = Complex(0f, 0f)
        val ONE = Complex(1f, 0f)
        val I = Complex(0f, 1f)

        /**
         * Creates a Complex number from polar coordinates.
         *
         * @param magnitude The radial distance from origin.
         * @param phaseRads The angle in radians.
         */
        fun fromPolar(magnitude: Float, phaseRads: Float): Complex {
            return Complex(
                magnitude * cos(phaseRads),
                magnitude * sin(phaseRads)
            )
        }
    }
}
