package com.webtoapp.core.update

import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

object UpdateChecker {

    private const val TAG = "UpdateChecker"

    private const val OWNER = "shiaho777"
    private const val REPO = "web-to-app"
    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    private const val ALL_RELEASES_API =
        "https://api.github.com/repos/$OWNER/$REPO/releases?per_page=100"

    private const val CONNECT_TIMEOUT_MS = 6000
    private const val READ_TIMEOUT_MS = 10000

    /**
     * Hard cap on one raced JSON fetch: the first valid response wins long before
     * this, the cap only bounds the "every route hangs" case.
     */
    private const val RACE_TIMEOUT_MS = 12000L

    /**
     * Losing race requests are launched here rather than in the caller's scope,
     * so the winner returns immediately instead of waiting for every candidate
     * to finish or time out.
     */
    private val raceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                // Raw asset URL: ApkUpdateInstaller expands the measured mirror
                // candidates itself and retries, so a single stale prefix can
                // no longer sink the download.
                downloadUrl = rawUrl,
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
            // Null means every candidate failed — surface it as an error so the
            // history UI shows a failure state instead of an empty list.
            val json = fetchAllReleasesJson()
                ?: throw IllegalStateException("Empty response from release API")
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
                        downloadUrl = rawUrl,
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

    /**
     * A public repository owned by the app author, as shown by the About page's
     * "more projects" section. Forks and this app itself are filtered out.
     */
    data class RepoSummary(
        val name: String,
        val description: String,
        val stars: Long,
        val forks: Long,
        val language: String?,
        val pushedAt: String,
        val url: String
    )

    private const val AUTHOR_REPOS_API =
        "https://api.github.com/users/$OWNER/repos?per_page=100&type=owner"

    /**
     * Pre-generated repo list on the `author-repos-data` branch, refreshed by
     * the Update Author Repos workflow. Serving it through raw.githubusercontent.com
     * matters: the proxy pool is whitelisted for repo file paths (releases/raw),
     * while `api.github.com` is rate-limited per egress IP and most shared
     * proxy IPs are exhausted — the API alone leaves CN users with no route.
     */
    private const val AUTHOR_REPOS_RAW =
        "https://raw.githubusercontent.com/$OWNER/$REPO/author-repos-data/repos.json"

    /**
     * Fetches the author's public repos for the About page. Client-side sorting
     * covers both "by stars" and "latest" so the sort toggle never refetches.
     */
    suspend fun fetchAuthorRepos(context: android.content.Context): List<RepoSummary> =
        withContext(Dispatchers.IO) {
            try {
                val json = fetchJsonRaced(authorReposCandidates()) { it.trimStart().startsWith("[") }
                    ?: throw IllegalStateException("Empty response from repos API")
                AuthorReposCache.write(context, json)
                parseAuthorRepos(json)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Fetch author repos failed", e)
                throw e
            }
        }

    /**
     * Race candidates for the repo list: the raw data file through every proxy
     * channel plus direct, then the REST API through the same — first response
     * that parses as a JSON array wins.
     */
    private fun authorReposCandidates(): List<String> {
        val channels = com.webtoapp.core.network.CnMirrorProbe.peekChannels()
        return (channels.map { it.rewrite(AUTHOR_REPOS_RAW) } +
            AUTHOR_REPOS_RAW +
            channels.map { it.rewrite(AUTHOR_REPOS_API) } +
            AUTHOR_REPOS_API).distinct()
    }

    internal fun parseAuthorRepos(json: String): List<RepoSummary> {
        val arr = org.json.JSONArray(json)
        val out = ArrayList<RepoSummary>(arr.length())
        for (i in 0 until arr.length()) {
            val repo = arr.optJSONObject(i) ?: continue
            if (repo.optBoolean("fork", false)) continue
            val name = repo.optString("name").trim()
            if (name.isBlank() || name.equals(REPO, ignoreCase = true)) continue
            // optString("description") returns the literal "null" when the field
            // is JSON null — check the raw value instead.
            val rawDesc = repo.opt("description")
            val rawLang = repo.opt("language")
            out.add(
                RepoSummary(
                    name = name,
                    description = if (rawDesc == null || rawDesc == JSONObject.NULL) "" else rawDesc.toString().trim(),
                    stars = repo.optLong("stargazers_count", 0L),
                    forks = repo.optLong("forks_count", 0L),
                    language = if (rawLang == null || rawLang == JSONObject.NULL) null else rawLang.toString(),
                    pushedAt = repo.optString("pushed_at").trim(),
                    url = repo.optString("html_url").trim()
                )
            )
        }
        return out
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

    private suspend fun fetchLatestReleaseJson(): String? =
        // A proxy can answer 200 with a plain-text error ("Suspent",
        // "404 not found") — not every mirror actually proxies
        // api.github.com. Only a JSON object counts as a hit.
        fetchJsonRaced(LATEST_RELEASE_API) { it.trimStart().startsWith("{") }

    private suspend fun fetchAllReleasesJson(): String? =
        // The all-releases endpoint answers with an array, not an object.
        fetchJsonRaced(ALL_RELEASES_API) { it.trimStart().startsWith("[") }

    /**
     * Fires one GET per mirror candidate concurrently and returns the first body
     * accepted by [accept]. Candidate order comes from
     * [CnMirrorProbe.peekChannels] — never blocking — so a cold probe cannot
     * delay the first request; losing requests keep running detached until
     * their own socket timeout (tiny payloads, bounded by the timeouts below).
     */
    private suspend fun fetchJsonRaced(apiUrl: String, accept: (String) -> Boolean): String? {
        val urls = (com.webtoapp.core.network.CnMirrorProbe.peekChannels()
            .map { it.rewrite(apiUrl) } + apiUrl).distinct()
        return fetchJsonRaced(urls, accept)
    }

    private suspend fun fetchJsonRaced(urls: List<String>, accept: (String) -> Boolean): String? {
        val results = Channel<Pair<String, String?>>(urls.size)
        val jobs = urls.map { endpoint ->
            raceScope.launch {
                val body = runCatching { httpGet(endpoint) }
                    .onFailure {
                        AppLogger.w(TAG, "API request failed via $endpoint: ${it.message}")
                    }
                    .getOrNull()
                results.send(endpoint to body)
            }
        }
        var winner: String? = null
        var remaining = urls.size
        withTimeoutOrNull(RACE_TIMEOUT_MS) {
            while (remaining > 0 && winner == null) {
                remaining--
                val (endpoint, body) = results.receive()
                if (body != null && accept(body)) {
                    AppLogger.d(TAG, "API request won via $endpoint")
                    winner = body
                } else if (body != null) {
                    AppLogger.w(TAG, "API returned non-JSON via $endpoint: ${body.take(80)}")
                }
            }
        }
        jobs.forEach { it.cancel() }
        return winner
    }

    private fun httpGet(endpoint: String): String {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
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
