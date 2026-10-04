package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class TopTabChromeTest {

    @Test
    fun `page sample wins over the stored theme color`() {
        assertThat(TopTabChrome.backgroundHex("#112233", "#ABCDEF", false)).isEqualTo("#112233")
        assertThat(TopTabChrome.backgroundHex(null, "#abcdef", false)).isEqualTo("#ABCDEF")
        assertThat(TopTabChrome.backgroundHex("  ", "abc", true)).isEqualTo(TopTabChrome.DARK_BG)
        assertThat(TopTabChrome.backgroundHex(null, null, false)).isEqualTo(TopTabChrome.LIGHT_BG)
        assertThat(TopTabChrome.backgroundHex("#abc", null, false)).isEqualTo("#AABBCC")
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
        assertThat(shell).contains("SiteTabSwipe.onSwipe")
        assertThat(shell).contains("surface.onCovered()")
        assertThat(browser).contains("SiteTabSwipe.onUp(")
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
