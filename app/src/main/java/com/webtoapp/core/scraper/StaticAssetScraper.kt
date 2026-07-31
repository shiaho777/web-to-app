package com.webtoapp.core.scraper

import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.StaticAssetPack
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Build-time scraper for the Static Asset Pack feature.
 *
 * Unlike [WebsiteScraper] (which produces a full offline mirror with rewritten relative
 * URLs for an HTML-type app), this scraper only collects *static frontend assets*
 * (CSS/JS/fonts/icons, optionally images) referenced by a live site, leaves the site's
 * HTML untouched, and emits a [StaticAssetPack.Manifest] mapping each asset's normalized
 * URL to its packaged path. The generated APK then loads the live URL at runtime and
 * serves only the matched static assets locally (see WebViewManager). This keeps dynamic
 * content (HTML/API/images/video/user data) on the network, which is exactly what large
 * sites (YouTube/GitHub/Steam) need — a UI-shell cache, not a whole-site mirror.
 *
 * Host-only: scraping happens at export time inside the builder, so this class is not
 * synced into the shell module.
 */
class StaticAssetScraper {

    companion object {
        private const val TAG = "StaticAssetScraper"

        private const val DEFAULT_MAX_FILES = 300
        private const val DEFAULT_MAX_FILE_SIZE = 10L * 1024 * 1024
        private const val DEFAULT_MAX_TOTAL_SIZE = 50L * 1024 * 1024
        private const val DEFAULT_MAX_DEPTH = 2
        private const val CONNECT_TIMEOUT = 8L
        private const val READ_TIMEOUT = 15L
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

        private val HTML_SRC_PATTERN = Pattern.compile(
            """(?:src|href|data-src|data-original|poster)\s*=\s*["']([^"'#]+?)["']""",
            Pattern.CASE_INSENSITIVE
        )
        private val CSS_URL_PATTERN = Pattern.compile(
            """url\(\s*["']?([^"')]+?)["']?\s*\)""",
            Pattern.CASE_INSENSITIVE
        )
        private val CSS_IMPORT_PATTERN = Pattern.compile(
            """@import\s+["']([^"']+?)["']""",
            Pattern.CASE_INSENSITIVE
        )
        private val SRCSET_PATTERN = Pattern.compile(
            """srcset\s*=\s*["']([^"']+?)["']""",
            Pattern.CASE_INSENSITIVE
        )

        /**
         * Stable on-disk relative path for a URL. Same-host assets keep their URL path;
         * cross-host (CDN) assets are namespaced under `_cdn/<host>/` to avoid collisions.
         * Mirrors WebsiteScraper's layout convention.
         */
        fun relativePathFor(url: String, baseHost: String): String {
            return try {
                val u = URL(url)
                val host = u.host.lowercase().replace(":", "_")
                var path = u.path?.trimStart('/') ?: ""
                if (!u.query.isNullOrEmpty()) {
                    val ext = path.substringAfterLast(".", "")
                    val baseName = path.substringBeforeLast(".")
                    val queryHash = Integer.toHexString(u.query.hashCode())
                    path = if (ext.isNotEmpty()) "${baseName}_$queryHash.$ext" else "${path}_$queryHash"
                }
                if (path.isEmpty() || path.endsWith("/")) path += "index.bin"
                if (!path.contains(".") || path.substringAfterLast(".").length > 10) path += ".bin"
                path = path.replace(Regex("[^a-zA-Z0-9/_.-]"), "_")
                val urlHost = u.host.lowercase()
                if (urlHost == baseHost || urlHost.endsWith(".$baseHost")) path else "_cdn/$host/$path"
            } catch (e: Exception) {
                "asset_${url.hashCode().toUInt()}.bin"
            }
        }
    }

    data class Config(
        val url: String,
        val outputDir: File,
        val includeImages: Boolean = false,
        val includeCdn: Boolean = true,
        val maxFiles: Int = DEFAULT_MAX_FILES,
        val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
        val maxTotalSize: Long = DEFAULT_MAX_TOTAL_SIZE,
        val maxDepth: Int = DEFAULT_MAX_DEPTH,
        val timeoutSeconds: Int = 60,
        val userAgent: String = DEFAULT_USER_AGENT
    )

