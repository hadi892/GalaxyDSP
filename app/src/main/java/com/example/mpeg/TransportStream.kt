package com.example.mpeg

/**
 * Top-level MPEG-2 Transport Stream (DVB-S) data model definitions.
 */
data class TransportStream(
    val syncByte: Byte = 0x47.toByte(),
    val pid: Int,
    val payloadUnitStartIndicator: Boolean,
    val continuityCounter: Int,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransportStream

        if (syncByte != other.syncByte) return false
        if (pid != other.pid) return false
        if (payloadUnitStartIndicator != other.payloadUnitStartIndicator) return false
        if (continuityCounter != other.continuityCounter) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = syncByte.toInt()
        result = 31 * result + pid
        result = 31 * result + payloadUnitStartIndicator.hashCode()
        result = 31 * result + continuityCounter
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

/**
 * Program Association Table (PAT) DVB-S metadata.
 */
data class PAT(
    val transportStreamId: Int,
    val versionNumber: Int,
    val programs: Map<Int, Int> // Program Number -> PMT PID
)

/**
 * Program Map Table (PMT) DVB-S metadata.
 */
data class PMT(
    val programNumber: Int,
    val pcrPid: Int,
    val elementaryStreams: List<ElementaryStreamInfo>
) {
    data class ElementaryStreamInfo(
        val streamType: Int,
        val elementaryPid: Int
    )
}

/**
 * Packetized Elementary Stream (PES) packet structure.
 */
data class PES(
    val streamId: Int,
    val packetLength: Int,
    val presentationTimestamp: Long?,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PES

        if (streamId != other.streamId) return false
        if (packetLength != other.packetLength) return false
        if (presentationTimestamp != other.presentationTimestamp) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = streamId
        result = 31 * result + packetLength
        result = 31 * result + (presentationTimestamp?.hashCode() ?: 0)
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
