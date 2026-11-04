package com.webtoapp.core.appearance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrowserDisguiseJsGeneratorTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun `disabled config produces no script`() {
        assertThat(BrowserDisguiseJsGenerator.generate(BrowserDisguiseConfig(), resources)).isEmpty()
    }

    @Test
    fun `enabled config rebuilds the full script from raw resources`() {
        val js = BrowserDisguiseJsGenerator.generate(BrowserDisguiseConfig(enabled = true), resources)
        assertThat(js.length).isGreaterThan(20000)
        assertThat(js).contains("window.__wta_browser_disguise__=1")
        assertThat(js).contains("[native code]")
        assertThat(js).contains("Object.defineProperty(navigator,'webdriver'")
        assertThat(js).contains("})();")
    }

    @Test
    fun `generated script carries no self describing stealth markers`() {
        val js = BrowserDisguiseJsGenerator.generate(BrowserDisguiseConfig(enabled = true), resources)
        for (marker in listOf(
            "UNIVERSAL STEALTH",
            "PROTOTYPE SHIELD",
            "防检测",
            "表层 API 全伪装",
            "// ──"
        )) {
            assertThat(js).doesNotContain(marker)
        }
    }
}
