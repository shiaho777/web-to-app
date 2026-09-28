package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Pins the issue #1075 contract: on API 30+ the shell runs edge-to-edge with
 * SOFT_INPUT_ADJUST_NOTHING plus a window-level manual IME padding (see
 * WindowHelper.installManualImePadding). Any Compose-side bottom inset that
 * still applies the navigation-bar height while the IME is open stacks on top
 * of that window padding and shows a dead band — reported as a thick black bar
 * — between the page content and the keyboard. Every such site must therefore
 * take the nav-bar inset minus the IME-covered region via
 * `exclude(WindowInsets.ime)`, matching the existing idiom in AgentScreen.
 */
class ImeDoubleInsetGuardTest {

    private val excludeIme = "exclude(WindowInsets.ime)"

    @Test
    fun `shell scaffold content insets exclude the IME region`() {
        val src = readSanitized("com/webtoapp/ui/shell/ShellScaffoldLayout.kt")
        assertWithMessage(
            "ScaffoldDefaults.contentWindowInsets must exclude the IME-covered " +
                "bottom region; the window-level IME padding already lifts the content"
        ).that(src).contains("ScaffoldDefaults.contentWindowInsets.$excludeIme")
    }

    @Test
    fun `preview scaffold content insets exclude the IME region`() {
        val src = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
        assertWithMessage(
            "WebViewActivity preview must mirror the shell contract"
        ).that(src).contains("ScaffoldDefaults.contentWindowInsets.$excludeIme")
    }

    @Test
    fun `multi-web bottom bars exclude the IME region from nav padding`() {
        val src = readSanitized("com/webtoapp/ui/shell/MultiWebShellMode.kt")
        assertWithMessage(
            "TabsMode bottom bar must drop its nav-bar lift while the IME is open"
        ).that(src).contains("WindowInsets.navigationBars.$excludeIme")
        assertWithMessage(
            "Drawer sheet windowInsets must drop its nav-bar lift while the IME is open"
        ).that(src).contains("DrawerDefaults.windowInsets.$excludeIme")
        assertWithMessage("No bare navigationBars padding may remain in multi-web mode")
            .that(src).doesNotContain("windowInsetsPadding(WindowInsets.navigationBars)")
    }

    @Test
    fun `gallery overlays exclude the IME region from nav padding`() {
        val src = readSanitized("com/webtoapp/ui/shell/ShellGallery.kt")
        assertWithMessage(
            "Gallery bottom overlays must drop their nav-bar lift while the IME is open"
        ).that(src).contains("WindowInsets.navigationBars.$excludeIme")
        assertWithMessage("No bare navigationBarsPadding may remain in the gallery player")
            .that(src).doesNotContain("navigationBarsPadding()")
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Test harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
