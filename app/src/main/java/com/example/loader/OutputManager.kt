package com.example.loader

import com.example.core.ComplexVector
import com.example.pipeline.SignalSinkInterface
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Output Manager for collecting demodulated MPEG-2 TS packets and processed IQ streams
 * for visualization and telemetry.
 */
class OutputManager : SignalSinkInterface {

    val tsPacketQueue = ConcurrentLinkedQueue<ByteArray>()
    private var totalPacketsWritten = 0L

    /**
     * Total TS packets received from the demodulator.
     */
    val packetCount: Long
        @Synchronized get() = totalPacketsWritten

    override fun writeSamples(buffer: ComplexVector, count: Int) {
        // No-op for base sink
    }

    override fun writeTransportPacket(tsPacket: ByteArray) {
        if (tsPacket.size == 188) {
            tsPacketQueue.offer(tsPacket.copyOf())
            synchronized(this) {
                totalPacketsWritten++
            }
            while (tsPacketQueue.size > 200) {
                tsPacketQueue.poll() // Cap queue memory
            }
        }
    }

    /**
     * Retrieves the next available TS packet from the output queue.
     */
    fun pollTransportPacket(): ByteArray? = tsPacketQueue.poll()

    override fun close() {
        tsPacketQueue.clear()
    }

    @Synchronized
    fun reset() {
        tsPacketQueue.clear()
        totalPacketsWritten = 0L
    }
}
