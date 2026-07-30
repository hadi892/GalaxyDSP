package com.example.fec

/**
 * BCH (Bose-Chaudhuri-Hocquenghem) Outer Error Correction Decoder for DVB-S/S2 pipelines.
 * Implements Galois Field GF(2^6) syndrome evaluation, Berlekamp-Massey error locator solver,
 * and Chien search for exact bit-error identification and correction.
 *
 * @property errorCorrectionCapability T parameter (maximum correctable errors per block).
 */
class BCHDecoder(val errorCorrectionCapability: Int = 12) {

    private val alphaTo = IntArray(64)
    private val indexOf = IntArray(64)

    init {
        var mask = 1
        for (i in 0 until 63) {
            alphaTo[i] = mask
            indexOf[mask] = i
            mask = mask shl 1
            if (mask >= 64) {
                mask = (mask xor 67) and 63 // Primitive polynomial x^6 + x + 1 (0x43 / 67)
            }
        }
        indexOf[0] = -1
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return alphaTo[(indexOf[a] + indexOf[b]) % 63]
    }

    private fun gfDiv(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return alphaTo[(indexOf[a] - indexOf[b] + 63) % 63]
    }

    private fun gfPow(a: Int, power: Int): Int {
        if (a == 0) return 0
        return alphaTo[(indexOf[a] * power) % 63]
    }

    /**
     * Result of BCH block decoding.
     *
     * @property success Whether decoding succeeded without exceeding correction capability.
     * @property correctedBitsCount Number of bit errors found and flipped.
     */
    data class Result(
        val success: Boolean,
        val correctedBitsCount: Int
    )

    /**
     * Decodes and corrects residual bit errors in [blockBits] in-place using GF(64) algebra.
     *
     * @param blockBits Array of bits representing one BCH code block.
     * @return Result containing success status and corrected error count.
     */
    fun decodeInPlace(blockBits: IntArray): Result {
        if (blockBits.isEmpty()) return Result(true, 0)

        val s = IntArray(2 * errorCorrectionCapability + 1)
        var allZero = true
        val len = minOf(blockBits.size, 63)
        for (j in 1..2 * errorCorrectionCapability) {
            var sum = 0
            for (i in 0 until len) {
                if (blockBits[i] != 0) {
                    sum = sum xor gfPow(alphaTo[i % 63], j)
                }
            }
            s[j] = sum
            if (sum != 0) allZero = false
        }
        if (allZero) {
            return Result(true, 0)
        }

        var sigma = IntArray(errorCorrectionCapability + 2)
        var beta = IntArray(errorCorrectionCapability + 2)
        sigma[0] = 1
        beta[0] = 1
        var l = 0
        var m = 1
        var b = 1
        for (n in 1..2 * errorCorrectionCapability) {
            var d = s[n]
            for (i in 1..l) {
                if (i < sigma.size && (n - i) < s.size) {
                    d = d xor gfMul(sigma[i], s[n - i])
                }
            }
            if (d == 0) {
                m++
            } else {
                val temp = sigma.clone()
                val factor = gfDiv(d, b)
                for (i in 0 until beta.size) {
                    if (i + m < sigma.size) {
                        sigma[i + m] = sigma[i + m] xor gfMul(factor, beta[i])
                    }
                }
                if (2 * l <= n - 1) {
                    l = n - l
                    beta = temp
                    b = d
                    m = 1
                } else {
                    m++
                }
            }
        }

        var corrected = 0
        val errPos = IntArray(errorCorrectionCapability)
        for (i in 0 until len) {
            val inv = alphaTo[(63 - i) % 63]
            var sum = 1
            for (j in 1..l) {
                if (j < sigma.size) {
                    sum = sum xor gfMul(sigma[j], gfPow(inv, j))
                }
            }
            if (sum == 0) {
                if (corrected < errorCorrectionCapability) {
                    errPos[corrected++] = i
                }
            }
        }
        for (k in 0 until corrected) {
            val pos = errPos[k]
            if (pos < blockBits.size) {
                blockBits[pos] = blockBits[pos] xor 1
            }
        }
        return Result(corrected == l, corrected)
    }
}

