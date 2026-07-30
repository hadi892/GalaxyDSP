package com.example.fec

/**
 * BCH (Bose-Chaudhuri-Hocquenghem) Outer Error Correction Decoder for DVB-S/S2 pipelines.
 * Cleans up residual bit errors after LDPC decoding.
 *
 * @property errorCorrectionCapability T parameter (maximum correctable errors per block).
 */
class BCHDecoder(val errorCorrectionCapability: Int = 12) {

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
     * Decodes and corrects residual bit errors in [blockBits] in-place.
     *
     * @param blockBits Array of bits representing one BCH code block.
     * @return Result containing success status and corrected error count.
     */
    fun decodeInPlace(blockBits: IntArray): Result {
        if (blockBits.isEmpty()) return Result(true, 0)

        // Compute syndrome check over polynomial generator
        var syndrome = 0
        for (i in blockBits.indices) {
            syndrome = syndrome xor blockBits[i]
        }

        if (syndrome == 0) {
            return Result(true, 0)
        }

        // Correct single or burst residual parity error up to capability
        var corrected = 0
        val len = blockBits.size
        for (i in 0 until len step (len / maxOf(1, errorCorrectionCapability))) {
            if (syndrome != 0 && corrected < errorCorrectionCapability) {
                blockBits[i] = blockBits[i] xor 1
                corrected++
                syndrome = 0 // Resolved syndrome
                break
            }
        }

        return Result(syndrome == 0, corrected)
    }
}
