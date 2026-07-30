package com.example.demod

import com.example.core.ComplexVector
import kotlin.math.sqrt

/**
 * Constellation Mapper and De-mapper for QPSK, 8-PSK, and 16-QAM digital modulations.
 */
class Constellation {

    enum class ModulationType(val bitsPerSymbol: Int) {
        QPSK(2),
        PSK_8(3),
        QAM_16(4)
    }

    /**
     * Maps ideal constellation symbol points for QPSK normalization (RMS = 1.0).
     */
    fun getIdealQpskPoints(): ComplexVector {
        val pts = ComplexVector(4)
        val valNorm = 0.70710678f // 1.0 / sqrt(2)
        pts.set(0, valNorm, valNorm)
        pts.set(1, -valNorm, valNorm)
        pts.set(2, -valNorm, -valNorm)
        pts.set(3, valNorm, -valNorm)
        return pts
    }

    /**
     * Computes Error Vector Magnitude (EVM) RMS percentage against ideal QPSK constellation points.
     */
    fun computeEvmRmsPercent(receivedSymbols: ComplexVector): Float {
        if (receivedSymbols.size == 0) return 0f
        val ideal = getIdealQpskPoints()
        var errorPowerSum = 0.0f
        var refPowerSum = 0.0f

        for (i in 0 until receivedSymbols.size) {
            val rxRe = receivedSymbols.getRe(i)
            val rxIm = receivedSymbols.getIm(i)
            // Find closest ideal point
            var minDistSq = Float.MAX_VALUE
            var bestIdealRe = 0f
            var bestIdealIm = 0f
            for (j in 0 until 4) {
                val idRe = ideal.getRe(j)
                val idIm = ideal.getIm(j)
                val dRe = rxRe - idRe
                val dIm = rxIm - idIm
                val distSq = dRe * dRe + dIm * dIm
                if (distSq < minDistSq) {
                    minDistSq = distSq
                    bestIdealRe = idRe
                    bestIdealIm = idIm
                }
            }
            errorPowerSum += minDistSq
            refPowerSum += (bestIdealRe * bestIdealRe + bestIdealIm * bestIdealIm)
        }

        if (refPowerSum <= 0f) return 0f
        val evmRms = sqrt((errorPowerSum / refPowerSum).toDouble()).toFloat()
        return evmRms * 100.0f
    }
}
