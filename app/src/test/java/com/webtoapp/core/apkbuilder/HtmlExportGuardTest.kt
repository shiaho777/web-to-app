package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.data.model.MediaConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.ZipProjectImporter
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HtmlExportGuardTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun htmlFile(name: String, sizeBytes: Int): HtmlFile {
        val file = temp.newFile(name).apply {
            writeBytes(ByteArray(sizeBytes))
        }
        return HtmlFile(name, file.absolutePath, HtmlFileType.HTML)
    }

    @Test
    fun `small project stays within the inline size limit`() {
        val files = listOf(htmlFile("index.html", 1024), htmlFile("app.js", 2048))
        assertThat(exceedsInlineSizeLimit(files)).isFalse()
    }

    @Test
    fun `single oversized file exceeds the inline size limit`() {
        val files = listOf(htmlFile("big.html", 3 * 1024 * 1024))
        assertThat(exceedsInlineSizeLimit(files)).isTrue()
    }

    @Test
    fun `large total content exceeds the inline size limit`() {
        val files = listOf(
            htmlFile("a.html", 2 * 1024 * 1024),
            htmlFile("b.html", 2 * 1024 * 1024),
            htmlFile("c.html", 2 * 1024 * 1024),
            htmlFile("d.html", 1024)
        )
        assertThat(exceedsInlineSizeLimit(files)).isTrue()
    }

    @Test
    fun `missing files do not count toward the inline size limit`() {
        val ghost = HtmlFile("ghost.html", File(temp.root, "does-not-exist.html").absolutePath, HtmlFileType.HTML)
        assertThat(exceedsInlineSizeLimit(listOf(ghost))).isFalse()
    }

    @Test
    fun `web content exclusion list is shared by import and export`() {
        val shared = ZipProjectImporter.WEB_CONTENT_EXCLUDED_DIRS
        assertThat(shared).containsAtLeast(
            "node_modules", ".git", ".cache", "__pycache__", ".next", ".nuxt"
        )
        assertThat(RuntimeAssetEmbedder.frontendConfig().excludeDirs).isEqualTo(shared)
        assertThat(RuntimeAssetEmbedder.multiWebConfig().excludeDirs).isEqualTo(shared)
    }

    @Test
    fun `remote media reference detected for image and video sources`() {
        val remoteImage = WebApp(
            name = "img", url = "", appType = AppType.IMAGE,
            mediaConfig = MediaConfig(mediaPath = "https://cdn.example.com/a.png")
        )
        assertThat(remoteMediaReference(remoteImage)).isEqualTo("https://cdn.example.com/a.png")

        val remoteVideoViaUrl = WebApp(
            name = "vid", url = "http://cdn.example.com/v.mp4", appType = AppType.VIDEO
        )
        assertThat(remoteMediaReference(remoteVideoViaUrl)).isEqualTo("http://cdn.example.com/v.mp4")
    }

    @Test
    fun `local media and non-media sources have no remote reference`() {
        val localImage = WebApp(
            name = "img", url = "", appType = AppType.IMAGE,
            mediaConfig = MediaConfig(mediaPath = "/storage/a.png")
        )
        assertThat(remoteMediaReference(localImage)).isNull()

        val webApp = WebApp(name = "web", url = "https://example.com", appType = AppType.WEB)
        assertThat(remoteMediaReference(webApp)).isNull()

        val emptyMedia = WebApp(name = "img", url = "", appType = AppType.IMAGE)
        assertThat(remoteMediaReference(emptyMedia)).isNull()
    }
}
