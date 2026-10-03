package com.webtoapp.core.apkbuilder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.data.model.HtmlLoadMode
import com.webtoapp.data.model.NetworkTrustConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WordPressConfig
import com.webtoapp.ui.shell.buildPackagedHtmlShellEntryUrl
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApkExportPreflightTest {

    @Rule @JvmField
    val koinRule = com.webtoapp.util.KoinCleanupRule()

    @get:Rule
    val temp = TemporaryFolder()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `frontend app with saved files passes without source project directory`() {
        val index = temp.newFile("index.html").apply {
            writeText("<html></html>")
        }
        val app = WebApp(
            name = "Frontend",
            url = "",
            appType = AppType.FRONTEND,
            htmlConfig = HtmlConfig(
                entryFile = "index.html",
                files = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML))
            )
        )

        val report = ApkExportPreflight.check(context, app)

        assertThat(report.errors.map { it.key }).doesNotContain("htmlFiles")
        assertThat(report.passed).isTrue()
    }

    @Test
    fun `frontend app without source project directory conservatively targets loopback`() {
        val index = temp.newFile("index.html").apply {
            writeText("<html></html>")
        }
        val app = WebApp(
            name = "Frontend",
            url = "",
            appType = AppType.FRONTEND,
            htmlConfig = HtmlConfig(
                entryFile = "index.html",
                files = listOf(HtmlFile("index.html", index.absolutePath, HtmlFileType.HTML)),
                loadMode = HtmlLoadMode.LOCAL_HTTP
            )
        )

        val config = app.toApkConfig("com.example.frontend", context)

        assertThat(config.htmlUsesFileScheme).isFalse()
        assertThat(config.targetUrl).isEqualTo(
            buildPackagedHtmlShellEntryUrl("com.example.frontend", "index.html")
        )
    }

    @Test
    fun `auto html static project uses loopback server`() {
        val projectDir = temp.newFolder("static-html")
        File(projectDir, "index.html").writeText("<html><body>Hello</body></html>")
        val app = WebApp(
            name = "Html",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(
                projectDir = projectDir.absolutePath,
                entryFile = "index.html",
                loadMode = HtmlLoadMode.LOCAL_HTTP
            )
        )

        val config = app.toApkConfig("com.example.static", context)

        assertThat(config.htmlUsesFileScheme).isFalse()
        assertThat(config.targetUrl).isEqualTo(
            buildPackagedHtmlShellEntryUrl("com.example.static", "index.html")
        )
    }

    @Test
    fun `auto html web audio project uses loopback server`() {
        val projectDir = temp.newFolder("game-html")
        File(projectDir, "index.html").writeText(
            "<html><script>const ac = new AudioContext(); localStorage.setItem('score','1')</script></html>"
        )
        val app = WebApp(
            name = "Game",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(
                projectDir = projectDir.absolutePath,
                entryFile = "index.html",
                loadMode = HtmlLoadMode.LOCAL_HTTP
            )
        )

        val config = app.toApkConfig("com.example.game", context)

        assertThat(config.htmlUsesFileScheme).isFalse()
        assertThat(config.targetUrl).isEqualTo(
            buildPackagedHtmlShellEntryUrl("com.example.game", "index.html")
        )
    }

    @Test
    fun `explicit html load mode overrides automatic detection`() {
        val projectDir = temp.newFolder("forced-html")
        File(projectDir, "index.html").writeText("<html><script>fetch('/data.json')</script></html>")
        val fileApp = WebApp(
            name = "ForcedFile",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(
                projectDir = projectDir.absolutePath,
                entryFile = "index.html",
                loadMode = HtmlLoadMode.FILE
            )
        )
        val serverApp = fileApp.copy(
            htmlConfig = fileApp.htmlConfig?.copy(loadMode = HtmlLoadMode.LOCAL_HTTP)
        )

        val fileConfig = fileApp.toApkConfig("com.example.file", context)
        val serverConfig = serverApp.toApkConfig("com.example.server", context)

        assertThat(fileConfig.htmlUsesFileScheme).isTrue()
        assertThat(serverConfig.htmlUsesFileScheme).isFalse()
    }

    @Test
    fun `frontend app with source project directory targets frontend asset`() {
        val app = WebApp(
            name = "Frontend",
            url = "",
            appType = AppType.FRONTEND,
            htmlConfig = HtmlConfig(
                projectDir = temp.newFolder("frontend").absolutePath,
                entryFile = "index.html"
            )
        )

        val config = app.toApkConfig("com.example.frontend", context)

        assertThat(config.targetUrl).isEqualTo(
            buildPackagedHtmlShellEntryUrl("com.example.frontend", "index.html")
        )
    }

    @Test
    fun `html app targets stable packaged loopback entry url`() {
        val app = WebApp(
            name = "Html",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(entryFile = "main/index.html", loadMode = HtmlLoadMode.LOCAL_HTTP)
        )

        val config = app.toApkConfig("com.example.frontend", context)

        assertThat(config.htmlUsesFileScheme).isFalse()
        assertThat(config.targetUrl).isEqualTo(
            buildPackagedHtmlShellEntryUrl("com.example.frontend", "main/index.html")
        )
    }

    @Test
    fun `wordpress app exports full runtime configuration`() {
        val app = WebApp(
            name = "WordPress",
            url = "",
            appType = AppType.WORDPRESS,
            wordpressConfig = WordPressConfig(
                projectId = "wp1",
                projectName = "My WP",
                siteTitle = "My Site",
                adminUser = "owner",
                adminEmail = "owner@example.com",
                adminPassword = "secret",
                themeName = "twentytwentyfour",
                plugins = listOf("woocommerce", "seo"),
                activePlugins = listOf("woocommerce"),
                permalinkStructure = "postname",
                siteLanguage = "zh_CN",
                autoInstall = true,
                sourceType = "SAMPLE",
                phpPort = 8088
            )
        )

        val config = app.toApkConfig("com.example.wp", context)

        assertThat(config.targetUrl).isEqualTo("wordpress://localhost")
        assertThat(config.wordpressSiteTitle).isEqualTo("My Site")
        assertThat(config.wordpressAdminUser).isEqualTo("owner")
        assertThat(config.wordpressAdminEmail).isEqualTo("owner@example.com")
        assertThat(config.wordpressAdminPassword).isEqualTo("secret")
        assertThat(config.wordpressThemeName).isEqualTo("twentytwentyfour")
        assertThat(config.wordpressPlugins).containsExactly("woocommerce", "seo").inOrder()
        assertThat(config.wordpressActivePlugins).containsExactly("woocommerce")
        assertThat(config.wordpressPermalinkStructure).isEqualTo("postname")
        assertThat(config.wordpressSiteLanguage).isEqualTo("zh_CN")
        assertThat(config.wordpressAutoInstall).isTrue()
        assertThat(config.wordpressPhpPort).isEqualTo(8088)
    }

    @Test
    fun `network trust without any anchor is blocking error`() {
        val app = WebApp(
            name = "No Trust",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(
                networkTrustConfig = NetworkTrustConfig(
                    trustSystemCa = false,
                    trustUserCa = false,
                    customCaCertificates = emptyList()
                )
            )
        )

        val report = ApkExportPreflight.check(context, app)

        assertThat(report.passed).isFalse()
        assertThat(report.errors.map { it.key }).contains("networkTrust")
    }
}
