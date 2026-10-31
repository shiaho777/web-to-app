package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import com.webtoapp.ui.shell.translatePageScript
import com.webtoapp.util.JsStrings
import org.junit.Test

class TranslateCallbackTest {

    @Test
    fun `native success is a quoted json string`() {
        val results = listOf("你好", "a\"b", "line\n2", "slash\\end")
        val json = """["你好","a\"b","line\n2","slash\\end"]"""
        val call = translateCallbackSuccess("cb_'1", results)

        assertThat(encodeJsonStringArray(results)).isEqualTo(json)
        assertThat(call).isEqualTo(
            "window._translateCallback(${JsStrings.quote("cb_'1")}, ${JsStrings.quote(json)});"
        )
        assertThat(call).doesNotContain(", [")
    }

    @Test
    fun `native failure passes a quoted error and a null payload`() {
        val call = translateCallbackError("cb_1", "All engines failed")
        assertThat(call).isEqualTo(
            "window._translateCallback(\"cb_1\", null, \"All engines failed\");"
        )
    }

    @Test
    fun `page callback accepts a json string and a raw array`() {
        val script = translatePageScript("zh-CN", true)
        assertThat(script).contains("typeof resultsJson === 'string'")
        assertThat(script).contains("JSON.parse(resultsJson)")
        assertThat(script).contains("Array.isArray(parsed)")
        assertThat(script).contains("JSON.stringify(results)")
        assertThat(script).contains("All engines failed")
    }
}
