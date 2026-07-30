package com.example.visualizer

import com.example.core.ComplexVector
import com.example.fft.FFT
import com.example.fft.PowerSpectrum
import com.example.fft.WindowFunctions
import com.example.fft.WindowType

/**
 * Real-time Spectrum Analyzer engine.
 * Applies windowing, FFT, and power spectrum estimation with configurable smoothing and peak hold.
 *
 * @property fftSize Size of FFT analysis (must be power of 2, e.g. 1024 or 2048).
 * @property sampleRateHz System sample rate in Hertz.
 */
class SpectrumAnalyzer(
    val fftSize: Int = 1024,
    val sampleRateHz: Float = 1_000_000f
) {
    private val fft = FFT(fftSize)
    private val powerSpectrum = PowerSpectrum(fftSize)
    private val window = WindowFunctions.generate(WindowType.BLACKMAN, fftSize)
    private val windowEnbw = WindowFunctions.computeEnbw(window)

    private val workBuffer = ComplexVector(fftSize)
    private val peakHoldSpectrum = FloatArray(fftSize) { -120f }

    /**
     * Processes input I/Q samples and outputs smoothed log power spectrum in dBFS.
     *
     * @param input Input complex vector of length [fftSize].
     * @param destinationDb Output array of length [fftSize] to receive spectrum in dBFS.
     * @param alpha Exponential smoothing factor [0.0..1.0].
     * @return Array of current peak-hold values.
     */
    @Synchronized
    fun analyze(
        input: ComplexVector,
        destinationDb: FloatArray,
        alpha: Float = 0.3f
    ): FloatArray {
        require(input.size == fftSize && destinationDb.size >= fftSize) {
            "Buffer sizes must match fftSize ($fftSize)."
        }
        workBuffer.copyFrom(input)
        WindowFunctions.applyWindowInPlace(workBuffer, window)
        fft.transformInPlace(workBuffer)
        powerSpectrum.computeLogSpectrum(workBuffer, destinationDb, fftShift = true, windowEnbw, alpha)

        for (i in 0 until fftSize) {
            if (destinationDb[i] > peakHoldSpectrum[i]) {
                peakHoldSpectrum[i] = destinationDb[i]
            } else {
                // Slow decay on peak hold
                peakHoldSpectrum[i] = maxOf(-120f, peakHoldSpectrum[i] - 0.2f)
            }
        }
        return peakHoldSpectrum
    }

    /**
     * Resets spectrum averaging and peak hold buffers.
     */
    @Synchronized
    fun reset() {
        powerSpectrum.resetAveraging()
        peakHoldSpectrum.fill(-120f)
    }
}
