package com.example.hardware

import com.example.core.ComplexVector
import com.example.iq.IQGenerator
import com.example.pipeline.SignalSourceInterface
import kotlin.math.sin

/**
 * Hardware Abstraction Layer (HAL) for RF Frontend and SDR devices.
 * Provides synthetic streaming I/Q source with real-time simulated DVB-S QPSK signals,
 * AWGN noise floor, and configurable RF carrier frequencies.
 *
 * @property sampleRateHz Configured sampling rate in Hertz.
 * @property centerFrequencyHz Configured RF center frequency in Hertz.
 */
class HardwareAbstractionLayer(
    override val sampleRateHz: Float = 1_000_000f,
    override var centerFrequencyHz: Float = 1_200_000_000f
) : SignalSourceInterface {

    override var isActive: Boolean = false
        private set

    private val generator = IQGenerator(sampleRateHz)
    private var snrDb = 18.0f

    /**
     * Starts RF sample acquisition.
     */
    fun startCapture() {
        isActive = true
    }

    /**
     * Stops RF sample acquisition.
     */
    fun stopCapture() {
        isActive = false
    }

    /**
     * Adjusts the simulated channel SNR in dB.
     */
    fun setChannelSnrDb(snr: Float) {
        snrDb = snr.coerceIn(-10f, 50f)
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        if (!isActive) {
            destination.clear()
            return 0
        }
        val count = minOf(destination.size, maxSamples)
        val target = if (count == destination.size) {
            destination
        } else {
            ComplexVector(count)
        }
        generator.generateQpskStream(target, symbolRateHz = 500_000f, snrDb = snrDb)
        if (count < destination.size) {
            System.arraycopy(target.data, 0, destination.data, 0, count * 2)
        }
        return count
    }
}
