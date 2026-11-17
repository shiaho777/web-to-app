package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.crypto.AssetEncryptor
import com.webtoapp.core.crypto.EncryptionConfig
import com.webtoapp.core.shell.MultiWebSiteShellConfig
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.crypto.spec.SecretKeySpec
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FrontendEmbedWiringTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val encryptor = AssetEncryptor(SecretKeySpec(ByteArray(32) { it.toByte() }, "AES"))

    private fun logger(): BuildLogger =
        BuildLogger(ApplicationProvider.getApplicationContext())

    private fun frontendConfig(appType: String = "FRONTEND"): ApkConfig = ApkConfig(
        meta = MetaBlock(
            appName = "Frontend",
            packageName = "com.example.frontend",
            targetUrl = "",
            appType = appType
        ),
        html = HtmlBlock(entryFile = "index.html")
    )

    private fun embedCtx(
        config: ApkConfig,
        projectDir: File?,
        htmlFiles: List<HtmlFile> = emptyList(),
        encryption: EncryptionConfig = EncryptionConfig.DISABLED,
        siteDirs: Map<String, File> = emptyMap(),
        hook: ((ZipOutputStream, String, File) -> Boolean)? = null
    ): EmbedContext = EmbedContext(
        config = config,
        logger = logger(),
        encryptor = if (encryption.enabled) encryptor else null,
        encryptionConfig = encryption,
        mediaContentPath = null,
        htmlFiles = htmlFiles,
        galleryItems = emptyList(),
        projectDir = projectDir,
        secondaryProjectDir = null,
        fnAddMediaContent = { _, _, _, _, _, _ -> },
        fnAddHtmlFiles = { _, _, _, _ -> 0 },
        fnAddGalleryItems = { _, _, _, _, _ -> },
        fnAddWordPressFiles = { _, _ -> },
        fnAddNodeJsFiles = { _, _ -> },
        fnAddFrontendFiles = { zo, dir, files, enc, ec ->
            RuntimeAssetEmbedder.embedProjectFiles(
                zo,
                dir,
                RuntimeAssetEmbedder.frontendConfig().copy(fileHook = hook),
                logger()
            )
        },
        fnAddFrontendFallback = { zo, files, enc, ec ->
            var count = 0
            files.forEach { f ->
                val source = File(f.path)
                val assetPath = "assets/frontend_app/${f.name}"
                if (hook != null && hook(zo, assetPath, source)) {
                    count++
                    return@forEach
                }
                zo.putNextEntry(ZipEntry(assetPath))
                zo.write(source.readBytes())
                zo.closeEntry()
                count++
            }
            count
        },
        fnAddPhpAppFiles = { _, _ -> },
        fnAddPythonAppFiles = { _, _ -> },
        fnAddGoAppFiles = { _, _ -> },
        multiWebSiteSourceDirs = siteDirs,
        projectFileHook = hook
    )

    private fun entryNames(apk: File): List<String> =
        ZipFile(apk).use { zip -> zip.entries().asSequence().map { it.name }.toList() }

    @Test
    fun `frontend project embed encrypts every file when encryption is enabled`() {
        val dir = temp.newFolder("frontend-project")
        File(dir, "index.html").writeText("<html><body>ok</body></html>")
        File(dir, "app.js").writeText("console.log(1)")
        val apk = temp.newFile("frontend-enc.apk")
        val hook = encryptionHook()

        ZipOutputStream(apk.outputStream()).use { zo ->
            val result = FrontendContentEmbedder().embed(
                zo,
                embedCtx(frontendConfig(), dir, encryption = EncryptionConfig(enabled = true), hook = hook)
            )
            assertThat(result.success).isTrue()
        }

        val names = entryNames(apk)
        assertThat(names).contains("assets/frontend_app/index.html.enc")
        assertThat(names).contains("assets/frontend_app/app.js.enc")
        assertThat(names).doesNotContain("assets/frontend_app/index.html")
    }

    @Test
    fun `frontend fallback embeds under frontend_app and encrypts when enabled`() {
        val index = temp.newFile("index.html").apply { writeText("<html></html>") }
        val files = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML))
        val apk = temp.newFile("frontend-fallback.apk")
        val hook = encryptionHook()

        ZipOutputStream(apk.outputStream()).use { zo ->
            val result = FrontendContentEmbedder().embed(
                zo,
                embedCtx(frontendConfig(), null, htmlFiles = files, encryption = EncryptionConfig(enabled = true), hook = hook)
            )
            assertThat(result.success).isTrue()
        }

        val names = entryNames(apk)
        assertThat(names).contains("assets/frontend_app/index.html.enc")
        assertThat(names).doesNotContain("assets/html/index.html")
        assertThat(names).doesNotContain("assets/html/index.html.enc")
    }

    @Test
    fun `frontend fallback without encryption stays plain under frontend_app`() {
        val index = temp.newFile("index.html").apply { writeText("<html></html>") }
        val files = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML))
        val apk = temp.newFile("frontend-fallback-plain.apk")

        ZipOutputStream(apk.outputStream()).use { zo ->
            FrontendContentEmbedder().embed(zo, embedCtx(frontendConfig(), null, htmlFiles = files))
        }

        assertThat(entryNames(apk)).contains("assets/frontend_app/index.html")
    }

    @Test
    fun `multi-web html site embed encrypts when encryption is enabled`() {
        val siteDir = temp.newFolder("site-project")
        File(siteDir, "index.html").writeText("<html>site</html>")
        val config = frontendConfig(appType = "MULTI_WEB").copy(
            multiWeb = MultiWebBlock(
                sites = listOf(
                    MultiWebSiteShellConfig(id = "site-a", name = "A", appType = "HTML")
                )
            )
        )
        val apk = temp.newFile("multiweb-enc.apk")
        val hook = encryptionHook()

        ZipOutputStream(apk.outputStream()).use { zo ->
            MultiWebContentEmbedder().embed(
                zo,
                embedCtx(config, null, encryption = EncryptionConfig(enabled = true), siteDirs = mapOf("site-a" to siteDir), hook = hook)
            )
        }

        val names = entryNames(apk)
        assertThat(names).contains("assets/multiweb_sites/site-a/html/index.html.enc")
        assertThat(names).doesNotContain("assets/multiweb_sites/site-a/html/index.html")
    }

    private fun encryptionHook(): (ZipOutputStream, String, File) -> Boolean = { zo, assetPath, file ->
        val assetName = assetPath.removePrefix("assets/")
        val entry = ZipEntry("${assetPath}.enc")
        entry.method = ZipEntry.DEFLATED
        zo.putNextEntry(entry)
        file.inputStream().buffered().use { input ->
            encryptor.encryptStreaming(input, assetName, zo)
        }
        zo.closeEntry()
        true
    }
}
