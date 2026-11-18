package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertWithMessage
import com.webtoapp.data.model.NativeBridgeCapabilities
import com.webtoapp.data.model.notificationPolyfillOnlyCapabilities
import com.webtoapp.data.model.withNotificationPolyfill
import org.junit.Test
import java.io.File

class BridgeSurfaceHardeningTest {

    private fun javaRoot(): File = listOf("app/src/main/java", "src/main/java").asSequence()
        .map(::File).firstOrNull(File::exists) ?: error("Cannot locate java directory")

    private fun readSanitized(relativePath: String): String {
        val file = File(javaRoot(), relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*+\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    @Test
    fun `polyfill-only capabilities expose notification and nothing else`() {
        val caps = notificationPolyfillOnlyCapabilities()
        assertWithMessage("notification must stay on for the polyfill").that(caps.notification).isTrue()
        val leaked = mutableListOf<String>()
        for (field in NativeBridgeCapabilities::class.java.declaredFields) {
            if (field.type != Boolean::class.javaPrimitiveType) continue
            field.isAccessible = true
            val name = field.name
            if (name == "notification") continue
            if (field.getBoolean(caps)) leaked.add(name)
        }
        assertWithMessage("polyfill-only registration must not inherit default-on capabilities: $leaked")
            .that(leaked).isEmpty()
    }

    @Test
    fun `bridge-enabled apps keep their configured capabilities with polyfill notification forced`() {
        val configured = NativeBridgeCapabilities(clipboard = false, download = false)
        val effective = configured.withNotificationPolyfill(true)
        assertWithMessage("configured capabilities must survive").that(effective.clipboard).isFalse()
        assertWithMessage("configured capabilities must survive").that(effective.download).isFalse()
        assertWithMessage("polyfill forces notification on").that(effective.notification).isTrue()
    }

    @Test
    fun `all three runtimes use notification-only capabilities when the bridge is off`() {
        val shell = readSanitized("com/webtoapp/ui/shell/ShellActivity.kt")
        val host = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
        val floating = readSanitized("com/webtoapp/core/floatingwindow/FloatingWindowService.kt")
        for ((name, source) in listOf("ShellActivity" to shell, "WebViewActivity" to host, "FloatingWindowService" to floating)) {
            assertWithMessage("$name must register the bridge for polyfill-only apps")
                .that(source).contains("enableNotificationPolyfill")
            assertWithMessage("$name must use notification-only capabilities when the bridge is off")
                .that(source).contains("notificationPolyfillOnlyCapabilities()")
        }
    }

    @Test
    fun `download bridge registration is gated on downloadEnabled in preview and floating window`() {
        val host = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
        val hostBlock = host.substringAfter("val effectiveDownloadConfig")
            .substringBefore("val previewWvConfig")
        assertWithMessage("host preview must gate DownloadBridge on downloadEnabled")
            .that(hostBlock).contains("downloadEnabled != false")
        val floating = readSanitized("com/webtoapp/core/floatingwindow/FloatingWindowService.kt")
        val floatingBlock = floating.substringAfter("val downloadBridgeFactory")
            .substringBefore("val webViewConfigurator")
        assertWithMessage("floating window must gate DownloadBridge on downloadEnabled")
            .that(floatingBlock).contains("downloadEnabled != false")
    }

    @Test
    fun `site favicons only load over http or https`() {
        val raw = File(javaRoot(), "com/webtoapp/ui/shell/MultiWebShellMode.kt").readText()
        val iconFn = raw.substringAfter("internal fun SiteIconContent(").substringBefore("fun MultiWebShellMode(")
        assertWithMessage("favicon must be scheme-gated before reaching Coil")
            .that(iconFn).contains("startsWith(\"http://\")")
        assertWithMessage("favicon must be scheme-gated before reaching Coil")
            .that(iconFn).contains("startsWith(\"https://\")")
        assertWithMessage("Coil must receive the gated value, not the raw config field")
            .that(iconFn).contains("model = remoteFavicon")
    }

    @Test
    fun `html preview honors model file-origin flags instead of forcing universal access`() {
        val host = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
        val block = host.substringAfter("currentApp?.appType == com.webtoapp.data.model.AppType.HTML")
            .substringBefore("javaScriptEnabled = currentApp.htmlConfig")
        assertWithMessage("file-origin access must follow the model flag")
            .that(block).contains("allowFileAccessFromFileURLs = currentApp.webViewConfig.allowFileAccessFromFileURLs")
        assertWithMessage("universal file-origin access must follow the model flag")
            .that(block).contains("allowUniversalAccessFromFileURLs = currentApp.webViewConfig.allowUniversalAccessFromFileURLs")
    }
}
