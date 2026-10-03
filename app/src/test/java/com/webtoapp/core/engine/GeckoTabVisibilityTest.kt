package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Pins the multi-web hidden-tab contract for the GeckoView kernel (#1161):
 * a hidden tab's GeckoView renders into its own surface layer, which the
 * Compose alpha()/zIndex used to hide inactive tabs never reaches — without an
 * explicit detach, the last-visited site stays composited on top of the
 * selected one. The fix releases the session's display on hide and rebinds it
 * on show, and both keep-composed modes (TABS, DRAWER) must drive it.
 */
class GeckoTabVisibilityTest {

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

    private val engineSrc =
        readSanitized("com/webtoapp/core/engine/GeckoViewEngine.kt")
    private val surfaceSrc =
        readSanitized("com/webtoapp/core/engine/BrowserSurface.kt")
    private val multiWebSrc =
        readSanitized("com/webtoapp/ui/shell/MultiWebShellMode.kt")

    @Test
    fun `gecko engine detaches the display on hide and rebinds on show`() {
        val fn = engineSrc.substringAfter("fun setDisplayVisible")
        assertWithMessage("GeckoViewEngine must expose setDisplayVisible")
            .that(fn).isNotEmpty()
        assertWithMessage("hide must release the session's display")
            .that(fn).contains("releaseSession()")
        assertWithMessage("show must rebind the session to the view")
            .that(fn).contains("setSession(")
        assertWithMessage("hide must throttle the session (background-tab semantics)")
            .that(fn).contains("setActive(false)")
    }

    @Test
    fun `BrowserSurface pause and resume drive gecko display visibility`() {
        val onPause = surfaceSrc.substringAfter("fun onPause()").substringBefore("fun ")
        val onResume = surfaceSrc.substringAfter("fun onResume()").substringBefore("fun ")
        assertWithMessage("onPause must detach the gecko display")
            .that(onPause).contains("setDisplayVisible(false)")
        assertWithMessage("onResume must reattach the gecko display")
            .that(onResume).contains("setDisplayVisible(true)")
    }

    @Test
    fun `multi-web keep-composed modes pause and resume engine surfaces`() {
        // TabsMode and DrawerMode both keep visited sites composed under
        // alpha(0); each must pause hidden engine surfaces and resume the
        // selected one — two independent call sites per direction.
        val pauses = Regex("surface\\.onPause\\(\\)").findAll(multiWebSrc).count()
        val resumes = Regex("surface\\.onResume\\(\\)").findAll(multiWebSrc).count()
        assertWithMessage("both TABS and DRAWER must pause hidden engine surfaces")
            .that(pauses).isAtLeast(2)
        assertWithMessage("both TABS and DRAWER must resume the selected engine surface")
            .that(resumes).isAtLeast(2)
    }

    @Test
    fun `crash recovery does not reattach a detached display`() {
        val recovery = engineSrc.substringAfter("private fun attemptCrashRecovery")
        assertWithMessage("crash recovery must honor the detached state")
            .that(recovery).contains("!displayDetached")
    }
}
