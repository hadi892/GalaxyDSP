package com.example.utils

import java.util.Locale

/**
 * Helper utilities for formatting RF frequency, dBFS levels, EVM percentage, and bitrates.
 */
object FormatUtils {

    /**
     * Formats frequency in Hertz to clean MHz or GHz representation.
     */
    fun formatFrequency(freqHz: Float): String {
        return when {
            freqHz >= 1_000_000_000f -> String.format(Locale.US, "%.3f GHz", freqHz / 1_000_000_000f)
            freqHz >= 1_000_000f -> String.format(Locale.US, "%.3f MHz", freqHz / 1_000_000f)
            freqHz >= 1_000f -> String.format(Locale.US, "%.2f kHz", freqHz / 1_000f)
            else -> String.format(Locale.US, "%.0f Hz", freqHz)
        }
    }

    /**
     * Formats power or SNR in dB.
     */
    fun formatDb(db: Float): String = String.format(Locale.US, "%.1f dB", db)

    /**
     * Formats EVM RMS percentage.
     */
    fun formatEvm(evm: Float): String = String.format(Locale.US, "%.2f%%", evm)

    /**
     * Formats sample rate or symbol rate.
     */
    fun formatRate(rateHz: Float): String {
        return when {
            rateHz >= 1_000_000f -> String.format(Locale.US, "%.2f MSym/s", rateHz / 1_000_000f)
            rateHz >= 1_000f -> String.format(Locale.US, "%.1f kSym/s", rateHz / 1_000f)
            else -> String.format(Locale.US, "%.0f Sym/s", rateHz)
        }
    }

    /**
     * Formats large packet or symbol counts.
     */
    fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000L -> String.format(Locale.US, "%.2fM", count / 1_000_000.0)
            count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
