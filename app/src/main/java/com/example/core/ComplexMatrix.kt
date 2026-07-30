package com.example.core

/**
 * Contiguous memory 2D complex matrix backed by a row-major interleaved FloatArray.
 * Optimized for MIMO, adaptive filtering, and correlation matrices without GC overhead.
 *
 * @param rows Number of rows in the matrix.
 * @param cols Number of columns in the matrix.
 * @param data Underlying interleaved FloatArray of size rows * cols * 2.
 */
class ComplexMatrix(
    val rows: Int,
    val cols: Int,
    val data: FloatArray = FloatArray(rows * cols * 2)
) {
    init {
        require(data.size == rows * cols * 2) {
            "Data array size must be exactly rows * cols * 2."
        }
    }

    private inline fun indexOf(row: Int, col: Int): Int = (row * cols + col) * 2

    /**
     * Gets the complex number at [row], [col].
     */
    operator fun get(row: Int, col: Int): Complex {
        val i = indexOf(row, col)
        return Complex(data[i], data[i + 1])
    }

    /**
     * Sets the complex number at [row], [col].
     */
    operator fun set(row: Int, col: Int, value: Complex) {
        val i = indexOf(row, col)
        data[i] = value.re
        data[i + 1] = value.im
    }

    /**
     * Sets the raw real and imaginary components at [row], [col].
     */
    fun setRaw(row: Int, col: Int, re: Float, im: Float) {
        val i = indexOf(row, col)
        data[i] = re
        data[i + 1] = im
    }

    /**
     * Multiplies this matrix by another complex matrix [other] and returns the result.
     */
    operator fun times(other: ComplexMatrix): ComplexMatrix {
        require(cols == other.rows) {
            "Matrix dimension mismatch: cannot multiply ($rows x $cols) with (${other.rows} x ${other.cols})."
        }
        val result = ComplexMatrix(rows, other.cols)
        for (r in 0 until rows) {
            for (c in 0 until other.cols) {
                var sumRe = 0f
                var sumIm = 0f
                for (k in 0 until cols) {
                    val idxA = indexOf(r, k)
                    val idxB = other.indexOf(k, c)
                    val aRe = data[idxA]
                    val aIm = data[idxA + 1]
                    val bRe = other.data[idxB]
                    val bIm = other.data[idxB + 1]
                    sumRe += aRe * bRe - aIm * bIm
                    sumIm += aRe * bIm + aIm * bRe
                }
                result.setRaw(r, c, sumRe, sumIm)
            }
        }
        return result
    }

    /**
     * Returns the transpose of this matrix.
     */
    fun transpose(): ComplexMatrix {
        val result = ComplexMatrix(cols, rows)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = indexOf(r, c)
                result.setRaw(c, r, data[idx], data[idx + 1])
            }
        }
        return result
    }

    /**
     * Returns the Hermitian conjugate (conjugate transpose) of this matrix.
     */
    fun hermitian(): ComplexMatrix {
        val result = ComplexMatrix(cols, rows)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = indexOf(r, c)
                result.setRaw(c, r, data[idx], -data[idx + 1])
            }
        }
        return result
    }

    /**
     * Fills the entire matrix with zeros.
     */
    fun clear() {
        data.fill(0f)
    }
}
