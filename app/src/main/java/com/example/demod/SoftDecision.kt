package com.example.demod

import kotlin.math.abs

/**
 * Helper class for Log-Likelihood Ratio (LLR) quantization, hard decision slicing,
 * and soft-bit scaling for LDPC and BCH Forward Error Correction decoders.
 */
object SoftDecision {

    /**
     * Converts floating point LLRs into quantized signed 8-bit soft decisions [-127..127].
     */
    fun quantizeLlrToByte(llrs: FloatArray, destinationBytes: ByteArray, maxMagnitude: Float = 10.0f) {
        val len = minOf(llrs.size, destinationBytes.size)
        val scale = 127.0f / maxOf(1e-3f, maxMagnitude)
        for (i in 0 until len) {
            val scaled = (llrs[i] * scale).toInt().coerceIn(-127, 127)
            destinationBytes[i] = scaled.toByte()
        }
    }

    /**
     * Slices soft LLRs into hard binary decisions (0 for positive LLR, 1 for negative LLR).
     */
    fun sliceHardDecisions(llrs: FloatArray, destinationBits: IntArray) {
        val len = minOf(llrs.size, destinationBits.size)
        for (i in 0 until len) {
            destinationBits[i] = if (llrs[i] >= 0f) 0 else 1
        }
    }

    /**
     * Computes mean reliability (average absolute value of LLRs).
     */
    fun computeMeanReliability(llrs: FloatArray): Float {
        if (llrs.isEmpty()) return 0f
        var sum = 0f
        for (value in llrs) {
            sum += abs(value)
        }
        return sum / llrs.size
    }
}
