package com.webtoapp.core.extension

import android.content.Context
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExtensionFileManagerTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun cleanExtensions() {
        File(context.filesDir, "extensions").deleteRecursively()
        File(context.cacheDir, "ext_temp").deleteRecursively()
    }

    @After
    fun tearDown() {
        File(context.filesDir, "extensions").deleteRecursively()
        File(context.cacheDir, "ext_temp").deleteRecursively()
    }

    @Test
    fun `manifest key names the unpacked directory`() = runBlocking<Unit> {
        val result = ExtensionFileManager(context).importChromeExtensionFromFile(
            writeExtensionZip(withKey = true)
        )

        val imported = result as ExtensionFileManager.ImportResult.ChromeExtension
        assertThat(imported.parseResult.modules.first().chromeExtId).isEqualTo(FIXTURE_ID)
        assertThat(imported.extractedDir.name).isEqualTo(FIXTURE_ID)
        assertThat(File(imported.extractedDir, "manifest.json").isFile).isTrue()
        assertThat(File(imported.extractedDir, "content.js").isFile).isTrue()
    }

    @Test
    fun `store install uses the store id as the directory name`() = runBlocking<Unit> {
        val storeId = "abcdefghijklmnopabcdefghijklmnop"
        val result = ExtensionFileManager(context).importChromeExtensionFromFile(
            writeExtensionZip(withKey = false),
            storeId = storeId
        )

        val imported = result as ExtensionFileManager.ImportResult.ChromeExtension
        assertThat(imported.parseResult.modules.first().chromeExtId).isEqualTo(storeId)
        assertThat(imported.extractedDir.name).isEqualTo(storeId)
    }

    @Test
    fun `zip without a key keeps the generated id`() = runBlocking<Unit> {
        val result = ExtensionFileManager(context).importChromeExtensionFromFile(
            writeExtensionZip(withKey = false)
        )

        val imported = result as ExtensionFileManager.ImportResult.ChromeExtension
        val extId = imported.parseResult.modules.first().chromeExtId
        assertThat(extId).isNotEmpty()
        assertThat(imported.extractedDir.name).isEqualTo(extId)
    }

    @Test
    fun `wrapper folder is flattened into the extension root`() = runBlocking<Unit> {
        val result = ExtensionFileManager(context).importChromeExtensionFromFile(
            writeExtensionZip(withKey = true, nested = true)
        )

        val imported = result as ExtensionFileManager.ImportResult.ChromeExtension
        assertThat(imported.extractedDir.name).isEqualTo(FIXTURE_ID)
        assertThat(File(imported.extractedDir, "manifest.json").isFile).isTrue()
    }

    @Test
    fun `legacy random directory resolves through the manifest key`() {
        val legacy = File(context.filesDir, "extensions/legacy-random-dir").apply { mkdirs() }
        File(legacy, "manifest.json").writeText(manifest(withKey = true))
        File(legacy, "popup.html").writeText("<html></html>")

        val manager = ExtensionFileManager(context)

        assertThat(manager.findChromeExtensionRoot(FIXTURE_ID)?.absolutePath)
            .isEqualTo(legacy.absolutePath)
        assertThat(File(manager.findChromeExtensionRoot(FIXTURE_ID)!!, "popup.html").isFile).isTrue()
    }

    private fun writeExtensionZip(
        withKey: Boolean,
        nested: Boolean = false
    ): File {
        val zip = File(context.cacheDir, "extension-${System.nanoTime()}.zip")
        val prefix = if (nested) "wrapper/" else ""
        ZipOutputStream(zip.outputStream()).use { out ->
            fun put(name: String, content: String) {
                out.putNextEntry(ZipEntry(prefix + name))
                out.write(content.toByteArray(Charsets.UTF_8))
                out.closeEntry()
            }
            put("manifest.json", manifest(withKey))
            put("content.js", "console.log('installed')")
            put("popup.html", "<html><body>popup</body></html>")
        }
        return zip
    }

    private fun manifest(withKey: Boolean): String {
        val key = if (withKey) """"key":"$FIXTURE_KEY",""" else ""
        return """
            {
              "manifest_version": 3,
              "name": "Fixture",
              "version": "1.0",
              $key
              "action": {"default_popup": "popup.html"},
              "content_scripts": [
                {"matches": ["https://www.example.com/*"], "js": ["content.js"]}
              ]
            }
        """.trimIndent()
    }

    private companion object {
        const val FIXTURE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu4wD92pRNqYR4URtq7XxAnR9bWB7" +
                "YqY3uGP64eYFqVgX0yY5Ge+oJZ6y5c4wvepijnQZe37sYPWWXXtcSm0L/ry2Rn5LOLRbi2W" +
                "YrXHgR8SzxrwQsHu88ruM7y0OhvgN7UpH+nDfeNDk1GQmyZ8zXGCw/NzL503FiI3k5ZiK+3A" +
                "xrUjlrZnSmALbOm6GCTwSZm9UY5Wuc+WaubuhrrW82qi3oPnEFiMy4VMSsosInkskbs0A5hz" +
                "5s+tIzcqZn2YZF47krHBopbgl1Y0EShXe/GsXRBsIy00I0H+8Hry4+d2eI+g1ootX0H0/NWo" +
                "PWJglDn+z1Dwtsl//RdOS+6UxDwIDAQAB"
        const val FIXTURE_ID = "gooibnpodkckifdenemmdmpfjhcbidib"
    }
}
