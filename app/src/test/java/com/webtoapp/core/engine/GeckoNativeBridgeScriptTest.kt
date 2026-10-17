package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * The Gecko CORS bridge content script runs in a sandbox. Returning its Promise
 * through exportFunction makes the page read `.then` across compartments, and
 * Firefox throws "permission denied to access property 'then'" (#1206). The
 * page has to own the Promise. Each GeckoSession also needs its own bridge:
 * the extension is installed once on the shared runtime.
 *
 * The built-in extension stays in the Gecko profile. Without
 * nativeMessagingFromContent, the next process start injects the content
 * script but browser.runtime.sendNativeMessage is missing (#1242).
 */
class GeckoNativeBridgeScriptTest {

    private fun repoFile(relativePath: String): File {
        val roots = listOf(File("."), File(".."))
        return roots.asSequence()
            .map { File(it, relativePath) }
            .firstOrNull { it.isFile }
            ?: error("Cannot locate $relativePath")
    }

    @Test
    fun `page creates the bridge promise and the sandbox function only takes callbacks`() {
        val src = repoFile("app/src/main/assets/web_extensions/wta_native_bridge/content_script.js").readText()
        val sandbox = src.substringAfter("function sandboxSendNative")
            .substringBefore("const pageWorldSetup")
        assertWithMessage("sandbox bridge must not hand a Promise back to the page")
            .that(sandbox).doesNotContain("return new Promise")
        assertWithMessage("sandbox bridge must not return sendNative's Promise")
            .that(sandbox).doesNotContain("return sendNative")
        assertThat(sandbox).contains("resolve(value")
        assertThat(sandbox).contains("reject(message)")

        val page = src.substringAfter("const pageWorldSetup")
            .substringBefore("window.XMLHttpRequest = BridgedXHR")
        assertThat(page).contains("return new Promise((resolve, reject)")
        assertThat(page).contains("sendNativeExported(String(payloadJson), resolve, reject)")
        assertThat(src).doesNotContain("return sendNative(payloadJson)")
    }

    @Test
    fun `each gecko session keeps its own native bridge`() {
        val src = repoFile("app/src/main/java/com/webtoapp/core/engine/GeckoViewEngine.kt").readText()
        assertThat(src).contains("bridgesBySession")
        assertThat(src).contains("sender.session?.let { bridgesBySession[it] }")
        assertThat(src).doesNotContain("activeNativeBridge")
        assertThat(src).contains("bindNativeBridge(newSession, it)")
        assertThat(src).contains("unbindNativeBridge(")
    }

    @Test
    fun `persisted gecko bridge keeps content-script native messaging`() {
        val manifest = repoFile("app/src/main/assets/web_extensions/wta_native_bridge/manifest.json").readText()
        assertThat(manifest).contains("\"id\": \"wta-native-bridge@webtoapp\"")
        assertThat(manifest).contains("\"nativeMessaging\"")
        assertThat(manifest).contains("\"nativeMessagingFromContent\"")
        assertThat(manifest).contains("\"geckoViewAddons\"")
        assertWithMessage("version must move so an already-installed 1.0 extension updates")
            .that(manifest).contains("\"version\": \"1.2\"")

        val engine = repoFile("app/src/main/java/com/webtoapp/core/engine/GeckoViewEngine.kt").readText()
        assertThat(engine).contains("wta-native-bridge@webtoapp")
        assertThat(engine).contains("webExtensionController.setMessageDelegate")
        assertThat(engine).doesNotContain("ext?.setMessageDelegate")
        assertThat(engine).contains("pendingLoadUrl")
        assertThat(engine).contains("uninstall")
    }

    @Test
    fun `gecko native messages do not call the http bridge on the message thread`() {
        val engine = repoFile("app/src/main/java/com/webtoapp/core/engine/GeckoViewEngine.kt").readText()
        val delegate = engine.substringAfter("private val nativeBridgeMessageDelegate")
            .substringBefore("private fun bindNativeBridge")
        val onMessage = delegate.substringAfter("override fun onMessage(")
            .substringBefore("dispatchGeckoNativeHttp")
        assertWithMessage("onMessage is on the UI thread and must hand the request off")
            .that(onMessage).doesNotContain("httpRequest")
        assertThat(delegate).contains("dispatchGeckoNativeHttp")
        val dispatch = engine.substringAfter("fun dispatchGeckoNativeHttp")
            .substringBefore("private fun bindNativeBridge")
        assertThat(dispatch).contains("Dispatchers.IO")
    }

    @Test
    fun `remote pages keep gecko fetch so cloudflare clearance can stick`() {
        val src = repoFile("app/src/main/assets/web_extensions/wta_native_bridge/content_script.js").readText()
        val guard = src.substringAfter("function isPrivateHost")
            .substringBefore("const NATIVE_APP")
        assertThat(guard).contains("if (!isPrivateHost(window.location.hostname)) return;")

        val page = src.substringAfter("const pageWorldSetup")
        assertThat(page).contains("function shouldBridge")
        assertThat(page).contains(
            "parsed.hostname.toLowerCase() === window.location.hostname.toLowerCase()"
        )
        assertThat(page).contains("if (!isHttpUrl(payload.url) || !shouldBridge(payload.url))")

        val engine = repoFile("app/src/main/java/com/webtoapp/core/engine/GeckoViewEngine.kt").readText()
        val onMessage = engine.substringAfter("override fun onMessage(")
            .substringBefore("dispatchGeckoNativeHttp")
        assertThat(onMessage).contains("op == \"scope\"")
        assertThat(onMessage).contains("networkBridgeScope()")
        assertThat(onMessage).doesNotContain("httpRequest")
    }
}
