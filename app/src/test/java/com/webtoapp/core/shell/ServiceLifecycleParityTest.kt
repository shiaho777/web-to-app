package com.webtoapp.core.shell

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class ServiceLifecycleParityTest {

    private fun read(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    @Test
    fun `background service is stopped when the exported config disables it`() {
        val init = read("com/webtoapp/ui/shell/ShellActivityInit.kt")
        val fn = init.substringAfter("fun initBackgroundService")
            .substringBefore("fun initNotificationService", "")
        assertWithMessage("disabling background run must stop the persisted service")
            .that(fn).contains("BackgroundRunService.stop(activity)")
    }

    @Test
    fun `notification services are stopped when disabled or switched away`() {
        val init = read("com/webtoapp/ui/shell/ShellActivityInit.kt")
        val fn = init.substringAfter("fun initNotificationService")
            .substringBefore("fun setTaskDescription", "")
        assertWithMessage("polling service must be stopped unless the active type is polling")
            .that(fn).contains("NotificationPollingService.stop(activity)")
        assertWithMessage("websocket service must be stopped unless the active type is websocket")
            .that(fn).contains("NotificationWebSocketService.stop(activity)")
    }

    @Test
    fun `notification polyfill implies the native bridge on both runtimes`() {
        val shell = read("com/webtoapp/ui/shell/ShellActivity.kt")
        assertWithMessage("shell must register the full bridge when the polyfill is on")
            .that(shell).contains("enableNativeBridge || config.webViewConfig.enableNotificationPolyfill")
        val preview = read("com/webtoapp/ui/webview/WebViewActivity.kt")
        assertWithMessage("preview must register the full bridge when the polyfill is on")
            .that(preview).contains("effectiveWebApp.webViewConfig.enableNotificationPolyfill")
    }

    @Test
    fun `server modes pass custom php extensions like the launcher path`() {
        val modes = read("com/webtoapp/ui/shell/ShellServerModes.kt")
        assertWithMessage("WordPress shell mode must pass custom PHP extensions")
            .that(modes).contains("customPhpExtensions = config.wordpressConfig.customPhpExtensions")
        assertWithMessage("PHP shell mode must pass custom PHP extensions")
            .that(modes).contains("customPhpExtensions = config.phpAppConfig.customPhpExtensions")
    }

    @Test
    fun `scheduled start registration carries the repeat flag`() {
        val init = read("com/webtoapp/ui/shell/ShellActivityInit.kt")
        assertWithMessage("shell scheduled-start registration must pass scheduledRepeat")
            .that(init).contains("repeat = autoStartConfig.scheduledRepeat")
    }

    @Test
    fun `multi-web site icons consume faviconUrl`() {
        val mode = read("com/webtoapp/ui/shell/MultiWebShellMode.kt")
        assertWithMessage("site icon rendering must fall back to faviconUrl")
            .that(mode).contains("site.faviconUrl")
        assertWithMessage("the shared site icon component must be used by the tab bars")
            .that(Regex("SiteIconContent\\(").findAll(mode).count()).isAtLeast(5)
    }
}
