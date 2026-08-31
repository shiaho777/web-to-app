package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.apkbuilder.ApkConfigJsonFactory
import com.webtoapp.core.apkbuilder.toApkConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.util.GsonProvider
import org.junit.Test

/**
 * Regression coverage for the toolbar flags that were silently dropped between the
 * shell config and the runtime WebViewConfig in `buildWebViewConfig`
 * (user report: "hide toolbar can't disappear" — exported APKs fell back to the
 * data-class defaults `true` for toolbarShowConsole / toolbarShowFind, so the toolbar
 * never vanished even with every button switched off).
 *
 * `WebViewConfigBooleanCoverageTest` covers the export → shell JSON layers; this test
 * covers the third hop (shell config → runtime WebViewConfig) that has no drift gate.
 */
class ShellWebViewConfigMappingTest {

    private fun runtimeConfigOf(app: WebApp): WebViewConfig {
        val apk = app.toApkConfig("com.example.test")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
        return buildWebViewConfig(shell)
    }

    @Test
    fun `disabled toolbar flags survive shell config to runtime WebViewConfig`() {
        val app = WebApp(
            name = "t",
            url = "https://t.example.com",
            webViewConfig = WebViewConfig(
                browserToolbarEnabled = true,
                toolbarShowTitle = false,
                toolbarShowUrl = false,
                toolbarShowBack = false,
                toolbarShowForward = false,
                toolbarShowRefresh = false,
                toolbarShowConsole = false,
                toolbarShowFind = false
            )
        )

        val runtime = runtimeConfigOf(app)

        assertThat(runtime.toolbarShowTitle).isFalse()
        assertThat(runtime.toolbarShowUrl).isFalse()
        assertThat(runtime.toolbarShowBack).isFalse()
        assertThat(runtime.toolbarShowForward).isFalse()
        assertThat(runtime.toolbarShowRefresh).isFalse()
        assertThat(runtime.toolbarShowConsole).isFalse()
        assertThat(runtime.toolbarShowFind).isFalse()
    }

    @Test
    fun `enabled toolbar flags stay enabled through the mapping`() {
        val runtime = runtimeConfigOf(WebApp(name = "t", url = "https://t.example.com"))

        assertThat(runtime.toolbarShowTitle).isTrue()
        assertThat(runtime.toolbarShowUrl).isTrue()
        assertThat(runtime.toolbarShowBack).isTrue()
        assertThat(runtime.toolbarShowForward).isTrue()
        assertThat(runtime.toolbarShowRefresh).isTrue()
        assertThat(runtime.toolbarShowConsole).isTrue()
        assertThat(runtime.toolbarShowFind).isTrue()
    }
}