    sealed class Result {
        data class Success(
            val outputDir: File,
            val manifest: StaticAssetPack.Manifest,
            val fileCount: Int,
            val totalBytes: Long,
            val elapsedMs: Long
        ) : Result()

        data class Error(val message: String, val cause: Exception? = null) : Result()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .dispatcher(Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 32
        })
        .build()

    suspend fun scrape(config: Config): Result = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val baseHost = try {
                URL(config.url).host.lowercase()
            } catch (e: Exception) {
                return@withContext Result.Error("Invalid target URL: ${config.url}", e)
            }
            val allowedExtensions = if (config.includeImages) {
                StaticAssetPack.DEFAULT_STATIC_EXTENSIONS + StaticAssetPack.IMAGE_EXTENSIONS
            } else {
                StaticAssetPack.DEFAULT_STATIC_EXTENSIONS
            }

            if (config.outputDir.exists()) config.outputDir.deleteRecursively()
            config.outputDir.mkdirs()

            val downloaded = ConcurrentHashMap<String, String>() // manifestKey -> relative path
            val visitedCss = ConcurrentHashMap<String, Unit>()
            val failed = ConcurrentHashMap<String, String>()
            val fileCount = AtomicInteger(0)
            val totalBytes = AtomicLong(0L)
            val deadline = System.currentTimeMillis() + config.timeoutSeconds * 1000L

            fun hostOf(url: String): String? = try {
                URL(url).host.lowercase()
            } catch (e: Exception) {
                null
            }

            fun isAllowedHost(url: String): Boolean {
                if (config.includeCdn) return true
                val h = hostOf(url) ?: return false
                return h == baseHost || h.endsWith(".$baseHost")
            }

            fun resolve(raw: String, source: String): String? = try {
                val resolved = URI(source).resolve(raw.replace(" ", "%20")).toString()
                if (resolved.startsWith("http://") || resolved.startsWith("https://")) resolved else null
            } catch (e: Exception) {
                null
            }

