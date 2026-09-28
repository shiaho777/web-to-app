package com.webtoapp.ui.screens

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Pins the issue #1095 contract: the "APK Export Config" drawer must not jank on
 * cold open. AnimatedVisibility disposes its subtree after the exit animation, so
 * every open after a few idle seconds re-pays full composition (headers, activity
 * result launchers, preset loading) inside the first animation frame. The drawer
 * therefore renders through WtaPersistentExpandedContent — height/alpha animation
 * over an always-composed subtree — and the network-trust preset load runs off the
 * composition thread.
 */
class ApkExportDrawerJankGuardTest {

    @Test
    fun `export config drawer keeps expanded content composed`() {
        val src = readSanitized("com/webtoapp/ui/screens/CreateAppScreen.kt")
        val drawer = src.substringAfter("fun ExportAndPermissionDrawer")
        assertWithMessage(
            "ExportAndPermissionDrawer must expand via WtaPersistentExpandedContent " +
                "so collapse does not dispose the subtree (#1095 cold-open jank)"
        ).that(drawer).contains("WtaPersistentExpandedContent(expanded = expanded)")
        assertWithMessage(
            "The drawer content must not be wrapped in a dispose-on-collapse AnimatedVisibility"
        ).that(drawer.substringBefore("WtaPersistentExpandedContent"))
            .doesNotContain("AnimatedVisibility(")
    }

    @Test
    fun `persistent expanded container animates height without disposing`() {
        val src = readSanitized("com/webtoapp/ui/animation/AnimationUtils.kt")
        assertWithMessage(
            "WtaPersistentExpandedContent must keep children composed and animate a " +
                "height fraction instead of adding/removing nodes"
        ).that(src).contains("fun WtaPersistentExpandedContent")
        assertWithMessage("The container must animate a layout height fraction")
            .that(src).contains("placeable.height * progress")
    }

    @Test
    fun `network trust presets load off the composition thread`() {
        val src = readSanitized("com/webtoapp/ui/screens/CreateAppApkSection.kt")
        assertWithMessage(
            "ConfigPresetStorage.loadNetworkTrust must not run synchronously inside " +
                "remember{} during composition — it does SharedPreferences + Gson work"
        ).that(src).doesNotContain("mutableStateOf(ConfigPresetStorage.loadNetworkTrust")
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Test harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
