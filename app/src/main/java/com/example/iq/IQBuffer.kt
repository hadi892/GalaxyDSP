package com.example.iq

import com.example.core.Complex
import com.example.core.ComplexVector

/**
 * Circular streaming buffer specifically designed for I/Q complex samples.
 * Supports efficient block read/write using interleaved arrays or ComplexVector.
 *
 * @param capacitySamples Maximum number of complex samples (each has I and Q) in the buffer.
 */
class IQBuffer(val capacitySamples: Int) {
    private val buffer = FloatArray(capacitySamples * 2)
    private var head = 0
    private var tail = 0
    private var sampleCount = 0

    val size: Int
        @Synchronized get() = sampleCount

    val isEmpty: Boolean
        @Synchronized get() = sampleCount == 0

    val isFull: Boolean
        @Synchronized get() = sampleCount == capacitySamples

    /**
     * Writes a single complex sample [sample] into the buffer.
     */
    @Synchronized
    fun write(sample: Complex) {
        val idx = tail * 2
        buffer[idx] = sample.re
        buffer[idx + 1] = sample.im
        tail = (tail + 1) % capacitySamples
        if (sampleCount == capacitySamples) {
            head = (head + 1) % capacitySamples
        } else {
            sampleCount++
        }
    }

    /**
     * Writes all complex samples from [vector] into the buffer.
     */
    @Synchronized
    fun write(vector: ComplexVector) {
        for (i in 0 until vector.size) {
            val i2 = i * 2
            val idx = tail * 2
            buffer[idx] = vector.data[i2]
            buffer[idx + 1] = vector.data[i2 + 1]
            tail = (tail + 1) % capacitySamples
            if (sampleCount == capacitySamples) {
                head = (head + 1) % capacitySamples
            } else {
                sampleCount++
            }
        }
    }

    /**
     * Reads up to [maxSamples] from the buffer into [destination].
     *
     * @return Number of complex samples actually read.
     */
    @Synchronized
    fun read(destination: ComplexVector, maxSamples: Int = destination.size): Int {
        val toRead = minOf(sampleCount, maxSamples)
        var currentHead = head
        for (i in 0 until toRead) {
            val srcIdx = currentHead * 2
            destination.setRaw(i, buffer[srcIdx], buffer[srcIdx + 1])
            currentHead = (currentHead + 1) % capacitySamples
        }
        head = currentHead
        sampleCount -= toRead
        return toRead
    }

    /**
     * Peeks up to [maxSamples] without removing them from the FIFO.
     */
    @Synchronized
    fun peek(destination: ComplexVector, maxSamples: Int = destination.size): Int {
        val toPeek = minOf(sampleCount, maxSamples)
        var currentHead = head
        for (i in 0 until toPeek) {
            val srcIdx = currentHead * 2
            destination.setRaw(i, buffer[srcIdx], buffer[srcIdx + 1])
            currentHead = (currentHead + 1) % capacitySamples
        }
        return toPeek
    }

    @Synchronized
    fun clear() {
        head = 0
        tail = 0
        sampleCount = 0
    }
}
