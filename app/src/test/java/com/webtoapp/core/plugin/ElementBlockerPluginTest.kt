package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Issue #1088: the built-in element picker was hard to confirm on small or
 * precariously placed elements (a second tap on the same element was the only
 * confirmation), and its hidden elements flashed on every load because the
 * plugin ran at document_idle — long after first paint.
 *
 * These contract assertions pin both fixes: a floating confirm bar whose taps
 * never get treated as element picks, and document_start application of the
 * stored hide rules.
 */
class ElementBlockerPluginTest {

    private val manifest = File(assetRoot(), "builtin-element-blocker/plugin.json").readText()
    private val pageJs = File(assetRoot(), "builtin-element-blocker/plugin.html").readText()
        .let { Regex("""<script type="hcj/page">(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
            .find(it)?.groupValues?.get(1) ?: error("hcj/page block missing") }

    @Test
    fun `blocked rules apply at document start so hidden elements never paint`() {
        assertWithMessage("plugin.json must run at document_start, not document_idle — " +
            "idle injection lands after first paint and causes the flash reported in #1088")
            .that(manifest).contains("\"runAt\": \"document_start\"")
        // If the parser has not emitted <html> yet the apply must retry instead
        // of silently doing nothing.
        assertThat(pageJs).contains("requestAnimationFrame(applyBlockedRules)")
        assertThat(pageJs).contains("addEventListener('DOMContentLoaded', applyBlockedRules)")
    }

    @Test
    fun `select mode offers a floating confirm button`() {
        assertThat(pageJs).contains("wta-picker-bar")
        assertThat(pageJs).contains("wta-picker-block")
        assertThat(pageJs).contains("onConfirmBlock")
        // The minimize chip the reporter asked for (so the bar cannot cover
        // the very element being picked).
        assertThat(pageJs).contains("wta-picker-chip")
    }

    @Test
    fun `taps on the picker bar never count as element picks`() {
        // onTap must bail on picker-UI targets before preventDefault so the
        // confirm/minimize/cancel buttons keep working.
        val onTap = pageJs.substringAfter("function onTap").substringBefore("function onDblClick")
        assertThat(onTap).contains("inPickerUi(e.target)")
        val onDblClick = pageJs.substringAfter("function onDblClick").substringBefore("// 进入选择模式")
        assertThat(onDblClick).contains("inPickerUi(e.target)")
    }

    @Test
    fun `plugin script is idempotent across re-injection`() {
        // updatePluginPayloads re-runs every injection phase when the store
        // resolves late; without a guard the picker would double-register.
        assertThat(pageJs).contains("window.__wtaEbInit")
    }

    private fun assetRoot(): File =
        listOf("app/src/main/assets/plugins", "src/main/assets/plugins")
            .asSequence().map(::File).firstOrNull { File(it, "builtin-element-blocker").isDirectory }
            ?: error("Cannot locate builtin plugins asset dir")
}
