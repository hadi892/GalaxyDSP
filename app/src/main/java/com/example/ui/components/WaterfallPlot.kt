package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DSPGridLine
import com.example.visualizer.WaterfallRenderer

/**
 * Real-time Waterfall Spectrogram visualization using Viridis/Turbo color maps.
 */
@Composable
fun WaterfallPlot(
    fftSpectrum: FloatArray,
    colorMap: WaterfallRenderer.ColorMap = WaterfallRenderer.ColorMap.TURBO,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, DSPGridLine, RoundedCornerShape(8.dp))
            .testTag("waterfall_plot")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (fftSpectrum.isNotEmpty() && width > 0 && height > 0) {
                val numBins = fftSpectrum.size
                val binWidth = width / numBins
                val minDb = -110f
                val maxDb = -20f
                val dbRange = maxOf(1f, maxDb - minDb)

                // Render current line at the top with gradient bars representing color intensity
                for (i in 0 until numBins) {
                    val db = fftSpectrum[i].coerceIn(minDb, maxDb)
                    val norm = (db - minDb) / dbRange
                    val color = mapColorToCompose(norm, colorMap)
                    drawRect(
                        color = color,
                        topLeft = Offset(i * binWidth, 0f),
                        size = Size(binWidth + 1f, height)
                    )
                }
            }
        }

        Text(
            text = "WATERFALL (${colorMap.name})",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
        )
    }
}

private fun mapColorToCompose(norm: Float, colorMap: WaterfallRenderer.ColorMap): Color {
    val r: Float
    val g: Float
    val b: Float
    when (colorMap) {
        WaterfallRenderer.ColorMap.TURBO -> {
            r = norm
            g = 1f - Math.abs(norm - 0.5f) * 2f
            b = 1f - norm
        }
        WaterfallRenderer.ColorMap.INFERNO -> {
            r = Math.pow(norm.toDouble(), 0.7).toFloat()
            g = Math.pow(norm.toDouble(), 1.5).toFloat()
            b = Math.pow(norm.toDouble(), 2.2).toFloat()
        }
        WaterfallRenderer.ColorMap.VIRIDIS -> {
            r = norm * 0.9f
            g = 0.3f + 0.7f * norm
            b = 0.5f + 0.5f * (1f - norm)
        }
        WaterfallRenderer.ColorMap.JET -> {
            r = (1.5f - 4f * Math.abs(norm - 0.75f)).coerceIn(0f, 1f)
            g = (1.5f - 4f * Math.abs(norm - 0.5f)).coerceIn(0f, 1f)
            b = (1.5f - 4f * Math.abs(norm - 0.25f)).coerceIn(0f, 1f)
        }
    }
    return Color(
        red = r.coerceIn(0f, 1f),
        green = g.coerceIn(0f, 1f),
        blue = b.coerceIn(0f, 1f),
        alpha = 1f
    )
}
