package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class UrlSwitcherPluginTest {

    @Test
    fun `url switcher package has a panel and a page-side go handler`() {
        val dir = builtinDir()
        val manifest = File(dir, "plugin.json").readText()
        val html = File(dir, "plugin.html").readText()
        assertThat(manifest).contains("\"id\": \"builtin-url-switcher\"")
        assertThat(manifest).contains("STORAGE")
        assertThat(hasPanelMarkup(html)).isTrue()
        val pageJs = extractPageJs(html)
        assertThat(pageJs).contains("location.assign")
        assertThat(pageJs).contains("askCurrent")
        assertThat(html).contains("hcjPanel.config")
        assertThat(html).contains("sites")
        assertThat(html).contains("LONG_MS")
        assertThat(html).contains("persistOrder")
        assertThat(html).contains("pointerdown")
        assertThat(html).contains("setPointerCapture")
        assertThat(html).contains("insertBefore")
    }

    private fun builtinDir(): File {
        val root = listOf("app/src/main/assets/plugins", "src/main/assets/plugins")
            .asSequence().map(::File).firstOrNull { File(it, "builtin-url-switcher").isDirectory }
            ?: error("Cannot locate builtin-url-switcher")
        return File(root, "builtin-url-switcher")
    }
}
