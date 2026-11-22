package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class TopTabChromeTest {

    @Test
    fun `page sample wins over the stored theme color`() {
        val colors = TopTabColors()
        assertThat(colors.siteColor("site", "#abcdef")).isEqualTo("#ABCDEF")
        colors.recordSample("site", "#abc")
        assertThat(colors.siteColor("site", "#112233")).isEqualTo("#AABBCC")
        assertThat(colors.siteColor("other", "abc")).isNull()
    }

    @Test
    fun `switching tabs keeps a displayed color until the destination has its own`() {
        val colors = TopTabColors()
        assertThat(colors.backgroundHex(null, false)).isEqualTo(TopTabChrome.LIGHT_BG)
        assertThat(colors.backgroundHex(null, true)).isEqualTo(TopTabChrome.DARK_BG)

        colors.recordSample("first", "#112233")
        colors.rememberDisplayedColor(colors.siteColor("first", null))
        assertThat(colors.backgroundHex(colors.siteColor("second", null), false)).isEqualTo("#112233")
        assertThat(colors.backgroundHex(colors.siteColor("second", "#FEDCBA"), false)).isEqualTo("#FEDCBA")

        colors.recordSample("second", "#FFFFFF")
        colors.recordSample("second", null)
        colors.recordSample("second", "invalid")
        colors.rememberDisplayedColor(colors.siteColor("second", null))
        assertThat(colors.backgroundHex(colors.siteColor("second", null), false)).isEqualTo("#FFFFFF")
        assertThat(colors.backgroundHex(colors.siteColor("first", null), false)).isEqualTo("#112233")
    }

    @Test
    fun `text contrast follows the bar luminance`() {
        assertThat(TopTabChrome.textIsLightOn("#112233")).isTrue()
        assertThat(TopTabChrome.textIsLightOn("#F5F5F5")).isFalse()
        assertThat(TopTabChrome.isLight("#FFFFFF")).isTrue()
        assertThat(TopTabChrome.isLight("#000000")).isFalse()
    }

    @Test
    fun `top tabs reuse the visited-tab session and accept a sideways swipe`() {
        val shell = readSource("com/webtoapp/ui/shell/MultiWebShellMode.kt")
        val browser = readSource("com/webtoapp/ui/shell/ShellBrowserView.kt")
        val topBranch = shell.substringAfter("\"TOP_TABS\"").substringBefore("\"CARDS\"")
        assertWithMessage("TOP_TABS must keep the TABS session and onCovered contract")
            .that(topBranch).contains("TabBarPlacement.TOP")
        assertThat(topBranch).contains("TabsMode(")
        assertThat(shell).contains("SiteTabSwipe.handler")
        assertThat(shell).contains("TopTabPager.translationX")
        assertThat(shell).contains("surface.onCovered()")
        assertThat(browser).contains("SiteTabSwipe.handler")
        assertThat(browser).contains("handler.onMove(")
        val editor = readSource("com/webtoapp/ui/screens/CreateMultiWebAppScreen.kt")
        assertThat(editor).contains("\"TOP_TABS\"")
        assertThat(editor).contains("displayMode == \"CARDS\" || displayMode == \"TOP_TABS\"")
    }

    private fun readSource(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        return File(javaRoot, relativePath).readText()
    }
}
