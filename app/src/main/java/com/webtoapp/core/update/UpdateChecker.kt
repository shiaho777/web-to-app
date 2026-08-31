package com.webtoapp.core.update

import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object UpdateChecker {

    private const val TAG = "UpdateChecker"

    private const val OWNER = "shiaho777"
    private const val REPO = "web-to-app"
    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    private const val ALL_RELEASES_API =
        "https://api.github.com/repos/$OWNER/$REPO/releases?per_page=100"

    private const val TIMEOUT_MS = 12000

    /**
     * Invisible boundary that separates the English and Chinese halves of a GitHub release body
     * (English first, Chinese after the marker). It renders as nothing on the releases page, so
     * readers see English then Chinese; the app picks the half matching the current language.
     * Releases without the marker (older notes, or a future release nobody localized yet) fall
     * back to the whole body, which is English — never blank.
     */
    private const val ZH_MARKER = "<!-- zh-CN -->"

    /**
     * Returns the release body in the current app language: the Chinese half when the app is set
     * to Chinese and a Chinese half exists, otherwise the English half (or the whole body when the
     * marker is absent).
     */
    private fun localizeReleaseBody(body: String): String {
        val raw = body.trim()
        val idx = raw.indexOf(ZH_MARKER)
        if (idx < 0) return raw
        val english = raw.substring(0, idx).trim()
        val chinese = raw.substring(idx + ZH_MARKER.length).trim()
        return if (Strings.currentLanguage.value == AppLanguage.CHINESE && chinese.isNotBlank()) {
            chinese
        } else {
            english
        }
    }

    data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
        override fun toString(): String = "$major.$minor.$patch"

        override fun compareTo(other: Version): Int {
            if (major != other.major) return major - other.major
            if (minor != other.minor) return minor - other.minor
            return patch - other.patch
        }

        companion object {
            fun parse(raw: String): Version? {
                val core = raw.trim().removePrefix("v").removePrefix("V")
                    .substringBefore('-').substringBefore('+')
                val parts = core.split('.')
                val major = parts.getOrNull(0)?.toIntOrNull() ?: return null
                val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
                return Version(major, minor, patch)
            }
        }
    }

    data class ReleaseInfo(
        val version: String,
        val downloadUrl: String,
        val sizeBytes: Long,
        val sha256: String?,
        val releaseNotes: String
    )

    /**
     * A single historical release as needed by the version history UI. Each entry keeps
     * enough info to render, show its changelog, and offer a download without re-fetching.
     *
     * [downloadUrl] is the already-mirrored browser_download_url; null when the release has
     * no APK asset (e.g. source-only / draft).
     */
    data class ReleaseSummary(
        val tag: String,
        val version: String,
        val name: String,
        val publishedAt: String,
        val body: String,
        val downloadUrl: String?,
        val sizeBytes: Long,
        val sha256: String?
    )

    sealed class Result {
        data class UpdateAvailable(val info: ReleaseInfo, val currentVersion: String) : Result()
        data class UpToDate(val version: String) : Result()
        data class Failed(val message: String, val throwable: Throwable? = null) : Result()
    }

    /**
     * Best current route for a GitHub URL. Release assets go through the
     * measured mirror pool (same routing the runtime downloads use, so an
     * update APK is not stuck on one hard-coded accelerator); anything else
     * comes back untouched.
     *
     * Single URL rather than a list because the result is carried on [Result]
     * and handed to the installer, which has no fallback logic of its own.
     */
    fun withMirror(url: String): String =
        com.webtoapp.core.network.GitHubMirror.proxiedCn(url).firstOrNull() ?: url

    suspend fun check(currentVersionName: String): Result = withContext(Dispatchers.IO) {
        val current = Version.parse(currentVersionName)
            ?: return@withContext Result.Failed("Cannot parse current version: $currentVersionName")

        try {
            val json = fetchLatestReleaseJson()
                ?: return@withContext Result.Failed("Empty response from release API")

            val release = JSONObject(json)
            val tag = release.optString("tag_name").ifBlank { release.optString("name") }
            val latest = Version.parse(tag)
                ?: return@withContext Result.Failed("Cannot parse release tag: $tag")

            if (latest <= current) {
                return@withContext Result.UpToDate(current.toString())
            }

            val asset = pickBestApkAsset(release)
                ?: return@withContext Result.Failed("No APK asset found in release $tag")

            val rawUrl = asset.optString("browser_download_url")
            if (rawUrl.isBlank()) {
                return@withContext Result.Failed("Release asset has no download URL")
            }

            val info = ReleaseInfo(
                version = latest.toString(),
                downloadUrl = withMirror(rawUrl),
                sizeBytes = asset.optLong("size", 0L),
                sha256 = asset.optString("digest").takeIf { it.isNotBlank() }
                    ?.substringAfter("sha256:", "")?.takeIf { it.isNotBlank() },
                releaseNotes = localizeReleaseBody(release.optString("body"))
            )
            Result.UpdateAvailable(info, current.toString())
        } catch (e: Exception) {
            AppLogger.e(TAG, "Update check failed", e)
            Result.Failed(e.message ?: "Update check failed", e)
        }
    }

    /**
     * Fetches the full release history (newest first) for the version-history UI. Each entry
     * carries its changelog body and — when available — a (mirrored) APK download URL.
     *
     * Releases without a parseable version tag or without an APK asset are still listed (so
     * the user can read their notes), but [ReleaseSummary.downloadUrl] is null when there is
     * no APK to download. Draft releases are excluded by the API.
     */
    suspend fun fetchAllReleases(): List<ReleaseSummary> = withContext(Dispatchers.IO) {
        try {
            val json = fetchAllReleasesJson()
                ?: return@withContext emptyList()
            val arr = org.json.JSONArray(json)
            val out = ArrayList<ReleaseSummary>(arr.length())
            for (i in 0 until arr.length()) {
                val release = arr.optJSONObject(i) ?: continue
                val tag = release.optString("tag_name").ifBlank { release.optString("name") }
                val version = Version.parse(tag)?.toString() ?: tag
                val asset = pickBestApkAsset(release)
                val rawUrl = asset?.optString("browser_download_url")?.takeIf { it.isNotBlank() }
                out.add(
                    ReleaseSummary(
                        tag = tag,
                        version = version,
                        name = release.optString("name").trim(),
                        publishedAt = release.optString("published_at").trim(),
                        body = localizeReleaseBody(release.optString("body")),
                        downloadUrl = rawUrl?.let { withMirror(it) },
                        sizeBytes = asset?.optLong("size", 0L) ?: 0L,
                        sha256 = asset?.optString("digest").takeIf { !it.isNullOrBlank() }
                            ?.substringAfter("sha256:", "")?.takeIf { it.isNotBlank() }
                    )
                )
            }
            // Sort by version descending so newest is on top; unparseable tags sink to the bottom.
            out.sortedWith(compareByDescending { Version.parse(it.version) ?: Version(0, 0, 0) })
        } catch (e: Exception) {
            AppLogger.e(TAG, "Fetch all releases failed", e)
            throw e
        }
    }

    private fun pickBestApkAsset(release: JSONObject): JSONObject? {
        val assets = release.optJSONArray("assets") ?: return null
        var best: JSONObject? = null
        var bestVersion: Version? = null
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val name = asset.optString("name")
            if (!name.endsWith(".apk", ignoreCase = true)) continue
            val assetVersion = Version.parse(name.removeSuffix(".APK").removeSuffix(".apk").substringAfterLast('-'))
            if (best == null || (assetVersion != null && bestVersion != null && assetVersion > bestVersion) ||
                (assetVersion != null && bestVersion == null)) {
                best = asset
                bestVersion = assetVersion
            }
        }
        return best
    }

    private fun fetchLatestReleaseJson(): String? {
        val candidates = com.webtoapp.core.network.GitHubMirror.proxiedCnGitHubHost(LATEST_RELEASE_API)
        var lastError: Exception? = null
        for (endpoint in candidates) {
            try {
                return httpGet(endpoint)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Release API failed via $endpoint: ${e.message}")
                lastError = e
            }
        }
        lastError?.let { throw it }
        return null
    }

    private fun fetchAllReleasesJson(): String? {
        val candidates = com.webtoapp.core.network.GitHubMirror.proxiedCnGitHubHost(ALL_RELEASES_API)
        var lastError: Exception? = null
        for (endpoint in candidates) {
            try {
                return httpGet(endpoint)
            } catch (e: Exception) {
                AppLogger.w(TAG, "All-releases API failed via $endpoint: ${e.message}")
                lastError = e
            }
        }
        lastError?.let { throw it }
        return null
    }

    private fun httpGet(endpoint: String): String {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "WebToApp-UpdateChecker")
            }
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code from $endpoint")
            }
            return connection.inputStream.bufferedReader().use(BufferedReader::readText)
        } finally {
            connection?.disconnect()
        }
    }
}
