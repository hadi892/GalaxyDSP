package com.example.core

import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Contiguous memory complex vector backed by an interleaved FloatArray [re0, im0, re1, im1, ...].
 * Designed for SIMD-friendly DSP operations and zero object allocations in signal loops.
 *
 * @param size The number of complex elements in the vector.
 * @param data The underlying interleaved real and imaginary array of length 2 * size.
 */
class ComplexVector(
    val size: Int,
    val data: FloatArray = FloatArray(size * 2)
) {
    init {
        require(data.size == size * 2) {
            "Data array length must be exactly twice the vector size."
        }
    }

    /**
     * Gets the complex element at [index].
     */
    operator fun get(index: Int): Complex {
        val i2 = index * 2
        return Complex(data[i2], data[i2 + 1])
    }

    /**
     * Sets the complex element at [index].
     */
    operator fun set(index: Int, value: Complex) {
        val i2 = index * 2
        data[i2] = value.re
        data[i2 + 1] = value.im
    }

    /**
     * Sets the real and imaginary components at [index] without object allocation.
     */
    fun setRaw(index: Int, re: Float, im: Float) {
        val i2 = index * 2
        data[i2] = re
        data[i2 + 1] = im
    }

    /**
     * Returns real component at [index].
     */
    fun getRe(index: Int): Float = data[index * 2]

    /**
     * Returns imaginary component at [index].
     */
    fun getIm(index: Int): Float = data[index * 2 + 1]

    /**
     * Computes the dot product of this vector with [other] without allocating intermediate Complex objects.
     */
    fun dot(other: ComplexVector): Complex {
        require(other.size == size) { "Vector sizes must match for dot product." }
        var sumRe = 0f
        var sumIm = 0f
        val len = size * 2
        var i = 0
        while (i < len) {
            val aRe = data[i]
            val aIm = data[i + 1]
            val bRe = other.data[i]
            val bIm = other.data[i + 1]
            // Complex multiplication: (aRe + j aIm) * (bRe - j bIm) for Hermitian dot product
            sumRe += aRe * bRe + aIm * bIm
            sumIm += aIm * bRe - aRe * bIm
            i += 2
        }
        return Complex(sumRe, sumIm)
    }

    /**
     * Scales the vector in-place by a real factor.
     */
    fun scaleInPlace(factor: Float) {
        val len = size * 2
        for (i in 0 until len) {
            data[i] *= factor
        }
    }

    /**
     * Computes the total energy (sum of magnitude squared) across all elements.
     */
    fun computeEnergy(): Float {
        var energy = 0f
        val len = size * 2
        var i = 0
        while (i < len) {
            val r = data[i]
            val im = data[i + 1]
            energy += r * r + im * im
            i += 2
        }
        return energy
    }

    /**
     * Extracts power spectrum (magnitude squared of each bin) into [destination].
     */
    fun extractPowerSpectrum(destination: FloatArray) {
        require(destination.size >= size) { "Destination buffer too small for power spectrum." }
        for (i in 0 until size) {
            val i2 = i * 2
            val re = data[i2]
            val im = data[i2 + 1]
            destination[i] = re * re + im * im
        }
    }

    /**
     * Extracts magnitude spectrum into [destination].
     */
    fun extractMagnitudeSpectrum(destination: FloatArray) {
        require(destination.size >= size) { "Destination buffer too small for magnitude spectrum." }
        for (i in 0 until size) {
            val i2 = i * 2
            destination[i] = hypot(data[i2], data[i2 + 1])
        }
    }

    /**
     * Copies elements from [other] into this vector.
     */
    fun copyFrom(other: ComplexVector) {
        require(other.size == size) { "Vector size mismatch in copyFrom." }
        System.arraycopy(other.data, 0, data, 0, size * 2)
    }

    /**
     * Fills vector with zeros.
     */
    fun clear() {
        data.fill(0f)
    }

    companion object {
        /**
         * Creates a copy of the given vector.
         */
        fun copyOf(vector: ComplexVector): ComplexVector {
            val copy = ComplexVector(vector.size)
            System.arraycopy(vector.data, 0, copy.data, 0, vector.size * 2)
            return copy
        }
    }
}
