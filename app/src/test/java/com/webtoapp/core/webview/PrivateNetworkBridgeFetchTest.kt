package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class PrivateNetworkBridgeFetchTest {

    @Test
    fun `cors bypass scope tries the native fetch before falling back to the bridge`() {
        val script = readSource()

        assertWithMessage(
            "The CORS bypass branch must attempt the native fetch and only bridge after it rejects"
        ).that(script).contains("return nativeFetch(input, init).catch(viaBridge);")

        val unconditionalBridge = Regex("if \\(isCorsBypass\\) \\{\\s*return nativeHttpRequest\\(payload\\)")
        assertWithMessage(
            "The CORS bypass branch must not route every cross-origin request through the bridge, " +
                "which buffers the whole body and Base64-encodes it across the JS bridge"
        ).that(unconditionalBridge.findAll(script).count()).isEqualTo(0)
    }

    private fun readSource(): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, "com/webtoapp/core/webview/WebViewManager.kt")
        assertWithMessage("Test harness cannot find source file").that(file.isFile).isTrue()
        return file.readText()
    }
}
