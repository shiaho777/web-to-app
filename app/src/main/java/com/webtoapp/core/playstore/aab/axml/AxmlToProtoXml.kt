package com.webtoapp.core.playstore.aab.axml

import com.android.aapt.Resources
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_ATTRIBUTE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_DIMENSION
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_FLOAT
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_FRACTION
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_BOOLEAN
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_COLOR_ARGB4
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_COLOR_ARGB8
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_COLOR_RGB4
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_COLOR_RGB8
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_DEC
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_INT_HEX
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_NULL
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_REFERENCE
import com.webtoapp.core.playstore.aab.axml.AxmlConstants.TYPE_STRING

object AxmlToProtoXml {

    fun convert(axmlData: ByteArray): Resources.XmlNode {
        val reader = AxmlReader(axmlData)
        val root = reader.readDocument()
        return convertElement(root)
    }

    private fun convertElement(element: AxmlReader.Element): Resources.XmlNode {
        val protoElement = Resources.XmlElement.newBuilder()
            .setName(element.name)
            .setNamespaceUri(element.namespaceUri)

        for (ns in element.namespaceDeclarations) {
            protoElement.addNamespaceDeclaration(
                Resources.XmlNamespace.newBuilder()
                    .setPrefix(ns.prefix)
                    .setUri(ns.uri)
                    .setSource(linePosition(ns.lineNumber))
                    .build()
            )
        }

        for (attr in element.attributes) {
            protoElement.addAttribute(convertAttribute(attr))
        }

        for (child in element.children) {
            when (child) {
                is AxmlReader.Element -> {
                    protoElement.addChild(convertElement(child))
                }
                is AxmlReader.TextNode -> {
                    protoElement.addChild(
                        Resources.XmlNode.newBuilder()
                            .setText(child.text)
                            .setSource(linePosition(child.lineNumber))
                            .build()
                    )
                }
            }
        }

        return Resources.XmlNode.newBuilder()
            .setElement(protoElement.build())
            .setSource(linePosition(element.lineNumber))
            .build()
    }

    private fun convertAttribute(attr: AxmlReader.Attribute): Resources.XmlAttribute {
        val builder = Resources.XmlAttribute.newBuilder()
            .setName(attr.name)
            .setNamespaceUri(attr.namespaceUri)

            .setValue(formatRawValue(attr))

        if (attr.resourceId != 0) {
            builder.setResourceId(attr.resourceId)
        }

        val compiledItem = compileValue(attr)
        if (compiledItem != null) {
            builder.setCompiledItem(compiledItem)
        }

        return builder.build()
    }

    private fun compileValue(attr: AxmlReader.Attribute): Resources.Item? {
        return when (attr.typedValueType) {
            TYPE_STRING -> {

                Resources.Item.newBuilder()
                    .setStr(Resources.String.newBuilder().setValue(attr.rawValue ?: "").build())
                    .build()
            }
            TYPE_REFERENCE -> {

                if (attr.typedValueData == 0) {
                    Resources.Item.newBuilder()
                        .setRef(Resources.Reference.getDefaultInstance())
                        .build()
                } else {
                    Resources.Item.newBuilder()
                        .setRef(
                            Resources.Reference.newBuilder()
                                .setType(Resources.Reference.Type.REFERENCE)
                                .setId(attr.typedValueData)
                                .build()
                        )
                        .build()
                }
            }
            TYPE_ATTRIBUTE -> {

                if (attr.typedValueData == 0) {
                    Resources.Item.newBuilder()
                        .setRef(
                            Resources.Reference.newBuilder()
                                .setType(Resources.Reference.Type.ATTRIBUTE)
                                .build()
                        )
                        .build()
                } else {
                    Resources.Item.newBuilder()
                        .setRef(
                            Resources.Reference.newBuilder()
                                .setType(Resources.Reference.Type.ATTRIBUTE)
                                .setId(attr.typedValueData)
                                .build()
                        )
                        .build()
                }
            }
            TYPE_NULL -> {

                val primBuilder = Resources.Primitive.newBuilder()
                if (attr.typedValueData == 1) {
                    primBuilder.setEmptyValue(
                        Resources.Primitive.EmptyType.getDefaultInstance()
                    )
                } else {
                    primBuilder.setNullValue(
                        Resources.Primitive.NullType.getDefaultInstance()
                    )
                }
                Resources.Item.newBuilder()
                    .setPrim(primBuilder.build())
                    .build()
            }
            TYPE_INT_BOOLEAN -> {

                val bool = attr.typedValueData != 0
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setBooleanValue(bool)
                            .build()
                    )
                    .build()
            }
            TYPE_INT_DEC -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setIntDecimalValue(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_INT_HEX -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setIntHexadecimalValue(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_FLOAT -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setFloatValue(Float.fromBits(attr.typedValueData))
                            .build()
                    )
                    .build()
            }
            TYPE_DIMENSION -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setDimensionValue(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_FRACTION -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setFractionValue(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_INT_COLOR_ARGB8 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorArgb8Value(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_INT_COLOR_RGB8 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorRgb8Value(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_INT_COLOR_ARGB4 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorArgb4Value(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            TYPE_INT_COLOR_RGB4 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorRgb4Value(attr.typedValueData)
                            .build()
                    )
                    .build()
            }
            else -> {

                null
            }
        }
    }

    private fun formatRawValue(attr: AxmlReader.Attribute): String {
        attr.rawValue?.takeIf { it.isNotEmpty() }?.let { return it }
        val data = attr.typedValueData
        val formatted = when (attr.typedValueType) {
            TYPE_NULL -> "@null"
            TYPE_REFERENCE -> if (data == 0) "@null" else "@" + Integer.toHexString(data)
            TYPE_ATTRIBUTE -> if (data == 0) "@null" else "?" + Integer.toHexString(data)
            TYPE_STRING -> attr.rawValue ?: ""
            TYPE_INT_DEC -> data.toString()
            TYPE_INT_HEX -> "0x" + Integer.toHexString(data)
            TYPE_INT_BOOLEAN -> if (data != 0) "true" else "false"
            TYPE_FLOAT -> Float.fromBits(data).toString()
            TYPE_INT_COLOR_ARGB8 -> "#%08x".format(data)
            TYPE_INT_COLOR_RGB8 -> "#%06x".format(data and 0x00FFFFFF)
            TYPE_INT_COLOR_ARGB4 -> "#%04x".format(data and 0x0000FFFF)
            TYPE_INT_COLOR_RGB4 -> "#%03x".format(data and 0x00000FFF)

            TYPE_DIMENSION, TYPE_FRACTION -> "0x" + Integer.toHexString(data)
            else -> "@null"
        }

        return formatted.ifEmpty { "@null" }
    }

    private fun linePosition(lineNumber: Int): Resources.SourcePosition {
        return Resources.SourcePosition.newBuilder()
            .setLineNumber(lineNumber)
            .setColumnNumber(0)
            .build()
    }
}
