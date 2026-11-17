package com.webtoapp.core.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.util.SafeZip
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DataBackupManagerJsonCapTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun zipEntry(name: String, body: ByteArray): ZipInputStream {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            zos.putNextEntry(ZipEntry(name))
            zos.write(body)
            zos.closeEntry()
        }
        return ZipInputStream(ByteArrayInputStream(out.toByteArray()))
    }

    @Test
    fun `readJsonEntry returns a small json entry`() {
        val json = """{"version":1,"apps":[]}""".toByteArray()
        val manager = DataBackupManager(context)
        zipEntry("apps.json", json).use { zis ->
            zis.nextEntry
            val guard = SafeZip.EntryGuard()
            assertThat(manager.readJsonEntry(zis, guard)).isEqualTo(json)
        }
    }

    @Test
    fun `readJsonEntry aborts mid-stream past the per-entry cap`() {
        val oversized = ByteArray((64L * 1024 * 1024 + 1024).toInt()) { 'a'.code.toByte() }
        val manager = DataBackupManager(context)
        zipEntry("apps.json", oversized).use { zis ->
            zis.nextEntry
            val guard = SafeZip.EntryGuard()
            val ex = assertThrows(SafeZip.ZipBombException::class.java) {
                manager.readJsonEntry(zis, guard)
            }
            assertThat(ex.kind).isEqualTo(SafeZip.ZipBombException.Kind.TOTAL_SIZE)
        }
    }
}