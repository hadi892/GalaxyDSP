package com.example.mpeg

/**
 * MPEG-2 Packetized Elementary Stream (PES) Parser.
 * Extracts presentation timestamps (PTS), decoding timestamps (DTS), and raw video/audio frames.
 */
class PESParser {

    /**
     * Parsed PES Header and Payload data.
     *
     * @property streamId PES Stream Identifier (0xE0..0xEF for Video, 0xC0..0xDF for Audio).
     * @property ptsMs Presentation timestamp in milliseconds (or -1 if absent).
     * @property payloadData Elementary stream byte payload.
     */
    data class PesPacket(
        val streamId: Int,
        val ptsMs: Long,
        val payloadData: ByteArray
    )

    /**
     * Parses a PES packet from reassembled TS payloads.
     */
    fun parsePesPacket(data: ByteArray): PesPacket? {
        if (data.size < 9) return null
        // Verify start code prefix 0x00 0x00 0x01
        if (data[0] != 0x00.toByte() || data[1] != 0x00.toByte() || data[2] != 0x01.toByte()) {
            return null
        }
        val streamId = data[3].toInt() and 0xFF
        val ptsFlag = (data[7].toInt() and 0x80) != 0
        val headerDataLen = data[8].toInt() and 0xFF

        var ptsMs = -1L
        if (ptsFlag && data.size >= 14) {
            val pts32_30 = ((data[9].toInt() and 0x0E) shl 29).toLong()
            val pts29_15 = ((data[10].toInt() and 0xFF) shl 22).toLong() or ((data[11].toInt() and 0xFE) shl 14).toLong()
            val pts14_0 = ((data[12].toInt() and 0xFF) shl 7).toLong() or ((data[13].toInt() and 0xFE) shr 1).toLong()
            val pts90Khz = pts32_30 or pts29_15 or pts14_0
            ptsMs = pts90Khz / 90L
        }

        val payloadStart = 9 + headerDataLen
        val payload = if (payloadStart < data.size) {
            data.copyOfRange(payloadStart, data.size)
        } else {
            ByteArray(0)
        }

        return PesPacket(streamId, ptsMs, payload)
    }
}
