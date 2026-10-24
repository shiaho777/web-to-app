package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PageKeyEventsTest {
    @Test
    fun `back script dispatches a cancelable wta back event`() {
        val script = PageKeyEvents.backScript(reportViaPrompt = false)
        assertThat(script).contains("new CustomEvent('wta:back'")
        assertThat(script).contains("cancelable: true")
        assertThat(script).doesNotContain("prompt(")
    }

    @Test
    fun `gecko back script reports preventDefault through the reserved prompt`() {
        val script = PageKeyEvents.backScript(reportViaPrompt = true)
        assertThat(script).contains("prompt('${PageKeyEvents.PROMPT_TOKEN}'")
    }
}
