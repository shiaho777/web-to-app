package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import org.json.JSONObject
import java.io.File

/**
 * A single recorded build output: the APK on disk plus its `<name>.build.json`
 * release-metadata sidecar written by [BuildMetadataWriter].
 *
 * [apkExists] is false when the APK was pruned/removed while its metadata record
 * survived — the row still documents what was built, only install/share are gone.
 */
data class BuildHistoryEntry(
    val apkFileName: String,
    val apkPath: String?,
    val metadataPath: String?,
    val appName: String,
    val packageName: String?,
    val versionName: String?,
    val versionCode: Int?,
    val buildMode: String?,
    val buildReason: String?,
    val durationMs: Long?,
    val sizeBytes: Long?,
    val generatedAtMs: Long,
    val apkExists: Boolean
)

/**
 * Reads the built-apk output directory into per-app history rows. Pure reader for the
 * `.build.json` sidecars — nothing is written here; deletion goes through
 * [ApkBuilder.deleteApk] / [deleteRecord] so file and record stay in sync.
 */
class BuildHistoryReader(private val context: Context) {

    private val outputDir: File get() = resolveOutputDir(context)

    /**
     * Entries belonging to [packageName]. Sidecarless APKs (built before the sidecar
     * existed) can only be attributed by file name, so [appNameForFallback] supplies the
     * `<sanitizedName>_v…` prefix the builder uses for output names.
     */
    fun listFor(packageName: String, appNameForFallback: String): List<BuildHistoryEntry> {
        val dir = outputDir
        val files = dir.listFiles() ?: return emptyList()

        val apkByBase = files
            .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
            .associateBy { it.nameWithoutExtension }
        val sidecarByBase = files
            .filter { it.isFile && it.name.endsWith(".build.json") }
            .associateBy { it.name.removeSuffix(".build.json") }

        val entries = mutableListOf<BuildHistoryEntry>()
        val fallbackPrefix = com.webtoapp.util.AppConstants.sanitizeFileName(appNameForFallback) + "_"

        for ((base, sidecar) in sidecarByBase) {
            val entry = parseSidecar(base, sidecar, apkByBase[base]) ?: continue
            if (entry.packageName == packageName) entries += entry
        }
        for ((base, apk) in apkByBase) {
            if (sidecarByBase.containsKey(base)) continue
            if (!apk.name.startsWith(fallbackPrefix, ignoreCase = true)) continue
            entries += BuildHistoryEntry(
                apkFileName = apk.name,
                apkPath = apk.absolutePath,
                metadataPath = null,
                appName = appNameForFallback,
                packageName = packageName,
                versionName = null,
                versionCode = null,
                buildMode = null,
                buildReason = null,
                durationMs = null,
                sizeBytes = apk.length(),
                generatedAtMs = apk.lastModified(),
                apkExists = true
            )
        }
        return entries.sortedByDescending { it.generatedAtMs }
    }

    private fun parseSidecar(base: String, sidecar: File, apk: File?): BuildHistoryEntry? {
        return try {
            val json = JSONObject(sidecar.readText())
            val app = json.optJSONObject("app")
            val apkJson = json.optJSONObject("apk")
            val build = json.optJSONObject("build")
            BuildHistoryEntry(
                apkFileName = apk?.name
                    ?: apkJson?.optString("fileName")?.takeIf { it.isNotBlank() }
                    ?: "$base.apk",
                apkPath = apk?.absolutePath,
                metadataPath = sidecar.absolutePath,
                appName = app?.optString("name")?.takeIf { it.isNotBlank() } ?: base,
                packageName = app?.optString("packageName")?.takeIf { it.isNotBlank() },
                versionName = app?.optString("versionName")?.takeIf { it.isNotBlank() },
                versionCode = app?.optInt("versionCode")?.takeIf { it > 0 },
                buildMode = build?.optString("mode")?.takeIf { it.isNotBlank() },
                buildReason = build?.optString("reason")?.takeIf { it.isNotBlank() },
                durationMs = build?.optLong("durationMs")?.takeIf { it > 0 },
                sizeBytes = apk?.length()
                    ?: apkJson?.optLong("sizeBytes")?.takeIf { it > 0 },
                generatedAtMs = json.optLong("generatedAtMs").takeIf { it > 0 }
                    ?: (apk?.lastModified() ?: sidecar.lastModified()),
                apkExists = apk != null
            )
        } catch (e: Exception) {
            AppLogger.w("BuildHistory", "Unreadable build metadata ${sidecar.name}: ${e.message}")
            null
        }
    }

    /** Drop a leftover metadata record whose APK is already gone. */
    fun deleteRecord(entry: BuildHistoryEntry) {
        entry.metadataPath?.let { File(it).delete() }
    }
}
