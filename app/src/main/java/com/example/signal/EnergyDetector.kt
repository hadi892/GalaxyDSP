package com.example.signal

import com.example.core.ComplexVector

/**
 * Energy Detector for burst and carrier sensing.
 * Compares incoming signal energy against a calibrated noise floor threshold.
 *
 * @param thresholdFactor Multiplier above estimated noise floor required to trigger detection.
 */
class EnergyDetector(var thresholdFactor: Float = 3.0f) {

    /**
     * Detection result containing energy metrics and presence flag.
     *
     * @property isPresent True if signal energy exceeds threshold.
     * @property measuredEnergy Total measured energy in the block.
     * @property noiseThreshold Calculated threshold energy level.
     * @property snrLinear Linear SNR ratio above noise floor.
     */
    data class Result(
        val isPresent: Boolean,
        val measuredEnergy: Float,
        val noiseThreshold: Float,
        val snrLinear: Float
    )

    /**
     * Evaluates whether signal energy in [buffer] exceeds [noiseFloorPower] * [thresholdFactor].
     */
    fun detect(buffer: ComplexVector, noiseFloorPower: Float): Result {
        val measuredEnergy = buffer.computeEnergy() / buffer.size // Average power
        val noiseThreshold = maxOf(1e-9f, noiseFloorPower * thresholdFactor)
        val isPresent = measuredEnergy > noiseThreshold
        val snrLinear = if (noiseFloorPower > 1e-9f) measuredEnergy / noiseFloorPower else 0f
        return Result(isPresent, measuredEnergy, noiseThreshold, snrLinear)
    }

    /**
     * Evaluates detection on a real-valued [SignalBuffer].
     */
    fun detect(buffer: SignalBuffer, noiseFloorPower: Float): Result {
        val measuredEnergy = buffer.computeEnergy() / maxOf(1, buffer.size)
        val noiseThreshold = maxOf(1e-9f, noiseFloorPower * thresholdFactor)
        val isPresent = measuredEnergy > noiseThreshold
        val snrLinear = if (noiseFloorPower > 1e-9f) measuredEnergy / noiseFloorPower else 0f
        return Result(isPresent, measuredEnergy, noiseThreshold, snrLinear)
    }
}
