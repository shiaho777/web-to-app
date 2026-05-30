package com.webtoapp.core.playstore.aab.axml

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AxmlToProtoXmlTest {

    @Test
    fun `convert round-trips root element and attributes`() {

        val axml = AxmlBuilder()
            .stringPool(
                listOf(
                    "android",
                    "http://schemas.android.com/apk/res/android",
                    "manifest",
                    "package",
                    "com.test.app",
                    "versionCode",
                    "",
                    "42"
                )
            )
            .resourceMap(

                intArrayOf(0, 0, 0, 0, 0, 0x0101021b)
            )
            .startNamespace(prefixIdx = 0, uriIdx = 1)
            .startElement(
                namespaceIdx = -1,
                nameIdx = 2,
                attributes = listOf(

                    AxmlBuilder.Attr(
                        nsIdx = -1,
                        nameIdx = 3,
                        rawIdx = 4,
                        typedValueType = AxmlConstants.TYPE_STRING,
                        typedValueData = 4
                    ),

                    AxmlBuilder.Attr(
                        nsIdx = 1,
                        nameIdx = 5,
                        rawIdx = 7,
                        typedValueType = AxmlConstants.TYPE_INT_DEC,
                        typedValueData = 42
                    )
                )
            )
            .endElement(namespaceIdx = -1, nameIdx = 2)
            .endNamespace(prefixIdx = 0, uriIdx = 1)
            .build()

        val node = AxmlToProtoXml.convert(axml)

        assertThat(node.hasElement()).isTrue()
        val element = node.element

        assertThat(element.name).isEqualTo("manifest")
        assertThat(element.namespaceUri).isEmpty()

        assertThat(element.namespaceDeclarationCount).isEqualTo(1)
        with(element.getNamespaceDeclaration(0)) {
            assertThat(prefix).isEqualTo("android")
            assertThat(uri).isEqualTo("http://schemas.android.com/apk/res/android")
        }

        assertThat(element.attributeCount).isEqualTo(2)

        val pkgAttr = element.attributeList.first { it.name == "package" }
        assertThat(pkgAttr.namespaceUri).isEmpty()
        assertThat(pkgAttr.value).isEqualTo("com.test.app")
        assertThat(pkgAttr.compiledItem.hasStr()).isTrue()
        assertThat(pkgAttr.compiledItem.str.value).isEqualTo("com.test.app")

        val vcAttr = element.attributeList.first { it.name == "versionCode" }
        assertThat(vcAttr.namespaceUri).isEqualTo("http://schemas.android.com/apk/res/android")
        assertThat(vcAttr.resourceId).isEqualTo(0x0101021b)
        assertThat(vcAttr.compiledItem.hasPrim()).isTrue()
        assertThat(vcAttr.compiledItem.prim.intDecimalValue).isEqualTo(42)
    }

    @Test
    fun `convert preserves boolean attribute as primitive bool`() {
        val axml = AxmlBuilder()
            .stringPool(listOf("manifest", "debuggable"))
            .resourceMap(intArrayOf(0, 0x0101000f))
            .startElement(
                namespaceIdx = -1,
                nameIdx = 0,
                attributes = listOf(
                    AxmlBuilder.Attr(
                        nsIdx = -1,
                        nameIdx = 1,
                        rawIdx = -1,
                        typedValueType = AxmlConstants.TYPE_INT_BOOLEAN,
                        typedValueData = -1
                    )
                )
            )
            .endElement(namespaceIdx = -1, nameIdx = 0)
            .build()

        val node = AxmlToProtoXml.convert(axml)
        val attr = node.element.getAttribute(0)
        assertThat(attr.compiledItem.hasPrim()).isTrue()
        assertThat(attr.compiledItem.prim.booleanValue).isTrue()
    }

    @Test
    fun `convert preserves reference attribute with resource id`() {
        val axml = AxmlBuilder()
            .stringPool(listOf("activity", "icon"))
            .resourceMap(intArrayOf(0, 0x01010002))
            .startElement(
                namespaceIdx = -1,
                nameIdx = 0,
                attributes = listOf(
                    AxmlBuilder.Attr(
                        nsIdx = -1,
                        nameIdx = 1,
                        rawIdx = -1,
                        typedValueType = AxmlConstants.TYPE_REFERENCE,
                        typedValueData = 0x7f080001
                    )
                )
            )
            .endElement(namespaceIdx = -1, nameIdx = 0)
            .build()

        val node = AxmlToProtoXml.convert(axml)
        val attr = node.element.getAttribute(0)
        assertThat(attr.compiledItem.hasRef()).isTrue()
        assertThat(attr.compiledItem.ref.id).isEqualTo(0x7f080001)
    }

    @Test
    fun `convert handles nested elements`() {

        val axml = AxmlBuilder()
            .stringPool(listOf("manifest", "application"))
            .resourceMap(intArrayOf(0, 0))
            .startElement(namespaceIdx = -1, nameIdx = 0, attributes = emptyList())
            .startElement(namespaceIdx = -1, nameIdx = 1, attributes = emptyList())
            .endElement(namespaceIdx = -1, nameIdx = 1)
            .endElement(namespaceIdx = -1, nameIdx = 0)
            .build()

        val node = AxmlToProtoXml.convert(axml)
        assertThat(node.element.name).isEqualTo("manifest")
        assertThat(node.element.childCount).isEqualTo(1)
        val child = node.element.getChild(0)
        assertThat(child.hasElement()).isTrue()
        assertThat(child.element.name).isEqualTo("application")
    }
}

private class AxmlBuilder {
    private val chunks = mutableListOf<ByteArray>()
    private var stringPoolBytes: ByteArray? = null
    private var resourceMapBytes: ByteArray? = null

