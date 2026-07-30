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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.benchmark.BenchmarkEngine
import com.example.loader.DSPLoader
import com.example.ui.components.TelemetryCard
import com.example.ui.theme.DSPLockedGreen

/**
 * Benchmark Suite Screen displaying ARM64 SIMD capabilities, zero-allocation heap status,
 * and throughput speeds in Mega-samples/sec for 1024-point FFT, RRC Filter, and QPSK Demodulator.
 */
@Composable
fun BenchmarkScreen(
    report: BenchmarkEngine.Report?,
    isBenchmarking: Boolean,
    onRunBenchmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val archInfo = DSPLoader.verifyArchitecture()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ARM64 SIMD & DSP PIPELINE BENCHMARK",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Architecture info card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryCard(
                title = "Target ABI",
                value = archInfo.cpuAbi.uppercase(),
                statusColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "SIMD Active",
                value = if (archInfo.isSimdEnabled) "YES (NEON)" else "NO",
                statusColor = if (archInfo.isSimdEnabled) DSPLockedGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                title = "Allocation Overhead",
                value = if (report != null) "${report.totalAllocationCount}" else "0",
                unit = "allocs/frame",
                statusColor = DSPLockedGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onRunBenchmark,
            enabled = !isBenchmarking,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_benchmark_button")
        ) {
            if (isBenchmarking) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                    Text(text = "RUNNING 500 ITERATIONS...")
                }
            } else {
                Text(text = "EXECUTE BENCHMARK SUITE")
            }
        }

        if (report != null) {
            Text(
                text = "THROUGHPUT RESULTS (MSPS = Mega-samples / second)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryCard(
                    title = "FFT (1024-pt)",
                    value = String.format("%.2f", report.fft1024ThroughputMsps),
                    unit = "MSPS",
                    statusColor = DSPLockedGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "RRC FIR Filter",
                    value = String.format("%.2f", report.rrcFilterThroughputMsps),
                    unit = "MSPS",
                    statusColor = DSPLockedGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "QPSK Demodulator",
                    value = String.format("%.2f", report.qpskDemodThroughputMsps),
                    unit = "MSPS",
                    statusColor = DSPLockedGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
