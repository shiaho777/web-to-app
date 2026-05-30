package com.webtoapp.core.errorpage

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.apkbuilder.ApkConfig
import com.webtoapp.core.apkbuilder.ApkConfigJsonFactory
import com.webtoapp.core.apkbuilder.ErrorPageBlock
import com.webtoapp.core.apkbuilder.MetaBlock
import com.webtoapp.core.shell.ErrorPageShellConfig
import org.junit.Test

class ErrorPageApkRoundTripTest {

    private val customHtml = """
        <!DOCTYPE html><html lang="en"><head><meta charset="UTF-8">
        <title>Offline</title></head>
        <body><h1>You are offline</h1>
        <p>It's a "test" page with quotes & ampersands.</p>
        </body></html>
    """.trimIndent()

    @Test
    fun `customHtml survives full ApkConfig - JSON - shell pipeline`() {
        val apkConfig = newApkConfig().copy(
            errorPage = ErrorPageBlock(
                mode = "CUSTOM_HTML",
                customHtml = customHtml,
                retryButtonText = "Try Again"
            )
        )

        val webViewBlock = extractWebViewConfig(apkConfig)
        val errorPageBlock = webViewBlock.getAsJsonObject("errorPageConfig")
        assertThat(errorPageBlock).isNotNull()
        assertThat(errorPageBlock.get("mode").asString).isEqualTo("CUSTOM_HTML")
        assertThat(errorPageBlock.get("customHtml").asString).isEqualTo(customHtml)
        assertThat(errorPageBlock.get("retryButtonText").asString).isEqualTo("Try Again")

        val shellConfig = Gson().fromJson(errorPageBlock, ErrorPageShellConfig::class.java)
        assertThat(shellConfig.mode).isEqualTo("CUSTOM_HTML")
        assertThat(shellConfig.customHtml).isEqualTo(customHtml)
        assertThat(shellConfig.retryButtonText).isEqualTo("Try Again")
        assertThat(shellConfig.customMediaPath).isEmpty()
    }

    @Test
    fun `customMediaPath survives full ApkConfig - JSON - shell pipeline`() {
        val mediaPath = "/storage/emulated/0/MyApp/offline.mp4"
        val apkConfig = newApkConfig().copy(
            errorPage = ErrorPageBlock(
                mode = "CUSTOM_MEDIA",
                customMediaPath = mediaPath,
                retryButtonText = "Reload"
            )
        )

        val errorPageBlock = extractWebViewConfig(apkConfig).getAsJsonObject("errorPageConfig")
        assertThat(errorPageBlock.get("customMediaPath").asString).isEqualTo(mediaPath)

        val shellConfig = Gson().fromJson(errorPageBlock, ErrorPageShellConfig::class.java)
        assertThat(shellConfig.mode).isEqualTo("CUSTOM_MEDIA")
        assertThat(shellConfig.customMediaPath).isEqualTo(mediaPath)
        assertThat(shellConfig.retryButtonText).isEqualTo("Reload")
        assertThat(shellConfig.customHtml).isEmpty()
    }

    @Test
    fun `default ApkConfig still serializes empty error page custom fields`() {
        val apkConfig = newApkConfig()
        val errorPageBlock = extractWebViewConfig(apkConfig).getAsJsonObject("errorPageConfig")

        assertThat(errorPageBlock.has("customHtml")).isTrue()
        assertThat(errorPageBlock.has("customMediaPath")).isTrue()
        assertThat(errorPageBlock.has("retryButtonText")).isTrue()
        assertThat(errorPageBlock.get("customHtml").asString).isEmpty()
        assertThat(errorPageBlock.get("customMediaPath").asString).isEmpty()
        assertThat(errorPageBlock.get("retryButtonText").asString).isEmpty()
    }

    @Test
    fun `shell ErrorPageConfig drops empty customHtml back to null at runtime`() {

        val shellConfig = ErrorPageShellConfig(
            mode = "CUSTOM_HTML",
            customHtml = customHtml
        )
        val materialized = ErrorPageConfig(
            mode = ErrorPageMode.CUSTOM_HTML,
            customHtml = shellConfig.customHtml.takeIf { it.isNotEmpty() },
            customMediaPath = shellConfig.customMediaPath.takeIf { it.isNotEmpty() },
            retryButtonText = shellConfig.retryButtonText
        )

        val rendered = ErrorPageManager(materialized)
            .generateErrorPage(-2, "ERR_INTERNET_DISCONNECTED", "https://offline.test")

        assertThat(rendered).isEqualTo(customHtml)
    }

    private fun newApkConfig(): ApkConfig = ApkConfig(
        meta = MetaBlock(
            appName = "OfflineFixtureApp",
            packageName = "com.example.test",
            targetUrl = "https://offline.test/",
            versionCode = 1,
            versionName = "1.0"
        )
    )

    private fun extractWebViewConfig(config: ApkConfig): JsonObject {
        val json = ApkConfigJsonFactory.create(config)
        val root = JsonParser.parseString(json).asJsonObject
        return root.getAsJsonObject("webViewConfig")
    }
}
