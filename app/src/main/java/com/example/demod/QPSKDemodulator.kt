package com.example.demod

import com.example.core.ComplexVector

/**
 * QPSK Demodulator for DVB-S receiver pipeline.
 * Maps Gray-coded QPSK constellation symbols to hard and soft decision bitstreams.
 */
class QPSKDemodulator {

    /**
     * Demodulates QPSK symbols from [symbols] into hard-decision bits (0 or 1).
     *
     * @param symbols Synchronized symbol stream.
     * @param destinationBitArray Target IntArray to hold unpacked bits (length = symbols.size * 2).
     * @return Number of bits demodulated.
     */
    fun demodulateHard(symbols: ComplexVector, destinationBitArray: IntArray): Int {
        val numBits = minOf(destinationBitArray.size, symbols.size * 2)
        for (i in 0 until numBits / 2) {
            val re = symbols.getRe(i)
            val im = symbols.getIm(i)
            // Gray mapping: I >= 0 => bit 0, Q >= 0 => bit 0
            val bitI = if (re >= 0f) 0 else 1
            val bitQ = if (im >= 0f) 0 else 1
            destinationBitArray[i * 2] = bitI
            destinationBitArray[i * 2 + 1] = bitQ
        }
        return numBits
    }

    /**
     * Demodulates QPSK symbols into Log-Likelihood Ratio (LLR) soft decisions.
     * Positive LLR implies bit '0', negative LLR implies bit '1'.
     *
     * @param symbols Symbol stream.
     * @param destinationLlr Target FloatArray for soft decisions.
     * @param noiseVariance Estimated AWGN noise variance sigma^2.
     * @return Number of LLR samples generated.
     */
    fun demodulateSoft(
        symbols: ComplexVector,
        destinationLlr: FloatArray,
        noiseVariance: Float = 0.1f
    ): Int {
        val numBits = minOf(destinationLlr.size, symbols.size * 2)
        val scale = 2.0f / maxOf(1e-6f, noiseVariance)
        for (i in 0 until numBits / 2) {
            val re = symbols.getRe(i)
            val im = symbols.getIm(i)
            destinationLlr[i * 2] = re * scale
            destinationLlr[i * 2 + 1] = im * scale
        }
        return numBits
    }
}
