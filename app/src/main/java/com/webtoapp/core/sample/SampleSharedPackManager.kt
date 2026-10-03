package com.webtoapp.core.sample

import android.content.Context
import com.google.gson.JsonParser
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.GitHubMirror
import com.webtoapp.core.network.NetworkModule
import com.webtoapp.util.SafeZip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.util.zip.ZipInputStream

/**
 * On-demand fetcher for the heavyweight shared dependency packs used by the
 * typed sample projects (python-*-shared/.pypackages, go-*-shared/vendor).
 *
 * The packs live in the repo as sample-bundles/<name>.zip pinned by
 * sample-bundles/manifest.json instead of inside the APK — ~30MB of assets
 * that only the few users who open those samples ever touch. Bundled assets
 * still win when present (see SampleProjectExtractor), so a fork can
 * re-embed a pack without code changes.
 */
object SampleSharedPackManager {

    private const val TAG = "SampleSharedPack"
    private const val PACKS_DIR = "sample_shared_packs"
    private const val SHA_MARKER = ".sha256"
    private const val MANIFEST_FILE = "manifest.json"

    private const val RAW_BASE =
        "https://raw.githubusercontent.com/shiaho777/web-to-app/main/sample-bundles"
    private const val JSDELIVR_BASE =
        "https://cdn.jsdelivr.net/gh/shiaho777/web-to-app@main/sample-bundles"

    /** Unit-test seam: when true, manifest fetches behave as unreachable. */
    @Volatile
    internal var manifestUnreachable = false

    data class PackInfo(val sha256: String, val bytes: Long)

    /**
     * Ensures [packName] is extracted on-device and returns its root directory
     * (containing e.g. `.pypackages/`), or null when the pack is unavailable.
     * A previously downloaded pack is reused even when the manifest cannot be
     * reached — only the first fetch requires network.
     */
    suspend fun ensurePack(context: Context, packName: String): File? = withContext(Dispatchers.IO) {
        try {
            val packDir = File(context.filesDir, "$PACKS_DIR/$packName")
            val payloadDir = File(packDir, packName)
            val marker = File(packDir, SHA_MARKER)
            val cacheUsable = payloadDir.isDirectory &&
                payloadDir.walkTopDown().any { it.isFile }

            val info = fetchManifest(context)[packName]
            if (info == null) {
                if (cacheUsable) {
                    AppLogger.w(TAG, "manifest unreachable; reusing cached pack $packName")
                    return@withContext payloadDir
                }
                AppLogger.w(TAG, "pack not in manifest: $packName")
                return@withContext null
            }

            val cachedSha = marker.takeIf { it.isFile }?.readText()?.trim()
            if (cacheUsable && cachedSha == info.sha256) return@withContext payloadDir

            packDir.deleteRecursively()
            packDir.mkdirs()
            val zipFile = File(packDir, "$packName.zip")
            val downloaded = DependencyDownloadEngine.downloadFileWithFallback(
                urls = candidateUrls("$packName.zip"),
                destFile = zipFile,
                displayName = packName,
                context = context,
                expectedSha256For = { info.sha256 }
            )
            if (!downloaded) return@withContext null

            DependencyDownloadEngine.publishState(
                DependencyDownloadEngine.State.Extracting(packName)
            )
            val extracted = try {
                zipFile.inputStream().buffered().use { input ->
                    SafeZip.extractAll(ZipInputStream(input), packDir)
                }
                payloadDir.isDirectory && payloadDir.walkTopDown().any { it.isFile }
            } catch (e: Exception) {
                AppLogger.e(TAG, "pack extract failed: $packName", e)
                false
            } finally {
                zipFile.delete()
            }
            if (!extracted) {
                packDir.deleteRecursively()
                return@withContext null
            }
            marker.writeText(info.sha256)
            payloadDir
        } catch (e: Exception) {
            AppLogger.e(TAG, "shared pack fetch failed: $packName", e)
            null
        }
    }

    /** Wipes every downloaded pack (called when extracted samples are cleared). */
    fun clearPacks(context: Context) {
        File(context.filesDir, PACKS_DIR).deleteRecursively()
    }

    /** Mirror-ordered candidates: CN proxies + direct raw, jsDelivr last. */
    internal fun candidateUrls(fileName: String): List<String> =
        (GitHubMirror.proxiedCnGitHubHost("$RAW_BASE/$fileName") +
            "$JSDELIVR_BASE/$fileName").distinct()

    private fun fetchManifest(context: Context): Map<String, PackInfo> {
        if (manifestUnreachable) return emptyMap()
        for (url in candidateUrls(MANIFEST_FILE)) {
            try {
                val request = Request.Builder().url(url).build()
                NetworkModule.defaultClient.newCall(request).execute().use { resp ->
                    if (resp.isSuccessful) {
                        resp.body?.string()?.let { body ->
                            val parsed = parseManifest(body)
                            if (parsed.isNotEmpty()) return parsed
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "manifest fetch failed via $url: ${e.message}")
            }
        }
        return emptyMap()
    }

    internal fun parseManifest(json: String): Map<String, PackInfo> = try {
        val packs = JsonParser.parseString(json).asJsonObject
            .getAsJsonObject("packs") ?: return emptyMap()
        packs.entrySet().mapNotNull { (name, el) ->
            val obj = el.asJsonObject
            val sha = obj.get("sha256")?.asString
            val bytes = obj.get("bytes")?.asLong ?: 0L
            if (sha.isNullOrBlank()) null else name to PackInfo(sha, bytes)
        }.toMap()
    } catch (_: Exception) {
        emptyMap()
    }
}
