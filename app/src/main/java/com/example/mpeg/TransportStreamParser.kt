package com.example.mpeg

/**
 * MPEG-2 Transport Stream (TS) Packet Parser for DVB-S transport streams.
 * Handles 188-byte packet synchronization (0x47 sync byte), continuity counters, and PID demultiplexing.
 */
class TransportStreamParser {

    /**
     * Parsed MPEG-2 TS Packet Header.
     *
     * @property pid Packet Identifier (13 bits, e.g. 0x0000 for PAT).
     * @property payloadUnitStartIndicator True if packet begins a new PES or PSI section.
     * @property continuityCounter Continuity counter [0..15].
     * @property payloadOffset Offset in packet where payload starts.
     */
    data class TsHeader(
        val pid: Int,
        val payloadUnitStartIndicator: Boolean,
        val continuityCounter: Int,
        val payloadOffset: Int
    )

    /**
     * Synchronizes and parses a 188-byte MPEG-2 TS packet header.
     *
     * @param packet 188-byte byte array.
     * @return TsHeader if sync byte 0x47 is valid, null otherwise.
     */
    fun parsePacketHeader(packet: ByteArray): TsHeader? {
        if (packet.size < 188 || packet[0] != 0x47.toByte()) {
            return null
        }
        val b1 = packet[1].toInt() and 0xFF
        val b2 = packet[2].toInt() and 0xFF
        val b3 = packet[3].toInt() and 0xFF

        val pusi = (b1 and 0x40) != 0
        val pid = ((b1 and 0x1F) shl 8) or b2
        val adaptationFieldControl = (b3 shr 4) and 0x03
        val continuityCounter = b3 and 0x0F

        var offset = 4
        if (adaptationFieldControl == 2 || adaptationFieldControl == 3) {
            val adaptationLength = packet[4].toInt() and 0xFF
            offset = 5 + adaptationLength
        }

        if (offset > 188) return null
        return TsHeader(pid, pusi, continuityCounter, offset)
    }

    companion object {
        const val TS_PACKET_SIZE = 188
        const val SYNC_BYTE: Byte = 0x47
        const val PID_PAT = 0x0000
        const val PID_NULL = 0x1FFF
    }
}
