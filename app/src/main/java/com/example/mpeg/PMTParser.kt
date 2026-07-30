package com.example.mpeg

/**
 * MPEG-2 Program Map Table (PMT) Parser.
 * Maps Program Elementary Stream PIDs (Video, Audio, Data) to stream types.
 */
class PMTParser {

    /**
     * Elementary Stream PID and Type information.
     *
     * @property streamType MPEG Stream Type (e.g., 0x02 = MPEG-2 Video, 0x1B = H.264 Video, 0x03 = MP3 Audio).
     * @property elementaryPid PID of the elementary stream.
     */
    data class ElementaryStreamInfo(
        val streamType: Int,
        val elementaryPid: Int
    )

    /**
     * Parses a PMT section payload.
     */
    fun parsePmtSection(payload: ByteArray, offset: Int): List<ElementaryStreamInfo> {
        if (offset >= payload.size - 12) return emptyList()

        val tableId = payload[offset].toInt() and 0xFF
        if (tableId != 0x02) return emptyList()

        val sectionLength = ((payload[offset + 1].toInt() and 0x0F) shl 8) or (payload[offset + 2].toInt() and 0xFF)
        val programInfoLength = ((payload[offset + 10].toInt() and 0x0F) shl 8) or (payload[offset + 11].toInt() and 0xFF)

        var cursor = offset + 12 + programInfoLength
        val limit = minOf(payload.size - 4, offset + 3 + sectionLength - 4) // Skip CRC32

        val streams = mutableListOf<ElementaryStreamInfo>()
        while (cursor + 4 <= limit) {
            val streamType = payload[cursor].toInt() and 0xFF
            val elementaryPid = ((payload[cursor + 1].toInt() and 0x1F) shl 8) or (payload[cursor + 2].toInt() and 0xFF)
            val esInfoLength = ((payload[cursor + 3].toInt() and 0x0F) shl 8) or (payload[cursor + 4].toInt() and 0xFF)
            streams.add(ElementaryStreamInfo(streamType, elementaryPid))
            cursor += 5 + esInfoLength
        }
        return streams
    }
}
