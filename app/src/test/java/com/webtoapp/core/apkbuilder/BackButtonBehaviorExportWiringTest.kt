package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.util.GsonProvider
import org.junit.Test

/**
 * Guards the Back Button Behavior config chain (issue #151): editor model -> ApkConfig
 * export block -> shell config JSON -> runtime ShellConfig. The runtime back handler reads
 * this from ShellConfig, so a break here silently reverts generated apps to history-back.
 */
class BackButtonBehaviorExportWiringTest {

    private fun roundTrip(app: WebApp): ShellConfig {
        val apk = app.toApkConfig("com.example.test")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
    }

    @Test
    fun `exit back behavior flows from model through ApkConfig into shell config`() {
        val app = WebApp(
            name = "Back",
            url = "https://example.com",
            webViewConfig = WebViewConfig(backButtonBehavior = "EXIT")
        )

        val config = app.toApkConfig("com.example.back")
        assertThat(config.webView.backButtonBehavior).isEqualTo("EXIT")
        assertThat(roundTrip(app).webViewConfig.backButtonBehavior).isEqualTo("EXIT")
    }

    @Test
    fun `back behavior defaults to go-back`() {
        val app = WebApp(name = "Default", url = "https://example.com")

        assertThat(app.toApkConfig("com.example.default").webView.backButtonBehavior).isEqualTo("GO_BACK")
        assertThat(roundTrip(app).webViewConfig.backButtonBehavior).isEqualTo("GO_BACK")
    }
}
