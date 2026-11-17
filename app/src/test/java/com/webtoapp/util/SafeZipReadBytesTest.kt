package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class SafeZipReadBytesTest {

    private fun zipOf(name: String, body: ByteArray, declareSize: Long? = null): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            val entry = ZipEntry(name)
            if (declareSize != null) entry.size = declareSize
            zos.putNextEntry(entry)
            zos.write(body)
            zos.closeEntry()
        }
        return out.toByteArray()
    }

    @Test
    fun `readBytes returns the full entry when under the cap`() {
        val payload = "hello plugin".toByteArray()
        val guard = SafeZip.EntryGuard(maxTotalBytes = 1024)
        ZipInputStream(ByteArrayInputStream(zipOf("a.txt", payload))).use { zis ->
            zis.nextEntry
            assertThat(guard.readBytes(zis, 1024)).isEqualTo(payload)
        }
    }

    @Test
    fun `readBytes aborts on actual bytes even when header declares zero`() {
        val bomb = ByteArray(4 * 1024 * 1024) { 'x'.code.toByte() }
        val archive = zipOf("bomb.bin", bomb, declareSize = 0)
        val guard = SafeZip.EntryGuard(maxTotalBytes = 64 * 1024)
        ZipInputStream(ByteArrayInputStream(archive)).use { zis ->
            zis.nextEntry
            val ex = assertThrows(SafeZip.ZipBombException::class.java) {
                guard.readBytes(zis, 64 * 1024)
            }
            assertThat(ex.kind).isEqualTo(SafeZip.ZipBombException.Kind.TOTAL_SIZE)
        }
    }

    @Test
    fun `readBytes aborts a highly compressible bomb past the cap`() {
        val zeros = ByteArray(8 * 1024 * 1024)
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            zos.setLevel(Deflater.BEST_COMPRESSION)
            val entry = ZipEntry("zeros.bin")
            entry.size = 0
            zos.putNextEntry(entry)
            zos.write(zeros)
            zos.closeEntry()
        }
        val guard = SafeZip.EntryGuard(maxTotalBytes = 128 * 1024)
        ZipInputStream(ByteArrayInputStream(out.toByteArray())).use { zis ->
            zis.nextEntry
            assertThrows(SafeZip.ZipBombException::class.java) {
                guard.readBytes(zis, 128 * 1024)
            }
        }
    }

    @Test
    fun `readBytes enforces the cumulative guard across entries`() {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            repeat(4) { i ->
                zos.putNextEntry(ZipEntry("f$i.bin"))
                zos.write(ByteArray(40 * 1024) { 'a'.code.toByte() })
                zos.closeEntry()
            }
        }
        val guard = SafeZip.EntryGuard(maxTotalBytes = 100 * 1024)
        ZipInputStream(ByteArrayInputStream(out.toByteArray())).use { zis ->
            var entry = zis.nextEntry
            var thrown = false
            while (entry != null) {
                guard.onEntry()
                try {
                    guard.readBytes(zis, 100 * 1024)
                } catch (e: SafeZip.ZipBombException) {
                    thrown = true
                    break
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            assertThat(thrown).isTrue()
        }
    }
}