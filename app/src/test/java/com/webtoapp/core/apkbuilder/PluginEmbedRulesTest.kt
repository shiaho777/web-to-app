package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.EmbeddedShellPlugin
import com.webtoapp.ui.shell.mergeSitePlugins
import org.junit.Test

class PluginEmbedRulesTest {

    @Test
    fun `one failing plugin does not drop the others`() {
        val embedded = embedPlugins(listOf("bad", "missing", "ok")) { id ->
            when (id) {
                "bad" -> throw IllegalStateException("boom")
                "missing" -> null
                else -> id
            }
        }
        assertThat(embedded).containsExactly("ok")
    }

    @Test
    fun `config json at or above 1 MiB is stored`() {
        val limit = ZipUtils.ASSET_UNCOMPRESSED_STORE_THRESHOLD
        assertThat(ZipUtils.configBytesShouldBeStored(limit - 1)).isFalse()
        assertThat(ZipUtils.configBytesShouldBeStored(limit)).isTrue()
        assertThat(ZipUtils.configBytesShouldBeStored(limit + 1)).isTrue()
    }

    @Test
    fun `parent plugins stay on a site when the site does not use its own config`() {
        val parent = EmbeddedShellPlugin(id = "parent", name = "Parent")
        val site = EmbeddedShellPlugin(id = "site", name = "Site")
        val merged = mergeSitePlugins(
            sitesUseOwnConfig = false,
            parentEnabled = true,
            parentIds = listOf("parent"),
            parentPlugins = listOf(parent),
            siteEnabled = true,
            siteIds = listOf("site"),
            sitePlugins = listOf(site),
        )
        assertThat(merged.pluginsEnabled).isTrue()
        assertThat(merged.pluginIds).containsExactly("parent")
        assertThat(merged.embeddedPlugins.map { it.id }).containsExactly("parent")
    }

    @Test
    fun `a site plugin does not erase the parent and wins an id collision`() {
        val parent = EmbeddedShellPlugin(id = "shared", name = "Parent")
        val siteShared = EmbeddedShellPlugin(id = "shared", name = "Site")
        val siteOnly = EmbeddedShellPlugin(id = "site-only", name = "Only")
        val merged = mergeSitePlugins(
            sitesUseOwnConfig = true,
            parentEnabled = true,
            parentIds = listOf("shared", "parent-only"),
            parentPlugins = listOf(parent, EmbeddedShellPlugin(id = "parent-only")),
            siteEnabled = false,
            siteIds = listOf("site-only", "shared"),
            sitePlugins = listOf(siteOnly, siteShared),
        )
        assertThat(merged.pluginsEnabled).isTrue()
        assertThat(merged.pluginIds).containsExactly("site-only", "shared", "parent-only").inOrder()
        assertThat(merged.embeddedPlugins.map { it.id })
            .containsExactly("site-only", "shared", "parent-only").inOrder()
        assertThat(merged.embeddedPlugins.first { it.id == "shared" }.name).isEqualTo("Site")
    }

    @Test
    fun `own config off does not turn plugins on from the site alone`() {
        val merged = mergeSitePlugins(
            sitesUseOwnConfig = false,
            parentEnabled = false,
            parentIds = emptyList(),
            parentPlugins = emptyList(),
            siteEnabled = true,
            siteIds = listOf("site"),
            sitePlugins = listOf(EmbeddedShellPlugin(id = "site")),
        )
        assertThat(merged.pluginsEnabled).isFalse()
        assertThat(merged.embeddedPlugins).isEmpty()
    }
}
