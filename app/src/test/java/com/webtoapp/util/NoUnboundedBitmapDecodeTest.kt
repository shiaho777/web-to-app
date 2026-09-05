package com.webtoapp.util

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * Crash gate for #779 ("Canvas: trying to draw too large bitmap").
 *
 * Raw BitmapFactory decodes of untrusted content must never reach a draw call:
 * the crash fires at DRAW time, past any try/catch around the decode. All such
 * decodes go through [BoundedBitmaps]; files below are the only exceptions
 * (each implements its own bounds sampling or provably needs full pixels).
 */
class NoUnboundedBitmapDecodeTest {

    private val allowedFiles = setOf(
        // The guard implementation itself.
        "com/webtoapp/util/BoundedBitmaps.kt",
        // Own two-pass bounds sampling for the crop UI.
        "com/webtoapp/ui/components/IconCropDialog.kt",
        "com/webtoapp/ui/components/StatusBarImageCropper.kt",
        // Own bounds sampling on the import path.
        "com/webtoapp/util/IconStorage.kt",
        "com/webtoapp/util/MediaStorage.kt",
        "com/webtoapp/util/SplashStorage.kt",
        "com/webtoapp/util/FaviconFetcher.kt",
        // Build-time background recompression: output quality requires pixels;
        // OOM there fails one file (caught), never a draw call.
        "com/webtoapp/core/linux/PerformanceOptimizer.kt"
    )

    private val rawDecodeCall = Regex(
        """BitmapFactory\.(decodeFile|decodeStream|decodeByteArray)\("""
    )
    private val noArgToBitmap = Regex("""\.toBitmap\(\)""")

    @Test
    fun `no unbounded bitmap decodes outside the allowlist`() {
        val roots = listOf("app/src/main/java", "src/main/java")
            .map(::File)
            .filter(File::exists)
        assertWithMessage("Could not locate Kotlin source root")
            .that(roots).isNotEmpty()

        val offenders = mutableListOf<String>()
        roots.forEach { root ->
            root.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    val relPath = file.relativeTo(root).path
                    if (relPath in allowedFiles) return@forEach
                    file.useLines { lines ->
                        lines.forEachIndexed { index, raw ->
                            val line = raw.trim()
                            if (line.startsWith("//") || line.startsWith("*")) return@forEachIndexed
                            if (rawDecodeCall.containsMatchIn(line) || noArgToBitmap.containsMatchIn(line)) {
                                offenders += "${relPath}:${index + 1}: ${raw.trim()}"
                            }
                        }
                    }
                }
        }

        assertWithMessage(
            buildString {
                appendLine("Unbounded bitmap decode outside BoundedBitmaps.")
                appendLine("Decode user/adaptive content ONLY via BoundedBitmaps helpers")
                appendLine("(decodeBoundedBitmapFile/Stream/Bytes, Drawable.toBoundedBitmap),")
                appendLine("or add the file above with a reason when it provably samples itself.")
                appendLine()
                appendLine("Why: RecordingCanvas kills the process at DRAW time for bitmaps")
                appendLine("beyond its limit (issue #779: 943,718,400 bytes) — a try/catch")
                appendLine("around the decode cannot save you.")
                appendLine()
                appendLine("Offending references:")
                offenders.forEach { appendLine("  $it") }
            }
        ).that(offenders).isEmpty()
    }
}
