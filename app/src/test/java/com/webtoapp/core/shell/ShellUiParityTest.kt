package com.webtoapp.core.shell

import com.google.common.truth.Truth.assertWithMessage
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.MediaConfig
import org.junit.Test
import java.io.File

/**
 * Preview/export UI parity gate (incident: gallery thumbnail bar, #781).
 *
 * Rule: every field of a per-type host model that host runtime code reads must
 * also be read by shell runtime code. Otherwise the editor shows a switch, the
 * preview honors it, and the exported APK silently ignores it.
 *
 * When you add a field to a covered model AND read it in host runtime UI, this
 * test fails naming the field until you either port the behavior to shell or
 * add an explicit allow entry with a reason. When you genuinely consume a new
 * flag in shell, nothing needs to change here.
 *
 * Covered pairs (extend by adding a [ShellUiParityPair], not by editing logic):
 * - Gallery: GalleryPlayerScreen/Activity vs ShellGallery/ShellScreen
 * - Media: MediaAppActivity vs ShellMediaContent/ShellScreen
 */
class ShellUiParityTest {

    data class ShellUiParityPair(
        val name: String,
        val modelClass: Class<*>,
        val hostFiles: List<String>,
        val hostQualifiers: List<String>,
        val shellFiles: List<String>,
        val shellQualifiers: List<String>,
        /** Host field name -> shell-side name when they differ (media orientation -> landscape). */
        val renames: Map<String, String> = emptyMap(),
        /** Host field name -> reason it needs no shell consumer. */
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
            hostQualifiers = listOf("config.", "galleryConfig."),
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
            hostQualifiers = listOf("mediaConfig.", "config."),
            shellFiles = listOf(
                "com/webtoapp/ui/shell/ShellMediaContent.kt",
                "com/webtoapp/ui/shell/ShellScreen.kt"
            ),
            shellQualifiers = listOf("mediaConfig."),
            renames = mapOf(
                // Export flattens SplashOrientation to a boolean (buildMediaBlock).
                "orientation" to "landscape"
            ),
            allow = mapOf(
                // Asset path is fixed at export (media_content.*); the shell
                // mediaPath parameter is host-preview-only.
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
                pair.hostQualifiers.any { q ->
                    hostSources.values.any { src -> containsRef(src, q, field) }
                }
            }
            for (field in required) {
                if (field in pair.allow) continue
                val shellField = pair.renames[field] ?: field
                val covered = pair.shellQualifiers.any { q ->
                    shellSources.values.any { src -> containsRef(src, q, shellField) }
                }
                if (!covered) {
                    val hostHits = hostSources.filter { (_, src) ->
                        pair.hostQualifiers.any { q -> containsRef(src, q, field) }
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

    private fun containsRef(sanitized: String, qualifier: String, field: String): Boolean {
        // qualifier + field as one token reference, e.g. "galleryConfig.showThumbnailBar".
        val pattern = Regex("(?<![A-Za-z0-9_$])" + Regex.escape(qualifier + field) + "(?![A-Za-z0-9_])")
        return pattern.containsMatchIn(sanitized)
    }

    private fun stripCodeNoise(source: String): String {
        var s = source
        // Triple-quoted strings first (may contain // or /* */).
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        // Double-quoted strings (log lines must not count as code refs).
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        // Block comments, then line comments.
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    private fun resolveExistingDir(vararg candidates: String): File {
        return candidates.asSequence().map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory from: ${candidates.joinToString()}")
    }
}
