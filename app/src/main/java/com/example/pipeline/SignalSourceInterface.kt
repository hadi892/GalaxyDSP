package com.example.pipeline

import com.example.core.ComplexVector

/**
 * Signal Source Interface for hardware-independent streaming I/Q samples.
 */
interface SignalSourceInterface {
    /**
     * Sample rate of the source in Hertz.
     */
    val sampleRateHz: Float

    /**
     * Center RF frequency in Hertz.
     */
    val centerFrequencyHz: Float

    /**
     * Reads up to [maxSamples] into [destination].
     *
     * @return Number of complex samples read.
     */
    fun readSamples(destination: ComplexVector, maxSamples: Int = destination.size): Int

    /**
     * Whether the signal source is currently active and producing samples.
     */
    val isActive: Boolean
}
