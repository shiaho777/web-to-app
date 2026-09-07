package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipFile
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Pins the launcher-background rewrite done by [ArscRebuilder]: the template ships
 * `color/ic_launcher_background` as a black color int, and a rebuild with icon
 * replacement converts it into a drawable string reference so the generated app's
 * adaptive icons get the patched `res/ic_launcher_bg.png`.
 *
 * Localization goes through [ArscRebuilder.findLauncherBackgroundEntry] — the same
 * by-name lookup the runtime uses. An earlier revision scanned for the entry at a
 * hardcoded index (0x71) inside the color type chunk, which silently assumed a fixed
 * resource-merge order; adding the Credential Manager libraries shifted the indices
 * and broke the test without any functional change.
 */
class ArscRebuilderLauncherBgTest {

    private val templateApk: File by lazy {
        resolveFile(
            "src/main/assets/template/webview_shell.apk",
            "app/src/main/assets/template/webview_shell.apk"
        )
    }

    private fun assumeTemplateBuilt() {
        assumeTrue(
            "shell template not built — run ':app:syncShellTemplateApk' first",
            templateApk.exists()
        )
    }

    private fun readTemplateArsc(): ByteArray {
        assumeTemplateBuilt()
        return ZipFile(templateApk).use { it.getInputStream(it.getEntry("resources.arsc")).readBytes() }
    }

    @Test
    fun `original launcher background entry value is a black color int`() {
        val original = readTemplateArsc()
        val entry = ArscRebuilder().findLauncherBackgroundEntry(original)

        assertThat(entry).isNotNull()
        assertThat(entry!![1]).isEqualTo(0x1d)
        assertThat(entry[2]).isEqualTo(0xff000000.toInt())
    }

    @Test
    fun `rebuild converts launcher background entry to drawable string reference`() {
        val original = readTemplateArsc()
        val rebuilder = ArscRebuilder()
        val rebuilt = rebuilder.rebuildWithNewAppNameAndIcons(original, "TestApp", replaceIcons = true)

        assertThat(rebuilt.size).isGreaterThan(0)

        val entry = ArscRebuilder().findLauncherBackgroundEntry(rebuilt)
        assertThat(entry).isNotNull()

        entry!!.let {
            assertThat(it[1]).isEqualTo(0x03)
            assertThat(it[2]).isAtLeast(0)
        }
    }

    @Test
    fun `rebuild leaves the launcher background drawable string index in valid range`() {
        val original = readTemplateArsc()
        val rebuilder = ArscRebuilder()
        val rebuilt = rebuilder.rebuildWithNewAppNameAndIcons(original, "TestApp", replaceIcons = true)

        val entry = ArscRebuilder().findLauncherBackgroundEntry(rebuilt)!!
        // The global string pool sits right after the 12-byte table header
        // (ResTable_header): its count is at offset 8 + 8.
        val scount = readI32(rebuilt, 8 + 8)

        assertThat(entry[2]).isIn(0 until scount)
    }

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
