package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * About / Architecture screen detailing GalaxyDSP zero-allocation interleaved buffers,
 * SIMD ARM64 optimization, DVB-S receiver pipeline blocks, and FEC LDPC/BCH decoders.
 */
@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "GALAXYDSP ARCHITECTURE OVERVIEW",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ZERO-ALLOCATION SIMD ENGINE",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "All complex baseband operations execute on interleaved FloatArray buffers [I0, Q0, I1, Q1, ...] " +
                            "to maximize L1/L2 cache locality and enable ARM64 NEON SIMD vectorization without garbage collection pauses.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DVB-S RECEIVER PROCESSING PIPELINE",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "1. Automatic Gain Control (AGC) - Normalizes input RMS power.\n" +
                            "2. Root Raised Cosine (RRC) Filter - Matched pulse shaping filter (roll-off = 0.35).\n" +
                            "3. Gardner Timing Recovery - Symbol timing synchronization loop.\n" +
                            "4. Costas Carrier Recovery - Carrier frequency & phase offset correction.\n" +
                            "5. QPSK Soft Demodulator - Log-likelihood ratio (LLR) calculation.\n" +
                            "6. LDPC / BCH Forward Error Correction - Iterative min-sum bit correction.\n" +
                            "7. MPEG-2 TS Packet Assembly - Synchronizes 188-byte transport stream packets.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
