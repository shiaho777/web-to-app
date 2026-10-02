package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.BgmShellItem
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.data.model.MultiWebSite
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ApkArtifactVerifierTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `web app requires app config entry`() {
        val apk = createApk("assets/other.txt")

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Web",
                        packageName = "com.example.web",
                        targetUrl = "https://example.com",
                        appType = "WEB"
                    )
                ),
                encryptionEnabled = false
            )
        )

        assertThat(result.passed).isFalse()
        assertThat(result.issues.single().key).isEqualTo("config")
    }

    @Test
    fun `encrypted html app requires encrypted config and encrypted entry file`() {
        val index = temp.newFile("index.html").apply {
            writeText("<html></html>")
        }
        val apk = createApk(
            ApkTemplate.CONFIG_PATH,
            "${ApkTemplate.CONFIG_PATH}.enc",
            "assets/html/index.html.enc"
        )

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "HTML",
                        packageName = "com.example.html",
                        targetUrl = "",
                        appType = "HTML"
                    ),
                    html = HtmlBlock(entryFile = "index.html")
                ),
                encryptionEnabled = true,
                htmlFiles = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML))
            )
        )

        assertThat(result.passed).isTrue()
        assertThat(result.checkedEntryCount).isEqualTo(3)
    }

    @Test
    fun `encrypted bgm export requires encrypted track and lyric assets`() {
        val apk = createApk(
            ApkTemplate.CONFIG_PATH,
            "${ApkTemplate.CONFIG_PATH}.enc",
            "assets/bgm/bgm_0.mp3.enc",
            "assets/bgm/bgm_0.lrc.enc"
        )

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Music",
                        packageName = "com.example.music",
                        targetUrl = "https://example.com",
                        appType = "WEB"
                    ),
                    bgm = BgmBlock(
                        enabled = true,
                        playlist = listOf(
                            BgmShellItem(
                                id = "track-1",
                                name = "Track",
                                assetPath = "bgm/bgm_0.mp3",
                                lrcAssetPath = "bgm/bgm_0.lrc"
                            )
                        )
                    )
                ),
                encryptionEnabled = true
            )
        )

        assertThat(result.passed).isTrue()
        assertThat(result.checkedEntryCount).isEqualTo(4)
    }

    @Test
    fun `gallery app reports missing embedded item`() {
        val media = temp.newFile("photo.png").apply {
            writeText("png")
        }
        val apk = createApk(ApkTemplate.CONFIG_PATH)

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Gallery",
                        packageName = "com.example.gallery",
                        targetUrl = "",
                        appType = "GALLERY"
                    )
                ),
                encryptionEnabled = false,
                galleryItems = listOf(
                    GalleryItem(
                        path = media.absolutePath,
                        type = GalleryItemType.IMAGE,
                        name = "Photo"
                    )
                )
            )
        )

        assertThat(result.passed).isFalse()
        assertThat(result.issues.map { it.key }).contains("galleryItems[0]")
    }

    fun `frontend app with saved html files passes without project directory`() {
        val index = temp.newFile("index.html").apply {
            writeText("<html></html>")
        }
        val apk = createApk(
            ApkTemplate.CONFIG_PATH,
            "assets/html/index.html"
        )

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Frontend",
                        packageName = "com.example.frontend",
                        targetUrl = "",
                        appType = "FRONTEND"
                    ),
                    html = HtmlBlock(entryFile = "index.html")
                ),
                encryptionEnabled = false,
                htmlFiles = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML))
            )
        )

        assertThat(result.passed).isTrue()
        assertThat(result.checkedEntryCount).isEqualTo(2)
    }

    @Test
    fun `multi web verification requires embedded local site file`() {
        val projectDir = temp.newFolder("multi-web-project")
        File(projectDir, "site-a/index.html").apply {
            parentFile?.mkdirs()
            writeText("<html>ok</html>")
        }
        val apk = createApk(
            ApkTemplate.CONFIG_PATH,
            "assets/html_projects/site-a/index.html"
        )

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Multi Web",
                        packageName = "com.example.multiweb",
                        targetUrl = "",
                        appType = "MULTI_WEB"
                    )
                ),
                encryptionEnabled = false,
                multiWebSites = listOf(
                    MultiWebSite(
                        id = "site-a",
                        name = "Local",
                        type = "LOCAL",
                        localFilePath = "site-a/index.html"
                    )
                ),
                multiWebProjectDir = projectDir
            )
        )

        assertThat(result.passed).isTrue()
    }

    @Test
    fun `multi web existing site verifies per-site embedded assets`() {
        // EXISTING site referencing a standalone HTML project: the embedder
        // writes the source project under assets/multiweb_sites/<siteId>/html/
        // and the site-id prefix of localFilePath is host-side only.
        val sourceDir = temp.newFolder("source-project")
        File(sourceDir, "index.html").writeText("<html>ok</html>")
        val apk = createApk(
            ApkTemplate.CONFIG_PATH,
            "assets/multiweb_sites/site-a/html/index.html"
        )

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Multi Web",
                        packageName = "com.example.multiweb",
                        targetUrl = "",
                        appType = "MULTI_WEB"
                    )
                ),
                encryptionEnabled = false,
                multiWebSites = listOf(
                    MultiWebSite(
                        id = "site-a",
                        name = "Existing",
                        type = "EXISTING",
                        localFilePath = "site-a/index.html",
                        sourceProjectId = "source-project",
                        appType = "HTML"
                    )
                ),
                multiWebSiteSourceDirs = mapOf("site-a" to sourceDir)
            )
        )

        assertThat(result.passed).isTrue()
    }

    @Test
    fun `multi web html site falls back to shared project dir embed`() {
        // HTML site without a live source project: the embedder packs the
        // shared multi-web project dir under the same per-site prefix, keeping
        // the full project-relative localFilePath.
        val projectDir = temp.newFolder("multi-web-project")
        File(projectDir, "site-a/index.html").apply {
            parentFile?.mkdirs()
            writeText("<html>ok</html>")
        }
        val apk = createApk(
            ApkTemplate.CONFIG_PATH,
            "assets/multiweb_sites/site-a/html/site-a/index.html"
        )

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Multi Web",
                        packageName = "com.example.multiweb",
                        targetUrl = "",
                        appType = "MULTI_WEB"
                    )
                ),
                encryptionEnabled = false,
                multiWebSites = listOf(
                    MultiWebSite(
                        id = "site-a",
                        name = "Inline",
                        type = "INLINE_HTML",
                        localFilePath = "site-a/index.html",
                        appType = "HTML"
                    )
                ),
                multiWebProjectDir = projectDir
            )
        )

        assertThat(result.passed).isTrue()
    }

    @Test
    fun `multi web html site reports missing per-site embedded file`() {
        val sourceDir = temp.newFolder("source-project")
        File(sourceDir, "index.html").writeText("<html>ok</html>")
        val apk = createApk(ApkTemplate.CONFIG_PATH)

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Multi Web",
                        packageName = "com.example.multiweb",
                        targetUrl = "",
                        appType = "MULTI_WEB"
                    )
                ),
                encryptionEnabled = false,
                multiWebSites = listOf(
                    MultiWebSite(
                        id = "site-a",
                        name = "Existing",
                        type = "EXISTING",
                        localFilePath = "site-a/index.html",
                        sourceProjectId = "source-project",
                        appType = "HTML"
                    )
                ),
                multiWebSiteSourceDirs = mapOf("site-a" to sourceDir)
            )
        )

        assertThat(result.passed).isFalse()
        assertThat(result.issues.map { it.key }).contains("multiWebSites[0]")
    }

    @Test
    fun `multi web url site needs no local assets`() {
        val apk = createApk(ApkTemplate.CONFIG_PATH)

        val result = ApkArtifactVerifier.verify(
            ApkArtifactVerificationRequest(
                apkFile = apk,
                config = ApkConfig(
                    meta = MetaBlock(
                        appName = "Multi Web",
                        packageName = "com.example.multiweb",
                        targetUrl = "",
                        appType = "MULTI_WEB"
                    )
                ),
                encryptionEnabled = false,
                multiWebSites = listOf(
                    MultiWebSite(
                        id = "site-url",
                        name = "URL",
                        type = "URL",
                        url = "https://example.com",
                        appType = "WEB"
                    )
                )
            )
        )

        assertThat(result.passed).isTrue()
    }

    private fun createApk(vararg entries: String): File {
        val apk = temp.newFile("artifact-${System.nanoTime()}.apk")
        ZipOutputStream(apk.outputStream()).use { zipOut ->
            entries.forEach { name ->
                zipOut.putNextEntry(ZipEntry(name))
                zipOut.write("data:$name".toByteArray())
                zipOut.closeEntry()
            }
        }
        return apk
    }

    private fun createApkWithContent(vararg entries: Pair<String, ByteArray>): File {
        val apk = temp.newFile("artifact-${System.nanoTime()}.apk")
        ZipOutputStream(apk.outputStream()).use { zipOut ->
            entries.forEach { (name, content) ->
                zipOut.putNextEntry(ZipEntry(name))
                zipOut.write(content)
                zipOut.closeEntry()
            }
        }
        return apk
    }
}
