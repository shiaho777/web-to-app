package com.webtoapp.core.playstore.aab.axml

internal object AxmlConstants {

    const val RES_NULL_TYPE = 0x0000
    const val RES_STRING_POOL_TYPE = 0x0001
    const val RES_TABLE_TYPE = 0x0002
    const val RES_XML_TYPE = 0x0003

    const val RES_XML_FIRST_CHUNK_TYPE = 0x0100
    const val RES_XML_START_NAMESPACE_TYPE = 0x0100
    const val RES_XML_END_NAMESPACE_TYPE = 0x0101
    const val RES_XML_START_ELEMENT_TYPE = 0x0102
    const val RES_XML_END_ELEMENT_TYPE = 0x0103
    const val RES_XML_CDATA_TYPE = 0x0104
    const val RES_XML_LAST_CHUNK_TYPE = 0x017f

    const val RES_XML_RESOURCE_MAP_TYPE = 0x0180

    const val RES_TABLE_PACKAGE_TYPE = 0x0200
    const val RES_TABLE_TYPE_TYPE = 0x0201
    const val RES_TABLE_TYPE_SPEC_TYPE = 0x0202
    const val RES_TABLE_LIBRARY_TYPE = 0x0203

    const val UTF8_FLAG = 0x00000100
    const val SORTED_FLAG = 0x00000001

    const val TYPE_NULL = 0x00
    const val TYPE_REFERENCE = 0x01
    const val TYPE_ATTRIBUTE = 0x02
    const val TYPE_STRING = 0x03
    const val TYPE_FLOAT = 0x04
    const val TYPE_DIMENSION = 0x05
    const val TYPE_FRACTION = 0x06
    const val TYPE_DYNAMIC_REFERENCE = 0x07
    const val TYPE_DYNAMIC_ATTRIBUTE = 0x08

    const val TYPE_FIRST_INT = 0x10
    const val TYPE_INT_DEC = 0x10
    const val TYPE_INT_HEX = 0x11
    const val TYPE_INT_BOOLEAN = 0x12

    const val TYPE_FIRST_COLOR_INT = 0x1c
    const val TYPE_INT_COLOR_ARGB8 = 0x1c
    const val TYPE_INT_COLOR_RGB8 = 0x1d
    const val TYPE_INT_COLOR_ARGB4 = 0x1e
    const val TYPE_INT_COLOR_RGB4 = 0x1f

    const val DATA_NULL_UNDEFINED = 0x00000000
    const val DATA_NULL_EMPTY = 0x00000001
}
