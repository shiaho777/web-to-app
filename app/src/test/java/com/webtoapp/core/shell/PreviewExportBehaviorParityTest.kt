package com.webtoapp.core.shell

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class PreviewExportBehaviorParityTest {

    private val activitySrc = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
    private val scaffoldSrc = readSanitized("com/webtoapp/ui/shell/ShellScaffoldLayout.kt")
    private val shellScreenSrc = readSanitized("com/webtoapp/ui/shell/ShellScreen.kt")
    private val apkBuilderSrc = readSanitized("com/webtoapp/core/apkbuilder/ApkBuilder.kt")

    @Test
    fun `shell renders the floating back button the host preview renders`() {
        assertWithMessage("ShellScaffoldLayout must consume showFloatingBackButton")
            .that(scaffoldSrc).contains("showFloatingBackButton")
        assertWithMessage("the shell FAB must walk history through ShellWebViewNavigation")
            .that(scaffoldSrc).contains("ShellWebViewNavigation.goBackOrFinish")
    }

    @Test
    fun `shell announcement gate accepts content-only announcements like the host`() {
        val gate = shellScreenSrc.substringAfter("showAnnouncementDialog &&")
            .substringBefore("ShellAnnouncementDialog", "")
        assertWithMessage("the render gate must not require a non-empty title alone")
            .that(gate).contains("announcementContent.isNotEmpty()")
    }

    @Test
    fun `preview mirrors the export cache and offline gate under clear-on-launch`() {
        val block = activitySrc.substringAfter("previewWebViewConfig")
            .substringBefore("EngineViewFactory.create", "")
        assertWithMessage("preview must force cache off when clearBrowsingDataOnLaunch is set")
            .that(block).contains("cacheEnabled = false")
        assertWithMessage("preview must force PWA offline off when clearBrowsingDataOnLaunch is set")
            .that(block).contains("pwaOfflineEnabled = false")
    }

    @Test
    fun `preview initializes the isolation environment like the shell does`() {
        assertWithMessage("WebViewActivity must initialize IsolationManager from the app config")
            .that(activitySrc).contains("IsolationManager.getInstance(context)")
        assertWithMessage("the preview init must consume the app's isolationConfig")
            .that(activitySrc).contains("isolationConfig")
    }

    @Test
    fun `export bakes perf scripts only behind the performance switch`() {
        val fn = apkBuilderSrc.substringAfter("internal fun buildExportInjectScripts")
            .substringBefore("private fun", "")
        assertWithMessage("perf baking must be gated on performanceOptimization")
            .that(fn).contains("config.performanceOptimization")
        assertWithMessage("kernel baking must respect the BASIC level gate")
            .that(fn).contains("KernelDisguiseLevel.BASIC")
    }

    @Test
    fun `kernel spoof script is idempotent against double injection`() {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists) ?: error("Cannot locate java directory")
        val kotlin = File(javaRoot, "com/webtoapp/core/kernel/BrowserKernel.kt").readText()
        assertWithMessage("the fallback kernel JS must guard against re-injection")
            .that(kotlin).contains("__wta_kernel_injected")
        val cppRoot = listOf("app/src/main/cpp", "../app/src/main/cpp").asSequence()
            .map(::File).firstOrNull(File::exists) ?: error("Cannot locate cpp directory")
        val native = File(cppRoot, "browser_kernel.c").readText()
        assertWithMessage("the native kernel JS must guard against re-injection")
            .that(native).contains("__wta_kernel_injected")
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
