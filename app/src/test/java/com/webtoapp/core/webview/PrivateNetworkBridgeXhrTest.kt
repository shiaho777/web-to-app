package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class PrivateNetworkBridgeXhrTest {

    private val webViewScript: String by lazy {
        val source = sourceFile("app/src/main/java", "com/webtoapp/core/webview/WebViewManager.kt").readText()
        val start = source.indexOf("internal object PrivateNetworkApiBridgeScriptHolder")
        assertWithMessage("PrivateNetworkApiBridgeScriptHolder not found").that(start).isAtLeast(0)
        source.substring(start, source.indexOf("\"\"\".trimIndent()", start))
    }

    private val geckoScript: String by lazy {
        sourceFile("app/src/main/assets", "web_extensions/wta_native_bridge/content_script.js").readText()
    }

    private fun scripts() = mapOf("WebView bridge" to webViewScript, "GeckoView content script" to geckoScript)

    @Test
    fun `bridged xhr exposes the native upload object`() {
        scripts().forEach { (name, script) ->
            assertWithMessage("$name must expose the real XMLHttpRequestUpload so upload.addEventListener and upload.onprogress work")
                .that(script).contains("this.upload = this._xhr.upload")
            assertWithMessage("$name must not replace upload with a plain object")
                .that(script).doesNotContain("this.upload = {}")
        }
    }

    @Test
    fun `bridged xhr forwards open arguments so two-argument open stays asynchronous`() {
        scripts().forEach { (name, script) ->
            assertWithMessage("$name must forward the caller's open() arguments unchanged")
                .that(script).contains("this._xhr.open.apply(this._xhr, arguments)")
            assertWithMessage("$name must not pass an explicit undefined async flag, which opens a synchronous XHR")
                .that(script).doesNotContain("this._xhr.open(method, url, async, user, password)")
        }
    }

    @Test
    fun `bridged xhr forwards timeout and withCredentials to the native request`() {
        scripts().forEach { (name, script) ->
            assertWithMessage("$name must define timeout/withCredentials accessors backed by the native XHR")
                .that(script).contains("['timeout', 'withCredentials'].forEach(")
            assertWithMessage("$name must not shadow withCredentials with a wrapper-only field")
                .that(script).doesNotContain("this.withCredentials = false")
        }
    }

    @Test
    fun `bridged requests report upload progress events`() {
        scripts().forEach { (name, script) ->
            listOf("'loadstart'", "'progress'", "'load'", "'loadend'", "'error'").forEach { type ->
                assertWithMessage("$name must dispatch upload $type for requests sent through the native bridge")
                    .that(script).contains("_dispatchUpload($type")
            }
        }
    }

    private fun sourceFile(root: String, relative: String): File {
        val base = listOf(root, root.removePrefix("app/")).asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate $root")
        return File(base, relative).also {
            assertWithMessage("Test harness cannot find $relative").that(it.isFile).isTrue()
        }
    }
}
