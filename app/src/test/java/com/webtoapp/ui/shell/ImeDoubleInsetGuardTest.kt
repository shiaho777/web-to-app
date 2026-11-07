package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class ImeDoubleInsetGuardTest {

    private val excludeIme = "exclude(WindowInsets.ime)"

    @Test
    fun `shell scaffold does not duplicate the window IME height`() {
        val src = readSanitized("com/webtoapp/ui/shell/ShellScaffoldLayout.kt")
        assertWithMessage(
            "WindowHelper.installNativeImeResize owns the IME height; a second " +
                "scaffold-side exclude would stack another inset on the content"
        ).that(src).doesNotContain(excludeIme)
    }

    @Test
    fun `preview scaffold mirrors the shell IME contract`() {
        val src = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
        assertWithMessage(
            "WebViewActivity preview must mirror the shell contract"
        ).that(src).doesNotContain(excludeIme)
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
