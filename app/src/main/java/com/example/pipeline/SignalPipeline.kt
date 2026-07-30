package com.example.pipeline

import com.example.agc.AGC
import com.example.carrier.CarrierRecovery
import com.example.core.ComplexVector
import com.example.demod.ConstellationProcessor
import com.example.demod.QPSKDemodulator
import com.example.demod.SoftDecision
import com.example.fec.BCHDecoder
import com.example.fec.LDPCDecoder
import com.example.filters.RootRaisedCosineFilter
import com.example.mpeg.TransportStreamParser
import com.example.timing.TimingRecovery
import java.util.concurrent.atomic.AtomicLong

/**
 * Complete DVB-S Receiver DSP Pipeline.
 * Chains: AGC -> Matched RRC Filter -> Timing Recovery -> Carrier Recovery ->
 * QPSK Demod -> LDPC/BCH FEC -> MPEG-2 TS Packet Assembly.
 *
 * @property sampleRateHz System sample rate in Hertz.
 * @property symbolRateHz DVB-S QPSK symbol rate in Hertz.
 */
class SignalPipeline(
    val sampleRateHz: Float = 1_000_000f,
    val symbolRateHz: Float = 500_000f
) {
    private val agc = AGC(targetLevelRms = 0.707f)
    private val rrcFilter = RootRaisedCosineFilter(
        numTaps = 33,
        rollOff = 0.35f,
        samplesPerSymbol = maxOf(2, (sampleRateHz / symbolRateHz).toInt())
    )
    private val timingRecovery = TimingRecovery(
        samplesPerSymbol = sampleRateHz / symbolRateHz,
        loopBandwidth = 0.01f
    )
    private val carrierRecovery = CarrierRecovery(
        dampingFactor = 0.707f,
        normalizedBandwidth = 0.01f
    )
    private val demodulator = QPSKDemodulator()
    private val constellationProcessor = ConstellationProcessor()
    private val ldpcDecoder = LDPCDecoder(codewordLength = 188 * 8)
    private val bchDecoder = BCHDecoder()
    private val tsParser = TransportStreamParser()

    private val symbolBuffer = ComplexVector(2048)
    private val llrBuffer = FloatArray(4096)
    private val hardBits = IntArray(4096)
    private val tsPayloadBytes = ByteArray(TransportStreamParser.TS_PACKET_SIZE)

    // Statistics telemetry
    private val _processedSymbols = AtomicLong(0L)
    private val _demodulatedPackets = AtomicLong(0L)
    private var currentEvmRms: Float = 0f
    private var currentPhaseErrorRads: Float = 0f

    val processedSymbols: Long
        get() = _processedSymbols.get()

    val demodulatedPackets: Long
        get() = _demodulatedPackets.get()

    val evmRmsPercent: Float
        @Synchronized get() = currentEvmRms

    val phaseErrorRads: Float
        @Synchronized get() = currentPhaseErrorRads

    /**
     * Executes one processing block from input source through DVB-S receiver chain.
     *
     * @param input Raw complex I/Q baseband samples.
     * @param tsSink Optional destination sink for extracted MPEG-2 TS packets.
     * @return Number of QPSK symbols successfully synchronized and demodulated.
     */
    @Synchronized
    fun processBlock(input: ComplexVector, tsSink: SignalSinkInterface? = null): Int {
        if (input.size == 0) return 0

        // 1. Automatic Gain Control (AGC)
        agc.processInPlace(input)

        // 2. Matched Root Raised Cosine (RRC) Pulse Filtering
        rrcFilter.filterInPlace(input)

        // 3. Symbol Timing Recovery (Gardner Loop)
        val symbolCount = timingRecovery.processBlock(input, symbolBuffer)
        if (symbolCount == 0) return 0

        // 4. Carrier Frequency and Phase Recovery (Costas Loop)
        val subVector = ComplexVector(symbolCount, symbolBuffer.data.copyOfRange(0, symbolCount * 2))
        currentPhaseErrorRads = carrierRecovery.processBlockInPlace(subVector)

        // 5. Constellation EVM & Telemetry
        currentEvmRms = constellationProcessor.computeEvmRmsPercent(subVector)
        _processedSymbols.addAndGet(symbolCount.toLong())

        // 6. QPSK Demodulation to soft LLRs
        val bitCount = demodulator.demodulateSoft(subVector, llrBuffer, noiseVariance = 0.1f)
        SoftDecision.sliceHardDecisions(llrBuffer, hardBits)

        // 7. Forward Error Correction & TS Framing
        val tsPacketBits = 188 * 8
        if (bitCount >= tsPacketBits) {
            ldpcDecoder.decode(llrBuffer, hardBits)
            bchDecoder.decodeInPlace(hardBits)

            // Pack 188 bytes
            tsPayloadBytes[0] = TransportStreamParser.SYNC_BYTE
            for (byteIdx in 1 until 188) {
                var byteVal = 0
                for (bitIdx in 0 until 8) {
                    byteVal = (byteVal shl 1) or (hardBits[byteIdx * 8 + bitIdx] and 1)
                }
                tsPayloadBytes[byteIdx] = byteVal.toByte()
            }

            // Verify MPEG-2 TS Sync Header
            val header = tsParser.parsePacketHeader(tsPayloadBytes)
            if (header != null) {
                _demodulatedPackets.incrementAndGet()
                tsSink?.writeTransportPacket(tsPayloadBytes)
            }
        }

        return symbolCount
    }

    /**
     * Resets entire pipeline state.
     */
    @Synchronized
    fun reset() {
        agc.reset()
        rrcFilter.reset()
        timingRecovery.reset()
        carrierRecovery.reset()
        _processedSymbols.set(0L)
        _demodulatedPackets.set(0L)
        currentEvmRms = 0f
        currentPhaseErrorRads = 0f
    }
}
