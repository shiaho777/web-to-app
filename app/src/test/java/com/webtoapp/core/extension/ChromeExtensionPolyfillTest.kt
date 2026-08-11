package com.webtoapp.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChromeExtensionPolyfillTest {

    @Test
    fun `polyfill defines STORAGE_PREFIX before using it`() {
        // Regression (#502): the polyfill referenced an undefined STORAGE_PREFIX,
        // throwing a ReferenceError that aborted the whole IIFE (onInstalled event
        // and background message delivery never got installed).
        val polyfill = ChromeExtensionPolyfill.generatePolyfill(
            extensionId = "abcdefghijklmnop",
            manifestJson = """{"name":"test"}""",
            isBackground = false
        )

        assertThat(polyfill).contains("var STORAGE_PREFIX = '__WTA_EXT_' + EXT_ID + '_';")
        assertThat(polyfill).contains("var installedKey = STORAGE_PREFIX + '__installed__';")
        assertThat(polyfill).contains("__WTA_DELIVER_TO_BACKGROUND__")
    }

    @Test
    fun `polyfill escapes extension ids that could break the generated script`() {
        val polyfill = ChromeExtensionPolyfill.generatePolyfill(
            extensionId = "id'with'quotes",
            manifestJson = "{}",
            isBackground = false
        )

        assertThat(polyfill).contains("'id\\'with\\'quotes'")
    }
}
