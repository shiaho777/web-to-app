package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import org.junit.Test
import java.io.File

class BuiltinPluginI18nTest {

    @Test
    fun `every built-in plugin id has a translated name and description`() {
        val original = Strings.currentLanguage.value
        val ids = builtinIds()
        assertThat(ids).isNotEmpty()
        try {
            for (lang in AppLanguage.entries) {
                Strings.setLanguage(lang)
                for (id in ids) {
                    val name = Strings.builtinPluginName(id, "FALLBACK")
                    val desc = Strings.builtinPluginDescription(id, "FALLBACK")
                    assertThat(name).isNotEqualTo("FALLBACK")
                    assertThat(name).isNotEmpty()
                    assertThat(desc).isNotEqualTo("FALLBACK")
                    assertThat(desc).isNotEmpty()
                }
            }
        } finally {
            Strings.setLanguage(original)
        }
    }

    @Test
    fun `unknown ids keep the fallback`() {
        assertThat(Strings.builtinPluginName("custom-foo", "Keep me")).isEqualTo("Keep me")
        assertThat(Strings.builtinPluginDescription("custom-foo", "Keep me")).isEqualTo("Keep me")
    }

    @Test
    fun `english names match on-disk manifests`() {
        val original = Strings.currentLanguage.value
        try {
            Strings.setLanguage(AppLanguage.ENGLISH)
            for (dir in builtinDirs()) {
                val json = File(dir, "plugin.json").readText()
                val name = Regex(""""name"\s*:\s*"([^"]+)"""").find(json)
                    ?.groupValues?.get(1)
                    ?: error("missing name in ${dir.name}/plugin.json")
                assertThat(Strings.builtinPluginName(dir.name, "FALLBACK")).isEqualTo(name)
            }
        } finally {
            Strings.setLanguage(original)
        }
    }

    @Test
    fun `chinese names differ from the english manifest`() {
        val original = Strings.currentLanguage.value
        try {
            Strings.setLanguage(AppLanguage.CHINESE)
            for (dir in builtinDirs()) {
                val json = File(dir, "plugin.json").readText()
                val english = Regex(""""name"\s*:\s*"([^"]+)"""").find(json)
                    ?.groupValues?.get(1)
                    ?: error("missing name in ${dir.name}/plugin.json")
                val localized = Strings.builtinPluginName(dir.name, "FALLBACK")
                assertThat(localized).isNotEqualTo(english)
                assertThat(localized).isNotEqualTo("FALLBACK")
            }
        } finally {
            Strings.setLanguage(original)
        }
    }

    @Test
    fun `built-in Plugin records localize only when builtIn is true`() {
        val original = Strings.currentLanguage.value
        try {
            Strings.setLanguage(AppLanguage.CHINESE)
            val builtin = Plugin(
                id = "builtin-dark-mode",
                name = "Dark Mode",
                description = "Smart inversion",
                builtIn = true
            )
            val userCopy = builtin.copy(builtIn = false)
            assertThat(builtin.localizedName()).isEqualTo(Strings.builtinDarkMode)
            assertThat(builtin.localizedDescription()).isEqualTo(Strings.builtinDarkModeDesc)
            assertThat(userCopy.localizedName()).isEqualTo("Dark Mode")
            assertThat(userCopy.localizedDescription()).isEqualTo("Smart inversion")
        } finally {
            Strings.setLanguage(original)
        }
    }

    private fun builtinIds(): List<String> = builtinDirs().map { it.name }

    private fun builtinDirs(): List<File> {
        val dir = listOf(
            File("app/src/main/assets/plugins"),
            File("src/main/assets/plugins"),
        ).firstOrNull { it.isDirectory } ?: error("could not find assets/plugins")
        return dir.listFiles { file -> file.isDirectory && file.name.startsWith("builtin-") }
            ?.sortedBy { it.name }
            .orEmpty()
            .toList()
    }
}
