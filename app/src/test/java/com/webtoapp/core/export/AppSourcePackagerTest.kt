package com.webtoapp.core.export

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.WebApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

/**
 * Source zip for #1138: runtime config, local project files, and no
 * node_modules or files reached through a link that leaves the project.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class AppSourcePackagerTest {

    private val context: Application
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `web app zip contains the runtime config and network trust file`() {
        val app = WebApp(name = "Demo", url = "https://example.com/app")

        val entries = zipEntries(AppSourcePackager(context).pack(app))

        assertThat(entries).containsKey("README.md")
        assertThat(entries.getValue("README.md")).contains("Demo")
        assertThat(entries).containsKey("app_config.json")
        assertThat(entries.getValue("app_config.json")).contains("https://example.com/app")
        assertThat(entries.getValue("app_config.json")).contains("com.w2a.")
        assertThat(entries.getValue("network_security_config.xml")).contains("network-security-config")
        assertThat(entries.keys).doesNotContain("app_config_error.txt")
    }

    @Test
    fun `html project is copied and node_modules plus escaped links are omitted`() {
        val project = File(context.filesDir, "html_projects/p1")
        File(project, "index.html").apply {
            parentFile?.mkdirs()
            writeText("<h1>hi</h1>")
        }
        File(project, "js/app.js").apply {
            parentFile?.mkdirs()
            writeText("console.log(1)")
        }
        File(project, "node_modules/pkg/index.js").apply {
            parentFile?.mkdirs()
            writeText("skip-me")
        }
        val outside = File(context.cacheDir, "secret.txt").apply { writeText("secret-bytes") }
        Files.createSymbolicLink(File(project, "leak.txt").toPath(), outside.toPath())

        val app = WebApp(
            name = "Page",
            url = "https://example.com/page",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(projectId = "p1", entryFile = "index.html")
        )

        val entries = zipEntries(AppSourcePackager(context).pack(app))

        assertThat(entries.getValue("content/project/index.html")).isEqualTo("<h1>hi</h1>")
        assertThat(entries.getValue("content/project/js/app.js")).isEqualTo("console.log(1)")
        assertThat(entries.keys.none { it.contains("node_modules") }).isTrue()
        assertThat(entries.values).doesNotContain("secret-bytes")
        assertThat(entries.values).doesNotContain("skip-me")
    }

    @Test
    fun `gallery item file is included once`() {
        val image = File(context.cacheDir, "shot.png").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val app = WebApp(
            name = "Album",
            url = "https://example.com/album",
            appType = AppType.GALLERY,
            galleryConfig = GalleryConfig(
                items = listOf(
                    GalleryItem(path = image.absolutePath, type = GalleryItemType.IMAGE, name = "shot")
                )
            )
        )

        val entries = zipEntries(AppSourcePackager(context).pack(app))
        val packed = entries.entries.filter { it.key.endsWith("shot.png") }

        assertThat(packed).hasSize(1)
        assertThat(packed.single().value).isEqualTo("\u0001\u0002\u0003\u0004")
    }

    private fun zipEntries(file: File): Map<String, String> {
        val out = linkedMapOf<String, String>()
        ZipFile(file).use { zip ->
            zip.entries().asIterator().forEach { entry ->
                if (!entry.isDirectory) {
                    out[entry.name] = zip.getInputStream(entry).bufferedReader().readText()
                }
            }
        }
        return out
    }
}
