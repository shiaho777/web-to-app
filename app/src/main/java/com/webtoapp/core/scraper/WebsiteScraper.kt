package com.webtoapp.core.scraper

import android.content.Context
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

class WebsiteScraper(private val context: Context) {

    companion object {
        private const val TAG = "WebsiteScraper"

        private const val DEFAULT_MAX_DEPTH = 3
        private const val DEFAULT_MAX_FILES = 500
        private const val DEFAULT_MAX_FILE_SIZE = 20L * 1024 * 1024
        private const val DEFAULT_MAX_TOTAL_SIZE = 200L * 1024 * 1024
        private const val DEFAULT_CONCURRENCY = 16
        private const val CONNECT_TIMEOUT = 8L
        private const val READ_TIMEOUT = 15L

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

        private val DOWNLOADABLE_EXTENSIONS = setOf(

            "html", "htm", "css", "js", "mjs", "json", "xml", "svg", "webmanifest",

            "png", "jpg", "jpeg", "gif", "webp", "ico", "bmp", "avif",

            "woff", "woff2", "ttf", "otf", "eot",

            "mp3", "mp4", "webm", "ogg", "wav",

            "txt", "map", "wasm"
        )

        private val TEXT_CONTENT_TYPES = setOf(
            "text/html", "text/css", "application/javascript", "text/javascript",
            "application/json", "text/xml", "application/xml", "image/svg+xml",
            "application/manifest+json", "text/plain"
        )
    }

    data class ScrapeConfig(
        val url: String,
        val maxDepth: Int = DEFAULT_MAX_DEPTH,
        val maxFiles: Int = DEFAULT_MAX_FILES,
        val maxFileSize: Long = DEFAULT_MAX_FILE_SIZE,
        val maxTotalSize: Long = DEFAULT_MAX_TOTAL_SIZE,
        val concurrency: Int = DEFAULT_CONCURRENCY,
        val followLinks: Boolean = true,
        val downloadCdnResources: Boolean = true,
        val userAgent: String = "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        val skipPatterns: List<String> = emptyList(),
        val timeoutSeconds: Int = 30
    )

    data class ScrapeProgress(
        val phase: Phase,
        val downloadedFiles: Int,
        val totalDiscovered: Int,
        val downloadedBytes: Long,
        val currentFile: String,
        val message: String
    ) {
        enum class Phase {
            ANALYZING,
            DOWNLOADING,
            REWRITING,
            COMPLETE,
            ERROR
        }
    }

    sealed class ScrapeResult {
        data class Success(
            val projectDir: File,
            val files: List<HtmlFile>,
            val entryFile: String,
            val totalFiles: Int,
            val totalSize: Long,
            val elapsedMs: Long
        ) : ScrapeResult()

        data class Error(val message: String, val cause: Exception? = null) : ScrapeResult()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .dispatcher(okhttp3.Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 32
        })
        .build()

    private val downloadedUrls = ConcurrentHashMap<String, String>()
    private val processedUrls = ConcurrentHashMap<String, Unit>()
    private val failedUrls = ConcurrentHashMap<String, String>()
    private val fileCounter = AtomicInteger(0)
    private var totalDownloadedBytes = 0L
    private val downloadedBytesLock = Any()

    private lateinit var projectDir: File
    private lateinit var baseUrl: URL
    private lateinit var baseHost: String

