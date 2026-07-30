package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.SignalMetrics
import com.example.ui.theme.DSPGridLine
import com.example.ui.theme.DSPLockedGreen
import com.example.ui.theme.DSPUnlockedRed
import com.example.utils.FormatUtils

/**
 * Reusable Telemetry Metric Card displaying title, value, unit, and status color indicator.
 */
@Composable
fun TelemetryCard(
    title: String,
    value: String,
    unit: String = "",
    statusColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .border(1.dp, DSPGridLine, RoundedCornerShape(8.dp))
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Grid of telemetry cards for primary DVB-S receiver metrics.
 */
@Composable
fun PrimaryTelemetryGrid(
    metrics: SignalMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Carrier Lock",
                value = if (metrics.carrierLocked) "LOCKED" else "SEARCHING",
                statusColor = if (metrics.carrierLocked) DSPLockedGreen else DSPUnlockedRed,
                modifier = Modifier.weight(1f),
                testTag = "card_carrier_lock"
            )
            TelemetryCard(
                title = "Channel SNR",
                value = String.format("%.1f", metrics.snrDb),
                unit = "dB",
                statusColor = if (metrics.snrDb >= 12f) DSPLockedGreen else Color.Yellow,
                modifier = Modifier.weight(1f),
                testTag = "card_channel_snr"
            )
            TelemetryCard(
                title = "EVM RMS",
                value = String.format("%.1f", metrics.evmRmsPercent),
                unit = "%",
                statusColor = if (metrics.evmRmsPercent <= 8f) DSPLockedGreen else Color.Yellow,
                modifier = Modifier.weight(1f),
                testTag = "card_evm_rms"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Phase Error",
                value = String.format("%.1f", metrics.phaseErrorDeg),
                unit = "deg",
                statusColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                testTag = "card_phase_error"
            )
            TelemetryCard(
                title = "Noise Floor",
                value = String.format("%.1f", metrics.noiseFloorDb),
                unit = "dBFS",
                statusColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                testTag = "card_noise_floor"
            )
            TelemetryCard(
                title = "MPEG TS Packets",
                value = FormatUtils.formatCount(metrics.demodulatedPackets),
                statusColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                testTag = "card_packets_count"
            )
        }
    }
}
