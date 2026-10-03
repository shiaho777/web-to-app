package com.webtoapp.core.agent.tool.builtin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewScriptTest {

    @Test
    fun wrapIsAFunctionBodyThatReturnsJson() {
        val wrapped = PreviewScript.wrap("return document.title")
        assertTrue(wrapped.contains("return document.title"))
        assertTrue(wrapped.contains("JSON.stringify({ok:true"))
        assertTrue(wrapped.trim().endsWith("})()"))
    }

    @Test
    fun snapshotTagsControlsTheNextScriptCanAddress() {
        val snapshot = PreviewScript.snapshot()
        assertTrue(snapshot.contains("data-wta-n"))
        assertTrue(snapshot.contains("getBoundingClientRect"))
        assertFalse(snapshot.contains("document.documentElement.outerHTML"))
    }

    @Test
    fun unwrapPeelsTheWebViewStringEncoding() {
        assertEquals("{\"ok\":true}", PreviewScript.unwrap("\"{\\\"ok\\\":true}\""))
        assertEquals("", PreviewScript.unwrap("null"))
        assertEquals("", PreviewScript.unwrap(null))
    }

    @Test
    fun onlyHttpUrlsAreAccepted() {
        assertTrue(PreviewScript.acceptableHttpUrl("https://example.com/a"))
        assertTrue(PreviewScript.acceptableHttpUrl("http://127.0.0.1:8080"))
        assertFalse(PreviewScript.acceptableHttpUrl("javascript:alert(1)"))
        assertFalse(PreviewScript.acceptableHttpUrl("file:///sdcard/index.html"))
    }

    @Test
    fun longResultsAreCut() {
        val cut = PreviewScript.truncate("x".repeat(PreviewScript.MAX_RESULT_CHARS + 20))
        assertTrue(cut.endsWith("…[truncated]"))
        assertTrue(cut.length < PreviewScript.MAX_RESULT_CHARS + 40)
    }
}
