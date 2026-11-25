package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test

class AppReturnSchemesTest {

    private fun schemesFor(
        appType: AppType = AppType.WEB,
        config: WebViewConfig = WebViewConfig()
    ): List<String> = WebApp(
        name = "t",
        url = "https://bbs.example.com",
        appType = appType,
        webViewConfig = config
    ).toApkConfig("com.example.forum").deepLink.schemes

    @Test
    fun `app return is on by default and declares the QQ return channel`() {
        assertThat(WebViewConfig().enableAppReturn).isTrue()

        val schemes = schemesFor()

        assertThat(schemes).contains("mqqopensdkapi")
        assertThat(schemes).contains("mqqopensdkapiV2")
        assertThat(schemes).contains("mqqopensdkapiV3")
    }

    @Test
    fun `turning app return off drops every built-in return channel`() {
        val schemes = schemesFor(config = WebViewConfig(enableAppReturn = false))

        assertThat(schemes).doesNotContain("mqqopensdkapi")
        assertThat(schemes).doesNotContain("mqqopensdkapiV2")
        assertThat(schemes).doesNotContain("mqqopensdkapiV3")
    }

    @Test
    fun `a web app keeps its own package scheme alongside the return channels`() {
        assertThat(schemesFor()).contains("wta-com-example-forum")
    }

    @Test
    fun `custom return schemes are kept and normalised`() {
        val schemes = schemesFor(
            config = WebViewConfig(
                customAppReturnSchemes = listOf("wx-demo-return", "myapp://", " TradeScheme ")
            )
        )

        assertThat(schemes).contains("wx-demo-return")
        assertThat(schemes).contains("myapp")
        assertThat(schemes).contains("tradescheme")
    }

    @Test
    fun `blank custom entries are ignored and the list stays duplicate free`() {
        val schemes = schemesFor(
            config = WebViewConfig(customAppReturnSchemes = listOf("", "   ", "://", "myapp", "MyApp"))
        )

        assertThat(schemes.none { it.isBlank() }).isTrue()
        assertThat(schemes).containsNoDuplicates()
    }

    @Test
    fun `built-in schemes stay return-only so they cannot hijack a provider's own links`() {
        val schemes = schemesFor()

        assertThat(schemes).doesNotContain("weixin")
        assertThat(schemes).doesNotContain("alipays")
        assertThat(schemes).doesNotContain("alipay")
        assertThat(schemes).doesNotContain("taobao")
        assertThat(schemes).doesNotContain("openapp.jdmobile")
    }

    @Test
    fun `custom schemes are still honoured when the built-in set is switched off`() {
        val schemes = schemesFor(
            config = WebViewConfig(
                enableAppReturn = false,
                customAppReturnSchemes = listOf("wx-demo-return")
            )
        )

        assertThat(schemes).contains("wx-demo-return")
        assertThat(schemes).doesNotContain("mqqopensdkapi")
    }
}
