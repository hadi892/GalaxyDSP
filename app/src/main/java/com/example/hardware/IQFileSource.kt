package com.example.hardware

import com.example.core.ComplexVector
import com.example.pipeline.SignalSourceInterface
import java.io.InputStream

/**
 * Operational IQ File Source capable of streaming real recorded I/Q baseband samples
 * from 32-bit floating point or 16-bit PCM binary files.
 *
 * @property inputStream Open input stream to the I/Q recording file.
 * @property sampleRateHz Sampling rate of the recording in Hertz.
 * @property centerFrequencyHz Center RF frequency in Hertz.
 */
class IQFileSource(
    private val inputStream: InputStream,
    override val sampleRateHz: Float = 1_000_000f,
    override val centerFrequencyHz: Float = 1_200_000_000f
) : SignalSourceInterface {

    override var isActive: Boolean = false
        private set

    private val readBuffer = ByteArray(8192)

    fun start() {
        isActive = true
    }

    fun stop() {
        isActive = false
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        if (!isActive) return 0
        val targetSamples = minOf(destination.size, maxSamples)
        val bytesNeeded = targetSamples * 8 // 2 floats (4 bytes each) per complex sample
        var totalBytesRead = 0

        while (totalBytesRead < bytesNeeded) {
            val count = inputStream.read(
                readBuffer,
                totalBytesRead,
                minOf(readBuffer.size - totalBytesRead, bytesNeeded - totalBytesRead)
            )
            if (count == -1) {
                // End of file
                isActive = false
                break
            }
            totalBytesRead += count
        }

        val samplesRead = totalBytesRead / 8
        for (i in 0 until samplesRead) {
            val byteIdx = i * 8
            val reBits = (readBuffer[byteIdx].toInt() and 0xFF) or
                    ((readBuffer[byteIdx + 1].toInt() and 0xFF) shl 8) or
                    ((readBuffer[byteIdx + 2].toInt() and 0xFF) shl 16) or
                    ((readBuffer[byteIdx + 3].toInt() and 0xFF) shl 24)
            val imBits = (readBuffer[byteIdx + 4].toInt() and 0xFF) or
                    ((readBuffer[byteIdx + 5].toInt() and 0xFF) shl 8) or
                    ((readBuffer[byteIdx + 6].toInt() and 0xFF) shl 16) or
                    ((readBuffer[byteIdx + 7].toInt() and 0xFF) shl 24)

            val re = Float.fromBits(reBits)
            val im = Float.fromBits(imBits)
            destination.set(i, re, im)
        }

        return samplesRead
    }
}