            fun fetchText(url: String): String? = try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", config.userAgent)
                    .header("Accept", "*/*")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) null else response.body?.string()
                }
            } catch (e: Exception) {
                AppLogger.d(TAG, "fetchText failed: $url -> ${e.message}")
                null
            }

            fun downloadAsset(url: String): Boolean {
                val key = StaticAssetPack.manifestKey(url)
                if (downloaded.containsKey(key)) return true
                if (fileCount.get() >= config.maxFiles) return false
                if (System.currentTimeMillis() >= deadline) return false
                return try {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", config.userAgent)
                        .header("Accept", "*/*")
                        .build()
                    httpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            failed[url] = "HTTP ${response.code}"
                            return@use false
                        }
                        val body = response.body ?: return@use false
                        if (body.contentLength() > config.maxFileSize) {
                            failed[url] = "Too large"
                            return@use false
                        }
                        val data = body.bytes()
                        if (totalBytes.get() + data.size > config.maxTotalSize) {
                            failed[url] = "Total size limit exceeded"
                            return@use false
                        }
                        val relativePath = relativePathFor(url, baseHost)
                        val file = File(config.outputDir, relativePath)
                        file.parentFile?.mkdirs()
                        file.writeBytes(data)
                        fileCount.incrementAndGet()
                        totalBytes.addAndGet(data.size.toLong())
                        downloaded[key] = relativePath
                        AppLogger.d(TAG, "Packed: $relativePath (${data.size / 1024} KB)")
                        true
                    }
                } catch (e: Exception) {
                    failed[url] = e.message ?: "Unknown error"
                    false
                }
            }

            fun discover(content: String, sourceUrl: String, isCss: Boolean): List<String> {
                val found = mutableSetOf<String>()
                if (!isCss) {
                    val srcMatcher = HTML_SRC_PATTERN.matcher(content)
                    while (srcMatcher.find()) srcMatcher.group(1)?.let { found.add(it.trim()) }
                    val srcsetMatcher = SRCSET_PATTERN.matcher(content)
                    while (srcsetMatcher.find()) srcsetMatcher.group(1)?.let { srcset ->
                        srcset.split(",").forEach { entry ->
                            entry.trim().split(Regex("\\s+")).firstOrNull()?.let { found.add(it.trim()) }
                        }
                    }
                }
                val urlMatcher = CSS_URL_PATTERN.matcher(content)
                while (urlMatcher.find()) urlMatcher.group(1)?.let {
                    if (!it.startsWith("data:")) found.add(it.trim())
                }
                val importMatcher = CSS_IMPORT_PATTERN.matcher(content)
                while (importMatcher.find()) importMatcher.group(1)?.let { found.add(it.trim()) }

                val result = mutableListOf<String>()
                for (raw in found) {
                    if (raw.isBlank() || raw.startsWith("data:") || raw.startsWith("javascript:") ||
                        raw.startsWith("mailto:") || raw.startsWith("tel:") || raw.startsWith("#") ||
                        raw.startsWith("about:") || raw.startsWith("blob:")
                    ) {
                        continue
                    }
                    val absolute = resolve(raw, sourceUrl) ?: continue
                    if (!isAllowedHost(absolute)) continue
                    result.add(absolute)
                }
                return result
            }

            // 1) Fetch the entry HTML purely for discovery — it is never packaged.
            val entryBody = fetchText(config.url)
                ?: return@withContext Result.Error("Failed to download entry page: ${config.url}")

            val cssQueue = ArrayDeque<String>()
            for (candidate in discover(entryBody, config.url, isCss = false)) {
                val ext = StaticAssetPack.extensionOf(candidate)
                when {
                    ext == "css" -> if (visitedCss.putIfAbsent(StaticAssetPack.manifestKey(candidate), Unit) == null) {
                        cssQueue.add(candidate)
                    }
                    ext in allowedExtensions -> downloadAsset(candidate)
                }
            }

            // 2) Walk the CSS chain (CSS -> fonts/images/nested CSS) up to maxDepth.
            var depth = 0
            while (cssQueue.isNotEmpty() && depth < config.maxDepth) {
                val nextRound = ArrayDeque<String>()
                while (cssQueue.isNotEmpty()) {
                    if (System.currentTimeMillis() >= deadline) break
                    val cssUrl = cssQueue.removeFirst()
                    val cssBody = fetchText(cssUrl) ?: continue
                    downloadAsset(cssUrl)
                    for (candidate in discover(cssBody, cssUrl, isCss = true)) {
                        val ext = StaticAssetPack.extensionOf(candidate)
                        when {
                            ext == "css" -> if (visitedCss.putIfAbsent(StaticAssetPack.manifestKey(candidate), Unit) == null) {
                                nextRound.add(candidate)
                            }
                            ext in allowedExtensions -> downloadAsset(candidate)
                        }
                    }
                }
                cssQueue.addAll(nextRound)
                depth++
            }

            if (downloaded.isEmpty()) {
                return@withContext Result.Error("No static assets discovered for ${config.url}")
            }

            val manifest = StaticAssetPack.Manifest(
                version = StaticAssetPack.SCHEMA_VERSION,
                scrapedAt = System.currentTimeMillis(),
                baseUrl = StaticAssetPack.normalizeUrl(config.url),
                entries = downloaded.toMap()
            )
            // Write the manifest into the output dir so the embedder packages it alongside
            // the assets; the runtime reads it back through the same asset loader.
            val manifestJson = com.webtoapp.util.GsonProvider.gson.toJson(manifest)
            File(config.outputDir, StaticAssetPack.MANIFEST_FILE_NAME)
                .writeText(manifestJson, Charsets.UTF_8)

            val elapsed = System.currentTimeMillis() - startTime
            AppLogger.d(
                TAG,
                "Static pack complete: ${downloaded.size} files, ${totalBytes.get() / 1024} KB, ${elapsed}ms, failed=${failed.size}"
            )
            Result.Success(
                outputDir = config.outputDir,
                manifest = manifest,
                fileCount = downloaded.size,
                totalBytes = totalBytes.get(),
                elapsedMs = elapsed
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Static pack failed", e)
            Result.Error(e.message ?: "Static pack failed", e)
        }
    }
}
