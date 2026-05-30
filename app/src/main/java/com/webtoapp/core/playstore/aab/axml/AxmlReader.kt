package com.webtoapp.core.playstore.aab.axml

import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_STRING_POOL_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_CDATA_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_END_ELEMENT_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_END_NAMESPACE_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_RESOURCE_MAP_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_START_ELEMENT_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_START_NAMESPACE_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.RES_XML_TYPE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.UTF8_FLAG

internal class AxmlReader(private val data: ByteArray) {

    private val stringPool: List<String>

    private val resourceMap: IntArray
    private var pos: Int = 0

    data class Attribute(

        val namespaceUri: String,

        val name: String,

        val typedValueType: Int,

        val typedValueData: Int,

        val rawValue: String?,

        val resourceId: Int
    )

    data class Element(
        val namespaceUri: String,
        val name: String,
        val attributes: List<Attribute>,
        val children: List<Node>,
        val lineNumber: Int,

        val namespaceDeclarations: List<NamespaceDeclaration>
    ) : Node

    data class TextNode(val text: String, val lineNumber: Int) : Node

    sealed interface Node

    data class NamespaceDeclaration(val prefix: String, val uri: String, val lineNumber: Int)

    init {
        require(data.size >= 8) { "AXML too short: ${data.size} bytes" }
        require(readU16(0) == RES_XML_TYPE) {
            "Not an AXML file (magic = 0x%04x)".format(readU16(0))
        }
        val headerSize = readU16(2)
        val totalSize = readU32(4)
        require(totalSize == data.size) {
            "AXML size mismatch: header=$totalSize, actual=${data.size}"
        }

        pos = headerSize
        var pool: List<String>? = null
        var resMap = IntArray(0)

        while (pos + 8 <= data.size) {
            val type = readU16(pos)
            val chunkSize = readU32(pos + 4)
            when (type) {
                RES_STRING_POOL_TYPE -> {
                    pool = parseStringPool(pos)
                    pos += chunkSize
                }
                RES_XML_RESOURCE_MAP_TYPE -> {
                    resMap = parseResourceMap(pos)
                    pos += chunkSize
                }
                RES_XML_START_NAMESPACE_TYPE,
                RES_XML_START_ELEMENT_TYPE -> {

                    break
                }
                else -> {

                    pos += chunkSize
                }
            }
        }

        stringPool = pool ?: emptyList()
        resourceMap = resMap
    }

    fun readDocument(): Element {

        val rootElements = mutableListOf<Element>()
        val openStack = ArrayDeque<MutableElementBuilder>()

        val pendingNamespaces = mutableListOf<NamespaceDeclaration>()

        while (pos + 8 <= data.size) {
            val type = readU16(pos)
            val headerSize = readU16(pos + 2)
            val chunkSize = readU32(pos + 4)

            if (chunkSize <= 0) break

            when (type) {
                RES_XML_START_NAMESPACE_TYPE -> {

                    val lineNumber = readU32(pos + 8)
                    val prefixIdx = readU32(pos + headerSize)
                    val uriIdx = readU32(pos + headerSize + 4)
                    pendingNamespaces.add(
                        NamespaceDeclaration(
                            prefix = stringAt(prefixIdx),
                            uri = stringAt(uriIdx),
                            lineNumber = lineNumber
                        )
                    )
                }

                RES_XML_END_NAMESPACE_TYPE -> {

                }

                RES_XML_START_ELEMENT_TYPE -> {
                    val element = parseStartElement(chunkStart = pos, headerSize = headerSize)
                    val builder = MutableElementBuilder(
                        namespaceUri = element.namespaceUri,
                        name = element.name,
                        attributes = element.attributes,
                        lineNumber = element.lineNumber,
                        namespaceDeclarations = pendingNamespaces.toList()
                    )
                    pendingNamespaces.clear()
                    openStack.addLast(builder)
                }

                RES_XML_END_ELEMENT_TYPE -> {
                    val finished = openStack.removeLast().build()
                    if (openStack.isEmpty()) {
                        rootElements.add(finished)
                    } else {
                        openStack.last().children.add(finished)
                    }
                }

                RES_XML_CDATA_TYPE -> {

                    val lineNumber = readU32(pos + 8)
                    val dataIdx = readU32(pos + headerSize)
                    val text = TextNode(stringAt(dataIdx), lineNumber)
                    if (openStack.isEmpty()) {

                    } else {
                        openStack.last().children.add(text)
                    }
                }

                else -> {

                }
            }

            pos += chunkSize
        }

        check(openStack.isEmpty()) {
            "Unclosed elements at EOF: ${openStack.map { it.name }}"
        }
        check(rootElements.size == 1) {
            "Expected exactly one root element, got ${rootElements.size}"
        }
        return rootElements.single()
    }

    fun stringAt(index: Int): String {
        if (index < 0 || index >= stringPool.size) return ""
        return stringPool[index]
    }

    private class MutableElementBuilder(
        val namespaceUri: String,
        val name: String,
        val attributes: List<Attribute>,
        val lineNumber: Int,
        val namespaceDeclarations: List<NamespaceDeclaration>,
        val children: MutableList<Node> = mutableListOf()
    ) {
        fun build(): Element = Element(
            namespaceUri = namespaceUri,
            name = name,
            attributes = attributes,
            children = children.toList(),
            lineNumber = lineNumber,
            namespaceDeclarations = namespaceDeclarations
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

    private fun parseResourceMap(chunkStart: Int): IntArray {
        val headerSize = readU16(chunkStart + 2)
        val totalSize = readU32(chunkStart + 4)
        val payloadSize = totalSize - headerSize
        val count = payloadSize / 4
        val out = IntArray(count)
        for (i in 0 until count) {
            out[i] = readU32(chunkStart + headerSize + i * 4)
        }
        return out
    }

    private fun parseStartElement(chunkStart: Int, headerSize: Int): Element {
        val lineNumber = readU32(chunkStart + 8)
        val attrExtStart = chunkStart + headerSize
        val nsIdx = readU32(attrExtStart)
        val nameIdx = readU32(attrExtStart + 4)

        val attributeStart = readU16(attrExtStart + 8)
        val attributeSize = readU16(attrExtStart + 10)
        val attributeCount = readU16(attrExtStart + 12)

        val attrs = ArrayList<Attribute>(attributeCount)

        val attrsBase = attrExtStart + attributeStart
        for (i in 0 until attributeCount) {
            val o = attrsBase + i * attributeSize
            val attrNsIdx = readU32(o)
            val attrNameIdx = readU32(o + 4)
            val attrRawIdx = readU32(o + 8)

            val typedValueType = data[o + 15].toInt() and 0xFF
            val typedValueData = readU32(o + 16)

            val resourceId = if (attrNameIdx in resourceMap.indices) {
                resourceMap[attrNameIdx]
            } else {
                0
            }

            attrs.add(
                Attribute(
                    namespaceUri = stringAt(attrNsIdx),
                    name = stringAt(attrNameIdx),
                    typedValueType = typedValueType,
                    typedValueData = typedValueData,
                    rawValue = if (attrRawIdx >= 0) stringAt(attrRawIdx) else null,
                    resourceId = resourceId
                )
            )
        }

        return Element(
            namespaceUri = stringAt(nsIdx),
            name = stringAt(nameIdx),
            attributes = attrs,
            children = emptyList(),
            lineNumber = lineNumber,
            namespaceDeclarations = emptyList()
        )
    }

    private fun readU16(offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8)
    }

    private fun readU32(offset: Int): Int {

        return (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)
    }

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
}
