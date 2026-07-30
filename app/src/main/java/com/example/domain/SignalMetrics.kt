package com.example.domain

/**
 * Immutable domain model containing real-time DVB-S signal telemetry and DSP visualizer data.
 *
 * @property centerFrequencyHz Current RF center frequency in Hertz.
 * @property sampleRateHz System sampling rate in Hertz.
 * @property snrDb Estimated Signal-to-Noise Ratio in dB.
 * @property evmRmsPercent Error Vector Magnitude (RMS %) of demodulated QPSK constellation.
 * @property phaseErrorDeg Residual carrier phase error in degrees.
 * @property noiseFloorDb Estimated noise floor level in dBFS.
 * @property correlationScore Normalized cross-correlation score [0.0..1.0].
 * @property signalProbability Statistical probability of DVB-S carrier presence [0.0..1.0].
 * @property processedSymbols Total count of processed QPSK symbols.
 * @property demodulatedPackets Total count of valid MPEG-2 TS packets demodulated.
 * @property fftSpectrum Current 1024-point FFT power spectrum array in dBFS.
 * @property constellationPoints Interleaved array [I0, Q0, I1, Q1, ...] of recent QPSK symbols for scatter plot.
 * @property isRunning Whether the DSP processing pipeline is currently active.
 * @property carrierLocked True when Costas loop and Gardner timing are locked.
 */
data class SignalMetrics(
    val centerFrequencyHz: Float = 1_200_000_000f,
    val sampleRateHz: Float = 1_000_000f,
    val snrDb: Float = 18.5f,
    val evmRmsPercent: Float = 4.2f,
    val phaseErrorDeg: Float = 1.2f,
    val noiseFloorDb: Float = -85.0f,
    val correlationScore: Float = 0.94f,
    val signalProbability: Float = 0.99f,
    val processedSymbols: Long = 0L,
    val demodulatedPackets: Long = 0L,
    val fftSpectrum: FloatArray = FloatArray(1024) { -95f },
    val constellationPoints: FloatArray = FloatArray(512),
    val isRunning: Boolean = false,
    val carrierLocked: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SignalMetrics
        return centerFrequencyHz == other.centerFrequencyHz &&
                sampleRateHz == other.sampleRateHz &&
                snrDb == other.snrDb &&
                evmRmsPercent == other.evmRmsPercent &&
                phaseErrorDeg == other.phaseErrorDeg &&
                noiseFloorDb == other.noiseFloorDb &&
                correlationScore == other.correlationScore &&
                signalProbability == other.signalProbability &&
                processedSymbols == other.processedSymbols &&
                demodulatedPackets == other.demodulatedPackets &&
                isRunning == other.isRunning &&
                carrierLocked == other.carrierLocked
    }

    override fun hashCode(): Int {
        var result = centerFrequencyHz.hashCode()
        result = 31 * result + sampleRateHz.hashCode()
        result = 31 * result + snrDb.hashCode()
        result = 31 * result + evmRmsPercent.hashCode()
        result = 31 * result + phaseErrorDeg.hashCode()
        result = 31 * result + noiseFloorDb.hashCode()
        result = 31 * result + correlationScore.hashCode()
        result = 31 * result + signalProbability.hashCode()
        result = 31 * result + processedSymbols.hashCode()
        result = 31 * result + demodulatedPackets.hashCode()
        result = 31 * result + isRunning.hashCode()
        result = 31 * result + carrierLocked.hashCode()
        return result
    }
}
