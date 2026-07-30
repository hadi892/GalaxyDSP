package com.example.loader

import com.example.core.ComplexVector
import com.example.iq.IQBuffer
import com.example.pipeline.SignalSourceInterface

/**
 * Input Manager for buffering and distributing streaming I/Q samples from the hardware source
 * to DSP consumer threads without lock contention.
 *
 * @property bufferCapacity Maximum capacity of the circular I/Q staging buffer.
 */
class InputManager(val bufferCapacity: Int = 8192) {
    private val stagingBuffer = IQBuffer(bufferCapacity)

    /**
     * Polls new samples from [source] into the staging buffer.
     *
     * @param source Signal source interface.
     * @param maxSamples Maximum samples to acquire.
     * @return Number of samples acquired.
     */
    @Synchronized
    fun pollSource(source: SignalSourceInterface, maxSamples: Int = 1024): Int {
        if (!source.isActive) return 0
        val tempVector = ComplexVector(maxSamples)
        val acquired = source.readSamples(tempVector, maxSamples)
        if (acquired > 0) {
            val subVector = if (acquired == maxSamples) tempVector else ComplexVector(acquired, tempVector.data.copyOfRange(0, acquired * 2))
            stagingBuffer.write(subVector)
        }
        return acquired
    }

    /**
     * Reads buffered samples into [destination] for DSP processing.
     */
    @Synchronized
    fun consumeSamples(destination: ComplexVector): Int {
        return stagingBuffer.read(destination)
    }

    /**
     * Clears staging buffer.
     */
    @Synchronized
    fun clear() {
        stagingBuffer.clear()
    }
}
