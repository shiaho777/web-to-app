package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipAlignerTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `align stores native libraries at 16KB data offsets`() {
        val input = temp.newFile("input.apk")
        val output = temp.newFile("output.apk")
        ZipOutputStream(input.outputStream()).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("assets/a.txt"))
            zipOut.write("hello".toByteArray())
            zipOut.closeEntry()
            val data = ByteArray(128) { it.toByte() }
            val entry = ZipEntry("lib/arm64-v8a/libnode.so")
            entry.method = ZipEntry.STORED
            entry.size = data.size.toLong()
            entry.compressedSize = data.size.toLong()
            entry.crc = CRC32().apply { update(data) }.value
            zipOut.putNextEntry(entry)
            zipOut.write(data)
            zipOut.closeEntry()
        }

        val aligned = ZipAligner.align(input, output)

        assertThat(aligned).isTrue()
        assertThat(ZipAligner.verifyNativeLibAlignment(output)).isTrue()
    }

    /**
     * Regression: entries written by java.util.zip with unknown sizes (putNextEntry
     * without size/crc — every DEFLATED entry this pipeline writes) carry bit-3 and
     * compressedSize=0 in the local file header. The old forward LFH scan advanced
     * into the compressed stream on such an entry and bailed out reporting
     * "verified" without ever reaching the lib/ entries.
     */
    @Test
    fun `verify detects misaligned native lib behind data-descriptor entries`() {
        val apk = temp.newFile("untested.apk")
        ZipOutputStream(apk.outputStream()).use { zipOut ->
            // DEFLATED with unknown sizes → bit-3 data descriptor + LFH compressedSize=0.
            zipOut.putNextEntry(ZipEntry("META-INF/com/android/build/gradle/app-metadata.properties"))
            zipOut.write(ByteArray(4_096) { (it % 251).toByte() })
            zipOut.closeEntry()
            zipOut.putNextEntry(ZipEntry("classes.dex"))
            zipOut.write(ByteArray(8_192) { (it % 241).toByte() })
            zipOut.closeEntry()

            // STORED native lib landing at a misaligned offset (no padding extra).
            val data = ByteArray(100) { it.toByte() }
            val entry = ZipEntry("lib/arm64-v8a/libtest.so")
            entry.method = ZipEntry.STORED
            entry.size = data.size.toLong()
            entry.compressedSize = data.size.toLong()
            entry.crc = CRC32().apply { update(data) }.value
            zipOut.putNextEntry(entry)
            zipOut.write(data)
            zipOut.closeEntry()
        }

        // The lib entry above was written without alignment padding, so it must be
        // reported as misaligned — the old scan returned true here without looking.
        assertThat(ZipAligner.verifyNativeLibAlignment(apk)).isFalse()

        val output = temp.newFile("realigned.apk")
        assertThat(ZipAligner.align(apk, output)).isTrue()
        assertThat(ZipAligner.verifyNativeLibAlignment(output)).isTrue()
    }

    /**
     * Regression: the shell template is built with packaging.jniLibs.useLegacyPackaging=true,
     * so every template lib/ entry is DEFLATED and the manifest ships extractNativeLibs=true.
     * PackageManager extracts such libs to nativeLibraryDir at install — the zip data offset
     * is never mmap'd — therefore a compressed lib has no alignment requirement. Failing on
     * stored=false here made EVERY export abort after the central-directory verifier started
     * actually reaching lib/ (issue: "Native lib is not 16KB zip-aligned ... stored=false").
     * Only STORED libs (injected runtimes like libnode.so) can be mapped in place and must
     * sit on the 16KB boundary.
     */
    @Test
    fun `verify accepts deflated native libs that are extracted at install`() {
        val apk = temp.newFile("deflated-lib.apk")
        ZipOutputStream(apk.outputStream()).use { zipOut ->
            // DEFLATED via java.util.zip: bit-3 data descriptor, LFH compressedSize=0 —
            // exactly what the AGP-built template carries.
            zipOut.putNextEntry(ZipEntry("lib/arm64-v8a/libandroidx.graphics.path.so"))
            zipOut.write(ByteArray(4_096) { (it % 251).toByte() })
            zipOut.closeEntry()
            zipOut.putNextEntry(ZipEntry("lib/arm64-v8a/libc++_shared.so"))
            zipOut.write(ByteArray(8_192) { (it % 241).toByte() })
            zipOut.closeEntry()
        }

        assertThat(ZipAligner.verifyNativeLibAlignment(apk)).isTrue()
    }
}
