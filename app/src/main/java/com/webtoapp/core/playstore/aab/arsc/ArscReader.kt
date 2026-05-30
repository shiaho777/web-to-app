package com.webtoapp.core.playstore.aab.arsc

import com.webtoapp.core.playstore.aab.axml.AxmlConstants

internal class ArscReader(private val data: ByteArray) {

    private companion object {
        const val RES_NULL_TYPE = 0x0000
        const val RES_STRING_POOL_TYPE = 0x0001
        const val RES_TABLE_TYPE = 0x0002
        const val RES_TABLE_PACKAGE_TYPE = 0x0200
        const val RES_TABLE_TYPE_TYPE = 0x0201
        const val RES_TABLE_TYPE_SPEC_TYPE = 0x0202
        const val RES_TABLE_LIBRARY_TYPE = 0x0203

        const val ENTRY_FLAG_COMPLEX = 0x0001
        const val ENTRY_FLAG_PUBLIC = 0x0002
        const val ENTRY_FLAG_WEAK = 0x0004
        const val ENTRY_FLAG_COMPACT = 0x0008

        const val TYPE_FLAG_SPARSE = 0x01
        const val TYPE_FLAG_OFFSET16 = 0x02

        const val NO_ENTRY = -1

        const val UTF8_FLAG = 0x00000100
    }

    fun read(): ArscResourceTable {
        require(data.size >= 12) { "ARSC too short" }
        require(readU16(0) == RES_TABLE_TYPE) {
            "Not a resource table (magic=0x%04x)".format(readU16(0))
        }
        val fileHeaderSize = readU16(2)
        val fileSize = readU32(4)
        val packageCount = readU32(8)
        require(fileSize == data.size) {
            "ARSC size mismatch: header=$fileSize file=${data.size}"
        }

        var pos = fileHeaderSize
        var valueStringPool: List<String> = emptyList()
        val packages = ArrayList<ArscPackage>(packageCount)

        while (pos + 8 <= data.size) {
            val type = readU16(pos)
            val chunkSize = readU32(pos + 4)
            if (chunkSize <= 0) break

            when (type) {
                RES_STRING_POOL_TYPE -> {
                    valueStringPool = parseStringPool(pos)
                }
                RES_TABLE_PACKAGE_TYPE -> {
                    packages.add(parsePackage(pos))
                }
                RES_NULL_TYPE -> {

                }
                else -> {

                }
            }
            pos += chunkSize
        }

        return ArscResourceTable(
            valueStringPool = valueStringPool,
            packages = packages
        )
    }

    private fun parseStringPool(chunkStart: Int): List<String> {
        val headerSize = readU16(chunkStart + 2)
        val stringCount = readU32(chunkStart + 8)

        val flags = readU32(chunkStart + 16)
        val stringsStart = readU32(chunkStart + 20)

        val isUtf8 = (flags and UTF8_FLAG) != 0

        val offsetsBase = chunkStart + headerSize
        val stringsBase = chunkStart + stringsStart

        val out = ArrayList<String>(stringCount)
        for (i in 0 until stringCount) {
            val off = readU32(offsetsBase + i * 4)
            val absoluteOffset = stringsBase + off
            val s = if (isUtf8) readUtf8String(absoluteOffset) else readUtf16String(absoluteOffset)
            out.add(s)
        }
        return out
    }

    private fun parsePackage(chunkStart: Int): ArscPackage {
        val headerSize = readU16(chunkStart + 2)
        val chunkSize = readU32(chunkStart + 4)
        val packageId = readU32(chunkStart + 8)

        val nameBytes = ByteArray(256)
        System.arraycopy(data, chunkStart + 12, nameBytes, 0, 256)
        val packageName = nameBytes.toUtf16zString()

        val typeStringsOffset = readU32(chunkStart + 268)

        val keyStringsOffset = readU32(chunkStart + 276)

        val typeNames: List<String> = if (typeStringsOffset > 0) {
            parseStringPool(chunkStart + typeStringsOffset)
        } else emptyList()

        val keyNames: List<String> = if (keyStringsOffset > 0) {
            parseStringPool(chunkStart + keyStringsOffset)
        } else emptyList()

        val packageEnd = chunkStart + chunkSize
        var pos = chunkStart + headerSize

        if (typeStringsOffset > 0) {
            val typeStringsEnd = chunkStart + typeStringsOffset +
                readU32(chunkStart + typeStringsOffset + 4)
            if (typeStringsEnd > pos) pos = typeStringsEnd
        }
        if (keyStringsOffset > 0) {
            val keyStringsEnd = chunkStart + keyStringsOffset +
                readU32(chunkStart + keyStringsOffset + 4)
            if (keyStringsEnd > pos) pos = keyStringsEnd
        }

        val typeSpecs = mutableListOf<ArscTypeSpec>()
        val types = mutableListOf<ArscType>()

        while (pos + 8 <= packageEnd) {
            val type = readU16(pos)
            val chunkSizeInner = readU32(pos + 4)
            if (chunkSizeInner <= 0) break

            when (type) {
                RES_TABLE_TYPE_SPEC_TYPE -> {
                    typeSpecs.add(parseTypeSpec(pos))
                }
                RES_TABLE_TYPE_TYPE -> {
                    val parsed = parseType(pos, keyNames)
                    if (parsed != null) types.add(parsed)
                }
                RES_TABLE_LIBRARY_TYPE -> {

                }
                else -> {

                }
            }
            pos += chunkSizeInner
        }

        return ArscPackage(
            id = packageId,
            name = packageName,
            typeNames = typeNames,
            keyNames = keyNames,
            typeSpecs = typeSpecs,
            types = types
        )
    }

