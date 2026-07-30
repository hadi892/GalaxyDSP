package com.example.common

/**
 * Custom exception hierarchy for structured exception handling in GalaxyDSP framework.
 */
sealed class DSPException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class BufferOverflowException(message: String) : DSPException(message)
    class InvalidParameterException(message: String) : DSPException(message)
    class PipelineExecutionException(message: String, cause: Throwable? = null) : DSPException(message, cause)
    class HardwareAbstractionException(message: String, cause: Throwable? = null) : DSPException(message, cause)
}
