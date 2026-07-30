package com.example.fft

import com.example.core.ComplexVector
import kotlin.math.log10

/**
 * Computes Power Spectrum and Power Spectral Density (PSD) from complex FFT output buffers.
 * Includes window gain correction, FFT shift (DC in center), and temporal smoothing.
 *
 * @property fftSize Size of the FFT.
 */
class PowerSpectrum(val fftSize: Int) {

    private val avgPower = FloatArray(fftSize)
    private var isFirstFrame = true

    /**
     * Computes the log power spectrum in dBFS (decibels relative to full scale) with optional FFT shift.
     *
     * @param fftBuffer The complex FFT result vector of size [fftSize].
     * @param destination Output array of length [fftSize] to hold dBFS values.
     * @param fftShift If true, rotates bins so 0 Hz (DC) is at index fftSize/2.
     * @param windowEnbw Equivalent noise bandwidth of the window applied before FFT.
     * @param alpha Smoothing factor [0.0..1.0] for exponential averaging (1.0 = no smoothing).
     */
    fun computeLogSpectrum(
        fftBuffer: ComplexVector,
        destination: FloatArray,
        fftShift: Boolean = true,
        windowEnbw: Float = 1.0f,
        alpha: Float = 1.0f
    ) {
        require(fftBuffer.size == fftSize && destination.size >= fftSize) {
            "Buffer sizes must match fftSize ($fftSize)."
        }
        val normFactor = 1.0f / (fftSize * fftSize * windowEnbw)
        for (i in 0 until fftSize) {
            val i2 = i * 2
            val re = fftBuffer.data[i2]
            val im = fftBuffer.data[i2 + 1]
            val rawPower = (re * re + im * im) * normFactor

            val p = if (isFirstFrame || alpha >= 1.0f) {
                rawPower
            } else {
                avgPower[i] + alpha * (rawPower - avgPower[i])
            }
            avgPower[i] = p

            val dbFS = if (p <= 1e-12f) -120f else (10.0 * log10(p.toDouble())).toFloat()
            val targetIdx = if (fftShift) {
                (i + fftSize / 2) % fftSize
            } else {
                i
            }
            destination[targetIdx] = dbFS
        }
        isFirstFrame = false
    }

    /**
     * Resets the moving average power spectrum buffer.
     */
    fun resetAveraging() {
        avgPower.fill(0f)
        isFirstFrame = true
    }
}
