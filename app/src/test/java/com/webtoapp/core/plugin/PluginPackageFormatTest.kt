package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PluginPackageFormatTest {

    @Test
    fun `extractPageJs pulls the hcj-page block out of a full document`() {
        val html = """
            <!doctype html><html><head>
            <script type="hcj/page">
              hcj.on('action', () => {});
            </script>
            <style>body { color: #000; }</style>
            </head><body><div id="app"></div>
            <script>hcjPanel.send({})</script>
            </body></html>
        """.trimIndent()

        val js = extractPageJs(html)
        assertThat(js).contains("hcj.on('action'")
        assertThat(js).doesNotContain("hcjPanel.send")
        assertThat(js).doesNotContain("<script")
    }

    @Test
    fun `extractPageJs concatenates multiple blocks and tolerates quote styles`() {
        val html = """
            <script type='hcj/page'>var a = 1;</script>
            <script TYPE="hcj/page">var b = 2;</script>
            <script>var panel = true;</script>
        """.trimIndent()

        val js = extractPageJs(html)
        assertThat(js).contains("var a = 1;")
        assertThat(js).contains("var b = 2;")
        assertThat(js).doesNotContain("var panel")
    }

    @Test
    fun `hasPanelMarkup false for page-only plugin html`() {
        val html = """<script type="hcj/page">hcj.log('hi');</script>"""
        assertThat(hasPanelMarkup(html)).isFalse()
    }

    @Test
    fun `hasPanelMarkup true when body has real markup`() {
        val html = """
            <html><head><script type="hcj/page">var x = 1;</script></head>
            <body><button id="t">go</button><script>hcjPanel.send({})</script></body></html>
        """.trimIndent()
        assertThat(hasPanelMarkup(html)).isTrue()
    }

    @Test
    fun `hasPanelMarkup false for body holding only the page block`() {
        val html = """
            <html><body>
            <script type="hcj/page">var x = 1;</script>
            <!-- nothing else -->
            </body></html>
        """.trimIndent()
        assertThat(hasPanelMarkup(html)).isFalse()
    }
}