    private fun parseTypeSpec(chunkStart: Int): ArscTypeSpec {
        val headerSize = readU16(chunkStart + 2)
        val typeId = data[chunkStart + 8].toInt() and 0xFF

        val entryCount = readU32(chunkStart + 12)

        val flagsBase = chunkStart + headerSize
        val flags = IntArray(entryCount)
        for (i in 0 until entryCount) {
            flags[i] = readU32(flagsBase + i * 4)
        }
        return ArscTypeSpec(typeId = typeId, configFlags = flags)
    }

    private fun parseType(chunkStart: Int, keyNames: List<String>): ArscType? {
        val headerSize = readU16(chunkStart + 2)
        val chunkSize = readU32(chunkStart + 4)
        val typeId = data[chunkStart + 8].toInt() and 0xFF
        val typeFlags = data[chunkStart + 9].toInt() and 0xFF

        val entryCount = readU32(chunkStart + 12)
        val entriesStart = readU32(chunkStart + 16)

        val configStart = chunkStart + 20
        val configSize = readU32(configStart)
        val configRaw = ByteArray(configSize)
        System.arraycopy(data, configStart, configRaw, 0, configSize)
        val config = parseConfig(configRaw)

        val isSparse = (typeFlags and TYPE_FLAG_SPARSE) != 0
        val isOffset16 = (typeFlags and TYPE_FLAG_OFFSET16) != 0

        val offsetsBase = chunkStart + headerSize
        val entries = arrayOfNulls<ArscEntry>(entryCount)
        val entriesAbsoluteBase = chunkStart + entriesStart

        if (isSparse) {

            for (i in 0 until entryCount) {
                val idx = readU16(offsetsBase + i * 4)
                val off = readU16(offsetsBase + i * 4 + 2) * 4
                if (off == 0xFFFF * 4) continue
                entries[idx] = parseEntry(entriesAbsoluteBase + off, idx, keyNames)
            }
        } else if (isOffset16) {

            for (i in 0 until entryCount) {
                val raw = readU16(offsetsBase + i * 2)
                if (raw == 0xFFFF) continue
                entries[i] = parseEntry(entriesAbsoluteBase + raw * 4, i, keyNames)
            }
        } else {

            for (i in 0 until entryCount) {
                val off = readU32(offsetsBase + i * 4)
                if (off == NO_ENTRY) continue
                entries[i] = parseEntry(entriesAbsoluteBase + off, i, keyNames)
            }
        }

        return ArscType(
            typeId = typeId,
            config = config,
            entries = entries.toList()
        )
    }

    private fun parseEntry(start: Int, index: Int, keyNames: List<String>): ArscEntry {

        val sizeOrFlags = readU16(start)
        val flags = readU16(start + 2)

        val isCompact = (flags and ENTRY_FLAG_COMPACT) != 0

        if (isCompact) {

            val keyIdx = readU32(start + 4)
            val keyName = keyNames.getOrElse(keyIdx) { "" }

            val dataType = (sizeOrFlags ushr 8) and 0xFF
            val dataLo = sizeOrFlags and 0xFF
            return ArscEntry(
                index = index,
                name = keyName,
                flags = flags,
                body = ArscEntryBody.Simple(ArscValue(dataType = dataType, data = dataLo))
            )
        }

        val keyIdx = readU32(start + 4)
        val keyName = keyNames.getOrElse(keyIdx) { "" }
        val isComplex = (flags and ENTRY_FLAG_COMPLEX) != 0

        if (isComplex) {

            val parentRef = readU32(start + 8)
            val mapCount = readU32(start + 12)
            val maps = ArrayList<ArscBagEntry>(mapCount)

            val mapsBase = start + 16
            for (i in 0 until mapCount) {
                val mo = mapsBase + i * 12
                val nameRef = readU32(mo)
                val rv = readResValue(mo + 4)
                maps.add(ArscBagEntry(nameRef, rv))
            }
            return ArscEntry(
                index = index,
                name = keyName,
                flags = flags,
                body = ArscEntryBody.Bag(parentRef = parentRef, entries = maps)
            )
        } else {

            val rv = readResValue(start + 8)
            return ArscEntry(
                index = index,
                name = keyName,
                flags = flags,
                body = ArscEntryBody.Simple(rv)
            )
        }
    }

