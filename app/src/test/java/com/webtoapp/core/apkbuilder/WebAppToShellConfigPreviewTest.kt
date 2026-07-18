package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.core.activation.ActivationCodeType
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.getActivationCodeStrings
import com.webtoapp.util.GsonProvider
import org.junit.Test

class WebAppToShellConfigPreviewTest {

    @Test
    fun `export payload activation fields match shell config shape`() {
        val app = WebApp(
            id = 42L,
            name = "尚日历",
            url = "https://example.com",
            appType = AppType.WEB,
            activationEnabled = true,
            activationRequireEveryTime = true,
            activationCodeList = listOf(
                ActivationCode(
                    code = "1111",
                    type = ActivationCodeType.PERMANENT
                )
            ),
            activationDialogConfig = com.webtoapp.data.model.ActivationDialogConfig(
                title = "激活标题",
                subtitle = "激活副标题",
                inputLabel = "输入码",
                buttonText = "立即激活"
            )
        )

        val apk = app.toApkConfig(packageName = "com.example.preview")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)

        assertThat(shell.activationEnabled).isTrue()
        assertThat(shell.activationRequireEveryTime).isTrue()
        assertThat(shell.activationDialogTitle).isEqualTo("激活标题")
        assertThat(shell.activationDialogSubtitle).isEqualTo("激活副标题")
        assertThat(shell.activationDialogInputLabel).isEqualTo("输入码")
        assertThat(shell.activationDialogButtonText).isEqualTo("立即激活")
        assertThat(shell.activationCodes).isEqualTo(app.getActivationCodeStrings())
        assertThat(shell.targetUrl).isEqualTo("https://example.com")
        assertThat(shell.appName).isEqualTo("尚日历")
    }

    @Test
    fun `preview content dir field defaults empty on export payload`() {
        val app = WebApp(
            id = 1L,
            name = "t",
            url = "https://example.com",
            appType = AppType.WEB
        )
        val apk = app.toApkConfig(packageName = "com.example.preview")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)
        assertThat(shell.previewContentDir).isEmpty()
    }
}
