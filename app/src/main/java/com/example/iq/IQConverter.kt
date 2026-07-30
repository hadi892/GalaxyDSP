package com.example.iq

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility methods for converting between I/Q representation formats without unnecessary allocations.
 */
object IQConverter {

    /**
     * Converts separate real (I) and imaginary (Q) arrays into an interleaved [ComplexVector].
     */
    fun fromSplitArrays(iArray: FloatArray, qArray: FloatArray, destination: ComplexVector) {
        val len = minOf(iArray.size, qArray.size, destination.size)
        for (i in 0 until len) {
            destination.setRaw(i, iArray[i], qArray[i])
        }
    }

    /**
     * Converts a [ComplexVector] into separate real [iDestination] and imaginary [qDestination] arrays.
     */
    fun toSplitArrays(source: ComplexVector, iDestination: FloatArray, qDestination: FloatArray) {
        val len = minOf(source.size, iDestination.size, qDestination.size)
        for (i in 0 until len) {
            val idx = i * 2
            iDestination[i] = source.data[idx]
            qDestination[i] = source.data[idx + 1]
        }
    }

    /**
     * Converts real-valued passband samples to complex baseband I/Q via digital quadrature mixing.
     *
     * @param realSamples Source time-domain real samples.
     * @param destination Target complex baseband vector.
     * @param centerFreqHz Carrier frequency to mix down to DC (0 Hz).
     * @param sampleRateHz Sample rate in Hertz.
     */
    fun realToComplexBaseband(
        realSamples: FloatArray,
        destination: ComplexVector,
        centerFreqHz: Float,
        sampleRateHz: Float
    ) {
        val len = minOf(realSamples.size, destination.size)
        val phaseIncrement = (2.0 * Math.PI * centerFreqHz / sampleRateHz)
        var phase = 0.0
        for (i in 0 until len) {
            val sample = realSamples[i]
            val iVal = (sample * cos(phase)).toFloat()
            val qVal = -(sample * sin(phase)).toFloat() // Negative sign for down-mixing
            destination.setRaw(i, iVal, qVal)
            phase = (phase + phaseIncrement) % (2.0 * Math.PI)
        }
    }

    /**
     * Converts an interleaved FloatArray [re0, im0, re1, im1, ...] into a [ComplexVector].
     */
    fun fromInterleavedArray(source: FloatArray, destination: ComplexVector) {
        val samples = minOf(source.size / 2, destination.size)
        System.arraycopy(source, 0, destination.data, 0, samples * 2)
    }

    /**
     * Converts a [ComplexVector] into an interleaved FloatArray [re0, im0, re1, im1, ...].
     */
    fun toInterleavedArray(source: ComplexVector, destination: FloatArray) {
        val samples = minOf(source.size, destination.size / 2)
        System.arraycopy(source.data, 0, destination, 0, samples * 2)
    }
}
