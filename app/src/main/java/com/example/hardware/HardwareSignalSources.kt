package com.example.hardware

import com.example.common.DSPException
import com.example.core.ComplexVector
import com.example.pipeline.SignalSourceInterface

/**
 * Base Hardware Signal Source interface for physical SDR and RF receivers.
 */
interface HardwareSignalSource : SignalSourceInterface {
    val deviceName: String
    val isConnected: Boolean
    fun connect()
    fun disconnect()
}

/**
 * Snapdragon Hexagon DSP FastRPC hardware interface.
 * Throws explicit hardware exception when DSP FastRPC driver is unavailable on generic Android builds.
 */
class FutureFastRPCSource(
    override val sampleRateHz: Float = 1_000_000f,
    override val centerFrequencyHz: Float = 1_200_000_000f
) : HardwareSignalSource {

    override val deviceName: String = "Qualcomm Hexagon DSP (FastRPC)"
    override var isConnected: Boolean = false
        private set

    override val isActive: Boolean
        get() = isConnected

    override fun connect() {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: Qualcomm Hexagon cDSP/aDSP FastRPC device node (/dev/adsprpc-smd) is inaccessible or requires system/vendor privilege."
        )
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: Qualcomm Hexagon DSP I/Q streaming is not supported on this device kernel."
        )
    }
}

/**
 * Snapdragon Dedicated DSP baseband interface.
 */
class FutureDSPSource(
    override val sampleRateHz: Float = 1_000_000f,
    override val centerFrequencyHz: Float = 1_200_000_000f
) : HardwareSignalSource {

    override val deviceName: String = "Snapdragon Integrated DSP Baseband"
    override var isConnected: Boolean = false
        private set

    override val isActive: Boolean
        get() = isConnected

    override fun connect() {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: Dedicated Snapdragon baseband RF DMA interface is not exposed to userspace."
        )
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: DSP hardware I/Q DMA pipeline is offline."
        )
    }
}

/**
 * USB SDR Hardware Source (e.g., RTL-SDR, HackRF, Airspy via USB Host / LibUSB).
 */
class FutureUSBSDRSource(
    override val sampleRateHz: Float = 1_000_000f,
    override val centerFrequencyHz: Float = 1_200_000_000f
) : HardwareSignalSource {

    override val deviceName: String = "USB SDR Receiver (RTL-SDR / HackRF)"
    override var isConnected: Boolean = false
        private set

    override val isActive: Boolean
        get() = isConnected

    override fun connect() {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: USB Host SDR peripheral not attached or LibUSB permission denied."
        )
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: USB bulk transfer endpoint is not open."
        )
    }
}

/**
 * Audio Jack / Line-In SDR IF input interface.
 */
class FutureAudioSource(
    override val sampleRateHz: Float = 48_000f,
    override val centerFrequencyHz: Float = 0f
) : HardwareSignalSource {

    override val deviceName: String = "Stereo Audio IF Input"
    override var isConnected: Boolean = false
        private set

    override val isActive: Boolean
        get() = isConnected

    override fun connect() {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: High-rate stereo I/Q Line-In capture requires external USB DAC interface."
        )
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun readSamples(destination: ComplexVector, maxSamples: Int): Int {
        throw DSPException.HardwareAbstractionException(
            "Not implemented on this device: Audio I/Q IF streaming is not active."
        )
    }
}
