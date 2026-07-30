package com.example.timing

import com.example.core.Complex
import com.example.core.ComplexVector

/**
 * Gardner Timing Error Detector and interpolating timing recovery loop for QPSK/BPSK DVB-S symbol timing.
 *
 * @property samplesPerSymbol Nominal oversampling ratio (must be >= 2).
 * @property loopBandwidth Normalized timing loop bandwidth (typically 0.005 to 0.02).
 */
class TimingRecovery(
    val samplesPerSymbol: Float = 2.0f,
    val loopBandwidth: Float = 0.01f
) {
    init {
        require(samplesPerSymbol >= 2.0f) { "Gardner loop requires samplesPerSymbol >= 2.0" }
    }

    private var mu = 0.0f
    private var phase = 0.0f
    private var freq = 1.0f / samplesPerSymbol

    private val alpha = 2.0f * loopBandwidth
    private val beta = loopBandwidth * loopBandwidth * 0.25f

    private var prevSymbolRe = 0f
    private var prevSymbolIm = 0f
    private var midSymbolRe = 0f
    private var midSymbolIm = 0f
    private var isMidSample = true

    /**
     * Processes input oversampled stream and extracts synchronized symbols into [destination].
     *
     * @param source Input oversampled complex stream.
     * @param destination Target vector to receive symbol-rate samples.
     * @return Number of valid symbols produced.
     */
    fun processBlock(source: ComplexVector, destination: ComplexVector): Int {
        var outIdx = 0
        val srcLen = source.size
        for (i in 0 until srcLen) {
            val idx = i * 2
            val re = source.data[idx]
            val im = source.data[idx + 1]

            phase += freq
            if (phase >= 1.0f) {
                phase -= 1.0f
                if (isMidSample) {
                    midSymbolRe = re
                    midSymbolIm = im
                    isMidSample = false
                } else {
                    val currRe = re
                    val currIm = im
                    // Gardner TED error: e = I_mid * (I_prev - I_curr) + Q_mid * (Q_prev - Q_curr)
                    val error = midSymbolRe * (prevSymbolRe - currRe) + midSymbolIm * (prevSymbolIm - currIm)

                    // Update loop filter
                    freq += beta * error
                    phase += alpha * error

                    if (outIdx < destination.size) {
                        destination.setRaw(outIdx++, currRe, currIm)
                    }

                    prevSymbolRe = currRe
                    prevSymbolIm = currIm
                    isMidSample = true
                }
            }
        }
        return outIdx
    }

    /**
     * Resets timing loop state.
     */
    fun reset() {
        mu = 0.0f
        phase = 0.0f
        freq = 1.0f / samplesPerSymbol
        prevSymbolRe = 0f
        prevSymbolIm = 0f
        midSymbolRe = 0f
        midSymbolIm = 0f
        isMidSample = true
    }
}
