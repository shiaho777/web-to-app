package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.util.GsonProvider
import org.junit.Test

/**
 * Guards the Static Asset Pack config chain: editor model -> ApkConfig export block ->
 * shell config JSON -> runtime ShellConfig. A break here is the classic "works in preview,
 * silently inert after export" failure, so both the export block and the parsed shell config
 * are asserted.
 */
class StaticAssetPackExportWiringTest {

    private fun roundTrip(app: WebApp): ShellConfig {
        val apk = app.toApkConfig("com.example.test")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
    }

    @Test
    fun `static asset pack flags flow from model through ApkConfig into shell config`() {
        val app = WebApp(
            name = "Pack",
            url = "https://example.com",
            webViewConfig = WebViewConfig(
                staticAssetPackEnabled = true,
                staticAssetPackMaxAgeDays = 45,
                staticAssetPackIncludeImages = true,
                staticAssetPackIncludeCdn = false,
                staticAssetPackMaxTotalSizeMb = 80
            )
        )

        val config = app.toApkConfig("com.example.pack")
        assertThat(config.webView.staticAssetPackEnabled).isTrue()
        assertThat(config.webView.staticAssetPackMaxAgeDays).isEqualTo(45)
        // Build-only tuning also lands on the export block (consumed by the builder, not the shell).
        assertThat(config.webView.staticAssetPackIncludeImages).isTrue()
        assertThat(config.webView.staticAssetPackIncludeCdn).isFalse()
        assertThat(config.webView.staticAssetPackMaxTotalSizeMb).isEqualTo(80)

        val shell = roundTrip(app)
        assertThat(shell.webViewConfig.staticAssetPackEnabled).isTrue()
        assertThat(shell.webViewConfig.staticAssetPackMaxAgeDays).isEqualTo(45)
    }

    @Test
    fun `static asset pack defaults to disabled with 30 day validity`() {
        val app = WebApp(name = "Default", url = "https://example.com")

        val config = app.toApkConfig("com.example.default")
        assertThat(config.webView.staticAssetPackEnabled).isFalse()

        val shell = roundTrip(app)
        assertThat(shell.webViewConfig.staticAssetPackEnabled).isFalse()
        assertThat(shell.webViewConfig.staticAssetPackMaxAgeDays).isEqualTo(30)
    }
}
