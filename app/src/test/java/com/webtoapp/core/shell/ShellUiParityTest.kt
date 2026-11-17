package com.webtoapp.core.shell

import com.google.common.truth.Truth.assertWithMessage
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.MediaConfig
import org.junit.Test
import java.io.File

class ShellUiParityTest {

    data class ShellUiParityPair(
        val name: String,
        val modelClass: Class<*>,
        val hostFiles: List<String>,
        val shellFiles: List<String>,
        val shellQualifiers: List<String>,
        val renames: Map<String, String> = emptyMap(),
        val allow: Map<String, String> = emptyMap()
    )

    private val pairs = listOf(
        ShellUiParityPair(
            name = "gallery",
            modelClass = GalleryConfig::class.java,
            hostFiles = listOf(
                "com/webtoapp/ui/gallery/GalleryPlayerScreen.kt",
                "com/webtoapp/ui/gallery/GalleryPlayerActivity.kt"
            ),
            shellFiles = listOf(
                "com/webtoapp/ui/shell/ShellGallery.kt",
                "com/webtoapp/ui/shell/ShellScreen.kt"
            ),
            shellQualifiers = listOf("galleryConfig.")
        ),
        ShellUiParityPair(
            name = "media",
            modelClass = MediaConfig::class.java,
            hostFiles = listOf(
                "com/webtoapp/ui/media/MediaAppActivity.kt"
            ),
            shellFiles = listOf(
                "com/webtoapp/ui/shell/ShellMediaContent.kt",
                "com/webtoapp/ui/shell/ShellScreen.kt"
            ),
            shellQualifiers = listOf("mediaConfig."),
            renames = mapOf(
                "orientation" to "landscape"
            ),
            allow = mapOf(
                "mediaPath" to "fixed asset path, preview-only parameter"
            )
        )
    )

    @Test
    fun `shell consumes every host-consumed model field`() {
        val javaRoot = resolveExistingDir("app/src/main/java", "src/main/java")
        val failures = mutableListOf<String>()
        for (pair in pairs) {
            val modelFields = pair.modelClass.declaredFields
                .filter { !it.isSynthetic && it.name != "Companion" }
                .map { it.name }
                .toSet()

            val staleAllow = pair.allow.keys - modelFields
            if (staleAllow.isNotEmpty()) {
                failures += "[${pair.name}] stale allow entries (field gone from ${pair.modelClass.simpleName}): $staleAllow"
            }

            val hostSources = pair.hostFiles.associateWith { readSanitized(javaRoot, it) }
            val shellSources = pair.shellFiles.associateWith { readSanitized(javaRoot, it) }

            val required = modelFields.filter { field ->
                hostSources.values.any { src -> containsFieldRef(src, field) }
            }
            for (field in required) {
                if (field in pair.allow) continue
                val shellField = pair.renames[field] ?: field
                val covered = shellSources.values.any { src ->
                    containsFieldRef(src, shellField) ||
                        pair.shellQualifiers.any { q -> containsRef(src, q, shellField) }
                }
                if (!covered) {
                    val hostHits = hostSources.filter { (_, src) ->
                        containsFieldRef(src, field)
                    }.keys
                    failures += "[${pair.name}] '${pair.modelClass.simpleName}.$field' read by host $hostHits " +
                        "but not by shell ${pair.shellFiles}. Port the behavior to shell " +
                        "(see thumbnail bar, #781) or add an allow entry with a reason."
                }
            }
        }
        assertWithMessage(
            "Shell/host UI parity violations:\n" + failures.joinToString("\n")
        ).that(failures).isEmpty()
    }

    private fun readSanitized(javaRoot: File, relativePath: String): String {
        val file = File(javaRoot, relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        return stripCodeNoise(file.readText())
    }

    private fun containsFieldRef(sanitized: String, field: String): Boolean {
        val pattern = Regex("(?<![A-Za-z0-9_$])" + Regex.escape(field) + "(?![A-Za-z0-9_])")
        return pattern.containsMatchIn(sanitized)
    }

    private fun containsRef(sanitized: String, qualifier: String, field: String): Boolean {
        val pattern = Regex("(?<![A-Za-z0-9_$])" + Regex.escape(qualifier + field) + "(?![A-Za-z0-9_])")
        return pattern.containsMatchIn(sanitized)
    }

    private fun stripCodeNoise(source: String): String {
        var s = source
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    private fun resolveExistingDir(vararg candidates: String): File {
        return candidates.asSequence().map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory from: ${candidates.joinToString()}")
    }
}
