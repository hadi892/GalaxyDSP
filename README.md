# GalaxyDSP - Modular Android Digital Signal Processing Framework

**GalaxyDSP** is an elite, research-grade Android DSP framework designed for processing **REAL digital signal streams** on ARM64 devices (optimized for Snapdragon 695 and modern Hexagon/Snapdragon architectures). It implements a modular DVB-S/DVB-S2 satellite receiver processing pipeline with real-time interactive **Spectrum**, **Waterfall**, **Constellation (QPSK/8PSK/16QAM)**, and **Benchmark** visualizers.

---

## 🚫 Strict Production Hardware Discipline

In compliance with the **Real Signal Architecture** directive:
- **No Synthetic or Simulated Signals in Production:** All synthetic generators (`IQGenerator`, AWGN, swept chirp, CW tone generators) reside strictly within the testing module (`/testing`).
- **Hardware Abstraction Interface Only:** Every DSP module receives samples exclusively through `SignalSourceInterface` and `HardwareAbstractionLayer`.
- **Fail-Safe Real Hardware State:** If no operational signal source (`IQFileSource` or a connected SDR) is present, the UI explicitly displays **`"No Signal Source"`** / **`"No compatible signal source available"`**. The application **never** renders fake FFT spectrums or fake constellation symbols.

---

## 🏛️ System Architecture Diagram

```mermaid
graph TD
    subgraph Hardware Layer
        A1[IQFileSource - Operational I/Q File]
        A2[FutureFastRPCSource - Hexagon cDSP/aDSP]
        A3[FutureDSPSource - Snapdragon Integrated Baseband]
        A4[FutureUSBSDRSource - RTL-SDR / HackRF USB]
        A5[FutureAudioSource - Stereo IF Line-In]
    end

    subgraph HAL [Hardware Abstraction Layer]
        HAL1[HardwareAbstractionLayer]
    end

    subgraph Core Pipeline
        IM[InputManager - Circular SIMD Buffer]
        SP[SignalPipeline - DSP Processing Thread]
        OM[OutputManager - Transport Stream & Metrics Queue]
    end

    subgraph UI / Presentation [MVVM Clean Architecture]
        REPO[DSPRepository / DSPRepositoryImpl]
        VM[DSPViewModel - StateFlow / Lifecycle Aware]
        UI[Material 3 Compose Dark UI - Spectrum / Waterfall / QPSK]
    end

    A1 -->|ComplexVector Samples| HAL1
    A2 -.->|Not Implemented Exception| HAL1
    A3 -.->|Not Implemented Exception| HAL1
    A4 -.->|Not Implemented Exception| HAL1
    A5 -.->|Not Implemented Exception| HAL1

    HAL1 -->|Stream I/Q| IM
    IM -->|SIMD Window| SP
    SP -->|DVB-S Symbols / LLR / MPEG-2 TS| OM
    OM -->|Flow<SignalMetrics>| REPO
    REPO -->|StateFlow<SignalMetrics>| VM
    VM -->|UI State| UI
```

---

## 📡 Real Signal Pipeline Diagram

```mermaid
sequenceDiagram
    participant Source as SignalSource (HAL)
    participant Input as InputManager
    participant Pipeline as SignalPipeline
    participant DSP as DSP Engine (AGC/FFT/Demod/FEC)
    participant Output as OutputManager
    participant UI as M3 Compose Dashboard

    loop Every Acquisition Frame
        Source->>Input: readSamples(ComplexVector, count)
        alt Source Active & Samples Available
            Input->>Pipeline: pull SIMD Window (1024 Complex Samples)
            Pipeline->>DSP: AGC.processInPlace() -> FFT -> Filter -> Carrier/Timing Recovery
            DSP->>Pipeline: QPSK Demodulated Symbols & Soft Decision LLR
            Pipeline->>DSP: Viterbi / BCH FEC -> MPEG-2 TS Parser (PAT/PMT/PES)
            Pipeline->>Output: writeTransportPacket() + publish SignalMetrics
            Output->>UI: StateFlow Update -> Render Spectrum / Waterfall / QPSK
        else No Signal Source
            Source-->>Input: 0 Samples Read
            Input-->>Pipeline: Empty Frame
            Pipeline-->>Output: metrics.carrierLocked = false, isRunning = false
            Output-->>UI: Display "No Signal Source" Warning
        end
    end
```

---

## 🎛️ DSP Core Feature Matrix

All DSP algorithms are implemented from scratch in pure Kotlin without external C++ or JNI dependency, utilizing contiguous FloatArray SIMD memory layouts:

| Category | Completed Implementations |
|---|---|
| **Complex Math & Buffers** | `Complex`, `ComplexVector` (interleaved float array), `ComplexMatrix`, `CircularBuffer` (high throughput SIMD-friendly) |
| **Spectral Analysis** | Radix-2 / Radix-4 `FFT`, `IFFT`, `PowerSpectrum` (dBm/dBFS calculation), `WindowFunctions` (Blackman-Harris, Hann, Hamming, Flat-top) |
| **Digital Filters** | `LowPassFilter`, `HighPassFilter`, `BandPassFilter`, `NotchFilter`, `MatchedFilter`, `RootRaisedCosineFilter` |
| **Correlation & Detection** | `Correlation`, `AutoCorrelation`, `CrossCorrelation`, `EnergyDetector`, `NoiseEstimator`, `NoiseFloor`, `PeakDetector` |
| **Synchronization** | `CarrierRecovery` (Costas Loop & PLL), `TimingRecovery` (Gardner & Mueller-Müller TED), `ClockRecovery`, `NCO` |
| **Adaptive & Gain Control** | `AGC` (RMS & Peak feedforward/feedback), `AdaptiveLMS`, `AdaptiveRLS` |
| **Demodulation & FEC** | `Constellation` (QPSK, 8PSK, 16QAM EVM measurement), `QPSKDemodulator`, `SoftDecision` (LLR slicing), `BCHDecoder`, `LDPCDecoder` |
| **MPEG-2 Transport Stream** | `TransportStreamParser`, `PATParser`, `PMTParser`, `PESParser`, `PAT`, `PMT`, `PES` data structures |
| **Visualizers & Benchmarks** | `SpectrumAnalyzer` (real-time FFT canvas), `WaterfallRenderer` (rolling spectrograph), `BenchmarkEngine` (nanosecond MSPS profiling) |

---

## 🛠️ Build & CI/CD Verification

### Android Studio / Gradle 9.3.1 (JDK 21)
- **Compile SDK:** 36
- **Target SDK:** 36
- **Min SDK:** 35
- **Architecture:** ARM64 (`arm64-v8a`)

```bash
# Run unit tests (DSP math, FFT, DVB-S demodulation, MPEG-2 TS framing)
gradle :app:testDebugUnitTest

# Assemble Release & Debug APKs
gradle :app:assembleDebug
gradle :app:assembleRelease
```

### GitHub Actions Integration
An automated workflow is configured at `.github/workflows/android.yml`:
- Runs Android Lint & JVM Unit Tests on every push and PR.
- Assembles both `GalaxyDSP-debug.apk` and `GalaxyDSP-release.apk`.
- Uploads APK artifacts for immediate download from the GitHub Actions dashboard.