    suspend fun scrape(
        config: ScrapeConfig,
        onProgress: (ScrapeProgress) -> Unit = {}
    ): ScrapeResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {

            baseUrl = URL(config.url)
            baseHost = baseUrl.host

            val projectId = generateProjectId(config.url)
            projectDir = File(context.filesDir, "scraped_sites/$projectId")
            if (projectDir.exists()) projectDir.deleteRecursively()
            projectDir.mkdirs()

            AppLogger.d(TAG, "Starting scrape: url=${config.url}, projectDir=${projectDir.absolutePath}, concurrency=${config.concurrency}")

            downloadedUrls.clear()
            processedUrls.clear()
            failedUrls.clear()
            fileCounter.set(0)
            totalDownloadedBytes = 0L

            onProgress(ScrapeProgress(
                phase = ScrapeProgress.Phase.ANALYZING,
                downloadedFiles = 0,
                totalDiscovered = 1,
                downloadedBytes = 0,
                currentFile = config.url,
                message = Strings.scrapeAnalyzing
            ))

            val deadline = System.currentTimeMillis() + config.timeoutSeconds * 1000L
            val urlChannel = kotlinx.coroutines.channels.Channel<Pair<String, Int>>(kotlinx.coroutines.channels.Channel.UNLIMITED)
            val activeTasks = AtomicInteger(0)

            fun enqueue(url: String, depth: Int) {
                if (processedUrls.putIfAbsent(url, Unit) == null) {
                    activeTasks.incrementAndGet()
                    urlChannel.trySend(url to depth)
                }
            }

            enqueue(config.url, 0)

            val workers = (1..config.concurrency).map {
                launch {
                    for ((url, depth) in urlChannel) {
                        try {
                            if (fileCounter.get() >= config.maxFiles ||
                                System.currentTimeMillis() >= deadline
                            ) {
                                continue
                            }
                            val totalSize: Long
                            synchronized(downloadedBytesLock) {
                                totalSize = totalDownloadedBytes
                            }
                            if (totalSize >= config.maxTotalSize) continue

                            onProgress(ScrapeProgress(
                                phase = ScrapeProgress.Phase.DOWNLOADING,
                                downloadedFiles = fileCounter.get(),
                                totalDiscovered = fileCounter.get() + activeTasks.get(),
                                downloadedBytes = totalSize,
                                currentFile = url.substringAfterLast("/").take(40),
                                message = Strings.scrapeDownloadingProgress.format(fileCounter.get())
                            ))

                            val (_, discovered) = downloadFile(url, depth, config)

                            for ((newUrl, newDepth) in discovered) {
                                if (fileCounter.get() < config.maxFiles &&
                                    System.currentTimeMillis() < deadline
                                ) {
                                    enqueue(newUrl, newDepth)
                                }
                            }
                        } finally {
                            if (activeTasks.decrementAndGet() == 0) {
                                urlChannel.close()
                            }
                        }
                    }
                }
            }

            workers.joinAll()

            if (System.currentTimeMillis() >= deadline) {
                AppLogger.w(TAG, "Scrape timed out after ${config.timeoutSeconds}s, ${fileCounter.get()} files downloaded")
            }

            onProgress(ScrapeProgress(
                phase = ScrapeProgress.Phase.REWRITING,
                downloadedFiles = fileCounter.get(),
                totalDiscovered = fileCounter.get(),
                downloadedBytes = totalDownloadedBytes,
                currentFile = "",
                message = Strings.scrapeRewritingPaths
            ))

            rewriteUrls()

            val files = collectHtmlFiles()
            val entryFileName = downloadedUrls[normalizeUrl(config.url)]
                ?: "index.html"

            val elapsed = System.currentTimeMillis() - startTime

            AppLogger.d(TAG, "Scrape complete: ${files.size} files, ${totalDownloadedBytes / 1024} KB, ${elapsed}ms")
            AppLogger.d(TAG, "Failed URLs: ${failedUrls.size}")
            failedUrls.forEach { (url, reason) ->
                AppLogger.d(TAG, "  FAILED: $url -> $reason")
            }

            onProgress(ScrapeProgress(
                phase = ScrapeProgress.Phase.COMPLETE,
                downloadedFiles = files.size,
                totalDiscovered = files.size,
                downloadedBytes = totalDownloadedBytes,
                currentFile = "",
                message = Strings.scrapeComplete.format(files.size, totalDownloadedBytes / 1024)
            ))

            ScrapeResult.Success(
                projectDir = projectDir,
                files = files,
                entryFile = entryFileName,
                totalFiles = files.size,
                totalSize = totalDownloadedBytes,
                elapsedMs = elapsed
            )

        } catch (e: Exception) {
            AppLogger.e(TAG, "Scrape failed", e)
            onProgress(ScrapeProgress(
                phase = ScrapeProgress.Phase.ERROR,
                downloadedFiles = fileCounter.get(),
                totalDiscovered = fileCounter.get(),
                downloadedBytes = totalDownloadedBytes,
                currentFile = "",
                message = Strings.scrapeFailed.format(e.message ?: "")
            ))
            ScrapeResult.Error(Strings.scrapeFailed.format(e.message ?: ""), e)
        }
    }

    private fun downloadFile(
        urlString: String,
        depth: Int,
        config: ScrapeConfig
    ): Pair<String?, List<Pair<String, Int>>> {
        val normalizedUrl = normalizeUrl(urlString)

        if (downloadedUrls.containsKey(normalizedUrl)) {
            return downloadedUrls[normalizedUrl] to emptyList()
        }

        if (config.skipPatterns.any { normalizedUrl.matches(Regex(it)) }) {
            return null to emptyList()
        }

        if (downloadedUrls.putIfAbsent(normalizedUrl, "__pending__") != null) {
            return downloadedUrls[normalizedUrl]?.takeIf { it != "__pending__" } to emptyList()
        }

        try {
            val request = Request.Builder()
                .url(normalizedUrl)
                .header("User-Agent", config.userAgent)
                .header("Accept", "*/*")
                .header("Referer", config.url)
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                failedUrls[normalizedUrl] = "HTTP ${response.code}"
                downloadedUrls.remove(normalizedUrl)
                response.close()
                return null to emptyList()
            }

            val body = response.body ?: run {
                failedUrls[normalizedUrl] = "Empty response body"
                downloadedUrls.remove(normalizedUrl)
                response.close()
                return null to emptyList()
            }

            val contentType = response.header("Content-Type")?.lowercase() ?: ""
            val contentLength = body.contentLength()

            if (contentLength > config.maxFileSize) {
                failedUrls[normalizedUrl] = "Too large: ${contentLength / 1024} KB"
                downloadedUrls.remove(normalizedUrl)
                response.close()
                return null to emptyList()
            }

            val data = body.bytes()
            response.close()

            synchronized(downloadedBytesLock) {
                if (totalDownloadedBytes + data.size > config.maxTotalSize) {
                    failedUrls[normalizedUrl] = "Total size limit exceeded"
                    downloadedUrls.remove(normalizedUrl)
                    return null to emptyList()
                }
                totalDownloadedBytes += data.size
            }

            val localPath = urlToLocalPath(normalizedUrl)
            val localFile = File(projectDir, localPath)
            localFile.parentFile?.mkdirs()
            localFile.writeBytes(data)

            fileCounter.incrementAndGet()
            downloadedUrls[normalizedUrl] = localPath

            AppLogger.d(TAG, "Downloaded: $localPath (${data.size / 1024} KB) depth=$depth")

            if (depth < config.maxDepth) {
                val isTextContent = TEXT_CONTENT_TYPES.any { contentType.contains(it) } ||
                    localPath.endsWith(".html") || localPath.endsWith(".htm") ||
                    localPath.endsWith(".css") || localPath.endsWith(".js") ||
                    localPath.endsWith(".svg")

                if (isTextContent) {
                    val text = String(data, Charsets.UTF_8)
                    val discovered = discoverResources(text, normalizedUrl, depth, config, contentType)
                    return localPath to discovered
                }
            }

            return localPath to emptyList()

        } catch (e: Exception) {
            failedUrls[normalizedUrl] = e.message ?: "Unknown error"
            downloadedUrls.remove(normalizedUrl)
            AppLogger.d(TAG, "Download failed: $normalizedUrl -> ${e.message}")
            return null to emptyList()
        }
    }

    private fun discoverResources(
        content: String,
        sourceUrl: String,
        depth: Int,
        config: ScrapeConfig,
        contentType: String
    ): List<Pair<String, Int>> {
        val isHtml = contentType.contains("html") ||
            sourceUrl.endsWith(".html") || sourceUrl.endsWith(".htm") ||
            content.trimStart().startsWith("<!") || content.trimStart().startsWith("<html")
        val isCss = contentType.contains("css") || sourceUrl.endsWith(".css")

        val discoveredUrls = mutableSetOf<String>()

        if (isHtml) {

            val matcher = HTML_SRC_PATTERN.matcher(content)
            while (matcher.find()) {
                matcher.group(1)?.let { discoveredUrls.add(it.trim()) }
            }

            val srcsetMatcher = SRCSET_PATTERN.matcher(content)
            while (srcsetMatcher.find()) {
                srcsetMatcher.group(1)?.let { srcset ->
                    srcset.split(",").forEach { entry ->
                        entry.trim().split("\\s+".toRegex()).firstOrNull()?.let { url ->
                            discoveredUrls.add(url.trim())
                        }
                    }
                }
            }
        }

        if (isCss || isHtml) {

            val urlMatcher = CSS_URL_PATTERN.matcher(content)
            while (urlMatcher.find()) {
                urlMatcher.group(1)?.let { url ->
                    if (!url.startsWith("data:")) {
                        discoveredUrls.add(url.trim())
                    }
                }
            }

            val importMatcher = CSS_IMPORT_PATTERN.matcher(content)
            while (importMatcher.find()) {
                importMatcher.group(1)?.let { discoveredUrls.add(it.trim()) }
            }
        }

        val result = mutableListOf<Pair<String, Int>>()

        for (rawUrl in discoveredUrls) {
            if (rawUrl.isBlank() || rawUrl.startsWith("data:") || rawUrl.startsWith("javascript:") ||
                rawUrl.startsWith("mailto:") || rawUrl.startsWith("tel:") || rawUrl.startsWith("#")) {
                continue
            }

            val resolvedUrl = resolveUrl(rawUrl, sourceUrl) ?: continue
            val normalizedResolved = normalizeUrl(resolvedUrl)

            if (downloadedUrls.containsKey(normalizedResolved) || processedUrls.containsKey(normalizedResolved)) {
                continue
            }

            val resolvedHost = try { URL(normalizedResolved).host } catch (e: Exception) { continue }
            val isSameDomain = resolvedHost == baseHost || resolvedHost.endsWith(".$baseHost")

            if (!isSameDomain && !config.downloadCdnResources) {
                continue
            }

            val isPageLink = isHtml && rawUrl !in extractResourceUrls(content)
            if (isPageLink && !config.followLinks) {
                continue
            }
            if (isPageLink && !isSameDomain) {
                continue
            }

            val ext = normalizedResolved.substringAfterLast(".").substringBefore("?").lowercase()
            val isResource = ext in DOWNLOADABLE_EXTENSIONS || !isPageLink

            if (isResource || (isPageLink && depth + 1 <= config.maxDepth)) {
                result.add(normalizedResolved to depth + 1)
            }
        }
        return result
    }

    private fun extractResourceUrls(htmlContent: String): Set<String> {
        val resources = mutableSetOf<String>()
        val srcPattern = Pattern.compile(
            """(?:src|data-src|data-original|poster)\s*=\s*["']([^"']+?)["']""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = srcPattern.matcher(htmlContent)
        while (matcher.find()) {
            matcher.group(1)?.let { resources.add(it.trim()) }
        }

        val linkPattern = Pattern.compile(
            """<link[^>]+href\s*=\s*["']([^"']+?)["'][^>]*rel\s*=\s*["'](stylesheet|icon|shortcut icon|apple-touch-icon|preload|prefetch)["']""",
            Pattern.CASE_INSENSITIVE
        )
        val linkMatcher = linkPattern.matcher(htmlContent)
        while (linkMatcher.find()) {
            linkMatcher.group(1)?.let { resources.add(it.trim()) }
        }
        return resources
    }

    private fun rewriteUrls() {
        projectDir.walkTopDown().filter { it.isFile }.forEach { file ->
            val ext = file.extension.lowercase()
            if (ext in setOf("html", "htm", "css", "js", "svg")) {
                try {
                    var content = file.readText(Charsets.UTF_8)
                    val originalContent = content
                    var modified = false

                    val sortedEntries = downloadedUrls.entries
                        .filter { it.value != "__pending__" }
                        .filter { entry ->

                            File(projectDir, entry.value).isFile
                        }
                        .sortedByDescending { it.value.length }

                    for ((url, localPath) in sortedEntries) {

                        val fileRelDir = file.relativeTo(projectDir).parent ?: ""
                        val relativePath = calculateRelativePath(fileRelDir, localPath)

                        val urlVariants = generateUrlVariants(url)
                            .sortedByDescending { it.length }

                        for (variant in urlVariants) {
                            if (originalContent.contains(variant)) {
                                content = content.replace(variant, relativePath)
                                modified = true
                            }
                        }
                    }

                    if (modified) {
                        file.writeText(content, Charsets.UTF_8)
                    }
                } catch (e: Exception) {
                    AppLogger.d(TAG, "Rewrite failed for ${file.name}: ${e.message}")
                }
            }
        }
    }

    private fun generateUrlVariants(url: String): List<String> {
        val variants = mutableListOf(url)

        if (url.startsWith("https://")) {
            variants.add(url.removePrefix("https:"))
        } else if (url.startsWith("http://")) {
            variants.add(url.removePrefix("http:"))
        }

        if (url.contains("?")) {
            variants.add(url.substringBefore("?"))
        }

        return variants
    }

    private fun calculateRelativePath(fromDir: String, toPath: String): String {
        if (fromDir.isEmpty()) return toPath

        val fromParts = fromDir.split("/").filter { it.isNotEmpty() }
        val toParts = toPath.split("/").filter { it.isNotEmpty() }

        var commonPrefix = 0
        while (commonPrefix < fromParts.size && commonPrefix < toParts.size &&
               fromParts[commonPrefix] == toParts[commonPrefix]) {
            commonPrefix++
        }

        val upCount = fromParts.size - commonPrefix
        val relativeParts = List(upCount) { ".." } + toParts.subList(commonPrefix, toParts.size)

        return relativeParts.joinToString("/")
    }

    private fun collectHtmlFiles(): List<HtmlFile> {
        return projectDir.walkTopDown()
            .filter { it.isFile }
            .map { file ->
                val relativePath = file.relativeTo(projectDir).path
                HtmlFile(
                    name = relativePath,
                    path = file.absolutePath,
                    type = when (file.extension.lowercase()) {
                        "html", "htm" -> HtmlFileType.HTML
                        "css" -> HtmlFileType.CSS
                        "js", "mjs" -> HtmlFileType.JS
                        "png", "jpg", "jpeg", "gif", "webp", "svg", "ico", "avif", "bmp" -> HtmlFileType.IMAGE
                        "woff", "woff2", "ttf", "otf", "eot" -> HtmlFileType.FONT
                        else -> HtmlFileType.OTHER
                    }
                )
            }
            .toList()
    }

    private fun resolveUrl(rawUrl: String, baseUrlString: String): String? {
        return try {
            val base = URI(baseUrlString)
            val resolved = base.resolve(rawUrl.replace(" ", "%20"))
            val result = resolved.toString()

            if (result.startsWith("http://") || result.startsWith("https://")) result else null
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeUrl(url: String): String {
        return try {
            val u = URL(url)
            val path = if (u.path.isNullOrEmpty()) "/" else u.path
            val query = u.query?.let { "?$it" } ?: ""
            "${u.protocol}://${u.host}${if (u.port != -1 && u.port != u.defaultPort) ":${u.port}" else ""}$path$query"
        } catch (e: Exception) {
            url
        }
    }

    private fun urlToLocalPath(url: String): String {
        return try {
            val u = URL(url)
            val host = u.host.replace(":", "_")
            var path = u.path?.trimStart('/') ?: ""

            if (!u.query.isNullOrEmpty()) {
                val ext = path.substringAfterLast(".", "")
                val baseName = path.substringBeforeLast(".")
                val queryHash = md5(u.query).take(8)
                path = if (ext.isNotEmpty()) {
                    "${baseName}_${queryHash}.$ext"
                } else {
                    "${path}_${queryHash}"
                }
            }

            if (path.isEmpty() || path.endsWith("/")) {
                path += "index.html"
            }

            if (!path.contains(".") || path.substringAfterLast(".").length > 10) {
                path += ".html"
            }

            path = path.replace("[^a-zA-Z0-9/_.-]".toRegex(), "_")

            if (u.host == baseHost || u.host.endsWith(".$baseHost")) {
                path
            } else {
                "_cdn/$host/$path"
            }
        } catch (e: Exception) {
            "file_${fileCounter.get()}.dat"
        }
    }

    private fun generateProjectId(url: String): String {
        val host = try { URL(url).host } catch (e: Exception) { "unknown" }
        val hash = md5(url).take(8)
        return "${host.replace(".", "_")}_$hash"
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun getScrapedSites(): List<ScrapedSiteInfo> {
        val sitesDir = File(context.filesDir, "scraped_sites")
        if (!sitesDir.exists()) return emptyList()

        return sitesDir.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { dir ->
                val files = dir.walkTopDown().filter { it.isFile }.toList()
                val totalSize = files.sumOf { it.length() }
                ScrapedSiteInfo(
                    projectId = dir.name,
                    dirPath = dir.absolutePath,
                    fileCount = files.size,
                    totalSize = totalSize,
                    lastModified = dir.lastModified()
                )
            }
            ?.sortedByDescending { it.lastModified }
            ?: emptyList()
    }

    fun deleteScrapedSite(projectId: String) {
        val siteDir = File(context.filesDir, "scraped_sites/$projectId")
        if (siteDir.exists()) {
            siteDir.deleteRecursively()
        }
    }

    fun clearAllScrapedSites() {
        val sitesDir = File(context.filesDir, "scraped_sites")
        if (sitesDir.exists()) {
            sitesDir.deleteRecursively()
        }
    }

    data class ScrapedSiteInfo(
        val projectId: String,
        val dirPath: String,
        val fileCount: Int,
        val totalSize: Long,
        val lastModified: Long
    )
}
