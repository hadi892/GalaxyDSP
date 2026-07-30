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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DSPGridLine
import com.example.ui.theme.DSPPrimary
import com.example.utils.FormatUtils

/**
 * Real-time FFT Spectrum Analyzer plot with grid lines, dBFS scale [-110..-20 dBFS],
 * peak marker indicator, and center frequency labels.
 */
@Composable
fun SpectrumPlot(
    fftSpectrum: FloatArray,
    centerFrequencyHz: Float,
    sampleRateHz: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, DSPGridLine, RoundedCornerShape(8.dp))
            .padding(4.dp)
            .testTag("spectrum_plot")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val minDb = -110f
            val maxDb = -20f
            val dbRange = maxDb - minDb

            // 1. Draw grid lines (horizontal dBFS levels)
            val horizontalLines = 4
            for (i in 1..horizontalLines) {
                val y = height * i / (horizontalLines + 1)
                drawLine(
                    color = DSPGridLine.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // 2. Draw vertical grid lines
            val verticalLines = 6
            for (i in 1..verticalLines) {
                val x = width * i / (verticalLines + 1)
                drawLine(
                    color = DSPGridLine.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }

            // 3. Draw FFT spectrum path
            if (fftSpectrum.isNotEmpty()) {
                val path = Path()
                val stepX = width / maxOf(1, fftSpectrum.size - 1)
                var first = true

                var peakVal = -120f
                var peakX = 0f
                var peakY = height

                for (i in fftSpectrum.indices) {
                    val db = fftSpectrum[i].coerceIn(minDb, maxDb)
                    val norm = (db - minDb) / dbRange
                    val x = i * stepX
                    val y = height * (1f - norm)

                    if (db > peakVal) {
                        peakVal = db
                        peakX = x
                        peakY = y
                    }

                    if (first) {
                        path.moveTo(x, y)
                        first = false
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Fill area below curve with faint cyan gradient
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    color = DSPPrimary.copy(alpha = 0.15f)
                )

                // Stroke main spectrum curve
                drawPath(
                    path = path,
                    color = DSPPrimary,
                    style = Stroke(width = 2.5f)
                )

                // 4. Draw peak marker circle
                drawCircle(
                    color = Color.Yellow,
                    radius = 4f,
                    center = Offset(peakX, peakY)
                )
            }
        }

        // Labels
        Text(
            text = "${FormatUtils.formatFrequency(centerFrequencyHz)} (Center)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
        )
        Text(
            text = "0 dBFS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
        )
        Text(
            text = "-110 dBFS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
        )
    }
}
