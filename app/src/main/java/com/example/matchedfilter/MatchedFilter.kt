package com.example.matchedfilter

import com.example.core.Complex
import com.example.core.ComplexVector
import kotlin.math.sqrt

/**
 * Matched Filter for optimal SNR maximization in AWGN channels.
 * Constructs the time-reversed complex conjugate impulse response from a given waveform template.
 *
 * @param templateReference Complex reference waveform to match against.
 */
class MatchedFilter(templateReference: ComplexVector) {

    val numTaps: Int = templateReference.size
    val tapsRe: FloatArray = FloatArray(numTaps)
    val tapsIm: FloatArray = FloatArray(numTaps)

    private val delayRe = FloatArray(numTaps)
    private val delayIm = FloatArray(numTaps)
    private var delayIdx = 0

    init {
        require(numTaps > 0) { "Template reference must not be empty." }
        var energy = 0f
        for (i in 0 until numTaps) {
            val srcIdx = numTaps - 1 - i // Time-reversed
            val re = templateReference.getRe(srcIdx)
            val im = -templateReference.getIm(srcIdx) // Complex conjugate
            tapsRe[i] = re
            tapsIm[i] = im
            energy += re * re + im * im
        }
        // Normalize unity energy
        val norm = if (energy > 1e-12f) 1.0f / sqrt(energy) else 1.0f
        for (i in 0 until numTaps) {
            tapsRe[i] *= norm
            tapsIm[i] *= norm
        }
    }

    /**
     * Filters complex I/Q samples in-place using matched impulse response.
     */
    fun filterInPlace(buffer: ComplexVector) {
        val len = buffer.size
        for (i in 0 until len) {
            val i2 = i * 2
            val re = buffer.data[i2]
            val im = buffer.data[i2 + 1]

            delayRe[delayIdx] = re
            delayIm[delayIdx] = im

            var accRe = 0f
            var accIm = 0f
            var idx = delayIdx
            for (t in 0 until numTaps) {
                val cRe = tapsRe[t]
                val cIm = tapsIm[t]
                val xRe = delayRe[idx]
                val xIm = delayIm[idx]

                // Complex multiplication: x * c
                accRe += xRe * cRe - xIm * cIm
                accIm += xRe * cIm + xIm * cRe

                idx--
                if (idx < 0) idx = numTaps - 1
            }

            buffer.data[i2] = accRe
            buffer.data[i2 + 1] = accIm

            delayIdx = (delayIdx + 1) % numTaps
        }
    }

    /**
     * Clears internal delay line state.
     */
    fun reset() {
        delayRe.fill(0f)
        delayIm.fill(0f)
        delayIdx = 0
    }
}
