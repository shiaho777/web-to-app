package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile
import org.junit.Test

class ArscRebuilderLauncherBgTest {

    private val templateApk: File by lazy {
        resolveFile(
            "app/src/main/assets/template/webview_shell.apk",
            "src/main/assets/template/webview_shell.apk"
        )
    }

    private fun readTemplateArsc(): ByteArray {
        assertThat(templateApk.exists()).isTrue()
        return ZipFile(templateApk).use { it.getInputStream(it.getEntry("resources.arsc")).readBytes() }
    }

    @Test
    fun `original launcher background entry value is a black color int`() {
        val original = readTemplateArsc()
        val entry = findColorLauncherBackgroundEntry(original)

        assertThat(entry).isNotNull()
        assertThat(entry!!.vType).isEqualTo(0x1d)
        assertThat(entry.vData).isEqualTo(0xff000000.toInt())
    }

    @Test
    fun `rebuild converts launcher background entry to drawable string reference`() {
        val original = readTemplateArsc()
        val rebuilder = ArscRebuilder()
        val rebuilt = rebuilder.rebuildWithNewAppNameAndIcons(original, "TestApp", replaceIcons = true)

        assertThat(rebuilt.size).isGreaterThan(0)

        val entry = findColorLauncherBackgroundEntry(rebuilt)
        assertThat(entry).isNotNull()

        entry!!.let {
            assertThat(it.vType)
                .isEqualTo(0x03)
            assertThat(it.vData).isAtLeast(0)
        }
    }

    @Test
    fun `rebuild leaves the launcher background drawable string index in valid range`() {
        val original = readTemplateArsc()
        val rebuilder = ArscRebuilder()
        val rebuilt = rebuilder.rebuildWithNewAppNameAndIcons(original, "TestApp", replaceIcons = true)

        val entry = findColorLauncherBackgroundEntry(rebuilt)!!
        val scount = readI32(rebuilt, 8 + 8)

        assertThat(entry.vData).isIn(0 until scount)
    }

    private data class EntryValue(val valuePos: Int, val vType: Int, val vData: Int)

    private fun findColorLauncherBackgroundEntry(arsc: ByteArray): EntryValue? {
        val total = arsc.size
        val buf = ByteBuffer.wrap(arsc).order(ByteOrder.LITTLE_ENDIAN)

        var i = 0
        while (i < total - 20) {
            if (arsc[i] == 0x01.toByte() && arsc[i + 1] == 0x02.toByte()) {
                val ch = readU16(arsc, i + 2)
                val csz = readI32(arsc, i + 4)
                val typeId = arsc[i + 8].toInt() and 0xFF
                val typeFlags = arsc[i + 9].toInt() and 0xFF
                val entryCount = readI32(arsc, i + 12)
                val entriesStart = readI32(arsc, i + 16)
                val configSize = readI32(arsc, i + 20)

                if (isValidTypeChunk(ch, csz, total - i, typeId, typeFlags, entryCount, configSize) &&
                    typeId == 0x05
                ) {
                    val targetIdx = 0x71
                    if (targetIdx < entryCount) {
                        val entryOff = readI32(arsc, i + ch + targetIdx * 4)
                        if (entryOff != -1 && entryOff > 0) {
                            val entryPos = i + entriesStart + entryOff
                            if (entryPos + 12 <= total) {
                                val flags = readU16(arsc, entryPos + 2)
                                if (flags and 1 == 0) {
                                    val valuePos = entryPos + 8
                                    val vType = arsc[valuePos + 3].toInt() and 0xFF
                                    val vData = readI32(arsc, valuePos + 4)
                                    return EntryValue(valuePos, vType, vData)
                                }
                            }
                        }
                    }
                }
            }
            i += 1
        }
        return null
    }

    private fun isValidTypeChunk(
        ch: Int, csz: Int, remaining: Int, typeId: Int, typeFlags: Int, entryCount: Int, configSize: Int
    ): Boolean = ch in 16..96 && csz in 1..remaining && typeId in 1..0x10 &&
        typeFlags == 0 && entryCount in 1 until 100000 && configSize in 8..128

    private fun readU16(d: ByteArray, o: Int) = (d[o].toInt() and 0xFF) or ((d[o + 1].toInt() and 0xFF) shl 8)
    private fun readI32(d: ByteArray, o: Int) =
        (d[o].toInt() and 0xFF) or ((d[o + 1].toInt() and 0xFF) shl 8) or
            ((d[o + 2].toInt() and 0xFF) shl 16) or ((d[o + 3].toInt() and 0xFF) shl 24)

    private fun resolveFile(vararg candidates: String): File {
        for (c in candidates) {
            val f = File(c)
            if (f.exists()) return f
            val f2 = File(System.getProperty("user.dir"), c)
            if (f2.exists()) return f2
        }
        return File(candidates.first())
    }
}
