package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeckoPluginHostTest {

    @Test
    fun `page plugin script carries saved config css and the page body`() {
        val plugin = Plugin(
            id = "builtin-dark-mode",
            kind = PluginKind.HCJ,
            name = "Dark Mode",
            runAt = PluginRunAt.DOCUMENT_START,
        )
        val resolved = PluginSession.Resolved(
            plugin = plugin,
            mainJs = "hcj.config.get('enabled', false);",
            css = "html{filter:invert(1)}",
        )

        val script = GeckoPluginHost.pageScript(
            plugins = listOf(resolved),
            url = "https://ittf.com/",
            appLang = "en",
            configSeedJson = """{"builtin-dark-mode":{"enabled":true}}""",
        )

        assertThat(script).contains("__wta_hcj_cfg__")
        assertThat(script).contains("hcj.config.get('enabled', false);")
        assertThat(script).contains("html{filter:invert(1)}")
        assertThat(script).contains("wta-dark-mode")
        assertThat(script).contains("builtin-dark-mode")
    }

    @Test
    fun `chrome extensions are left out`() {
        val extension = PluginSession.Resolved(
            plugin = Plugin(
                id = "ublock",
                kind = PluginKind.CHROME_EXTENSION,
                name = "uBlock",
            ),
            mainJs = "chrome.runtime.sendMessage('nope')",
        )

        assertThat(GeckoPluginHost.runnable(listOf(extension))).isEmpty()
        assertThat(
            GeckoPluginHost.pageScript(listOf(extension), "https://ittf.com/", "en", "{}")
        ).isEmpty()
    }

    @Test
    fun `a userscript style call is wrapped`() {
        val script = PluginSession.Resolved(
            plugin = Plugin(
                id = "tm",
                kind = PluginKind.USERSCRIPT,
                name = "TM",
            ),
            mainJs = "GM_addStyle('body{background:#111}')",
        )

        val page = GeckoPluginHost.pageScript(
            listOf(script),
            "https://ittf.com/",
            "en",
            "{}",
        )

        assertThat(page).contains("GM_addStyle('body{background:#111}')")
        assertThat(page).contains("function GM_addStyle")
        assertThat(page).doesNotContain("__hcjBridge")
    }
}
