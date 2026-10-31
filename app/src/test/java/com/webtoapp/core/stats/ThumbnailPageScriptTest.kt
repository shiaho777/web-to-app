package com.webtoapp.core.stats

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.plugin.Plugin
import com.webtoapp.core.plugin.PluginKind
import com.webtoapp.core.plugin.PluginMatchRule
import com.webtoapp.core.plugin.PluginRunAt
import com.webtoapp.core.plugin.PluginSession
import org.junit.Test

class ThumbnailPageScriptTest {

    @Test
    fun `page host drops scheme port and file urls`() {
        assertThat(thumbnailPageHost("https://Example.COM:443/a?q=1")).isEqualTo("example.com")
        assertThat(thumbnailPageHost("file:///tmp/index.html")).isEmpty()
    }

    @Test
    fun `cosmetic payload is applied by the same function as the live page`() {
        val blocker = AdBlocker()
        blocker.initialize(useDefaultRules = false)
        blocker.setEnabled(true)
        blocker.addRule("example.com#$#body { background: #121212 !important; }")

        val script = thumbnailPageScript(
            cosmeticPayloadJson = blocker.getCosmeticPayloadJson("example.com"),
            cssBlocks = emptyList(),
            scriptBodies = emptyList(),
        )

        assertThat(script).contains("__wta_cosmetic_observer__")
        assertThat(script).contains("#121212")
        assertThat(script).doesNotContain("GM_addStyle")
    }

    @Test
    fun `document start plugin css and matching userscripts are included`() {
        val page = "https://example.com/home"
        val cssPlugin = PluginSession.Resolved(
            plugin = Plugin(
                id = "css",
                kind = PluginKind.HCJ,
                name = "CSS",
                runAt = PluginRunAt.DOCUMENT_START,
            ),
            css = "body{background:#111}",
        )
        val lateCss = PluginSession.Resolved(
            plugin = Plugin(
                id = "late",
                kind = PluginKind.HCJ,
                name = "Late",
                runAt = PluginRunAt.DOCUMENT_END,
            ),
            css = "body{background:#eee}",
        )
        val userscript = PluginSession.Resolved(
            plugin = Plugin(
                id = "tm",
                kind = PluginKind.USERSCRIPT,
                name = "TM",
                matches = listOf(PluginMatchRule(pattern = "*://example.com/*")),
            ),
            mainJs = "GM_addStyle('body{color:red}')",
        )
        val otherHost = PluginSession.Resolved(
            plugin = Plugin(
                id = "other",
                kind = PluginKind.USERSCRIPT,
                name = "Other",
                matches = listOf(PluginMatchRule(pattern = "*://other.example/*")),
            ),
            mainJs = "GM_addStyle('body{color:blue}')",
        )
        val plugins = listOf(cssPlugin, lateCss, userscript, otherHost)

        val script = thumbnailPageScript(
            cosmeticPayloadJson = "",
            cssBlocks = thumbnailCssBlocks(plugins, page),
            scriptBodies = thumbnailScriptBodies(plugins, page, listOf("document.body.style.color='green'")),
        )

        assertThat(script).contains("body{background:#111}")
        assertThat(script).doesNotContain("body{background:#eee}")
        assertThat(script).contains("GM_addStyle('body{color:red}')")
        assertThat(script).contains("document.body.style.color='green'")
        assertThat(script).doesNotContain("body{color:blue}")
        assertThat(script).contains("function GM_addStyle")
    }

    @Test
    fun `no styles produce an empty script`() {
        assertThat(
            thumbnailPageScript("", emptyList(), emptyList())
        ).isEmpty()
    }
}
