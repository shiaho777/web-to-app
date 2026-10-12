package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GeneratedSdkOverrideWiringTest {

    @Test
    fun `web app minSdk and targetSdk overrides land on apk config`() {
        val app = WebApp(
            name = "Sdk",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(minSdk = 26, targetSdk = 34)
        )
        val config = app.toApkConfig("com.example.sdk")
        assertThat(config.minSdkOverride).isEqualTo(26)
        assertThat(config.targetSdkOverride).isEqualTo(34)
    }

    @Test
    fun `unset sdk overrides leave apk config rewrite fields null`() {
        val app = WebApp(
            name = "SdkDefault",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig()
        )
        val config = app.toApkConfig("com.example.sdkdefault")
        assertThat(config.minSdkOverride).isNull()
        assertThat(config.targetSdkOverride).isNull()
    }

    @Test
    fun `process-exec type pins target 28 and clamps a raised minSdk`() {
        val app = WebApp(
            name = "NodeSdk",
            url = "https://example.com",
            appType = AppType.NODEJS_APP,
            apkExportConfig = ApkExportConfig(minSdk = 34, targetSdk = 36)
        )
        val config = app.toApkConfig("com.example.nodesdk")
        assertThat(config.targetSdkOverride).isEqualTo(28)
        assertThat(config.minSdkOverride).isEqualTo(28)
    }
}
