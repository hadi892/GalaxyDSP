package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DSPLockedGreen
import com.example.ui.theme.DSPUnlockedRed
import com.example.utils.FormatUtils

/**
 * Control Panel for adjusting channel SNR, RF center frequency, and toggling
 * DVB-S DSP pipeline execution.
 */
@Composable
fun ControlPanel(
    snrDb: Float,
    centerFrequencyHz: Float,
    isRunning: Boolean,
    onSnrChanged: (Float) -> Unit,
    onFrequencyChanged: (Float) -> Unit,
    onTogglePipeline: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Start / Stop Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DSP SIMULATION CONTROLS",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = { onTogglePipeline(!isRunning) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) DSPUnlockedRed else DSPLockedGreen
                    ),
                    modifier = Modifier.testTag("toggle_pipeline_button")
                ) {
                    Text(
                        text = if (isRunning) "STOP PIPELINE" else "START PIPELINE",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Channel SNR Slider [-5 dB .. 35 dB]
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Channel SNR (AWGN)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format("%.1f dB", snrDb),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = snrDb,
                    onValueChange = onSnrChanged,
                    valueRange = -5f..35f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snr_slider")
                )
            }

            // RF Center Frequency Slider [1.1 GHz .. 1.3 GHz]
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RF Carrier Frequency",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = FormatUtils.formatFrequency(centerFrequencyHz),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = centerFrequencyHz,
                    onValueChange = onFrequencyChanged,
                    valueRange = 1_100_000_000f..1_300_000_000f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("freq_slider")
                )
            }
        }
    }
}
