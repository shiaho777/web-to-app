package com.webtoapp.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.WebViewConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ConfigTemplateStoreTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private fun cleanStore() {
        context.filesDir.resolve("config_templates.json").delete()
    }

    @Test
    fun `save then list and get returns the template`() {
        cleanStore()
        val config = WebViewConfig(zoomEnabled = false, toolbarShowConsole = false)
        assertThat(ConfigTemplateStore.save(context, "My Setup", config)).isTrue()

        val listed = ConfigTemplateStore.list(context)
        assertThat(listed.map { it.name }).containsExactly("My Setup")

        val loaded = ConfigTemplateStore.get(context, "my setup")
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.webViewConfig.zoomEnabled).isFalse()
        assertThat(loaded.webViewConfig.toolbarShowConsole).isFalse()
    }

    @Test
    fun `save with the same name overwrites case-insensitively`() {
        cleanStore()
        ConfigTemplateStore.save(context, "Base", WebViewConfig(zoomEnabled = true))
        ConfigTemplateStore.save(context, "BASE", WebViewConfig(zoomEnabled = false))

        val listed = ConfigTemplateStore.list(context)
        assertThat(listed).hasSize(1)
        assertThat(listed.single().webViewConfig.zoomEnabled).isFalse()
    }

    @Test
    fun `invalid names are rejected`() {
        cleanStore()
        assertThat(ConfigTemplateStore.save(context, "   ", WebViewConfig())).isFalse()
        assertThat(ConfigTemplateStore.save(context, "x".repeat(41), WebViewConfig())).isFalse()
    }

    @Test
    fun `delete and rename operate case-insensitively and report misses`() {
        cleanStore()
        ConfigTemplateStore.save(context, "A", WebViewConfig())
        ConfigTemplateStore.save(context, "B", WebViewConfig())

        assertThat(ConfigTemplateStore.delete(context, "a")).isTrue()
        assertThat(ConfigTemplateStore.delete(context, "a")).isFalse()

        assertThat(ConfigTemplateStore.rename(context, "b", "C")).isTrue()
        assertThat(ConfigTemplateStore.list(context).map { it.name }).containsExactly("C")
        // renaming onto an existing name is rejected
        ConfigTemplateStore.save(context, "D", WebViewConfig())
        assertThat(ConfigTemplateStore.rename(context, "c", "d")).isFalse()
    }

    @Test
    fun exportThenImportRoundTripsTheConfigAndKeepsAnExistingName() {
        cleanStore()
        val config = WebViewConfig(zoomEnabled = false, userAgent = "ExportTest/1")
        val json = ConfigTemplateStore.encode("Shared", 1_700_000_000_000L, config)
        assertThat(json).contains("webtoapp-config-template")

        ConfigTemplateStore.save(context, "Shared", WebViewConfig(zoomEnabled = true))
        val imported = ConfigTemplateStore.importJson(context, json)
        assertThat(imported).isNotNull()
        assertThat(imported!!.name).isEqualTo("Shared 2")
        assertThat(imported.webViewConfig.zoomEnabled).isFalse()
        assertThat(imported.webViewConfig.userAgent).isEqualTo("ExportTest/1")
        assertThat(ConfigTemplateStore.get(context, "Shared")!!.webViewConfig.zoomEnabled).isTrue()
    }

    @Test
    fun importAcceptsABareConfigObjectAndRejectsUnrelatedJson() {
        cleanStore()
        val bare = """{"javaScriptEnabled":false,"userAgent":"Bare/1"}"""
        val imported = ConfigTemplateStore.importJson(context, bare)
        assertThat(imported).isNotNull()
        assertThat(imported!!.name).isEqualTo("Imported")
        assertThat(imported.webViewConfig.javaScriptEnabled).isFalse()
        assertThat(imported.webViewConfig.userAgent).isEqualTo("Bare/1")

        assertThat(ConfigTemplateStore.decode("{not json")).isNull()
        assertThat(ConfigTemplateStore.decode("""{"foo":1}""")).isNull()
        assertThat(ConfigTemplateStore.importJson(context, """{"kind":"other","webViewConfig":{}}""")).isNull()
    }

    @Test
    fun `list on a fresh install is empty and a corrupt file degrades to empty`() {
        cleanStore()
        assertThat(ConfigTemplateStore.list(context)).isEmpty()
        context.filesDir.resolve("config_templates.json").writeText("{not json")
        assertThat(ConfigTemplateStore.list(context)).isEmpty()
    }
}
