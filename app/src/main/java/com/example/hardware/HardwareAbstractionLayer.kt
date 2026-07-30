package com.example.hardware

import com.example.common.DSPException
import com.example.core.ComplexVector
import com.example.pipeline.SignalSourceInterface

/**
 * Hardware Abstraction Layer (HAL) for RF Frontend and SDR devices.
 * Adheres strictly to the REAL SIGNAL ARCHITECTURE:
 * Never generates synthetic or simulated DSP data in production builds.
 *
 * All DSP modules receive samples only through hardware abstraction interfaces
 * such as [IQFileSource] or [HardwareSignalSource].
 * If hardware is unavailable, readSamples returns 0 so the application reports
 * "No compatible signal source available".
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

    private var activeSource: SignalSourceInterface? = null
    private var channelSnrDb: Float = 18.0f

    /**
     * Attaches an operational signal source (e.g. [IQFileSource]) or attempts to connect
     * a [HardwareSignalSource] which will throw a meaningful exception if unsupported on this device.
     */
    fun attachSource(source: SignalSourceInterface) {
        activeSource = source
        if (source is HardwareSignalSource) {
            source.connect()
        } else if (source is IQFileSource) {
            source.start()
        }
    }

    /**
     * Detaches any currently attached signal source.
     */
    fun detachSource() {
        val src = activeSource
        if (src is HardwareSignalSource) {
            src.disconnect()
        } else if (src is IQFileSource) {
            src.stop()
        }
        activeSource = null
    }

    /**
     * Starts sample acquisition from the attached signal source.
     */
    fun startCapture() {
        isActive = true
        val src = activeSource
        if (src is IQFileSource) {
            src.start()
        }
    }

    /**
     * Stops sample acquisition.
     */
    fun stopCapture() {
        isActive = false
        val src = activeSource
        if (src is IQFileSource) {
            src.stop()
        }
    }

    /**
     * Adjusts channel SNR reference setting in dB.
     */
    fun setChannelSnrDb(snr: Float) {
        channelSnrDb = snr.coerceIn(-10f, 50f)
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        if (!isActive) {
            destination.clear()
            return 0
        }
        val source = activeSource ?: return 0
        val count = source.readSamples(destination, maxSamples)
        if (count == 0) {
            destination.clear()
        }
        return count
    }
}
