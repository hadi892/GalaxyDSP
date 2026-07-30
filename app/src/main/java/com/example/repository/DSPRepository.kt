package com.example.repository

import com.example.benchmark.BenchmarkEngine
import com.example.domain.SignalMetrics
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for interacting with the hardware abstraction layer,
 * DSP processing pipeline, spectrum analyzer, and benchmark engine.
 */
interface DSPRepository {
    /**
     * Observable stream of real-time signal metrics and visualization data.
     */
    val signalMetrics: StateFlow<SignalMetrics>

    /**
     * Starts the DSP processing pipeline and RF sample capture.
     */
    fun startPipeline()

    /**
     * Stops the DSP processing pipeline.
     */
    fun stopPipeline()

    /**
     * Updates simulated channel SNR in decibels.
     */
    fun setChannelSnrDb(snrDb: Float)

    /**
     * Sets RF carrier center frequency in Hertz.
     */
    fun setCenterFrequencyHz(frequencyHz: Float)

    /**
     * Runs benchmark test suite across FFT, FIR filters, and QPSK demodulator.
     */
    suspend fun runBenchmarkSuite(): BenchmarkEngine.Report
}
