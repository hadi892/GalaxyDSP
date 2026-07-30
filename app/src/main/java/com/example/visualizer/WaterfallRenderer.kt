package com.example.visualizer

import kotlin.math.abs
import kotlin.math.pow

/**
 * Waterfall spectrogram renderer and historical buffer.
 * Maps dBFS spectra to RGBA color maps (Viridis, Inferno, Turbo, Jet) for real-time waterfall display.
 *
 * @property numBins Number of frequency bins per line.
 * @property historyLines Maximum number of historical spectrogram lines.
 */
class WaterfallRenderer(
    val numBins: Int = 1024,
    val historyLines: Int = 120
) {
    /**
     * Color map themes for spectrogram visualization.
     */
    enum class ColorMap {
        TURBO,
        INFERNO,
        VIRIDIS,
        JET
    }

    private val ringBuffer = Array(historyLines) { FloatArray(numBins) { -120f } }
    private var headLine = 0

    /**
     * Pushes a new spectrum line into the waterfall history buffer.
     *
     * @param spectrumDb Array of dBFS power values of length [numBins].
     */
    @Synchronized
    fun pushSpectrum(spectrumDb: FloatArray) {
        val target = ringBuffer[headLine]
        val len = minOf(spectrumDb.size, numBins)
        System.arraycopy(spectrumDb, 0, target, 0, len)
        headLine = (headLine + historyLines - 1) % historyLines
    }

    /**
     * Renders waterfall history into a 32-bit ARGB pixel buffer for Compose Canvas bitmap rendering.
     *
     * @param destinationPixels ARGB pixel array of size [numBins] * [historyLines].
     * @param minDb Minimum dB level mapped to bottom of color map.
     * @param maxDb Maximum dB level mapped to top of color map.
     * @param colorMap Selected ColorMap theme.
     */
    @Synchronized
    fun renderToPixels(
        destinationPixels: IntArray,
        minDb: Float = -110f,
        maxDb: Float = -20f,
        colorMap: ColorMap = ColorMap.TURBO
    ) {
        val dbRange = maxOf(1f, maxDb - minDb)
        for (line in 0 until historyLines) {
            val bufIdx = (headLine + line) % historyLines
            val lineData = ringBuffer[bufIdx]
            val rowOffset = line * numBins
            for (x in 0 until numBins) {
                val norm = ((lineData[x] - minDb) / dbRange).coerceIn(0f, 1f)
                destinationPixels[rowOffset + x] = mapColor(norm, colorMap)
            }
        }
    }

    private fun mapColor(norm: Float, colorMap: ColorMap): Int {
        val alpha = 0xFF000000.toInt()
        when (colorMap) {
            ColorMap.TURBO -> {
                val r = (norm * 255f).toInt().coerceIn(0, 255)
                val g = ((1f - Math.abs(norm - 0.5f) * 2f) * 255f).toInt().coerceIn(0, 255)
                val b = ((1f - norm) * 255f).toInt().coerceIn(0, 255)
                return alpha or (r shl 16) or (g shl 8) or b
            }
            ColorMap.INFERNO -> {
                val r = ((norm.pow(0.7f)) * 255f).toInt().coerceIn(0, 255)
                val g = ((norm.pow(1.5f)) * 240f).toInt().coerceIn(0, 255)
                val b = ((norm.pow(2.2f)) * 180f).toInt().coerceIn(0, 255)
                return alpha or (r shl 16) or (g shl 8) or b
            }
            ColorMap.VIRIDIS -> {
                val r = ((norm * 0.9f) * 255f).toInt().coerceIn(0, 255)
                val g = ((0.3f + 0.7f * norm) * 255f).toInt().coerceIn(0, 255)
                val b = ((0.5f + 0.5f * (1f - norm)) * 255f).toInt().coerceIn(0, 255)
                return alpha or (r shl 16) or (g shl 8) or b
            }
            ColorMap.JET -> {
                val r = ((1.5f - 4f * Math.abs(norm - 0.75f)).coerceIn(0f, 1f) * 255f).toInt()
                val g = ((1.5f - 4f * Math.abs(norm - 0.5f)).coerceIn(0f, 1f) * 255f).toInt()
                val b = ((1.5f - 4f * Math.abs(norm - 0.25f)).coerceIn(0f, 1f) * 255f).toInt()
                return alpha or (r shl 16) or (g shl 8) or b
            }
        }
    }

    @Synchronized
    fun clear() {
        for (i in 0 until historyLines) {
            ringBuffer[i].fill(-120f)
        }
    }
}
