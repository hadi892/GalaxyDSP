package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.NavigationTab
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.BenchmarkScreen
import com.example.ui.screens.ConstellationScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.SpectrumScreen
import com.example.ui.screens.WaterfallScreen
import com.example.ui.theme.GalaxyDSPTheme
import com.example.viewmodel.DSPViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DSPViewModel by viewModels { DSPViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val selectedTabIndex by viewModel.selectedTab.collectAsState()
            val signalMetrics by viewModel.signalMetrics.collectAsState()
            val benchmarkReport by viewModel.benchmarkReport.collectAsState()
            val isBenchmarking by viewModel.isBenchmarking.collectAsState()

            GalaxyDSPTheme(darkTheme = isDarkTheme) {
                GalaxyDSPApp(
                    selectedTabIndex = selectedTabIndex,
                    onSelectTab = { viewModel.selectTab(it) },
                    signalMetrics = signalMetrics,
                    onSnrChanged = { viewModel.setChannelSnr(it) },
                    onFrequencyChanged = { viewModel.setCenterFrequency(it) },
                    onTogglePipeline = { viewModel.togglePipeline(it) },
                    benchmarkReport = benchmarkReport,
                    isBenchmarking = isBenchmarking,
                    onRunBenchmark = { viewModel.runBenchmark() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyDSPApp(
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    signalMetrics: com.example.domain.SignalMetrics,
    onSnrChanged: (Float) -> Unit,
    onFrequencyChanged: (Float) -> Unit,
    onTogglePipeline: (Boolean) -> Unit,
    benchmarkReport: com.example.benchmark.BenchmarkEngine.Report?,
    isBenchmarking: Boolean,
    onRunBenchmark: () -> Unit
) {
    val currentTab = NavigationTab.entries.getOrElse(selectedTabIndex) { NavigationTab.DASHBOARD }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GALAXYDSP | DVB-S & RF SIMD FRAMEWORK",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                for ((index, tab) in NavigationTab.entries.withIndex()) {
                    val isSelected = index == selectedTabIndex
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onSelectTab(index) },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = {}
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> MainDashboardScreen(
                    metrics = signalMetrics,
                    onSnrChanged = onSnrChanged,
                    onFrequencyChanged = onFrequencyChanged,
                    onTogglePipeline = onTogglePipeline
                )
                NavigationTab.SPECTRUM -> SpectrumScreen(
                    metrics = signalMetrics,
                    onSnrChanged = onSnrChanged,
                    onFrequencyChanged = onFrequencyChanged,
                    onTogglePipeline = onTogglePipeline
                )
                NavigationTab.WATERFALL -> WaterfallScreen(
                    metrics = signalMetrics
                )
                NavigationTab.CONSTELLATION -> ConstellationScreen(
                    metrics = signalMetrics,
                    onSnrChanged = onSnrChanged,
                    onFrequencyChanged = onFrequencyChanged,
                    onTogglePipeline = onTogglePipeline
                )
                NavigationTab.BENCHMARK -> BenchmarkScreen(
                    report = benchmarkReport,
                    isBenchmarking = isBenchmarking,
                    onRunBenchmark = onRunBenchmark
                )
                NavigationTab.ABOUT -> AboutScreen()
            }
        }
    }
}
