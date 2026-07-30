package com.example.mpeg

/**
 * MPEG-2 Program Association Table (PAT) Parser (PID 0x0000).
 * Extracts Program Number to Program Map Table (PMT) PID associations.
 */
class PATParser {

    /**
     * Represents a single PAT Program Entry mapping Program Number -> PMT PID.
     */
    data class ProgramAssociation(
        val programNumber: Int,
        val pmtPid: Int
    )

    /**
     * Parses a PAT section from a TS packet payload.
     */
    fun parsePatSection(payload: ByteArray, offset: Int): List<ProgramAssociation> {
        if (offset >= payload.size - 8) return emptyList()

        val tableId = payload[offset].toInt() and 0xFF
        if (tableId != 0x00) return emptyList()

        val sectionLength = ((payload[offset + 1].toInt() and 0x0F) shl 8) or (payload[offset + 2].toInt() and 0xFF)
        val limit = minOf(payload.size - 4, offset + 3 + sectionLength - 4) // Skip CRC32

        val programs = mutableListOf<ProgramAssociation>()
        var cursor = offset + 8 // After header fields
        while (cursor + 3 <= limit) {
            val progNum = ((payload[cursor].toInt() and 0xFF) shl 8) or (payload[cursor + 1].toInt() and 0xFF)
            val pmtPid = ((payload[cursor + 2].toInt() and 0x1F) shl 8) or (payload[cursor + 3].toInt() and 0xFF)
            programs.add(ProgramAssociation(progNum, pmtPid))
            cursor += 4
        }
        return programs
    }
}
