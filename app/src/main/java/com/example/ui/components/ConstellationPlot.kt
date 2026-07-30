package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DSPGridLine
import com.example.ui.theme.DSPPrimary
import com.example.ui.theme.DSPSecondary

/**
 * Interactive QPSK Constellation Scatter Plot with I/Q axes, quadrant boundaries,
 * EVM reference circles, and symbol density glow.
 */
@Composable
fun ConstellationPlot(
    constellationPoints: FloatArray,
    evmRmsPercent: Float,
    carrierLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, DSPGridLine, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .testTag("constellation_plot")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = minOf(cx, cy) * 0.85f

            // 1. Draw Crosshair I/Q axes
            drawLine(
                color = DSPGridLine,
                start = Offset(cx, 0f),
                end = Offset(cx, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = DSPGridLine,
                start = Offset(0f, cy),
                end = Offset(size.width, cy),
                strokeWidth = 1f
            )

            // 2. Draw Ideal QPSK reference circle (r = 1.0)
            drawCircle(
                color = DSPGridLine.copy(alpha = 0.6f),
                radius = radius,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            )

            // 3. Draw four ideal QPSK cluster centers (+/-0.707, +/-0.707)
            val idealOff = radius * 0.70710678f
            val idealCenters = listOf(
                Offset(cx + idealOff, cy - idealOff),
                Offset(cx - idealOff, cy - idealOff),
                Offset(cx - idealOff, cy + idealOff),
                Offset(cx + idealOff, cy + idealOff)
            )
            for (center in idealCenters) {
                drawCircle(
                    color = if (carrierLocked) DSPSecondary else Color.Yellow,
                    radius = 5f,
                    center = center
                )
            }

            // 4. Draw received QPSK symbols scatter
            val numSymbols = constellationPoints.size / 2
            val symbolColor = if (carrierLocked) {
                DSPPrimary.copy(alpha = 0.75f)
            } else {
                Color.Red.copy(alpha = 0.65f)
            }
            for (i in 0 until numSymbols) {
                val re = constellationPoints[i * 2]
                val im = constellationPoints[i * 2 + 1]
                val x = cx + re * radius
                val y = cy - im * radius // Invert Y for standard Cartesian plane

                drawCircle(
                    color = symbolColor,
                    radius = 2.5f,
                    center = Offset(x, y)
                )
            }
        }

        // EVM / Lock Overlay label
        Text(
            text = if (carrierLocked) "QPSK LOCKED | EVM: %.1f%%".format(evmRmsPercent) else "CARRIER UNLOCKED | EVM: %.1f%%".format(evmRmsPercent),
            style = MaterialTheme.typography.labelSmall,
            color = if (carrierLocked) DSPSecondary else Color.Red,
            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
        )
    }
}
