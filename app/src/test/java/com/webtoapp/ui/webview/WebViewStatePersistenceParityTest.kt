package com.webtoapp.ui.webview

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * Host/shell WebView state-persistence parity gate (incident: host preview lost
 * the whole session on any Activity recreation — ShellActivity already saved and
 * restored the WebView bundle, WebViewActivity had no onSaveInstanceState at all).
 *
 * Source-scan contract, same style as ShellUiParityTest: if the lifecycle wiring
 * is refactored, keep the semantics or update this test with a reason.
 */
class WebViewStatePersistenceParityTest {

    private val activitySrc = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
    private val shellSrc = readSanitized("com/webtoapp/ui/shell/ShellActivity.kt")
    private val shellScreenSrc = readSanitized("com/webtoapp/ui/shell/ShellScreen.kt")

    @Test
    fun `host preview saves WebView state into the instance bundle`() {
        assertWithMessage("WebViewActivity must override onSaveInstanceState")
            .that(activitySrc).contains("onSaveInstanceState")
        assertWithMessage("WebViewActivity must persist the WebView back-forward list")
            .that(activitySrc).contains("saveState(outState)")
    }

    @Test
    fun `host preview restores the WebView bundle before the initial load`() {
        assertWithMessage("WebViewActivity must consume the saved WebView bundle")
            .that(activitySrc).contains("consumeWebViewState")
        assertWithMessage("WebViewActivity must call restoreState on the WebView")
            .that(activitySrc).contains("restoreState(")
        assertWithMessage("restored sessions reload the current entry, not the start URL")
            .that(activitySrc).contains("reload()")
    }

    @Test
    fun `host preview persists and consumes the last-visited URL`() {
        assertWithMessage("WebViewActivity must persist the resume URL on pause/save")
            .that(activitySrc).contains("persistResumeUrl()")
        assertWithMessage("the initial load must prefer the persisted resume URL")
            .that(activitySrc).contains("consumeResumeUrl")
    }

    @Test
    fun `host preview does not recreate on bare relaunch intents`() {
        val onNewIntent = activitySrc.substringAfter("override fun onNewIntent")
            .substringBefore("override fun", "")
        assertWithMessage("onNewIntent must gate recreate() on the intent carrying a target")
            .that(onNewIntent).contains("shouldRecreateForNewIntent")
        assertWithMessage("bare intents must not destroy the live WebView session")
            .that(onNewIntent).doesNotContain("newAppId <= 0 || newAppId != trackedAppId")
    }

    @Test
    fun `shell saves state through the surface so engine sites are covered`() {
        val onSave = shellSrc.substringAfter("override fun onSaveInstanceState")
            .substringBefore("override fun", "")
        assertWithMessage("ShellActivity must save via browserSurface first (Gecko sites have no webView field)")
            .that(onSave).contains("browserSurface?.saveState(outState)")
    }

    @Test
    fun `memory-teardown stash keeps the multi-web site guard`() {
        // The TRIM_MEMORY_COMPLETE path must tag the stashed bundle with the
        // surface's site id, or the #1036 restore check is bypassed and a
        // saved surface can graft onto whichever site composes first.
        val stash = shellSrc.substringAfter("internal fun stashWebViewState")
            .substringBefore("internal fun", "")
        assertWithMessage("stashWebViewState must write the saved site id into the bundle")
            .that(stash).contains("KEY_SAVED_SURFACE_SITE_ID")
        assertWithMessage("the restore path must still consume the saved site id")
            .that(shellSrc).contains("getString(KEY_SAVED_SURFACE_SITE_ID)")

        val trim = shellScreenSrc.substringAfter("onTrimMemory")
            .substringBefore("registerComponentCallbacks", "")
        assertWithMessage("the trim-stash call must hand the surface's siteId to stashWebViewState")
            .that(trim).contains("stashWebViewState(")
        assertWithMessage("the trim-stash call must hand the surface's siteId to stashWebViewState")
            .that(trim).contains("siteId")
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
