package com.webtoapp.core.i18n

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AppStringsResourceConsistencyTest {

    private companion object {

        // The only keys allowed in res/values/strings.xml. Each must also carry
        // translatable="false" — anything user-visible belongs in Strings.kt.
        val NON_LOCALIZED_STRING_KEYS = setOf(

            "app_name",
        )

        // values-<lang>, values-<lang>-r<region>, values-b+<lang>… but NOT
        // non-locale qualifiers such as values-night.
        val LOCALE_QUALIFIER = Regex("^(?:[a-z]{2,3}(?:-r[A-Za-z]{2,3})?|b\\+.*)$")
    }

    @Test
    fun `values strings xml only holds non-localised resources`() {
        val resDir = resolveExistingDir("app/src/main/res", "src/main/res")
        val defaultFile = File(resDir, "values/strings.xml")
        assertWithMessage("values/strings.xml must exist")
            .that(defaultFile.exists()).isTrue()

        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(defaultFile)
        val nodes = document.getElementsByTagName("string")
        val offenders = mutableListOf<String>()
        for (index in 0 until nodes.length) {
            val element = nodes.item(index)
            val name = element.attributes?.getNamedItem("name")?.nodeValue ?: continue
            val translatable = element.attributes?.getNamedItem("translatable")?.nodeValue
            if (name !in NON_LOCALIZED_STRING_KEYS || translatable != "false") {
                offenders += "$name (translatable=$translatable)"
            }
        }

        assertWithMessage(
            buildString {
                appendLine("res/values/strings.xml must only contain translatable=\"false\"")
                appendLine("resources from NON_LOCALIZED_STRING_KEYS.")
                appendLine("All user-visible text belongs in Strings.kt as inline")
                appendLine("when(Strings.lang) blocks covering all 10 languages — resource")
                appendLine("lookups cannot cover locales that have no values-*/ directory and")
                appendLine("silently fall back to the default values/ (Chinese).")
                appendLine()
                appendLine("Offending entries:")
                offenders.forEach { appendLine("  $it") }
            }
        ).that(offenders).isEmpty()
    }

    @Test
    fun `no locale values dirs or grouped app strings files exist`() {
        val resDir = resolveExistingDir("app/src/main/res", "src/main/res")

        val localeDirs = resDir.listFiles { file ->
            file.isDirectory &&
                file.name.startsWith("values-") &&
                LOCALE_QUALIFIER.matches(file.name.removePrefix("values-"))
        }?.map { it.name }?.sorted().orEmpty()
        assertWithMessage(
            "Locale values-*/ directories must not exist — user-visible text lives in " +
                "Strings.kt (all 10 languages inline), and partial locale resources " +
                "silently fall back to Chinese for the missing 7. Found: $localeDirs"
        ).that(localeDirs).isEmpty()

        val groupedFiles = File(resDir, "values").listFiles { file ->
            file.isFile && file.name.startsWith("app_strings_") && file.name.endsWith(".xml")
        }?.map { it.name }?.sorted().orEmpty()
        assertWithMessage(
            "Grouped app_strings_*.xml files must not exist — they were dead duplicates " +
                "of Strings.kt content, referenced by no code. Found: $groupedFiles"
        ).that(groupedFiles).isEmpty()
    }

    @Test
    fun `kotlin source never references R string for user-visible text`() {
        val sourceRoots = listOf(
            "app/src/main/java",
            "src/main/java",
        ).map(::File).filter(File::exists)
        assertWithMessage("Could not locate Kotlin source root")
            .that(sourceRoots).isNotEmpty()

        val offenders = mutableListOf<String>()
        sourceRoots.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    val relPath = file.relativeTo(root).path
                    file.useLines { lines ->
                        lines.forEachIndexed { index, raw ->
                            val line = raw.trim()
                            if (line.startsWith("//") || line.startsWith("*")) {
                                return@forEachIndexed
                            }
                            if (R_STRING_REFERENCE.containsMatchIn(line)) {
                                offenders += "${relPath}:${index + 1}: ${raw.trim()}"
                            }
                        }
                    }
                }
        }

        assertWithMessage(
            buildString {
                appendLine("Kotlin source references R.string.* for user-visible text.")
                appendLine("All user-facing strings must live in Strings.kt as inline")
                appendLine("when(Strings.lang) blocks covering all 10 languages.")
                appendLine()
                appendLine("R.string.* is forbidden because res/values/ only holds")
                appendLine("translatable=\"false\" resources — any localized string there")
                appendLine("cannot cover the 10 languages and silently falls back to the")
                appendLine("default values/ (Chinese). Use Strings.xxx (or Strings.funName(arg)")
                appendLine("for parameterised strings) instead.")
                appendLine()
                appendLine("Offending references:")
                offenders.forEach { appendLine("  $it") }
            }
        ).that(offenders).isEmpty()
    }

    private val R_STRING_REFERENCE = Regex("""\bR\.string\.[A-Za-z_][A-Za-z0-9_]*""")

    private fun resolveExistingDir(vararg candidates: String): File {
        return candidates
            .asSequence()
            .map(::File)
            .firstOrNull(File::exists)
            ?: error("Cannot locate resource directory from: ${candidates.joinToString()}")
    }
}
