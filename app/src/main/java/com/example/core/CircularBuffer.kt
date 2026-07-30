package com.example.core

/**
 * High-performance Circular Buffer for real and complex digital signal processing.
 * Designed for SIMD-friendly contiguous window read operations and low garbage allocation.
 *
 * @param capacity Maximum number of complex samples stored in the buffer.
 */
class CircularBuffer(val capacity: Int) {
    init {
        require(capacity > 0) { "CircularBuffer capacity must be positive" }
    }

    private val reBuffer = FloatArray(capacity)
    private val imBuffer = FloatArray(capacity)
    private var writeIndex = 0
    var size = 0
        private set

    /**
     * Clears buffer contents and resets indices.
     */
    fun clear() {
        writeIndex = 0
        size = 0
    }

    /**
     * Pushes a single complex sample into the circular buffer.
     */
    fun push(re: Float, im: Float) {
        reBuffer[writeIndex] = re
        imBuffer[writeIndex] = im
        writeIndex = (writeIndex + 1) % capacity
        if (size < capacity) {
            size++
        }
    }

    /**
     * Pushes a block of complex samples from a [ComplexVector].
     */
    fun pushVector(vector: ComplexVector, count: Int = vector.size) {
        val toCopy = minOf(count, vector.size)
        for (i in 0 until toCopy) {
            push(vector.getRe(i), vector.getIm(i))
        }
    }

    /**
     * Reads the most recent [count] samples into [destination] in chronological order.
     */
    fun readLatest(destination: ComplexVector, count: Int): Int {
        val available = minOf(size, minOf(count, destination.size))
        var readIdx = (writeIndex - available + capacity) % capacity
        for (i in 0 until available) {
            destination.set(i, reBuffer[readIdx], imBuffer[readIdx])
            readIdx = (readIdx + 1) % capacity
        }
        return available
    }

    /**
     * Reads real and imaginary parts at offset from the oldest stored sample.
     */
    fun getRe(index: Int): Float {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        val actualIdx = (writeIndex - size + index + capacity) % capacity
        return reBuffer[actualIdx]
    }

    fun getIm(index: Int): Float {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        val actualIdx = (writeIndex - size + index + capacity) % capacity
        return imBuffer[actualIdx]
    }
}
