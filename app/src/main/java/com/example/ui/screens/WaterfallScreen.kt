package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.SignalMetrics
import com.example.ui.components.WaterfallPlot
import com.example.visualizer.WaterfallRenderer

/**
 * Dedicated Waterfall Spectrogram view with Turbo, Viridis, Inferno, and Jet colormap switcher.
 */
@Composable
fun WaterfallScreen(
    metrics: SignalMetrics,
    modifier: Modifier = Modifier
) {
    var selectedColorMap by remember { mutableStateOf(WaterfallRenderer.ColorMap.TURBO) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REAL-TIME WATERFALL SPECTROGRAM",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (cmap in WaterfallRenderer.ColorMap.entries) {
                    val isSelected = cmap == selectedColorMap
                    Button(
                        onClick = { selectedColorMap = cmap },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = cmap.name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        WaterfallPlot(
            fftSpectrum = metrics.fftSpectrum,
            colorMap = selectedColorMap,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
