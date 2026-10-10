package com.webtoapp.core.kernel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KernelFlavorJsTest {

    @Test
    fun `system default with no UA emits no flavor JS`() {
        assertThat(KernelFlavor.SYSTEM_DEFAULT.profile.buildFlavorJs()).isEmpty()
    }

    @Test
    fun `chrome flavor JS spoofs chrome surface and hides the old window flag`() {
        val js = KernelFlavor.BLINK_CHROME.profile.buildFlavorJs()

        assertThat(js).isNotEmpty()
        assertThat(js).contains("webdriver")
        assertThat(js).contains("[native code]")
        assertThat(js).contains("window.chrome.app")
        assertThat(js).contains("PluginArray")
        assertThat(js).contains("wow64")
        assertThat(js).contains("formFactors")
        assertThat(js).contains("pdfViewerEnabled")
        assertThat(js).contains("productSub")
        assertThat(js).doesNotContain("window.__wta_kernel_flavor__")
        assertThat(js).contains("Symbol.for('wta.kf')")
        assertThat(js).contains(KernelFlavor.BLINK_CHROME.profile.userAgent)
    }

    @Test
    fun `firefox flavor JS does not install a chrome object`() {
        val js = KernelFlavor.GECKO_FIREFOX.profile.buildFlavorJs()

        assertThat(js).isNotEmpty()
        assertThat(js).contains("webdriver")
        assertThat(js).contains("delete window.chrome")
        assertThat(js).doesNotContain("window.chrome.app")
        assertThat(js).doesNotContain("PluginArray")
    }

    @Test
    fun `device-disguise chrome UA still emits chrome flavor JS`() {
        val ua = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        val identity = BrowserIdentityResolver.resolve(
            flavor = KernelFlavor.SYSTEM_DEFAULT,
            legacyMode = com.webtoapp.data.model.UserAgentMode.DEFAULT,
            customUserAgent = null,
            desktopMode = false,
            desktopUserAgent = null,
            legacyUserAgent = null,
            deviceDisguiseUserAgent = ua
        )

        assertThat(identity.profile).isNotNull()
        assertThat(identity.profile!!.flavor).isEqualTo(KernelFlavor.SYSTEM_DEFAULT)
        val js = identity.profile!!.buildFlavorJs()
        assertThat(js).isNotEmpty()
        assertThat(js).contains("webdriver")
        assertThat(js).contains("window.chrome.app")
        assertThat(js).contains(ua)
    }

    @Test
    fun `chrome client-hint brands lead with GREASE`() {
        val brands = KernelFlavor.BLINK_CHROME.profile.brands
        assertThat(brands.first().brand).isEqualTo("Not_A Brand")
        assertThat(brands.map { it.brand }).contains("Google Chrome")
        assertThat(brands.map { it.brand }).contains("Chromium")
    }
}
