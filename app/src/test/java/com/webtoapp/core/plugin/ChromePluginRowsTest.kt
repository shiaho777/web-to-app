package com.webtoapp.core.plugin

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.webtoapp.core.extension.ExtensionModule
import com.webtoapp.core.extension.ModuleSourceType
import com.webtoapp.core.extension.ModuleVersion
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ChromePluginRowsTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Before
    fun resetStore() {
        PluginStore.release()
        File(context.filesDir, PluginStore.STATE_FILE).delete()
        File(context.filesDir, PluginStore.PLUGINS_DIR).deleteRecursively()
        File(context.filesDir, "extensions").deleteRecursively()
        File(context.filesDir, "extension_modules").deleteRecursively()
    }

    @After
    fun releaseStore() {
        PluginStore.release()
    }

    @Test
    fun `one extension with several content scripts is one plugin row`() = runBlocking<Unit> {
        val store = PluginStore.getInstance(context)
        store.awaitLoaded()
        val result = PluginImporter(context).installChromeRecords(
            listOf(
                chromeModule("e_cs_0", "Dark Reader [1]", popup = "popup.html", background = "bg.js"),
                chromeModule("e_cs_1", "Dark Reader [2]"),
                chromeModule("e_cs_2", "Dark Reader [3]")
            )
        )
        assertThat(result).isInstanceOf(PluginImporter.ImportResult.Success::class.java)
        val chrome = store.plugins.value.filter { it.kind == PluginKind.CHROME_EXTENSION }
        assertThat(chrome).hasSize(1)
        assertThat(chrome.single().id).isEqualTo("e_cs_0")
        assertThat(chrome.single().name).isEqualTo("Dark Reader")
        assertThat(chrome.single().chromeExtId).isEqualTo("ext")
        assertThat(chrome.single().popupPath).isEqualTo("popup.html")
        assertThat(chrome.single().backgroundScript).isEqualTo("bg.js")
        assertThat(chrome.single().manifestJson).contains("Dark Reader")
        assertThat(store.enabledIds.value).contains("e_cs_0")
    }

    @Test
    fun `a second copy of another directory stays its own row`() = runBlocking<Unit> {
        val store = PluginStore.getInstance(context)
        store.awaitLoaded()
        PluginImporter(context).installChromeRecords(
            listOf(chromeModule("e_cs_0", "Dark Reader", extId = "ext-a"))
        )
        PluginImporter(context).installChromeRecords(
            listOf(
                chromeModule("f_cs_0", "Dark Reader [1]", extId = "ext-b"),
                chromeModule("f_cs_1", "Dark Reader [2]", extId = "ext-b")
            )
        )
        val ids = store.plugins.value
            .filter { it.kind == PluginKind.CHROME_EXTENSION }
            .map { it.chromeExtId }
        assertThat(ids).containsExactly("ext-a", "ext-b")
    }

    @Test
    fun `load folds sibling rows and reads popup from the unpacked extension`() = runBlocking<Unit> {
        File(context.filesDir, "extensions/ext").mkdirs()
        File(context.filesDir, "extensions/ext/manifest.json").writeText(
            """
            {"name":"Dark Reader","version":"1.0",
             "action":{"default_popup":"popup.html"},
             "background":{"service_worker":"bg.js"},
             "options_ui":{"page":"options.html"}}
            """.trimIndent()
        )
        writeChromeState(
            listOf(
                chromePlugin("ext_cs_0", "Dark Reader [1]"),
                chromePlugin("ext_cs_1", "Dark Reader [2]"),
                chromePlugin("ext_cs_2", "Dark Reader [3]")
            ),
            enabled = listOf("ext_cs_2")
        )
        val store = PluginStore.getInstance(context)
        store.awaitLoaded()
        val chrome = store.plugins.value.filter { it.kind == PluginKind.CHROME_EXTENSION }
        assertThat(chrome.map { it.id }).containsExactly("ext_cs_0")
        assertThat(chrome.single().name).isEqualTo("Dark Reader")
        assertThat(chrome.single().popupPath).isEqualTo("popup.html")
        assertThat(chrome.single().backgroundScript).isEqualTo("bg.js")
        assertThat(chrome.single().optionsPagePath).isEqualTo("options.html")
        assertThat(chrome.single().manifestJson).contains("default_popup")
        assertThat(store.enabledIds.value).containsExactly("ext_cs_0")
        assertThat(store.getPlugin("ext_cs_2")?.id).isEqualTo("ext_cs_0")
        assertThat(store.getPluginsByIds(listOf("ext_cs_1", "ext_cs_0")).map { it.id })
            .containsExactly("ext_cs_0")
        assertThat(store.effectiveIds(true, listOf("ext_cs_1", "ext_cs_2")))
            .containsExactly("ext_cs_0")
    }

    @Test
    fun `deleting one sibling removes the extension and its files`() = runBlocking<Unit> {
        val extDir = File(context.filesDir, "extensions/ext")
        extDir.mkdirs()
        File(extDir, "manifest.json").writeText("""{"name":"Dark Reader","version":"1"}""")
        writeChromeState(
            listOf(
                chromePlugin("ext_cs_0", "Dark Reader [1]"),
                chromePlugin("ext_cs_1", "Dark Reader [2]")
            ),
            enabled = listOf("ext_cs_0", "ext_cs_1")
        )
        val store = PluginStore.getInstance(context)
        store.awaitLoaded()
        assertThat(store.removePlugin("ext_cs_1")).isTrue()
        assertThat(store.plugins.value.filter { it.kind == PluginKind.CHROME_EXTENSION }).isEmpty()
        assertThat(store.getPlugin("ext_cs_0")).isNull()
        assertThat(extDir.exists()).isFalse()
        assertThat(store.enabledIds.value).doesNotContain("ext_cs_0")
        assertThat(store.enabledIds.value).doesNotContain("ext_cs_1")
    }

    @Test
    fun `collapse keeps the first row and maps the others onto it`() {
        val rows = listOf(
            chromePlugin("ext_cs_0", "Dark Reader [1]"),
            chromePlugin("ext_cs_1", "Dark Reader [2]", popup = "popup.html"),
            Plugin(id = "script", name = "Script", kind = PluginKind.USERSCRIPT)
        )
        val collapsed = collapseChromePlugins(rows, enabledIds = listOf("ext_cs_1", "script"))
        assertThat(collapsed.plugins.map { it.id }).containsExactly("ext_cs_0", "script").inOrder()
        assertThat(collapsed.plugins.first().name).isEqualTo("Dark Reader")
        assertThat(collapsed.plugins.first().popupPath).isEqualTo("popup.html")
        assertThat(collapsed.enabledIds).containsExactly("ext_cs_0", "script").inOrder()
        assertThat(collapsed.aliases).containsEntry("ext_cs_1", "ext_cs_0")
        assertThat(collapsed.changed).isTrue()
        val again = collapseChromePlugins(
            collapsed.plugins, collapsed.enabledIds, collapsed.aliases
        )
        assertThat(again.changed).isFalse()
    }

    @Test
    fun `working copy drops a deleted row during a drag and keeps the drag order`() {
        val a = Plugin(id = "a", name = "A")
        val b = Plugin(id = "b", name = "B")
        val c = Plugin(id = "c", name = "C")
        assertThat(
            syncPluginWorkingCopy(listOf(c, a, b), listOf(a, c), holdWorkingOrder = true).map { it.id }
        ).containsExactly("c", "a").inOrder()
        assertThat(releasePluginOrderHold(listOf("b", "a"), listOf("a", "b"), dragging = false))
            .containsExactly("b", "a").inOrder()
        assertThat(releasePluginOrderHold(listOf("b", "a"), listOf("b", "a"), dragging = false)).isNull()
        assertThat(releasePluginOrderHold(listOf("b", "a"), listOf("a"), dragging = false)).isNull()
        assertThat(releasePluginOrderHold(listOf("b", "a"), listOf("a", "b"), dragging = true))
            .containsExactly("b", "a").inOrder()
        assertThat(
            syncPluginWorkingCopy(listOf(b, a), listOf(a, b), holdWorkingOrder = true).map { it.id }
        ).containsExactly("b", "a").inOrder()
        assertThat(
            syncPluginWorkingCopy(listOf(b, a), listOf(a, b), holdWorkingOrder = false).map { it.id }
        ).containsExactly("a", "b").inOrder()
    }

    private fun chromeModule(
        id: String,
        name: String,
        extId: String = "ext",
        popup: String = "",
        background: String = ""
    ) = ExtensionModule(
        id = id,
        name = name,
        sourceType = ModuleSourceType.CHROME_EXTENSION,
        chromeExtId = extId,
        popupPath = popup,
        backgroundScript = background,
        manifestJson = """{"name":"Dark Reader"}""",
        version = ModuleVersion(name = "4.9")
    )

    private fun writeChromeState(records: List<Plugin>, enabled: List<String>) {
        val gson = Gson()
        val root = JsonObject()
        val order = JsonArray()
        val enabledJson = JsonArray()
        val chrome = JsonArray()
        records.forEach {
            order.add(it.id)
            chrome.add(gson.toJsonTree(it))
        }
        enabled.forEach { enabledJson.add(it) }
        root.add("order", order)
        root.add("enabled", enabledJson)
        root.add("chromeRecords", chrome)
        File(context.filesDir, PluginStore.STATE_FILE).writeText(gson.toJson(root))
    }

    private fun chromePlugin(id: String, name: String, popup: String = "") = Plugin(
        id = id,
        name = name,
        kind = PluginKind.CHROME_EXTENSION,
        chromeExtId = "ext",
        popupPath = popup
    )
}
