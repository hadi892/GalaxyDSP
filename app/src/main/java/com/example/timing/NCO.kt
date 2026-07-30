package com.example.timing

import com.example.core.Complex
import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin

/**
 * Numerically Controlled Oscillator (NCO) for high-speed digital mixing, carrier translation,
 * and phase generation with 32-bit phase accumulator.
 *
 * @property sampleRateHz Sample rate in Hertz.
 */
class NCO(val sampleRateHz: Float) {

    private var phaseAccumulator: Long = 0L
    private var frequencyIncrement: Long = 0L

    /**
     * Sets the target frequency in Hertz.
     */
    fun setFrequency(frequencyHz: Float) {
        val normFreq = frequencyHz / sampleRateHz
        frequencyIncrement = (normFreq * 4294967296.0).toLong()
    }

    /**
     * Generates a single complex exponential sample e^{j * phase}.
     */
    fun nextSample(): Complex {
        val rads = (phaseAccumulator.toDouble() / 4294967296.0 * 2.0 * Math.PI).toFloat()
        phaseAccumulator += frequencyIncrement
        return Complex(cos(rads), sin(rads))
    }

    /**
     * Mixes down/up [buffer] in-place with the current NCO carrier frequency.
     */
    fun mixInPlace(buffer: ComplexVector) {
        val len = buffer.size
        for (i in 0 until len) {
            val i2 = i * 2
            val re = buffer.data[i2]
            val im = buffer.data[i2 + 1]

            val rads = (phaseAccumulator.toDouble() / 4294967296.0 * 2.0 * Math.PI).toFloat()
            val c = cos(rads)
            val s = sin(rads)

            buffer.data[i2] = re * c - im * s
            buffer.data[i2 + 1] = re * s + im * c

            phaseAccumulator += frequencyIncrement
        }
    }

    /**
     * Resets NCO phase accumulator.
     */
    fun resetPhase() {
        phaseAccumulator = 0L
    }
}
