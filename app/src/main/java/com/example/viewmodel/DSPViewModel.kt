package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.benchmark.BenchmarkEngine
import com.example.di.AppContainer
import com.example.domain.SignalMetrics
import com.example.repository.DSPRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Unified ViewModel for GalaxyDSP application.
 * Manages UI state across Spectrum, Waterfall, Constellation, Statistics, Noise Floor,
 * Correlation Score, Signal Probability, FFT Viewer, Benchmark, and Settings screens.
 */
class DSPViewModel(
    private val repository: DSPRepository = AppContainer.dspRepository
) : ViewModel() {

    val signalMetrics: StateFlow<SignalMetrics> = repository.signalMetrics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SignalMetrics()
        )

    private val _benchmarkReport = MutableStateFlow<BenchmarkEngine.Report?>(null)
    val benchmarkReport: StateFlow<BenchmarkEngine.Report?> = _benchmarkReport.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    fun togglePipeline(run: Boolean) {
        if (run) {
            repository.startPipeline()
        } else {
            repository.stopPipeline()
        }
    }

    fun setChannelSnr(snrDb: Float) {
        repository.setChannelSnrDb(snrDb)
    }

    fun setCenterFrequency(freqHz: Float) {
        repository.setCenterFrequencyHz(freqHz)
    }

    fun runBenchmark() {
        if (_isBenchmarking.value) return
        _isBenchmarking.value = true
        viewModelScope.launch {
            try {
                val report = repository.runBenchmarkSuite()
                _benchmarkReport.value = report
            } finally {
                _isBenchmarking.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DSPViewModel(AppContainer.dspRepository) as T
            }
        }
    }
}
