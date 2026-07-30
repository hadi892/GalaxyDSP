package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.SignalMetrics
import com.example.ui.components.ControlPanel
import com.example.ui.components.SpectrumPlot

/**
 * Dedicated full-screen Spectrum Analyzer view with dBFS grid and frequency resolution controls.
 */
@Composable
fun SpectrumScreen(
    metrics: SignalMetrics,
    onSnrChanged: (Float) -> Unit,
    onFrequencyChanged: (Float) -> Unit,
    onTogglePipeline: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SPECTRUM ANALYZER (1024-POINT FFT)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        SpectrumPlot(
            fftSpectrum = metrics.fftSpectrum,
            centerFrequencyHz = metrics.centerFrequencyHz,
            sampleRateHz = metrics.sampleRateHz,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        ControlPanel(
            snrDb = metrics.snrDb,
            centerFrequencyHz = metrics.centerFrequencyHz,
            isRunning = metrics.isRunning,
            onSnrChanged = onSnrChanged,
            onFrequencyChanged = onFrequencyChanged,
            onTogglePipeline = onTogglePipeline
        )
    }
}
