package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.SignalMetrics
import com.example.ui.components.ConstellationPlot
import com.example.ui.components.ControlPanel
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.DSPLockedGreen
import com.example.ui.theme.DSPUnlockedRed

/**
 * Dedicated QPSK Constellation & Carrier Recovery view showing EVM RMS,
 * Costas loop phase error, and AGC lock status.
 */
@Composable
fun ConstellationScreen(
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
            text = "DVB-S QPSK CONSTELLATION & CARRIER RECOVERY",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Costas Loop Lock",
                value = if (metrics.carrierLocked) "LOCKED" else "SEARCHING",
                statusColor = if (metrics.carrierLocked) DSPLockedGreen else DSPUnlockedRed,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "EVM RMS",
                value = String.format("%.2f", metrics.evmRmsPercent),
                unit = "%",
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Phase Error",
                value = String.format("%.2f", metrics.phaseErrorDeg),
                unit = "deg",
                modifier = Modifier.weight(1f)
            )
        }

        ConstellationPlot(
            constellationPoints = metrics.constellationPoints,
            evmRmsPercent = metrics.evmRmsPercent,
            carrierLocked = metrics.carrierLocked,
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
