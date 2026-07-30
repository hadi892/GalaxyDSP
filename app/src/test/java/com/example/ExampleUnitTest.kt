package com.example

import com.example.agc.AGC
import com.example.core.ComplexVector
import com.example.demod.QPSKDemodulator
import com.example.fec.BCHDecoder
import com.example.fft.FFT
import com.example.mpeg.TransportStreamParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit test suite verifying GalaxyDSP mathematical operations, DVB-S receiver DSP blocks,
 * FFT transform accuracy, error correction decoders, and MPEG-2 TS framing.
 */
class ExampleUnitTest {

    @Test
    fun testComplexVectorOperations() {
        val vector = ComplexVector(4)
        vector.set(0, 1.0f, -1.0f)
        vector.set(1, 0.5f, 0.5f)

        assertEquals(1.0f, vector.getRe(0), 1e-5f)
        assertEquals(-1.0f, vector.getIm(0), 1e-5f)
        assertEquals(2.0f, vector.magnitudeSquared(0), 1e-5f)
        assertEquals(0.5f, vector.magnitudeSquared(1), 1e-5f)

        // Test scaling
        vector.scale(2.0f)
        assertEquals(2.0f, vector.getRe(0), 1e-5f)
        assertEquals(-2.0f, vector.getIm(0), 1e-5f)
    }

    @Test
    fun testAutomaticGainControl() {
        val agc = AGC(targetLevelRms = 0.707f)
        val vector = ComplexVector(64)
        // Set small amplitude signals
        for (i in 0 until 64) {
            vector.set(i, 0.1f, 0.1f)
        }

        agc.processInPlace(vector)
        val afterRms = Math.sqrt(vector.magnitudeSquared(0).toDouble()).toFloat()
        assertTrue("AGC should boost low amplitude towards target RMS", afterRms > 0.15f)
    }

    @Test
    fun testFFTImpulseResponse() {
        val fftSize = 16
        val fft = FFT(fftSize)
        val vector = ComplexVector(fftSize)
        // Unit impulse at index 0
        vector.set(0, 1.0f, 0.0f)

        fft.transformInPlace(vector)

        // FFT of unit impulse should be flat magnitude across all bins
        for (i in 0 until fftSize) {
            val mag = Math.sqrt(vector.magnitudeSquared(i).toDouble())
            assertEquals("Bin $i magnitude should be 1.0", 1.0, mag, 1e-3)
        }
    }

    @Test
    fun testBCHDecoderErrorCorrection() {
        val bch = BCHDecoder(errorCorrectionCapability = 2)
        val blockBits = IntArray(64) { 0 }
        // Inject single bit error
        blockBits[10] = 1

        val result = bch.decodeInPlace(blockBits)
        assertTrue("BCH decode should succeed", result.success)
        assertEquals("BCH should report 1 corrected bit", 1, result.correctedBitsCount)
        assertEquals("Bit 10 should be corrected back to 0", 0, blockBits[10])
    }

    @Test
    fun testMpegTsPacketHeaderParsing() {
        val parser = TransportStreamParser()
        val packet = ByteArray(188)
        packet[0] = 0x47.toByte() // Sync byte
        packet[1] = 0x40.toByte() // PUSI = true, PID high bit = 0
        packet[2] = 0x00.toByte() // PID low byte = 0x00 (PAT)
        packet[3] = 0x15.toByte() // Payload only, CC = 5

        val header = parser.parsePacketHeader(packet)
        assertNotNull(header)
        assertEquals(0x0000, header?.pid)
        assertEquals(true, header?.payloadUnitStartIndicator)
        assertEquals(5, header?.continuityCounter)
        assertEquals(4, header?.payloadOffset)
    }

    @Test
    fun testQpskDemodulatorSoftSlicing() {
        val demod = QPSKDemodulator()
        val symbols = ComplexVector(2)
        symbols.set(0, 0.707f, 0.707f)   // First quadrant -> bits (0, 0)
        symbols.set(1, -0.707f, -0.707f) // Third quadrant -> bits (1, 1)

        val llrBuffer = FloatArray(4)
        val count = demod.demodulateSoft(symbols, llrBuffer, noiseVariance = 0.1f)
        assertEquals(4, count)
        assertTrue("QPSK symbol 0 I should be positive LLR", llrBuffer[0] > 0f)
        assertTrue("QPSK symbol 0 Q should be positive LLR", llrBuffer[1] > 0f)
        assertTrue("QPSK symbol 1 I should be negative LLR", llrBuffer[2] < 0f)
        assertTrue("QPSK symbol 1 Q should be negative LLR", llrBuffer[3] < 0f)
    }
}
