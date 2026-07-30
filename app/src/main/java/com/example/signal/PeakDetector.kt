package com.example.signal

/**
 * Detects spectral peaks in power or magnitude spectrum arrays.
 * Provides peak bin index, interpolated frequency, power level, and SNR.
 */
class PeakDetector {

    /**
     * Spectral peak detection result.
     *
     * @property binIndex Array index of the detected peak.
     * @property frequencyHz Center frequency corresponding to the peak bin.
     * @property powerLevel Measured power at the peak.
     * @property snrDb Estimated SNR of the peak above median floor in dB.
     */
    data class Peak(
        val binIndex: Int,
        val frequencyHz: Float,
        val powerLevel: Float,
        val snrDb: Float
    )

    /**
     * Finds the strongest spectral peak in [powerSpectrum].
     *
     * @param powerSpectrum Input spectrum array.
     * @param sampleRateHz Total sample rate for frequency calculation.
     * @param centerFrequencyHz RF center frequency offset (defaults to 0 Hz for baseband).
     */
    fun findDominantPeak(
        powerSpectrum: FloatArray,
        sampleRateHz: Float,
        centerFrequencyHz: Float = 0f
    ): Peak? {
        if (powerSpectrum.isEmpty()) return null
        var maxIndex = 0
        var maxPower = powerSpectrum[0]
        var totalPower = 0f
        for (i in powerSpectrum.indices) {
            val p = powerSpectrum[i]
            totalPower += p
            if (p > maxPower) {
                maxPower = p
                maxIndex = i
            }
        }
        val averageFloor = maxOf(1e-9f, (totalPower - maxPower) / maxOf(1, powerSpectrum.size - 1))
        val snrLinear = maxPower / averageFloor
        val snrDb = (10.0 * Math.log10(snrLinear.toDouble())).toFloat()

        // Map FFT bin index to normalized frequency [-sampleRate/2 to sampleRate/2]
        val binFreqHz = if (maxIndex < powerSpectrum.size / 2) {
            maxIndex.toFloat() / powerSpectrum.size * sampleRateHz
        } else {
            (maxIndex - powerSpectrum.size).toFloat() / powerSpectrum.size * sampleRateHz
        }
        return Peak(
            binIndex = maxIndex,
            frequencyHz = centerFrequencyHz + binFreqHz,
            powerLevel = maxPower,
            snrDb = snrDb
        )
    }

    /**
     * Finds all local peaks above [minSnrDb] threshold.
     */
    fun findAllPeaks(
        powerSpectrum: FloatArray,
        sampleRateHz: Float,
        minSnrDb: Float = 10f,
        maxPeaks: Int = 10
    ): List<Peak> {
        if (powerSpectrum.size < 3) return emptyList()
        var totalPower = 0f
        for (p in powerSpectrum) {
            totalPower += p
        }
        val averageFloor = maxOf(1e-9f, totalPower / powerSpectrum.size)
        val minPower = averageFloor * Math.pow(10.0, minSnrDb / 10.0).toFloat()

        val results = mutableListOf<Peak>()
        for (i in 1 until powerSpectrum.size - 1) {
            val prev = powerSpectrum[i - 1]
            val curr = powerSpectrum[i]
            val next = powerSpectrum[i + 1]
            if (curr > prev && curr > next && curr >= minPower) {
                val snrLinear = curr / averageFloor
                val snrDb = (10.0 * Math.log10(snrLinear.toDouble())).toFloat()
                val binFreqHz = if (i < powerSpectrum.size / 2) {
                    i.toFloat() / powerSpectrum.size * sampleRateHz
                } else {
                    (i - powerSpectrum.size).toFloat() / powerSpectrum.size * sampleRateHz
                }
                results.add(Peak(i, binFreqHz, curr, snrDb))
            }
        }
        return results.sortedByDescending { it.powerLevel }.take(maxPeaks)
    }
}
