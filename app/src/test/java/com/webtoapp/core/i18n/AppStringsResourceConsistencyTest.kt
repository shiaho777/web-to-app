package com.webtoapp.core.i18n

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AppStringsResourceConsistencyTest {

    private companion object {

        val NON_LOCALIZED_STRING_KEYS = setOf(

            "app_name",
        )

        val LOCALES = listOf("values-zh", "values-en", "values-ar")
    }

    @Test
    fun `grouped app strings stay aligned across locales`() {
        val resDir = resolveExistingDir("app/src/main/res", "src/main/res")
        val defaultDir = File(resDir, "values")
        val defaultFiles = defaultDir.listFiles { file ->
            file.isFile && file.name.startsWith("app_strings_") && file.name.endsWith(".xml")
        }?.sortedBy { it.name }.orEmpty()

        assertThat(defaultFiles).isNotEmpty()
        val expectedFileNames = defaultFiles.map { it.name }

        LOCALES.forEach { localeDir ->
            val actualNames = File(resDir, localeDir)
                .listFiles { file ->
                    file.isFile && file.name.startsWith("app_strings_") && file.name.endsWith(".xml")
                }
                ?.map { it.name }
                ?.sorted()
                .orEmpty()
            assertWithMessage("Locale file set mismatch for $localeDir")
                .that(actualNames)
                .containsExactlyElementsIn(expectedFileNames)
        }

        defaultFiles.forEach { defaultFile ->
            val expectedKeys = readStringKeys(defaultFile)
            LOCALES.forEach { localeDir ->
                val localeFile = File(resDir, "$localeDir/${defaultFile.name}")
                val actualKeys = readStringKeys(localeFile)
                assertWithMessage("Key mismatch for ${defaultFile.name} in $localeDir")
                    .that(actualKeys)
                    .containsExactlyElementsIn(expectedKeys)
            }
        }
    }

    @Test
    fun `top-level strings xml stays aligned across locales`() {
        val resDir = resolveExistingDir("app/src/main/res", "src/main/res")
        val defaultFile = File(resDir, "values/strings.xml")
        assertWithMessage("values/strings.xml must exist")
            .that(defaultFile.exists()).isTrue()

        val defaultKeys = readStringKeys(defaultFile).toSet()
        val translatable = defaultKeys - NON_LOCALIZED_STRING_KEYS

        LOCALES.forEach { localeDir ->
            val localeFile = File(resDir, "$localeDir/strings.xml")
            assertWithMessage("$localeDir/strings.xml must exist")
                .that(localeFile.exists()).isTrue()

            val localeKeys = readStringKeys(localeFile).toSet()

            val missing = translatable - localeKeys
            assertWithMessage(
                "Missing translations in $localeDir/strings.xml. " +
                    "Add the following keys: $missing"
            ).that(missing).isEmpty()

            val orphan = localeKeys - defaultKeys
            assertWithMessage(
                "Orphan keys in $localeDir/strings.xml that are absent from " +
                    "values/strings.xml: $orphan"
            ).that(orphan).isEmpty()

            val redeclaredNonLocalised = NON_LOCALIZED_STRING_KEYS.intersect(localeKeys)
            assertWithMessage(
                "$localeDir/strings.xml redeclares keys that are intentionally " +
                    "non-localised in values/. Either translate them by " +
                    "removing them from NON_LOCALIZED_STRING_KEYS, or drop the " +
                    "locale-specific copy. Offending keys: $redeclaredNonLocalised"
            ).that(redeclaredNonLocalised).isEmpty()
        }
    }

    private fun readStringKeys(file: File): List<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        return buildList {
            for (index in 0 until nodes.length) {
                val element = nodes.item(index)
                val name = element.attributes?.getNamedItem("name")?.nodeValue
                if (!name.isNullOrBlank()) {
                    add(name)
                }
            }
        }
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
                appendLine("R.string.* is forbidden because res/values*/ is not maintained")
                appendLine("for all 10 locales — resource lookups silently fall back to the")
                appendLine("default values/ (Chinese) for pt/es/fr/de/ru/ja/ko, masking")
                appendLine("missing translations. Use Strings.xxx (or Strings.funName(arg)")
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
