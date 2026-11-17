package com.webtoapp.core.plugin

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PluginImporterZipBombTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun writeZip(name: String, build: (ZipOutputStream) -> Unit): Uri {
        val file = File(context.cacheDir, name)
        ZipOutputStream(file.outputStream().buffered()).use(build)
        return Uri.fromFile(file)
    }

    @Test
    fun `unzipPackage rejects a bomb declaring zero size`() {
        val uri = writeZip("bomb_${System.nanoTime()}.hcj") { zos ->
            val entry = ZipEntry(PluginStore.MANIFEST_FILE)
            entry.size = 0
            zos.putNextEntry(entry)
            zos.write(ByteArray(16 * 1024 * 1024) { 'x'.code.toByte() })
            zos.closeEntry()
        }
        assertThrows(com.webtoapp.util.SafeZip.ZipBombException::class.java) {
            PluginImporter(context).unzipPackage(uri)
        }
    }

    @Test
    fun `unzipPackage imports a small valid package`() {
        val manifest = """{"name":"demo","kind":"hcj"}"""
        val uri = writeZip("ok_${System.nanoTime()}.hcj") { zos ->
            zos.putNextEntry(ZipEntry(PluginStore.MANIFEST_FILE))
            zos.write(manifest.toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("main.js"))
            zos.write("console.log(1)".toByteArray())
            zos.closeEntry()
        }
        val files = PluginImporter(context).unzipPackage(uri)
        assertThat(files).isNotNull()
        assertThat(files!![PluginStore.MANIFEST_FILE]).isEqualTo(manifest)
        assertThat(files["main.js"]).isEqualTo("console.log(1)")
    }

    @Test
    fun `unzipPackage strips a single wrapping root folder`() {
        val manifest = """{"name":"demo"}"""
        val uri = writeZip("root_${System.nanoTime()}.hcj") { zos ->
            zos.putNextEntry(ZipEntry("demo/" + PluginStore.MANIFEST_FILE))
            zos.write(manifest.toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("demo/main.js"))
            zos.write("x".toByteArray())
            zos.closeEntry()
        }
        val files = PluginImporter(context).unzipPackage(uri)
        assertThat(files).isNotNull()
        assertThat(files!!.containsKey(PluginStore.MANIFEST_FILE)).isTrue()
        assertThat(files.containsKey("main.js")).isTrue()
    }
}