package com.example.signal

/**
 * Tracks the dynamic noise floor of a power spectrum over time using
 * exponential moving averages and lower-percentile order statistics.
 *
 * @param numBins Number of frequency bins in the monitored spectrum.
 * @param alpha Exponential smoothing factor (0.0 to 1.0) for updating floor estimate.
 */
class NoiseFloor(val numBins: Int, val alpha: Float = 0.05f) {

    val floorSpectrum = FloatArray(numBins)
    private var isInitialized = false

    /**
     * Updates the noise floor estimate with a new incoming power spectrum block.
     *
     * @param powerSpectrum Current incoming power spectrum array of length [numBins].
     */
    @Synchronized
    fun update(powerSpectrum: FloatArray) {
        require(powerSpectrum.size >= numBins) { "Input power spectrum size mismatch." }
        if (!isInitialized) {
            System.arraycopy(powerSpectrum, 0, floorSpectrum, 0, numBins)
            isInitialized = true
            return
        }
        for (i in 0 until numBins) {
            val current = powerSpectrum[i]
            val prevFloor = floorSpectrum[i]
            // If current power is lower than estimated floor, drop quickly; otherwise rise slowly
            val adaptRate = if (current < prevFloor) alpha * 3f else alpha
            floorSpectrum[i] = prevFloor + adaptRate * (current - prevFloor)
        }
    }

    /**
     * Computes the average broadband noise floor power across all bins.
     */
    @Synchronized
    fun getAverageFloorPower(): Float {
        if (!isInitialized || numBins == 0) return 0f
        var sum = 0f
        for (value in floorSpectrum) {
            sum += value
        }
        return sum / numBins
    }

    /**
     * Resets the noise floor estimates.
     */
    @Synchronized
    fun reset() {
        floorSpectrum.fill(0f)
        isInitialized = false
    }
}
