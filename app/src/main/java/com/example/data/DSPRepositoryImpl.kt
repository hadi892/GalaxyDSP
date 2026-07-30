package com.example.data

import com.example.benchmark.BenchmarkEngine
import com.example.common.DSPLogger
import com.example.core.ComplexVector
import com.example.domain.SignalMetrics
import com.example.hardware.HardwareAbstractionLayer
import com.example.iq.IQConverter
import com.example.pipeline.SignalPipeline
import com.example.repository.DSPRepository
import com.example.visualizer.SpectrumAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Concrete implementation of [DSPRepository] utilizing clean architecture,
 * Coroutines, and atomic StateFlow updates.
 */
class DSPRepositoryImpl(
    private val scope: CoroutineScope
) : DSPRepository {

    private val hal = HardwareAbstractionLayer(sampleRateHz = 1_000_000f, centerFrequencyHz = 1_200_000_000f)
    private val pipeline = SignalPipeline(sampleRateHz = 1_000_000f, symbolRateHz = 500_000f)
    private val spectrumAnalyzer = SpectrumAnalyzer(fftSize = 1024, sampleRateHz = 1_000_000f)
    private val benchmarkEngine = BenchmarkEngine()

    private val _signalMetrics = MutableStateFlow(SignalMetrics())
    override val signalMetrics: StateFlow<SignalMetrics> = _signalMetrics.asStateFlow()

    private var processingJob: Job? = null
    private val workVector = ComplexVector(1024)
    private val spectrumDb = FloatArray(1024) { -95f }
    private val constellationHistory = FloatArray(512)

    init {
        // Automatically start the pipeline on initialization so user sees live data immediately
        startPipeline()
    }

    override fun startPipeline() {
        if (processingJob?.isActive == true) return
        hal.startCapture()
        DSPLogger.i("DSPRepository", "Starting DSP Processing Pipeline")

        processingJob = scope.launch(Dispatchers.Default) {
            while (isActive && hal.isActive) {
                try {
                    // Read 1024 complex samples from Hardware Abstraction Layer
                    val count = hal.readSamples(workVector, 1024)
                    if (count > 0) {
                        // 1. Process through DVB-S receiver pipeline
                        val symbolsProcessed = pipeline.processBlock(workVector)

                        // 2. Perform FFT log spectrum analysis
                        spectrumAnalyzer.analyze(workVector, spectrumDb, alpha = 0.25f)

                        // 3. Collect recent QPSK constellation symbols for scatter plot
                        val numConstPoints = minOf(256, count)
                        for (i in 0 until numConstPoints) {
                            val idx = i * 2
                            constellationHistory[idx] = workVector.getRe(i)
                            constellationHistory[idx + 1] = workVector.getIm(i)
                        }

                        // 4. Update real-time telemetry metrics
                        val currentEvm = pipeline.evmRmsPercent
                        val isLocked = currentEvm < 15.0f
                        val prob = if (isLocked) 0.99f else 0.45f
                        val score = if (isLocked) 0.95f else 0.30f
                        val phaseDeg = (pipeline.phaseErrorRads * 180f / Math.PI).toFloat()

                        _signalMetrics.value = SignalMetrics(
                            centerFrequencyHz = hal.centerFrequencyHz,
                            sampleRateHz = hal.sampleRateHz,
                            snrDb = (24.0f - currentEvm * 0.8f).coerceIn(5f, 35f),
                            evmRmsPercent = currentEvm,
                            phaseErrorDeg = phaseDeg,
                            noiseFloorDb = -88.5f,
                            correlationScore = score,
                            signalProbability = prob,
                            processedSymbols = pipeline.processedSymbols,
                            demodulatedPackets = pipeline.demodulatedPackets,
                            fftSpectrum = spectrumDb.copyOf(),
                            constellationPoints = constellationHistory.copyOf(),
                            isRunning = true,
                            carrierLocked = isLocked
                        )
                    }
                    delay(30L) // ~33 FPS UI visualizer refresh rate
                } catch (e: Exception) {
                    DSPLogger.e("DSPRepository", "Error during DSP pipeline execution", e)
                }
            }
        }
    }

    override fun stopPipeline() {
        hal.stopCapture()
        processingJob?.cancel()
        processingJob = null
        _signalMetrics.value = _signalMetrics.value.copy(isRunning = false, carrierLocked = false)
        DSPLogger.i("DSPRepository", "DSP Processing Pipeline Stopped")
    }

    override fun setChannelSnrDb(snrDb: Float) {
        hal.setChannelSnrDb(snrDb)
        DSPLogger.d("DSPRepository", "Channel SNR set to $snrDb dB")
    }

    override fun setCenterFrequencyHz(frequencyHz: Float) {
        hal.centerFrequencyHz = frequencyHz
        _signalMetrics.value = _signalMetrics.value.copy(centerFrequencyHz = frequencyHz)
    }

    override suspend fun runBenchmarkSuite(): BenchmarkEngine.Report {
        DSPLogger.i("DSPRepository", "Running DSP Benchmark Suite")
        return benchmarkEngine.runBenchmarkSuite(sampleRateHz = 1_000_000f)
    }
}
