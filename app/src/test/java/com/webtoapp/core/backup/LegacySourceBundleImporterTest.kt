package com.webtoapp.core.backup

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.export.AppSourcePackager
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.BgmItem
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Legacy source bundles (pre-#1522, no apps.json) must restore through the
 * runtime config they carry. The fixture strips the v5 payload from a current
 * pack, leaving exactly the old format.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class LegacySourceBundleImporterTest {

    private val context: Application
        get() = ApplicationProvider.getApplicationContext()

    private fun legacyZip(app: WebApp): File {
        val packed = AppSourcePackager(context).pack(app)
        val legacy = File(context.cacheDir, "legacy_${System.nanoTime()}.zip")
        ZipFile(packed).use { input ->
            ZipOutputStream(FileOutputStream(legacy)).use { output ->
                input.entries().asIterator().forEach { entry ->
                    val name = entry.name
                    if (entry.isDirectory || name == "apps.json" ||
                        name.startsWith("resources/") || name.startsWith("local/")
                    ) {
                        return@forEach
                    }
                    output.putNextEntry(java.util.zip.ZipEntry(name))
                    input.getInputStream(entry).use { it.copyTo(output) }
                    output.closeEntry()
                }
            }
        }
        return legacy
    }

    private fun restore(app: WebApp): LegacySourceBundleImporter.Restored =
        FileInputStream(legacyZip(app)).use { LegacySourceBundleImporter(context).restore(it) }

    @Test
    fun `web app config and loose media are restored`() {
        val icon = File(context.cacheDir, "icon.png").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val splash = File(context.cacheDir, "splash.png").apply { writeBytes(byteArrayOf(4, 5, 6)) }
        val track = File(context.cacheDir, "track.mp3").apply { writeBytes(byteArrayOf(7, 8, 9)) }
        val app = WebApp(
            name = "Old Web",
            url = "https://old.example.com",
            iconPath = icon.absolutePath,
            webViewConfig = WebViewConfig(desktopMode = true, pageZoomPercent = 80),
            splashEnabled = true,
            splashConfig = SplashConfig(type = SplashType.IMAGE, mediaPath = splash.absolutePath),
            bgmEnabled = true,
            bgmConfig = BgmConfig(playlist = listOf(BgmItem(name = "Track", path = track.absolutePath)))
        )

        val restored = restore(app)
        val result = restored.app

        assertThat(restored.missingProjectFiles).isFalse()
        assertThat(result.name).isEqualTo("Old Web")
        assertThat(result.url).isEqualTo("https://old.example.com")
        assertThat(result.webViewConfig.desktopMode).isTrue()
        assertThat(result.webViewConfig.pageZoomPercent).isEqualTo(80)
        assertThat(result.iconPath).isNotNull()
        assertThat(File(result.iconPath!!).readBytes()).isEqualTo(byteArrayOf(1, 2, 3))
        assertThat(result.splashConfig?.mediaPath).isNotNull()
        assertThat(File(result.splashConfig!!.mediaPath!!).readBytes()).isEqualTo(byteArrayOf(4, 5, 6))
        assertThat(result.bgmConfig?.playlist?.single()?.path).isNotNull()
        assertThat(File(result.bgmConfig!!.playlist.single().path).readBytes()).isEqualTo(byteArrayOf(7, 8, 9))
    }

    @Test
    fun `html project files are rebuilt under a new project id`() {
        val project = File(context.filesDir, "html_projects/old-p1")
        File(project, "index.html").apply {
            parentFile?.mkdirs()
            writeText("<h1>legacy</h1>")
        }
        File(project, "js/app.js").apply {
            parentFile?.mkdirs()
            writeText("console.log('legacy')")
        }
        val app = WebApp(
            name = "Old Page",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(projectId = "old-p1", entryFile = "index.html")
        )

        val result = restore(app).app

        val projectId = result.htmlConfig?.projectId
        assertThat(projectId).isNotEmpty()
        assertThat(projectId).isNotEqualTo("old-p1")
        val restoredDir = File(context.filesDir, "html_projects/$projectId")
        assertThat(File(restoredDir, "index.html").readText()).isEqualTo("<h1>legacy</h1>")
        assertThat(File(restoredDir, "js/app.js").readText()).isEqualTo("console.log('legacy')")
    }

    @Test
    fun `multi web sites keep urls inline html and local projects`() {
        val siteProject = File(context.filesDir, "html_projects/old-site")
        File(siteProject, "index.html").apply {
            parentFile?.mkdirs()
            writeText("<p>site project</p>")
        }
        val app = WebApp(
            name = "Old Multi",
            url = "",
            appType = AppType.MULTI_WEB,
            multiWebConfig = MultiWebConfig(
                sites = listOf(
                    MultiWebSite(id = "s1", name = "Remote", url = "https://remote.example.com"),
                    MultiWebSite(id = "s2", name = "Inline", type = "INLINE_HTML", inlineHtml = "<p>inline body</p>"),
                    MultiWebSite(id = "s3", name = "Project", type = "LOCAL", sourceProjectId = "old-site")
                ),
                displayMode = "DRAWER"
            )
        )

        val result = restore(app).app
        val sites = result.multiWebConfig?.sites.orEmpty()

        assertThat(result.multiWebConfig?.displayMode).isEqualTo("DRAWER")
        assertThat(sites).hasSize(3)
        assertThat(sites[0].url).isEqualTo("https://remote.example.com")
        assertThat(sites[1].inlineHtml).isEqualTo("<p>inline body</p>")
        assertThat(sites[2].sourceProjectId).isNotEmpty()
        assertThat(sites[2].sourceProjectId).isNotEqualTo("old-site")
        val restoredSiteDir = File(context.filesDir, "html_projects/${sites[2].sourceProjectId}")
        assertThat(File(restoredSiteDir, "index.html").readText()).isEqualTo("<p>site project</p>")
    }

    @Test
    fun `server runtime config restores with a missing files flag`() {
        val app = WebApp(
            name = "Old Server",
            url = "",
            appType = AppType.NODEJS_APP,
            nodejsConfig = NodeJsConfig(projectId = "old-node", entryFile = "server.js", serverPort = 3000)
        )

        val restored = restore(app)

        assertThat(restored.missingProjectFiles).isTrue()
        assertThat(restored.app.nodejsConfig?.entryFile).isEqualTo("server.js")
        assertThat(restored.app.nodejsConfig?.serverPort).isEqualTo(3000)
    }

    @Test
    fun `bundle without a runtime config is rejected`() {
        val empty = File(context.cacheDir, "empty_${System.nanoTime()}.zip")
        ZipOutputStream(FileOutputStream(empty)).use { output ->
            output.putNextEntry(java.util.zip.ZipEntry("README.md"))
            output.write("nothing".toByteArray())
            output.closeEntry()
        }

        val failure = runCatching {
            FileInputStream(empty).use { LegacySourceBundleImporter(context).restore(it) }
        }

        assertThat(failure.isFailure).isTrue()
    }
}