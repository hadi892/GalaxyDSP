package com.example.signal

/**
 * High-performance FIFO circular buffer for floating-point signal samples.
 * Thread-safe for single-producer single-consumer or synchronized access.
 *
 * @param capacity Maximum number of samples the buffer can hold.
 */
class CircularBuffer(val capacity: Int) {
    private val buffer = FloatArray(capacity)
    private var head = 0
    private var tail = 0
    private var count = 0

    /**
     * Current number of samples stored in the buffer.
     */
    val size: Int
        @Synchronized get() = count

    /**
     * Whether the buffer is empty.
     */
    val isEmpty: Boolean
        @Synchronized get() = count == 0

    /**
     * Whether the buffer is full.
     */
    val isFull: Boolean
        @Synchronized get() = count == capacity

    /**
     * Writes a single sample to the buffer. If full, overwrites oldest sample.
     */
    @Synchronized
    fun write(sample: Float) {
        buffer[tail] = sample
        tail = (tail + 1) % capacity
        if (count == capacity) {
            head = (head + 1) % capacity
        } else {
            count++
        }
    }

    /**
     * Writes an array of samples into the buffer.
     *
     * @param samples Array of samples to write.
     * @param offset Starting offset in [samples].
     * @param length Number of samples to write.
     */
    @Synchronized
    fun write(samples: FloatArray, offset: Int = 0, length: Int = samples.size) {
        require(offset >= 0 && length >= 0 && offset + length <= samples.size) {
            "Invalid offset or length for sample array write."
        }
        for (i in 0 until length) {
            write(samples[offset + i])
        }
    }

    /**
     * Reads up to [maxCount] samples from the buffer into [destination].
     *
     * @return Number of samples actually read.
     */
    @Synchronized
    fun read(destination: FloatArray, offset: Int = 0, maxCount: Int = destination.size - offset): Int {
        val toRead = minOf(count, maxCount)
        for (i in 0 until toRead) {
            destination[offset + i] = buffer[head]
            head = (head + 1) % capacity
        }
        count -= toRead
        return toRead
    }

    /**
     * Peeks at the oldest [count] samples without removing them from the buffer.
     *
     * @return Number of samples peeked.
     */
    @Synchronized
    fun peek(destination: FloatArray, offset: Int = 0, maxCount: Int = destination.size - offset): Int {
        val toPeek = minOf(count, maxCount)
        var currentHead = head
        for (i in 0 until toPeek) {
            destination[offset + i] = buffer[currentHead]
            currentHead = (currentHead + 1) % capacity
        }
        return toPeek
    }

    /**
     * Clears all samples from the circular buffer.
     */
    @Synchronized
    fun clear() {
        head = 0
        tail = 0
        count = 0
    }
}
