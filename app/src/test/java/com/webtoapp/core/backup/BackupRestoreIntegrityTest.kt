package com.webtoapp.core.backup

import androidx.datastore.preferences.PreferencesProto
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.WebApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class BackupRestoreIntegrityTest {

    private val manager by lazy { DataBackupManager(RuntimeEnvironment.getApplication()) }

    private fun prefsBytes(vararg entries: Pair<String, PreferencesProto.Value>): ByteArray {
        val builder = PreferencesProto.PreferenceMap.newBuilder()
        entries.forEach { (name, value) -> builder.putPreferences(name, value) }
        return builder.build().toByteArray()
    }

    private fun boolValue(v: Boolean) = PreferencesProto.Value.newBuilder().setBoolean(v).build()
    private fun intValue(v: Int) = PreferencesProto.Value.newBuilder().setInteger(v).build()
    private fun stringValue(v: String) = PreferencesProto.Value.newBuilder().setString(v).build()

    @Test
    fun `activation keys follow the app id remap and unknown ids stay untouched`() {
        val bytes = prefsBytes(
            "activated_5" to boolValue(true),
            "usage_count_5" to intValue(3),
            "remote_url_5" to stringValue("https://example.com"),
            "activated_9" to boolValue(true)
        )
        val remapped = manager.remapActivationDataStore(bytes, mapOf(5L to 42L))
        val prefs = PreferencesProto.PreferenceMap.parseFrom(remapped)
        assertThat(prefs.preferencesMap.keys).containsExactly(
            "activated_42", "usage_count_42", "remote_url_42", "activated_9"
        )
        assertThat(prefs.preferencesMap["activated_42"]!!.boolean).isTrue()
        assertThat(prefs.preferencesMap["usage_count_42"]!!.integer).isEqualTo(3)
    }

    @Test
    fun `empty remap returns activation bytes unchanged`() {
        val bytes = prefsBytes("activated_5" to boolValue(true))
        assertThat(manager.remapActivationDataStore(bytes, emptyMap())).isEqualTo(bytes)
    }

    @Test
    fun `host runtime restore keeps feature toggles and drops device-bound mcp keys`() {
        val bytes = prefsBytes(
            "webapp_separate_tasks" to boolValue(true),
            "advanced_features" to boolValue(true),
            "host_mcp_enabled" to boolValue(true),
            "host_mcp_token" to stringValue("secret-token"),
            "host_mcp_port" to intValue(8765)
        )
        val sanitized = manager.stripHostRuntimeMcpKeys(bytes)
        val prefs = PreferencesProto.PreferenceMap.parseFrom(sanitized)
        assertThat(prefs.preferencesMap.keys).containsExactly("webapp_separate_tasks", "advanced_features")
    }

    @Test
    fun `gallery position keys follow the app id remap`() {
        val xml = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <int name="pos_5" value="2" />
    <int name="pos_9" value="7" />
    <string name="other">keep</string>
</map>"""
        val remapped = manager.remapGalleryPositionsXml(xml, mapOf(5L to 42L))
        assertThat(remapped).contains("pos_42")
        assertThat(remapped).doesNotContain("pos_5\"")
        assertThat(remapped).contains("pos_9")
        assertThat(remapped).contains("keep")
    }

    private fun appWithExtraMedia(): WebApp = WebApp(
        id = 5,
        name = "probe",
        url = "https://example.com",
        announcement = Announcement(customIconPath = "/data/app_icons/ann.png"),
        webViewConfig = com.webtoapp.data.model.WebViewConfig().let { wv ->
            wv.copy(
                errorPageConfig = wv.errorPageConfig.copy(customMediaPath = "/data/media_apps/err.png"),
                floatingWindowConfig = wv.floatingWindowConfig.copy(minimizedIconPath = "/data/app_icons/min.png")
            )
        }
    )

    @Test
    fun `export rewrite covers announcement icon, error page media and floating minimized icon`() {
        val app = appWithExtraMedia()
        val resources = mapOf(
            "resources/announcement/5_announcement_icon.png" to "/data/app_icons/ann.png",
            "resources/media/5_errorpage_media.png" to "/data/media_apps/err.png",
            "resources/icons/5_floating_minimized.png" to "/data/app_icons/min.png"
        )
        val rewritten = manager.updateAppPathsToRelative(app, resources)
        assertThat(rewritten.announcement?.customIconPath).isEqualTo("resources/announcement/5_announcement_icon.png")
        assertThat(rewritten.webViewConfig.errorPageConfig.customMediaPath).isEqualTo("resources/media/5_errorpage_media.png")
        assertThat(rewritten.webViewConfig.floatingWindowConfig.minimizedIconPath).isEqualTo("resources/icons/5_floating_minimized.png")
    }

    @Test
    fun `import rewrite resolves the three extra media fields back to local paths`() {
        val app = appWithExtraMedia().let {
            it.copy(
                announcement = it.announcement?.copy(customIconPath = "resources/announcement/5_announcement_icon.png"),
                webViewConfig = it.webViewConfig.copy(
                    errorPageConfig = it.webViewConfig.errorPageConfig.copy(customMediaPath = "resources/media/5_errorpage_media.png"),
                    floatingWindowConfig = it.webViewConfig.floatingWindowConfig.copy(minimizedIconPath = "resources/icons/5_floating_minimized.png")
                )
            )
        }
        val extracted = mapOf(
            "resources/announcement/5_announcement_icon.png" to "/restored/ann.png",
            "resources/media/5_errorpage_media.png" to "/restored/err.png",
            "resources/icons/5_floating_minimized.png" to "/restored/min.png"
        )
        val restored = manager.updateAppPathsToLocal(app, extracted)
        assertThat(restored.announcement?.customIconPath).isEqualTo("/restored/ann.png")
        assertThat(restored.webViewConfig.errorPageConfig.customMediaPath).isEqualTo("/restored/err.png")
        assertThat(restored.webViewConfig.floatingWindowConfig.minimizedIconPath).isEqualTo("/restored/min.png")
    }

    @Test
    fun `clone config is injected exactly once when re-cloning a cloned apk`() {
        val source = File("app/src/main/java/com/webtoapp/core/appmodifier/AppCloner.kt")
            .takeIf { it.isFile } ?: File("src/main/java/com/webtoapp/core/appmodifier/AppCloner.kt")
        val text = source.readText()
        val branch = text.substringAfter("entry.name == \"assets/clone_config.json\" ->")
            .substringBefore("injectSplashMediaPath != null")
        assertThat(branch).doesNotContain("writeEntryDeflated")
        val postLoop = text.substringAfter("if (injectConfigJson != null) {")
        assertThat(postLoop).contains("writeEntryDeflated(zipOut, \"assets/clone_config.json\"")
    }
}
