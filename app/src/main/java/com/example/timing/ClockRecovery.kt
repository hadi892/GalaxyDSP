package com.example.timing

import com.example.core.ComplexVector

/**
 * Mueller-Muller decision-directed symbol clock recovery loop for Nyquist-pulse shaped QPSK/BPSK signals.
 *
 * @property samplesPerSymbol Nominal oversampling ratio.
 * @property dampingFactor Damping factor of the timing PLL.
 * @property normalizedBandwidth Normalized timing loop bandwidth.
 */
class ClockRecovery(
    val samplesPerSymbol: Float = 2.0f,
    val dampingFactor: Float = 0.707f,
    val normalizedBandwidth: Float = 0.01f
) {
    private var phase = 0f
    private var freq = 1.0f / samplesPerSymbol
    private val alpha: Float
    private val beta: Float

    private var prevSymbolRe = 0f
    private var prevSymbolIm = 0f
    private var prevDecisionRe = 0f
    private var prevDecisionIm = 0f

    init {
        val denom = (1.0f + 2.0f * dampingFactor * normalizedBandwidth + normalizedBandwidth * normalizedBandwidth)
        alpha = (4.0f * dampingFactor * normalizedBandwidth / denom)
        beta = (4.0f * normalizedBandwidth * normalizedBandwidth / denom)
    }

    /**
     * Processes oversampled samples and outputs symbol-synchronized decisions.
     */
    fun processBlock(source: ComplexVector, destination: ComplexVector): Int {
        var outIdx = 0
        for (i in 0 until source.size) {
            phase += freq
            if (phase >= 1.0f) {
                phase -= 1.0f
                val re = source.getRe(i)
                val im = source.getIm(i)

                val decRe = if (re >= 0f) 0.7071f else -0.7071f
                val decIm = if (im >= 0f) 0.7071f else -0.7071f

                // M&M Error: e = (a_k * x_{k-1} - a_{k-1} * x_k)
                val errRe = decRe * prevSymbolRe - prevDecisionRe * re
                val errIm = decIm * prevSymbolIm - prevDecisionIm * im
                val error = errRe + errIm

                freq += beta * error
                phase += alpha * error

                if (outIdx < destination.size) {
                    destination.setRaw(outIdx++, re, im)
                }

                prevSymbolRe = re
                prevSymbolIm = im
                prevDecisionRe = decRe
                prevDecisionIm = decIm
            }
        }
        return outIdx
    }

    fun reset() {
        phase = 0f
        freq = 1.0f / samplesPerSymbol
        prevSymbolRe = 0f
        prevSymbolIm = 0f
        prevDecisionRe = 0f
        prevDecisionIm = 0f
    }
}
