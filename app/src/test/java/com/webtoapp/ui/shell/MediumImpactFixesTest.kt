package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertWithMessage
import com.webtoapp.core.webview.PwaOfflineSupport
import org.junit.Test
import java.io.File

class MediumImpactFixesTest {

    private fun javaRoot(): File = listOf("app/src/main/java", "src/main/java").asSequence()
        .map(::File).firstOrNull(File::exists) ?: error("Cannot locate java directory")

    private fun readSanitized(relativePath: String): String {
        val file = File(javaRoot(), relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    @Test
    fun `floating window restore animates view alpha to full instead of squaring opacity`() {
        val manager = readSanitized("com/webtoapp/core/floatingwindow/FloatingWindowManager.kt")
        val restore = manager.substringAfter("fun restore()").substringBefore("fun updateSize(")
        assertWithMessage("restore must animate view alpha to 1f; window params already carry opacity")
            .that(restore).contains("?.alpha(1f)")
        assertWithMessage("restore must not multiply opacity into the view alpha again")
            .that(restore).doesNotContain("config.opacity / 100f")
    }

    @Test
    fun `translate collects every duplicate node and keeps the dynamic observer alive`() {
        val raw = File(javaRoot(), "com/webtoapp/ui/shell/ShellTranslateScript.kt").readText()
        val collect = raw.substringAfter("function collectTextNodes()").substringBefore("if (showBtn)")
        assertWithMessage("collectTextNodes must push every text node, not only the first per text")
            .that(collect).doesNotContain("!seen.has(text)")
        assertWithMessage("translate requests must still be deduplicated by unique text")
            .that(raw).contains("uniqueUncached")
        assertWithMessage("duplicate nodes must be filled from the shared cache after batching")
            .that(raw).contains("if (translateCache[dk]) cachedResults[d] = translateCache[dk]")
        assertWithMessage("the dynamic observer must not permanently disconnect after 30s")
            .that(raw).doesNotContain("}, 30000)")
    }

    @Test
    fun `offline cache name changes when the offline config changes`() {
        val base = PwaOfflineSupport.OfflineConfig(
            enabled = true,
            strategy = PwaOfflineSupport.CacheStrategy.NETWORK_FIRST
        )
        val bodyA = PwaOfflineSupport.generateSwScriptBody(base)
        val bodyB = PwaOfflineSupport.generateSwScriptBody(base.copy(strategy = PwaOfflineSupport.CacheStrategy.CACHE_FIRST))
        val bodyC = PwaOfflineSupport.generateSwScriptBody(base.copy(maxAgeHours = base.maxAgeHours + 1))
        val nameRegex = Regex("const CACHE_NAME = '([^']+)'")
        val nameA = nameRegex.find(bodyA)!!.groupValues[1]
        val nameB = nameRegex.find(bodyB)!!.groupValues[1]
        val nameC = nameRegex.find(bodyC)!!.groupValues[1]
        assertWithMessage("strategy change must produce a new cache name").that(nameA).isNotEqualTo(nameB)
        assertWithMessage("maxAge change must produce a new cache name").that(nameA).isNotEqualTo(nameC)
        assertWithMessage("identical config must keep a stable cache name")
            .that(nameA).isEqualTo(nameRegex.find(PwaOfflineSupport.generateSwScriptBody(base))!!.groupValues[1])
    }

    @Test
    fun `offline worker enforces ttl on reads and trims on writes`() {
        val body = PwaOfflineSupport.generateSwScriptBody(
            PwaOfflineSupport.OfflineConfig(enabled = true, strategy = PwaOfflineSupport.CacheStrategy.CACHE_FIRST)
        )
        assertWithMessage("cache reads must check x-wta-cached-at against MAX_AGE_MS")
            .that(body).contains("async function freshCached(request)")
        assertWithMessage("cache-first must use the freshness check").that(body).contains("const cached = await freshCached(request)")
        assertWithMessage("cache writes must periodically trim to honor the size cap")
            .that(body).contains("if (cacheWriteCount % 25 === 0) trimCache()")
    }

    @Test
    fun `service worker injection is skipped on file scheme pages`() {
        val raw = File(javaRoot(), "com/webtoapp/core/webview/WebViewManager.kt").readText()
        val block = raw.substringAfter("if (config.pwaOfflineEnabled)").substringBefore("view?.removeCallbacks(cookieFlushRunnable)")
        assertWithMessage("file/content scheme pages must skip SW injection")
            .that(block).contains("startsWith(\"file://\")")
        assertWithMessage("the skip must be logged, not silent")
            .that(block).contains("PWA offline skipped")
    }

    @Test
    fun `static asset pack also intercepts service worker subresource requests`() {
        val manager = readSanitized("com/webtoapp/core/webview/WebViewManager.kt")
        val block = manager.substringAfter("if (config.staticAssetPackEnabled)").substringBefore("if (adBlocker.isEnabled()")
        assertWithMessage("static pack must register a service-worker router handler")
            .that(block).contains("ServiceWorkerRequestRouter.addHandler")
        assertWithMessage("the SW handler must resolve packaged assets")
            .that(block).contains("resolveStaticPackAsset(reqUrl, config)")
    }

    @Test
    fun `packaged mime map serves mjs modules and modern images`() {
        val raw = File(javaRoot(), "com/webtoapp/core/webview/WebViewManager.kt").readText()
        val map = raw.substringAfter("private val MIME_TYPE_MAP").substringBefore("private val TEXT_MIME_TYPES")
        assertWithMessage("mjs must map to a JavaScript MIME type").that(map).contains("\"mjs\" to \"application/javascript\"")
        assertWithMessage("avif must map to image/avif").that(map).contains("\"avif\" to \"image/avif\"")
        assertWithMessage("bmp must map to image/bmp").that(map).contains("\"bmp\" to \"image/bmp\"")
    }

    @Test
    fun `wordpress auto install failure surfaces an error instead of silent ready`() {
        val modes = readSanitized("com/webtoapp/ui/shell/ShellServerModes.kt")
        val wp = modes.substringAfter("WordPressManager.autoInstallIfNeeded")
        assertWithMessage("the auto-install result must be checked")
            .that(wp).contains("if (!installed)")
        assertWithMessage("failure must enter the error phase with a localized message")
            .that(wp).contains("errorMsg = Strings.wpInstallFailed")
    }

    @Test
    fun `fcm start reports failure and shell logs the incomplete config honestly`() {
        val manager = readSanitized("com/webtoapp/core/notification/NotificationFcmManager.kt")
        val start = manager.substringAfter("fun start(context: Context, config: FcmConfig)")
            .substringBefore("fun restoreIfNeeded")
        assertWithMessage("start must return whether FCM actually started")
            .that(start).contains("return false")
        val init = readSanitized("com/webtoapp/ui/shell/ShellActivityInit.kt")
        assertWithMessage("shell init must branch on the start result")
            .that(init).contains("val started = com.webtoapp.core.notification.NotificationFcmManager.start")
        assertWithMessage("incomplete FCM config must log a warning, not claim started")
            .that(init).contains("if (started)")
    }

    @Test
    fun `site ad block rules are merged when sites use their own config`() {
        val screen = readSanitized("com/webtoapp/ui/shell/ShellScreen.kt")
        assertWithMessage("site-level ad block must activate the rule install")
            .that(screen).contains("siteAdBlockActive")
        assertWithMessage("site source rules must be merged into the compiled set")
            .that(screen).contains("siteShellConfig?.adBlockRules.orEmpty()")
        assertWithMessage("the hold must arm for site-level ad block too")
            .that(screen).contains("if (effectiveAdBlockEnabled) adBlocker.holdRuleInstall()")
    }
}
