package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Pins the multi-web hidden-tab contract for the GeckoView kernel (#1161, #1192).
 * A hidden tab's GeckoView renders into a SurfaceView, which Compose alpha()/zIndex
 * never reaches. releaseSession() drops the Gecko display but leaves that surface
 * and its last frame in front, so returning to an earlier tab stayed stuck on the
 * newest page (#1174). Covered tabs must set the view to GONE. Activity onPause
 * must not: that path is the only surface of a single-site Gecko app.
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
    private val activitySrc =
        readSanitized("com/webtoapp/ui/shell/ShellActivity.kt")

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
        assertWithMessage("covered tabs must destroy the SurfaceView via GONE (#1192)")
            .that(fn).contains("View.GONE")
        assertWithMessage("GONE is only for the covered-tab path, not every hide")
            .that(fn).contains("collapseSurface")
        val show = fn.substringAfter("if (visible)").substringBefore("} else {")
        assertWithMessage("show must restore VISIBLE before setSession, so the new surface can bind")
            .that(show.indexOf("View.VISIBLE")).isLessThan(show.indexOf("setSession("))
        assertWithMessage("VISIBLE must actually be assigned")
            .that(show).contains("View.VISIBLE")
    }

    @Test
    fun `BrowserSurface pause and resume drive gecko display visibility`() {
        val onPause = surfaceSrc.substringAfter("fun onPause()").substringBefore("fun ")
        val onResume = surfaceSrc.substringAfter("fun onResume()").substringBefore("fun ")
        val onCovered = surfaceSrc.substringAfter("fun onCovered()").substringBefore("fun ")
        assertWithMessage("onPause must detach the gecko display")
            .that(onPause).contains("setDisplayVisible(false)")
        assertWithMessage("activity onPause must not collapse the surface")
            .that(onPause).doesNotContain("collapseSurface")
        assertWithMessage("onResume must reattach the gecko display")
            .that(onResume).contains("setDisplayVisible(true)")
        assertWithMessage("a covered multi-web tab must collapse the SurfaceView")
            .that(onCovered).contains("collapseSurface = true")
    }

    @Test
    fun `multi-web keep-composed modes pause and resume engine surfaces`() {
        // TabsMode (bottom and top bars) and DrawerMode keep visited sites
        // composed under alpha(0). Each must collapse hidden Gecko surfaces
        // (onCovered) and resume the selected one — releaseSession alone does
        // not (#1192). TOP_TABS calls TabsMode, so it shares that contract.
        val covered = Regex("surface\\.onCovered\\(\\)").findAll(multiWebSrc).count()
        val resumes = Regex("surface\\.onResume\\(\\)").findAll(multiWebSrc).count()
        assertWithMessage("both TABS and DRAWER must collapse hidden engine surfaces")
            .that(covered).isAtLeast(2)
        assertWithMessage("both TABS and DRAWER must resume the selected engine surface")
            .that(resumes).isAtLeast(2)
        assertWithMessage("keep-composed modes must not use onPause, which leaves the SurfaceView up")
            .that(Regex("surface\\.onPause\\(\\)").findAll(multiWebSrc).count()).isEqualTo(0)
    }

    @Test
    fun `activity pause does not collapse the foreground gecko surface`() {
        val onPause = activitySrc.substringAfter("override fun onPause()").substringBefore("override fun ")
        assertWithMessage("ShellActivity must still pause the current surface")
            .that(onPause).contains("browserSurface?.onPause()")
        assertWithMessage("activity pause must not take the covered-tab path")
            .that(onPause).doesNotContain("onCovered()")
    }

    @Test
    fun `crash recovery does not reattach a detached display`() {
        val recovery = engineSrc.substringAfter("private fun attemptCrashRecovery")
        assertWithMessage("crash recovery must honor the detached state")
            .that(recovery).contains("!displayDetached")
    }
}
