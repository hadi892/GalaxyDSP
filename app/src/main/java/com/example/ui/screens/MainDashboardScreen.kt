package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.SignalMetrics
import com.example.ui.components.ConstellationPlot
import com.example.ui.components.ControlPanel
import com.example.ui.components.PrimaryTelemetryGrid
import com.example.ui.components.SpectrumPlot
import com.example.ui.components.WaterfallPlot

/**
 * Main Dashboard screen integrating live SpectrumPlot, ConstellationPlot, WaterfallPlot,
 * real-time DVB-S Telemetry Grid, and AWGN SNR / RF Frequency Controls.
 */
@Composable
fun MainDashboardScreen(
    metrics: SignalMetrics,
    onSnrChanged: (Float) -> Unit,
    onFrequencyChanged: (Float) -> Unit,
    onTogglePipeline: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Primary Telemetry Metrics Card Grid
        PrimaryTelemetryGrid(metrics = metrics)

        // 2. Interactive Spectrum Analyzer & Constellation Side-by-Side on wide screens or stacked
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SpectrumPlot(
                fftSpectrum = metrics.fftSpectrum,
                centerFrequencyHz = metrics.centerFrequencyHz,
                sampleRateHz = metrics.sampleRateHz,
                modifier = Modifier
                    .weight(1.3f)
                    .height(200.dp)
            )

            ConstellationPlot(
                constellationPoints = metrics.constellationPoints,
                evmRmsPercent = metrics.evmRmsPercent,
                carrierLocked = metrics.carrierLocked,
                modifier = Modifier
                    .weight(0.7f)
                    .height(200.dp)
            )
        }

        // 3. Real-time Waterfall Spectrogram
        WaterfallPlot(
            fftSpectrum = metrics.fftSpectrum,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )

        // 4. Channel SNR & Carrier Frequency Control Panel
        ControlPanel(
            snrDb = metrics.snrDb,
            centerFrequencyHz = metrics.centerFrequencyHz,
            isRunning = metrics.isRunning,
            onSnrChanged = onSnrChanged,
            onFrequencyChanged = onFrequencyChanged,
            onTogglePipeline = onTogglePipeline
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
