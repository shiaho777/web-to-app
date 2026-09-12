package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test

/**
 * Covers the schemes an exported APK declares so that another app can hand control **back**.
 *
 * A page can always *open* a provider app — any non-http scheme is handed to the system — but
 * the provider can only come back if the APK declares the return channel. Without one, the
 * system finds no receiver and reports that no app can handle the link, which is how the
 * reported Discuz "QQ login opens QQ but never returns" symptom shows up.
 */
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
                customAppReturnSchemes = listOf("wx1234567890abcdef", "myapp://", " TradeScheme ")
            )
        )

        assertThat(schemes).contains("wx1234567890abcdef")
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
        // Declaring a launcher scheme such as `weixin` or `alipays` would make this app a
        // candidate for the real provider's links, so "pay with WeChat" could surface this app
        // instead of WeChat. Guard against ever adding one to the defaults.
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
                customAppReturnSchemes = listOf("wx1234567890abcdef")
            )
        )

        assertThat(schemes).contains("wx1234567890abcdef")
        assertThat(schemes).doesNotContain("mqqopensdkapi")
    }
}