    private fun readResValue(offset: Int): ArscValue {

        val dataType = data[offset + 3].toInt() and 0xFF
        val dataField = readU32(offset + 4)
        return ArscValue(dataType = dataType, data = dataField)
    }

    private fun parseConfig(raw: ByteArray): ArscConfig {

        fun safeReadU32(o: Int): Int = if (o + 4 <= raw.size) {
            (raw[o].toInt() and 0xFF) or
                ((raw[o + 1].toInt() and 0xFF) shl 8) or
                ((raw[o + 2].toInt() and 0xFF) shl 16) or
                ((raw[o + 3].toInt() and 0xFF) shl 24)
        } else 0
        fun safeReadU16(o: Int): Int = if (o + 2 <= raw.size) {
            (raw[o].toInt() and 0xFF) or ((raw[o + 1].toInt() and 0xFF) shl 8)
        } else 0
        fun safeReadU8(o: Int): Int = if (o < raw.size) raw[o].toInt() and 0xFF else 0

        val mcc = safeReadU16(4)
        val mnc = safeReadU16(6)

        val language = unpackLangOrRegion(safeReadU8(8), safeReadU8(9), 'a')
        val region = unpackLangOrRegion(safeReadU8(10), safeReadU8(11), '0')
        val script = readAsciiBlock(raw, 36, 4)
        val variant = readAsciiBlock(raw, 40, 8)
        val locale = buildString {
            if (language.isNotEmpty()) {
                append(language)
                if (region.isNotEmpty()) {
                    append('-')
                    append(region)
                }
                if (script.isNotEmpty()) {
                    append('-')
                    append(script)
                }
                if (variant.isNotEmpty()) {
                    append('-')
                    append(variant)
                }
            }
        }

        return ArscConfig(
            rawBytes = raw,
            mcc = mcc,
            mnc = mnc,
            locale = locale,
            orientation = safeReadU8(12),
            touchscreen = safeReadU8(13),
            density = safeReadU16(14),
            keyboard = safeReadU8(16),
            navigation = safeReadU8(17),
            inputFlags = safeReadU8(18),
            screenWidth = safeReadU16(20),
            screenHeight = safeReadU16(22),
            sdkVersion = safeReadU16(24),

            screenLayout = safeReadU8(28),
            uiMode = safeReadU8(29),
            smallestScreenWidthDp = safeReadU16(30),
            screenWidthDp = safeReadU16(32),
            screenHeightDp = safeReadU16(34),
            layoutDirection = (safeReadU8(28) ushr 6) and 0x03,
            colorMode = safeReadU8(54)
        )
    }

    private fun unpackLangOrRegion(b0: Int, b1: Int, base: Char): String {
        if (b0 == 0) return ""
        if ((b0 and 0x80) == 0) {

            return if (b1 == 0) b0.toChar().toString()
            else b0.toChar().toString() + b1.toChar().toString()
        }

        val baseOrd = base.code
        val first = (b1 and 0x1f) + baseOrd
        val second = (((b1 and 0xe0) ushr 5) or ((b0 and 0x03) shl 3)) + baseOrd
        val third = ((b0 and 0x7c) ushr 2) + baseOrd
        return charArrayOf(first.toChar(), second.toChar(), third.toChar())
            .concatToString()
    }

    private fun readAsciiBlock(raw: ByteArray, offset: Int, length: Int): String {
        if (offset + length > raw.size) return ""
        val end = (0 until length).firstOrNull {
            raw[offset + it] == 0.toByte()
        } ?: length
        if (end == 0) return ""
        return String(raw, offset, end, Charsets.US_ASCII)
    }

    private fun readU16(offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8)

    private fun readU32(offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)

    private fun readUtf8String(offset: Int): String {
        var o = offset
        val cb1 = data[o].toInt() and 0xFF
        o += if ((cb1 and 0x80) != 0) 2 else 1
        val bb1 = data[o].toInt() and 0xFF
        val byteLen: Int
        if ((bb1 and 0x80) != 0) {
            byteLen = ((bb1 and 0x7F) shl 8) or (data[o + 1].toInt() and 0xFF)
            o += 2
        } else {
            byteLen = bb1
            o += 1
        }
        if (o + byteLen > data.size) return ""
        return String(data, o, byteLen, Charsets.UTF_8)
    }

    private fun readUtf16String(offset: Int): String {
        var o = offset
        var charCount = readU16(o)
        if ((charCount and 0x8000) != 0) {
            charCount = ((charCount and 0x7FFF) shl 16) or readU16(o + 2)
            o += 4
        } else {
            o += 2
        }
        val byteLen = charCount * 2
        if (o + byteLen > data.size) return ""
        return String(data, o, byteLen, Charsets.UTF_16LE)
    }

    private fun ByteArray.toUtf16zString(): String {
        var len = 0
        while (len + 1 < size) {
            if (this[len] == 0.toByte() && this[len + 1] == 0.toByte()) break
            len += 2
        }
        return String(this, 0, len, Charsets.UTF_16LE)
    }

    @Suppress("unused")
    private val keepAxmlConstantsAlive: Int = AxmlConstants.TYPE_STRING
}