    data class Attr(
        val nsIdx: Int,
        val nameIdx: Int,
        val rawIdx: Int,
        val typedValueType: Int,
        val typedValueData: Int
    )

    fun stringPool(strings: List<String>): AxmlBuilder = apply {

        val offsets = IntArray(strings.size)
        val stringsBlob = ByteArrayOutputStream()
        for ((i, s) in strings.withIndex()) {
            offsets[i] = stringsBlob.size()
            val bytes = s.toByteArray(Charsets.UTF_8)

            check(s.length < 0x80 && bytes.size < 0x80)
            stringsBlob.write(s.length)
            stringsBlob.write(bytes.size)
            stringsBlob.write(bytes)
            stringsBlob.write(0)
        }

        while (stringsBlob.size() % 4 != 0) stringsBlob.write(0)
        val poolPayload = stringsBlob.toByteArray()

        val out = ByteArrayOutputStream()
        val headerBuf = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN)
        val totalSize = 28 + offsets.size * 4 + poolPayload.size
        headerBuf.putShort(AxmlConstants.RES_STRING_POOL_TYPE.toShort())
        headerBuf.putShort(28.toShort())
        headerBuf.putInt(totalSize)
        headerBuf.putInt(strings.size)
        headerBuf.putInt(0)
        headerBuf.putInt(AxmlConstants.UTF8_FLAG)
        headerBuf.putInt(28 + offsets.size * 4)
        headerBuf.putInt(0)
        out.write(headerBuf.array())
        for (off in offsets) {
            out.write(intLE(off))
        }
        out.write(poolPayload)
        stringPoolBytes = out.toByteArray()
    }

    fun resourceMap(ids: IntArray): AxmlBuilder = apply {
        val out = ByteArrayOutputStream()
        val totalSize = 8 + ids.size * 4
        val header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        header.putShort(AxmlConstants.RES_XML_RESOURCE_MAP_TYPE.toShort())
        header.putShort(8.toShort())
        header.putInt(totalSize)
        out.write(header.array())
        for (id in ids) out.write(intLE(id))
        resourceMapBytes = out.toByteArray()
    }

    fun startNamespace(prefixIdx: Int, uriIdx: Int): AxmlBuilder = apply {
        chunks.add(xmlNodeChunk(AxmlConstants.RES_XML_START_NAMESPACE_TYPE, prefixIdx, uriIdx))
    }

    fun endNamespace(prefixIdx: Int, uriIdx: Int): AxmlBuilder = apply {
        chunks.add(xmlNodeChunk(AxmlConstants.RES_XML_END_NAMESPACE_TYPE, prefixIdx, uriIdx))
    }

    fun startElement(namespaceIdx: Int, nameIdx: Int, attributes: List<Attr>): AxmlBuilder = apply {

        val attrSize = 20
        val totalSize = 8 + 8 + 20 + attributes.size * attrSize

        val out = ByteArrayOutputStream()

        val head = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        head.putShort(AxmlConstants.RES_XML_START_ELEMENT_TYPE.toShort())
        head.putShort(16.toShort())
        head.putInt(totalSize)
        out.write(head.array())

        out.write(intLE(0))
        out.write(intLE(-1))

        out.write(intLE(namespaceIdx))
        out.write(intLE(nameIdx))
        val ext = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        ext.putShort(0x14.toShort())
        ext.putShort(attrSize.toShort())
        ext.putShort(attributes.size.toShort())
        ext.putShort(0)
        ext.putShort(0)
        ext.putShort(0)
        out.write(ext.array())

        for (a in attributes) {
            out.write(intLE(a.nsIdx))
            out.write(intLE(a.nameIdx))
            out.write(intLE(a.rawIdx))

            val rv = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            rv.putShort(8)
            rv.put(0)
            rv.put(a.typedValueType.toByte())
            rv.putInt(a.typedValueData)
            out.write(rv.array())
        }
        chunks.add(out.toByteArray())
    }

    fun endElement(namespaceIdx: Int, nameIdx: Int): AxmlBuilder = apply {

        val out = ByteArrayOutputStream()
        val head = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        head.putShort(AxmlConstants.RES_XML_END_ELEMENT_TYPE.toShort())
        head.putShort(16.toShort())
        head.putInt(24)
        out.write(head.array())
        out.write(intLE(0))
        out.write(intLE(-1))
        out.write(intLE(namespaceIdx))
        out.write(intLE(nameIdx))
        chunks.add(out.toByteArray())
    }

    private fun xmlNodeChunk(type: Int, prefixIdx: Int, uriIdx: Int): ByteArray {

        val out = ByteArrayOutputStream()
        val head = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        head.putShort(type.toShort())
        head.putShort(16.toShort())
        head.putInt(24)
        out.write(head.array())
        out.write(intLE(0))
        out.write(intLE(-1))
        out.write(intLE(prefixIdx))
        out.write(intLE(uriIdx))
        return out.toByteArray()
    }

    fun build(): ByteArray {

        val body = ByteArrayOutputStream()
        stringPoolBytes?.let { body.write(it) }
        resourceMapBytes?.let { body.write(it) }
        for (c in chunks) body.write(c)
        val bodyBytes = body.toByteArray()

        val out = ByteArrayOutputStream()
        val head = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        head.putShort(AxmlConstants.RES_XML_TYPE.toShort())
        head.putShort(8.toShort())
        head.putInt(8 + bodyBytes.size)
        out.write(head.array())
        out.write(bodyBytes)
        return out.toByteArray()
    }

    private fun intLE(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }
}
