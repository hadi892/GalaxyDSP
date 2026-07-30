package com.example.iq

import com.example.core.ComplexVector
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Programmable I/Q signal generator for synthesizing DVB-S test streams, continuous-wave (CW) carriers,
 * BPSK/QPSK constellations, AWGN noise, and swept chirps.
 *
 * @param sampleRateHz Sample rate in Hertz.
 */
class IQGenerator(val sampleRateHz: Float) {

    private var phaseRads = 0.0
    private val random = Random(42)

    /**
     * Generates a Continuous Wave (CW) complex tone at [frequencyHz] with [amplitude].
     */
    fun generateCW(destination: ComplexVector, frequencyHz: Float, amplitude: Float = 1.0f) {
        val phaseIncrement = (2.0 * Math.PI * frequencyHz / sampleRateHz)
        for (i in 0 until destination.size) {
            val iVal = (amplitude * cos(phaseRads)).toFloat()
            val qVal = (amplitude * sin(phaseRads)).toFloat()
            destination.setRaw(i, iVal, qVal)
            phaseRads = (phaseRads + phaseIncrement) % (2.0 * Math.PI)
        }
    }

    /**
     * Generates a synthesized DVB-S QPSK symbol stream with optional [snrDb] Additive White Gaussian Noise (AWGN).
     *
     * @param destination Target vector to fill with QPSK I/Q symbols.
     * @param symbolRateHz Symbol rate in Hertz.
     * @param snrDb Signal-to-Noise Ratio in dB.
     */
    fun generateQpskStream(
        destination: ComplexVector,
        symbolRateHz: Float = 500_000f,
        snrDb: Float = 15.0f
    ) {
        val samplesPerSymbol = maxOf(1, (sampleRateHz / symbolRateHz).toInt())
        val noiseSigma = if (snrDb >= 50f) 0f else sqrt(0.5f * Math.pow(10.0, -snrDb / 10.0)).toFloat()
        val constValue = 0.70710678f // 1 / sqrt(2)

        var currentI = constValue
        var currentQ = constValue
        for (i in 0 until destination.size) {
            if (i % samplesPerSymbol == 0) {
                // Generate new QPSK symbol (+/- constValue, +/- constValue)
                currentI = if (random.nextBoolean()) constValue else -constValue
                currentQ = if (random.nextBoolean()) constValue else -constValue
            }
            val noiseI = if (noiseSigma > 0f) nextGaussian() * noiseSigma else 0f
            val noiseQ = if (noiseSigma > 0f) nextGaussian() * noiseSigma else 0f
            destination.setRaw(i, currentI + noiseI, currentQ + noiseQ)
        }
    }

    /**
     * Generates a BPSK symbol stream with optional AWGN noise.
     */
    fun generateBpskStream(
        destination: ComplexVector,
        symbolRateHz: Float = 500_000f,
        snrDb: Float = 15.0f
    ) {
        val samplesPerSymbol = maxOf(1, (sampleRateHz / symbolRateHz).toInt())
        val noiseSigma = if (snrDb >= 50f) 0f else sqrt(0.5f * Math.pow(10.0, -snrDb / 10.0)).toFloat()

        var currentI = 1f
        for (i in 0 until destination.size) {
            if (i % samplesPerSymbol == 0) {
                currentI = if (random.nextBoolean()) 1f else -1f
            }
            val noiseI = if (noiseSigma > 0f) nextGaussian() * noiseSigma else 0f
            val noiseQ = if (noiseSigma > 0f) nextGaussian() * noiseSigma else 0f
            destination.setRaw(i, currentI + noiseI, noiseQ)
        }
    }

    /**
     * Generates pure AWGN noise with the specified standard deviation [sigma].
     */
    fun generateAwgn(destination: ComplexVector, sigma: Float = 0.1f) {
        for (i in 0 until destination.size) {
            destination.setRaw(i, nextGaussian() * sigma, nextGaussian() * sigma)
        }
    }

    /**
     * Generates a Linear Swept Chirp from [startFreqHz] to [endFreqHz].
     */
    fun generateChirp(
        destination: ComplexVector,
        startFreqHz: Float,
        endFreqHz: Float,
        amplitude: Float = 1.0f
    ) {
        val durationSeconds = destination.size / sampleRateHz
        val k = (endFreqHz - startFreqHz) / durationSeconds
        for (i in 0 until destination.size) {
            val t = i / sampleRateHz
            val instantaneousPhase = 2.0 * Math.PI * (startFreqHz * t + 0.5 * k * t * t)
            val iVal = (amplitude * cos(instantaneousPhase)).toFloat()
            val qVal = (amplitude * sin(instantaneousPhase)).toFloat()
            destination.setRaw(i, iVal, qVal)
        }
    }

    private fun nextGaussian(): Float {
        var u1: Double
        do {
            u1 = random.nextDouble()
        } while (u1 <= 1e-15)
        val u2 = random.nextDouble()
        val r = sqrt(-2.0 * ln(u1))
        val theta = 2.0 * Math.PI * u2
        return (r * cos(theta)).toFloat()
    }
}
