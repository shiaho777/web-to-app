package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WasmStreamCompatTest {

    @Test
    fun `script drains only the unread wasm clone and retries a rejected webgl context`() {
        val script = WasmStreamCompat.SCRIPT
        assertThat(script).contains("window.__wtaWasmStream")
        assertThat(script).contains("Response.prototype.clone")
        assertThat(script).contains("ct.indexOf('wasm')")
        assertThat(script).contains("failIfMajorPerformanceCaveat")
        assertThat(script).contains("HTMLCanvasElement.prototype.getContext")
    }
}
