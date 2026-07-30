package com.example.pipeline

import com.example.core.ComplexVector

/**
 * Signal Sink Interface for receiving processed I/Q samples or demodulated streams.
 */
interface SignalSinkInterface {
    /**
     * Writes processed I/Q samples to sink.
     */
    fun writeSamples(buffer: ComplexVector, count: Int = buffer.size)

    /**
     * Writes demodulated transport stream bytes to sink.
     */
    fun writeTransportPacket(tsPacket: ByteArray)

    /**
     * Closes sink and flushes internal buffers.
     */
    fun close()
}
