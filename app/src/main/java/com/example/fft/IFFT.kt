package com.example.fft

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin

/**
 * Inverse Fast Fourier Transform (IFFT) implementation using iterative Cooley-Tukey radix-2.
 * Includes automatic 1/N scaling.
 *
 * @property fftSize Size of the transform (must be a power of 2).
 */
class IFFT(val fftSize: Int) {

    init {
        require(fftSize > 0 && (fftSize and (fftSize - 1)) == 0) {
            "IFFT size must be a positive power of 2, got $fftSize"
        }
    }

    private val bitReverseTable: IntArray = buildBitReverseTable(fftSize)
    private val twiddleRe: FloatArray
    private val twiddleIm: FloatArray

    init {
        val halfSize = fftSize / 2
        twiddleRe = FloatArray(halfSize)
        twiddleIm = FloatArray(halfSize)
        for (i in 0 until halfSize) {
            val angle = 2.0 * Math.PI * i / fftSize // Positive exponent for inverse transform
            twiddleRe[i] = cos(angle).toFloat()
            twiddleIm[i] = sin(angle).toFloat()
        }
    }

    /**
     * Executes inverse FFT in-place on [buffer] and scales by 1/fftSize.
     */
    fun transformInPlace(buffer: ComplexVector) {
        require(buffer.size == fftSize) {
            "Buffer size (${buffer.size}) must match IFFT size ($fftSize)"
        }
        bitReverseReorder(buffer)
        var halfLen = 1
        while (halfLen < fftSize) {
            val len = halfLen shl 1
            val step = fftSize / len
            for (i in 0 until fftSize step len) {
                var twiddleIdx = 0
                for (j in 0 until halfLen) {
                    val idxA = (i + j) * 2
                    val idxB = (i + j + halfLen) * 2

                    val uRe = buffer.data[idxA]
                    val uIm = buffer.data[idxA + 1]

                    val wRe = twiddleRe[twiddleIdx]
                    val wIm = twiddleIm[twiddleIdx]

                    val bRe = buffer.data[idxB]
                    val bIm = buffer.data[idxB + 1]

                    val vRe = wRe * bRe - wIm * bIm
                    val vIm = wRe * bIm + wIm * bRe

                    buffer.data[idxA] = uRe + vRe
                    buffer.data[idxA + 1] = uIm + vIm
                    buffer.data[idxB] = uRe - vRe
                    buffer.data[idxB + 1] = uIm - vIm

                    twiddleIdx += step
                }
            }
            halfLen = len
        }

        // Apply 1/N scaling
        val invN = 1.0f / fftSize
        for (i in 0 until buffer.size * 2) {
            buffer.data[i] *= invN
        }
    }

    /**
     * Executes inverse FFT from [source] into [destination].
     */
    fun transform(source: ComplexVector, destination: ComplexVector) {
        require(source.size == fftSize && destination.size == fftSize) {
            "Source and destination must match IFFT size."
        }
        destination.copyFrom(source)
        transformInPlace(destination)
    }

    private fun bitReverseReorder(buffer: ComplexVector) {
        for (i in 0 until fftSize) {
            val rev = bitReverseTable[i]
            if (i < rev) {
                val idxA = i * 2
                val idxB = rev * 2
                val tmpRe = buffer.data[idxA]
                val tmpIm = buffer.data[idxA + 1]
                buffer.data[idxA] = buffer.data[idxB]
                buffer.data[idxA + 1] = buffer.data[idxB + 1]
                buffer.data[idxB] = tmpRe
                buffer.data[idxB + 1] = tmpIm
            }
        }
    }

    companion object {
        private fun buildBitReverseTable(size: Int): IntArray {
            val table = IntArray(size)
            var bits = 0
            var temp = size - 1
            while (temp > 0) {
                bits++
                temp = temp shr 1
            }
            for (i in 0 until size) {
                var rev = 0
                var x = i
                for (b in 0 until bits) {
                    rev = (rev shl 1) or (x and 1)
                    x = x shr 1
                }
                table[i] = rev
            }
            return table
        }
    }
}
