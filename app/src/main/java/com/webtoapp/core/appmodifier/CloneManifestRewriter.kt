package com.webtoapp.core.appmodifier

import com.webtoapp.core.logging.AppLogger
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CloneManifestRewriter {

    companion object {
        private const val TAG = "CloneManifestRewriter"

        private const val CHUNK_AXML_FILE = 0x0003
        private const val CHUNK_STRING_POOL = 0x0001
        private const val CHUNK_RESOURCE_MAP = 0x0180
        private const val CHUNK_START_NAMESPACE = 0x0100
        private const val CHUNK_END_NAMESPACE = 0x0101
        private const val CHUNK_START_ELEMENT = 0x0102
        private const val CHUNK_END_ELEMENT = 0x0103

        private const val ATTR_NAME = 0x01010003
        private const val ATTR_EXPORTED = 0x01010010

        private const val CLONE_LAUNCHER_CLASS = "com.webtoapp.clone.CloneLauncherActivity"
    }

    data class RewriteResult(
        val axmlData: ByteArray,
        val originalLauncherActivity: String?
    )

    fun rewrite(
        axmlData: ByteArray,
        originalPackage: String,
        newPackage: String
    ): RewriteResult {
        return try {
            val parsed = parseAxml(axmlData) ?: return RewriteResult(axmlData, null)

            val expansions = findRelativeClassNames(parsed, originalPackage)
            if (expansions.isNotEmpty()) {
                expandClassNames(parsed, expansions)
            }

            replacePackageString(parsed, originalPackage, newPackage)

            val originalLauncher = findOriginalLauncherActivity(parsed, originalPackage)

            removeLauncherIntentFilters(parsed)

            addCloneLauncherActivity(parsed, newPackage)

            val result = rebuildAxml(parsed)
            AppLogger.d(TAG, "Clone manifest rewrite: ${axmlData.size} -> ${result.size} bytes, originalLauncher=$originalLauncher")
            RewriteResult(result, originalLauncher)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Clone manifest rewrite failed", e)
            RewriteResult(axmlData, null)
        }
    }

    private fun findOriginalLauncherActivity(parsed: ParsedAxml, originalPackage: String): String? {
        val resourceMap = parsed.resourceMap ?: return null
        val nameAttrIndex = resourceMap.indexOf(ATTR_NAME)
        if (nameAttrIndex < 0) return null

        val mainActionIndex = parsed.stringPool.strings.indexOf("android.intent.action.MAIN")
        val launcherCatIndex = parsed.stringPool.strings.indexOf("android.intent.category.LAUNCHER")
        val intentFilterStrIndex = parsed.stringPool.strings.indexOf("intent-filter")
        val actionStrIndex = parsed.stringPool.strings.indexOf("action")
        val categoryStrIndex = parsed.stringPool.strings.indexOf("category")
        val activityStrIndex = parsed.stringPool.strings.indexOf("activity")

        if (mainActionIndex < 0 || launcherCatIndex < 0 || intentFilterStrIndex < 0 || activityStrIndex < 0) return null

        var currentActivityName: String? = null

        for (i in parsed.chunks.indices) {
            val chunk = parsed.chunks[i]
            if (chunk.type == CHUNK_START_ELEMENT) {
                val elementNameStr = parsed.stringPool.strings.getOrNull(readElementNameIndex(chunk))
                if (elementNameStr == "activity") {
                    currentActivityName = readStringAttribute(parsed, chunk, nameAttrIndex)
                }
            }

            if (chunk.type == CHUNK_START_ELEMENT && currentActivityName != null) {
                val elementNameStr = parsed.stringPool.strings.getOrNull(readElementNameIndex(chunk))
                if (elementNameStr == "intent-filter") {
                    val hasMainAndLauncher = intentFilterContainsMainAndLauncher(
                        parsed, i,
                        actionStrIndex, categoryStrIndex,
                        mainActionIndex, launcherCatIndex, nameAttrIndex
                    )
                    if (hasMainAndLauncher) {
                        val result = currentActivityName
                        if (result != null && result.startsWith(".")) {
                            return originalPackage + result
                        }
                        return result
                    }
                }
            }
        }
        return null
    }

    private fun removeLauncherIntentFilters(parsed: ParsedAxml) {
        val resourceMap = parsed.resourceMap ?: return
        val nameAttrIndex = resourceMap.indexOf(ATTR_NAME)
        if (nameAttrIndex < 0) return

        val mainActionIndex = parsed.stringPool.strings.indexOf("android.intent.action.MAIN")
        val launcherCatIndex = parsed.stringPool.strings.indexOf("android.intent.category.LAUNCHER")
        val intentFilterStrIndex = parsed.stringPool.strings.indexOf("intent-filter")
        val actionStrIndex = parsed.stringPool.strings.indexOf("action")
        val categoryStrIndex = parsed.stringPool.strings.indexOf("category")

        if (mainActionIndex < 0 || launcherCatIndex < 0 || intentFilterStrIndex < 0) return

        val indicesToRemove = mutableSetOf<Int>()
        var i = 0
        while (i < parsed.chunks.size) {
            val chunk = parsed.chunks[i]
            if (chunk.type == CHUNK_START_ELEMENT) {
                val elementNameIndex = readElementNameIndex(chunk)
                val elementNameStr = parsed.stringPool.strings.getOrNull(elementNameIndex)

                if (elementNameStr == "intent-filter" && intentFilterContainsMainAndLauncher(parsed, i, actionStrIndex, categoryStrIndex, mainActionIndex, launcherCatIndex, nameAttrIndex)) {
                    val endIndex = findMatchingEndElementIndex(parsed, i)
                    if (endIndex > i) {
                        for (idx in i..endIndex) {
                            indicesToRemove.add(idx)
                        }
                        i = endIndex + 1
                        continue
                    }
                }
            }
            i++
        }

        if (indicesToRemove.isNotEmpty()) {
            for (idx in indicesToRemove.sortedDescending()) {
                parsed.chunks.removeAt(idx)
            }
            AppLogger.d(TAG, "Removed ${indicesToRemove.size} chunks from launcher intent-filters")
        }
    }

    private fun intentFilterContainsMainAndLauncher(
        parsed: ParsedAxml,
        startIndex: Int,
        actionStrIndex: Int,
        categoryStrIndex: Int,
        mainActionIndex: Int,
        launcherCatIndex: Int,
        nameAttrIndex: Int
    ): Boolean {
        val endIndex = findMatchingEndElementIndex(parsed, startIndex)
        if (endIndex < 0) return false

        var hasMain = false
        var hasLauncher = false

        for (i in (startIndex + 1) until endIndex) {
            val chunk = parsed.chunks[i]
            if (chunk.type != CHUNK_START_ELEMENT) continue

            val elementNameIndex = readElementNameIndex(chunk)
            val elementNameStr = parsed.stringPool.strings.getOrNull(elementNameIndex)

            if (elementNameStr == "action" && actionStrIndex >= 0) {
                val nameValue = readStringAttribute(parsed, chunk, nameAttrIndex)
                if (nameValue == "android.intent.action.MAIN") {
                    hasMain = true
                }
            } else if (elementNameStr == "category" && categoryStrIndex >= 0) {
                val nameValue = readStringAttribute(parsed, chunk, nameAttrIndex)
                if (nameValue == "android.intent.category.LAUNCHER") {
                    hasLauncher = true
                }
            }
        }

        return hasMain && hasLauncher
    }

    private fun addCloneLauncherActivity(parsed: ParsedAxml, packageName: String) {
        val resourceMap = parsed.resourceMap ?: return

        val nameAttrIndex = resourceMap.indexOf(ATTR_NAME)
        val exportedAttrIndex = resourceMap.indexOf(ATTR_EXPORTED)
        if (nameAttrIndex < 0 || exportedAttrIndex < 0) {
            AppLogger.e(TAG, "Missing required attribute indices for CloneLauncherActivity")
            return
        }

        val androidNsIndex = getOrAddString(parsed.stringPool, "http://schemas.android.com/apk/res/android")
        val activityStrIndex = getOrAddString(parsed.stringPool, "activity")
        val intentFilterStrIndex = getOrAddString(parsed.stringPool, "intent-filter")
        val actionStrIndex = getOrAddString(parsed.stringPool, "action")
        val categoryStrIndex = getOrAddString(parsed.stringPool, "category")
        val mainActionIndex = getOrAddString(parsed.stringPool, "android.intent.action.MAIN")
        val launcherCatIndex = getOrAddString(parsed.stringPool, "android.intent.category.LAUNCHER")
        val cloneActivityIndex = getOrAddString(parsed.stringPool, CLONE_LAUNCHER_CLASS)

        val appEndIndex = findApplicationEndIndex(parsed)
        if (appEndIndex < 0) {
            AppLogger.e(TAG, "Cannot find </application> for CloneLauncherActivity injection")
            return
        }

        val newChunks = mutableListOf<Chunk>()

        val activityStart = buildActivityStartElement(
            androidNsIndex = androidNsIndex,
            elementNameIndex = activityStrIndex,
            nameAttrIndex = nameAttrIndex,
            nameValueIndex = cloneActivityIndex,
            exportedAttrIndex = exportedAttrIndex
        )
        newChunks.add(activityStart)

        newChunks.add(buildSimpleStartElement(androidNsIndex, intentFilterStrIndex, 0))
        newChunks.add(buildActionOrCategoryElement(androidNsIndex, actionStrIndex, nameAttrIndex, mainActionIndex))
        newChunks.add(buildEndElement(androidNsIndex, actionStrIndex))
        newChunks.add(buildActionOrCategoryElement(androidNsIndex, categoryStrIndex, nameAttrIndex, launcherCatIndex))
        newChunks.add(buildEndElement(androidNsIndex, categoryStrIndex))
        newChunks.add(buildEndElement(androidNsIndex, intentFilterStrIndex))

        newChunks.add(buildEndElement(androidNsIndex, activityStrIndex))

        parsed.chunks.addAll(appEndIndex, newChunks)
        AppLogger.d(TAG, "Injected CloneLauncherActivity with ${newChunks.size} chunks")
    }

    private fun buildActivityStartElement(
        androidNsIndex: Int,
        elementNameIndex: Int,
        nameAttrIndex: Int,
        nameValueIndex: Int,
        exportedAttrIndex: Int
    ): Chunk {
        val attrCount = 2
        val attrSize = 20
        val headerSize = 16
        val attrStart = 20
        val chunkSize = 36 + attrCount * attrSize

        val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putShort(CHUNK_START_ELEMENT.toShort())
        buffer.putShort(headerSize.toShort())
        buffer.putInt(chunkSize)
        buffer.putInt(0)
        buffer.putInt(-1)
        buffer.putInt(-1)
        buffer.putInt(elementNameIndex)
        buffer.putShort(attrStart.toShort())
        buffer.putShort(attrSize.toShort())
        buffer.putShort(attrCount.toShort())
        buffer.putShort(0)
        buffer.putShort(0)
        buffer.putShort(0)

        buffer.putInt(androidNsIndex)
        buffer.putInt(nameAttrIndex)
        buffer.putInt(nameValueIndex)
        buffer.putShort(8)
        buffer.put(0)
        buffer.put(0x03)
        buffer.putInt(nameValueIndex)

        buffer.putInt(androidNsIndex)
        buffer.putInt(exportedAttrIndex)
        buffer.putInt(-1)
        buffer.putShort(8)
        buffer.put(0)
        buffer.put(0x12)
        buffer.putInt(-1)

        return Chunk(CHUNK_START_ELEMENT, 0, chunkSize, buffer.array())
    }

    private fun buildSimpleStartElement(androidNsIndex: Int, elementNameIndex: Int, attrCount: Int): Chunk {
        val attrSize = 20
        val headerSize = 16
        val chunkSize = 36 + attrCount * attrSize

        val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putShort(CHUNK_START_ELEMENT.toShort())
        buffer.putShort(headerSize.toShort())
        buffer.putInt(chunkSize)
        buffer.putInt(0)
        buffer.putInt(-1)
        buffer.putInt(-1)
        buffer.putInt(elementNameIndex)
        buffer.putShort(20)
        buffer.putShort(attrSize.toShort())
        buffer.putShort(attrCount.toShort())
        buffer.putShort(0)
        buffer.putShort(0)
        buffer.putShort(0)

        return Chunk(CHUNK_START_ELEMENT, 0, chunkSize, buffer.array())
    }

    private fun buildActionOrCategoryElement(
        androidNsIndex: Int,
        elementNameIndex: Int,
        nameAttrIndex: Int,
        nameValueIndex: Int
    ): Chunk {
        val attrCount = 1
        val attrSize = 20
        val headerSize = 16
        val chunkSize = 36 + attrCount * attrSize

        val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putShort(CHUNK_START_ELEMENT.toShort())
        buffer.putShort(headerSize.toShort())
        buffer.putInt(chunkSize)
        buffer.putInt(0)
        buffer.putInt(-1)
        buffer.putInt(-1)
        buffer.putInt(elementNameIndex)
        buffer.putShort(20)
        buffer.putShort(attrSize.toShort())
        buffer.putShort(attrCount.toShort())
        buffer.putShort(0)
        buffer.putShort(0)
        buffer.putShort(0)

        buffer.putInt(androidNsIndex)
        buffer.putInt(nameAttrIndex)
        buffer.putInt(nameValueIndex)
        buffer.putShort(8)
        buffer.put(0)
        buffer.put(0x03)
        buffer.putInt(nameValueIndex)

        return Chunk(CHUNK_START_ELEMENT, 0, chunkSize, buffer.array())
    }

    private fun buildEndElement(androidNsIndex: Int, elementNameIndex: Int): Chunk {
        val headerSize = 16
        val chunkSize = 24

        val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putShort(CHUNK_END_ELEMENT.toShort())
        buffer.putShort(headerSize.toShort())
        buffer.putInt(chunkSize)
        buffer.putInt(0)
        buffer.putInt(-1)
        buffer.putInt(-1)
        buffer.putInt(elementNameIndex)

        return Chunk(CHUNK_END_ELEMENT, 0, chunkSize, buffer.array())
    }

    private fun findApplicationEndIndex(parsed: ParsedAxml): Int {
        val applicationStrIndex = parsed.stringPool.strings.indexOf("application")
        if (applicationStrIndex < 0) return -1

        for (i in parsed.chunks.indices) {
            val chunk = parsed.chunks[i]
            if (chunk.type == CHUNK_END_ELEMENT) {
                val buffer = ByteBuffer.wrap(chunk.data).order(ByteOrder.LITTLE_ENDIAN)
                buffer.position(16)
                buffer.int
                val name = buffer.int
                if (name == applicationStrIndex) {
                    return i
                }
            }
        }
        return -1
    }

    private fun findMatchingEndElementIndex(parsed: ParsedAxml, startIndex: Int): Int {
        val startChunk = parsed.chunks.getOrNull(startIndex) ?: return -1
        if (startChunk.type != CHUNK_START_ELEMENT) return -1

        val elementNameIndex = readElementNameIndex(startChunk)
        if (elementNameIndex < 0) return -1

        var depth = 0
        for (i in startIndex until parsed.chunks.size) {
            val chunk = parsed.chunks[i]
            if (readElementNameIndex(chunk) != elementNameIndex) continue

            when (chunk.type) {
                CHUNK_START_ELEMENT -> depth++
                CHUNK_END_ELEMENT -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun readElementNameIndex(chunk: Chunk): Int {
        if (chunk.data.size < 24) return -1
        val buffer = ByteBuffer.wrap(chunk.data).order(ByteOrder.LITTLE_ENDIAN)
        return buffer.getInt(20)
    }

    private fun readStringAttribute(parsed: ParsedAxml, chunk: Chunk, attrIndex: Int): String? {
        if (chunk.type != CHUNK_START_ELEMENT || chunk.data.size < 36) return null
        val buffer = ByteBuffer.wrap(chunk.data).order(ByteOrder.LITTLE_ENDIAN)
        val attrStart = buffer.getShort(24).toInt() and 0xFFFF
        val attrSize = buffer.getShort(26).toInt() and 0xFFFF
        val attrCount = buffer.getShort(28).toInt() and 0xFFFF
        if (attrSize == 0 || attrCount == 0) return null

        for (i in 0 until attrCount) {
            val attrOffset = 16 + attrStart + i * attrSize
            if (attrOffset + 20 > chunk.data.size) break
            val attrName = buffer.getInt(attrOffset + 4)
            if (attrName != attrIndex) continue

            val attrValueType = buffer.get(attrOffset + 15).toInt() and 0xFF
            if (attrValueType != 0x03) return null

            val attrValueData = buffer.getInt(attrOffset + 16)
            return parsed.stringPool.strings.getOrNull(attrValueData)
        }
        return null
    }

    private fun getOrAddString(pool: StringPool, str: String): Int {
        val index = pool.strings.indexOf(str)
        if (index >= 0) return index
        pool.strings.add(str)
        return pool.strings.size - 1
    }

    private fun parseAxml(data: ByteArray): ParsedAxml? {
        if (data.size < 8) return null
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val fileType = buffer.short.toInt() and 0xFFFF
        val fileHeaderSize = buffer.short.toInt() and 0xFFFF
        val fileSize = buffer.int

        if (fileType != CHUNK_AXML_FILE) return null

        val chunks = mutableListOf<Chunk>()
        var stringPool: StringPool? = null
        var resourceMap: IntArray? = null

        var offset = fileHeaderSize
        while (offset + 8 <= data.size) {
            buffer.position(offset)
            val chunkType = buffer.short.toInt() and 0xFFFF
            val chunkHeaderSize = buffer.short.toInt() and 0xFFFF
            val chunkSize = buffer.int

            if (chunkSize <= 0 || offset + chunkSize > data.size) break

            when (chunkType) {
                CHUNK_STRING_POOL -> stringPool = parseStringPool(data, offset)
                CHUNK_RESOURCE_MAP -> resourceMap = parseResourceMap(data, offset, chunkSize)
                else -> chunks.add(Chunk(chunkType, offset, chunkSize, data.copyOfRange(offset, offset + chunkSize)))
            }
            offset += chunkSize
        }

        if (stringPool == null) return null
        return ParsedAxml(fileHeaderSize, stringPool, resourceMap, chunks)
    }

    private fun parseStringPool(data: ByteArray, offset: Int): StringPool {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(offset)
        buffer.short
        val headerSize = buffer.short.toInt() and 0xFFFF
        val chunkSize = buffer.int
        val stringCount = buffer.int
        val styleCount = buffer.int
        val flags = buffer.int
        val stringsStart = buffer.int
        val stylesStart = buffer.int

        val isUtf8 = (flags and 0x100) != 0
        val stringOffsets = IntArray(stringCount) { buffer.int }
        val styleOffsets = IntArray(styleCount) { buffer.int }

        val stringsDataStart = offset + stringsStart
        val strings = mutableListOf<String>()

        for (i in 0 until stringCount) {
            val strOffset = stringsDataStart + stringOffsets[i]
            val str = if (isUtf8) readUtf8String(data, strOffset) else readUtf16String(data, strOffset)
            strings.add(str)
        }

        val stylesData = if (styleCount > 0 && stylesStart > 0) {
            data.copyOfRange(offset + stylesStart, offset + chunkSize)
        } else null

        val originalStringsDataSize = if (stylesStart > 0) stylesStart - stringsStart else chunkSize - stringsStart

        return StringPool(flags, isUtf8, strings.toMutableList(), styleOffsets, stylesData, originalStringsDataSize)
    }

    private fun parseResourceMap(data: ByteArray, offset: Int, size: Int): IntArray {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(offset + 8)
        val count = (size - 8) / 4
        return IntArray(count) { buffer.int }
    }

    private fun findRelativeClassNames(parsed: ParsedAxml, originalPackage: String): List<ClassNameExpansion> {
        val expansions = mutableListOf<ClassNameExpansion>()
        val resourceMap = parsed.resourceMap ?: return expansions
        val nameAttrIndex = resourceMap.indexOf(ATTR_NAME)
        if (nameAttrIndex < 0) return expansions

        for ((chunkIdx, chunk) in parsed.chunks.withIndex()) {
            if (chunk.type != CHUNK_START_ELEMENT) continue
            val buffer = ByteBuffer.wrap(chunk.data).order(ByteOrder.LITTLE_ENDIAN)
            buffer.position(16)
            buffer.int
            val elementName = buffer.int
            val attrStart = buffer.short.toInt() and 0xFFFF
            val attrSize = buffer.short.toInt() and 0xFFFF
            val attrCount = buffer.short.toInt() and 0xFFFF
            if (attrSize == 0 || attrCount == 0) continue

            val elementNameStr = parsed.stringPool.strings.getOrNull(elementName) ?: continue
            if (elementNameStr !in listOf("activity", "service", "receiver", "provider", "application", "activity-alias")) continue

            for (i in 0 until attrCount) {
                val attrOffset = 36 + i * attrSize
                if (attrOffset + 20 > chunk.data.size) break
                buffer.position(attrOffset)
                buffer.int
                val attrName = buffer.int
                buffer.int
                buffer.short
                buffer.get()
                val attrValueType = buffer.get().toInt() and 0xFF
                val attrValueData = buffer.int
                if (attrName != nameAttrIndex || attrValueType != 3) continue

                val stringValue = parsed.stringPool.strings.getOrNull(attrValueData) ?: continue
                if (stringValue.startsWith(".") || (!stringValue.contains(".") && stringValue.isNotEmpty())) {
                    val absoluteName = if (stringValue.startsWith(".")) {
                        originalPackage + stringValue
                    } else {
                        "$originalPackage.$stringValue"
                    }
                    expansions.add(ClassNameExpansion(chunkIdx, i, attrOffset, attrValueData, stringValue, absoluteName))
                }
            }
        }
        return expansions
    }

    private fun expandClassNames(parsed: ParsedAxml, expansions: List<ClassNameExpansion>) {
        for (expansion in expansions) {
            var newIndex = parsed.stringPool.strings.indexOf(expansion.expandedValue)
            if (newIndex < 0) {
                newIndex = parsed.stringPool.strings.size
                parsed.stringPool.strings.add(expansion.expandedValue)
            }
            val chunk = parsed.chunks[expansion.chunkIndex]
            val buffer = ByteBuffer.wrap(chunk.data).order(ByteOrder.LITTLE_ENDIAN)
            buffer.position(expansion.attrOffset + 8)
            buffer.putInt(newIndex)
            buffer.position(expansion.attrOffset + 16)
            buffer.putInt(newIndex)
        }
    }

    private fun replacePackageString(parsed: ParsedAxml, oldPackage: String, newPackage: String) {
        for (i in parsed.stringPool.strings.indices) {
            val str = parsed.stringPool.strings[i]
            when {
                str == oldPackage -> parsed.stringPool.strings[i] = newPackage
                str.startsWith("$oldPackage.") -> {
                    val suffix = str.substring(oldPackage.length + 1)
                    if (!isLikelyClassName(suffix)) {
                        parsed.stringPool.strings[i] = newPackage + str.substring(oldPackage.length)
                    }
                }
            }
        }
    }

    private fun isLikelyClassName(suffix: String): Boolean {
        val lastDotIndex = suffix.lastIndexOf('.')
        val className = if (lastDotIndex >= 0) suffix.substring(lastDotIndex + 1) else suffix
        if (className.isNotEmpty() && className[0].isUpperCase()) {
            val componentSuffixes = listOf("Activity", "Service", "Provider", "Receiver", "Application", "Fragment", "Adapter", "View", "Manager", "Helper", "Listener", "Callback")
            return componentSuffixes.any { className.endsWith(it) } || className.matches(Regex("^[A-Z][a-zA-Z0-9]*$"))
        }
        return false
    }

    private fun rebuildAxml(parsed: ParsedAxml): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val stringPoolData = rebuildStringPool(parsed.stringPool)
        val resourceMapData = parsed.resourceMap?.let { rebuildResourceMap(it) } ?: ByteArray(0)

        val chunksData = java.io.ByteArrayOutputStream()
        for (chunk in parsed.chunks) chunksData.write(chunk.data)

        val totalSize = parsed.fileHeaderSize + stringPoolData.size + resourceMapData.size + chunksData.size()

        val header = ByteBuffer.allocate(parsed.fileHeaderSize).order(ByteOrder.LITTLE_ENDIAN)
        header.putShort(CHUNK_AXML_FILE.toShort())
        header.putShort(parsed.fileHeaderSize.toShort())
        header.putInt(totalSize)
        output.write(header.array())
        output.write(stringPoolData)
        output.write(resourceMapData)
        chunksData.writeTo(output)
        return output.toByteArray()
    }

    private fun rebuildStringPool(pool: StringPool): ByteArray {
        val isUtf8 = pool.isUtf8
        val stringCount = pool.strings.size
        val styleCount = pool.styleOffsets.size

        val stringsBuffer = java.io.ByteArrayOutputStream()
        val stringOffsets = IntArray(stringCount)

        for (i in 0 until stringCount) {
            stringOffsets[i] = stringsBuffer.size()
            if (isUtf8) writeUtf8String(stringsBuffer, pool.strings[i])
            else writeUtf16String(stringsBuffer, pool.strings[i])
        }
        while (stringsBuffer.size() % 4 != 0) stringsBuffer.write(0)

        val stringsData = stringsBuffer.toByteArray()
        val stringsDataSizeDelta = stringsData.size - pool.originalStringsDataSize

        val headerSize = 28
        val offsetsSize = (stringCount + styleCount) * 4
        val stringsStart = headerSize + offsetsSize
        val stylesStart = if (styleCount > 0 && pool.stylesData != null) stringsStart + stringsData.size else 0
        val stylesDataSize = pool.stylesData?.size ?: 0
        val chunkSize = stringsStart + stringsData.size + stylesDataSize

        val result = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)
        result.putShort(CHUNK_STRING_POOL.toShort())
        result.putShort(headerSize.toShort())
        result.putInt(chunkSize)
        result.putInt(stringCount)
        result.putInt(styleCount)
        result.putInt(pool.flags and 0x01.inv())
        result.putInt(stringsStart)
        result.putInt(stylesStart)

        for (offset in stringOffsets) result.putInt(offset)
        for (offset in pool.styleOffsets) result.putInt(offset + stringsDataSizeDelta)
        result.put(stringsData)
        pool.stylesData?.let { result.put(it) }

        return result.array()
    }

    private fun rebuildResourceMap(resourceMap: IntArray): ByteArray {
        val size = 8 + resourceMap.size * 4
        val buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(CHUNK_RESOURCE_MAP.toShort())
        buffer.putShort(8.toShort())
        buffer.putInt(size)
        for (id in resourceMap) buffer.putInt(id)
        return buffer.array()
    }

    private fun readUtf8String(data: ByteArray, offset: Int): String {
        if (offset >= data.size) return ""
        var o = offset
        var charLen = data[o].toInt() and 0x7F
        if (data[o].toInt() and 0x80 != 0) {
            if (o + 1 >= data.size) return ""
            charLen = ((data[o].toInt() and 0x7F) shl 8) or (data[o + 1].toInt() and 0xFF)
            o += 2
        } else o += 1
        var byteLen = data[o].toInt() and 0x7F
        if (data[o].toInt() and 0x80 != 0) {
            if (o + 1 >= data.size) return ""
            byteLen = ((data[o].toInt() and 0x7F) shl 8) or (data[o + 1].toInt() and 0xFF)
            o += 2
        } else o += 1
        if (o + byteLen > data.size) return ""
        return String(data, o, byteLen, Charsets.UTF_8)
    }

    private fun readUtf16String(data: ByteArray, offset: Int): String {
        if (offset + 2 > data.size) return ""
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(offset)
        var length = buffer.short.toInt() and 0xFFFF
        if (length and 0x8000 != 0) {
            if (offset + 4 > data.size) return ""
            length = ((length and 0x7FFF) shl 16) or (buffer.short.toInt() and 0xFFFF)
        }
        val byteLen = length * 2
        if (buffer.position() + byteLen > data.size) return ""
        val strBytes = ByteArray(byteLen)
        buffer.get(strBytes)
        return String(strBytes, Charsets.UTF_16LE)
    }

    private fun writeUtf8String(output: java.io.ByteArrayOutputStream, str: String) {
        val bytes = str.toByteArray(Charsets.UTF_8)
        val charLen = str.length
        val byteLen = bytes.size
        if (charLen > 0x7F) { output.write(0x80 or ((charLen shr 8) and 0x7F)); output.write(charLen and 0xFF) }
        else output.write(charLen)
        if (byteLen > 0x7F) { output.write(0x80 or ((byteLen shr 8) and 0x7F)); output.write(byteLen and 0xFF) }
        else output.write(byteLen)
        output.write(bytes)
        output.write(0)
    }

    private fun writeUtf16String(output: java.io.ByteArrayOutputStream, str: String) {
        val length = str.length
        if (length > 0x7FFF) {
            output.write(0x80 or ((length shr 24) and 0x7F))
            output.write((length shr 16) and 0xFF)
            output.write((length shr 8) and 0xFF)
            output.write(length and 0xFF)
        } else {
            output.write(length and 0xFF)
            output.write((length shr 8) and 0xFF)
        }
        output.write(str.toByteArray(Charsets.UTF_16LE))
        output.write(0)
        output.write(0)
    }

    private data class ParsedAxml(
        val fileHeaderSize: Int,
        val stringPool: StringPool,
        var resourceMap: IntArray?,
        val chunks: MutableList<Chunk>
    )

    private data class StringPool(
        val flags: Int,
        val isUtf8: Boolean,
        val strings: MutableList<String>,
        val styleOffsets: IntArray,
        val stylesData: ByteArray?,
        val originalStringsDataSize: Int
    )

    private data class Chunk(val type: Int, val offset: Int, val size: Int, val data: ByteArray)

    private data class ClassNameExpansion(
        val chunkIndex: Int,
        val attrIndex: Int,
        val attrOffset: Int,
        val originalStringIndex: Int,
        val originalValue: String,
        val expandedValue: String
    )
}
