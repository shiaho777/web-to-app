package com.webtoapp.core.webview

import com.google.gson.annotations.SerializedName
import java.net.URL

/**
 * Shared constants + helpers for the build-time Static Asset Pack feature.
 *
 * The feature scrapes static frontend assets (CSS/JS/fonts/icons) from a WEB app's
 * target URL at export time, packages them under [ASSET_BASE] in the generated APK
 * together with a [Manifest] (URL -> asset-relative path), and at runtime
 * WebViewManager serves matching sub-resource requests from those packaged bytes
 * while the live site still provides the main document and all dynamic content
 * (images/video/API/user data are left on the network).
 *
 * This object lives under core/webview so it is synced into the shell module and is
 * therefore the single source of truth shared by the host-side scraper/builder and
 * the runtime interceptor. [manifestKey] MUST stay identical on both sides — the
 * manifest is keyed by it, so any divergence silently drops cache hits.
 */
object StaticAssetPack {

    /** APK asset folder the packaged static assets + manifest live under. */
    const val ASSET_BASE = "static_pack"

    /** Asset-relative path of the URL -> path manifest JSON. */
    const val MANIFEST_PATH = "static_pack/manifest.json"

    /** File name of the manifest inside the scraped output directory. */
    const val MANIFEST_FILE_NAME = "manifest.json"

    /** Current manifest schema version. */
    const val SCHEMA_VERSION = 1

    /**
     * Static frontend asset extensions packaged by default. Deliberately excludes
     * HTML pages (the live document is always fetched from the network), raster
     * images, media, JSON/XML (usually API data) and wasm. Images can be opted in
     * via the includeImages setting.
     */
    val DEFAULT_STATIC_EXTENSIONS = setOf(
        "css", "js", "mjs",
        "woff", "woff2", "ttf", "otf", "eot",
        "svg", "ico"
    )

    /** Extra extensions packaged when the user opts into including images. */
    val IMAGE_EXTENSIONS = setOf(
        "png", "jpg", "jpeg", "gif", "webp", "avif", "bmp"
    )

    /**
     * Canonical form of a URL (lower-cased host, default port elided, fragment
     * dropped, query kept). Used for display / baseUrl / logging.
     */
    fun normalizeUrl(url: String): String {
        return try {
            val u = URL(url)
            val path = if (u.path.isNullOrEmpty()) "/" else u.path
            val query = u.query?.let { "?$it" } ?: ""
            val port = if (u.port != -1 && u.port != u.defaultPort) ":${u.port}" else ""
            "${u.protocol}://${u.host.lowercase()}$port$path$query"
        } catch (e: Exception) {
            url
        }
    }

    /**
     * The manifest key for a URL: the canonical form with any query string stripped.
     * Static assets are frequently referenced with a cache-buster query that does not
     * change the bytes, so keying without the query raises the hit rate. Safe because
     * only extension-whitelisted static files are ever packaged (dynamic query-driven
     * endpoints never enter the pack). Both the scraper (writing entries) and the
     * runtime interceptor (lookup) MUST use this exact function.
     */
    fun manifestKey(url: String): String {
        val normalized = normalizeUrl(url)
        val q = normalized.indexOf('?')
        return if (q >= 0) normalized.substring(0, q) else normalized
    }

    /** Lower-cased file extension of a URL path (empty when none). */
    fun extensionOf(url: String): String {
        val path = try {
            URL(url).path ?: url
        } catch (e: Exception) {
            url
        }
        return path.substringAfterLast('.', "").lowercase()
    }

    /**
     * The URL -> asset-path manifest packaged alongside the static assets. Written by
     * the host-side scraper and parsed at runtime with Gson.
     */
    data class Manifest(
        @SerializedName("version") val version: Int = SCHEMA_VERSION,
        @SerializedName("scrapedAt") val scrapedAt: Long = 0L,
        @SerializedName("baseUrl") val baseUrl: String = "",
        @SerializedName("entries") val entries: Map<String, String> = emptyMap()
    ) {
        /**
         * Resolve a request URL to a packaged asset-relative path, or null on a miss.
         */
        fun lookup(url: String): String? = if (entries.isEmpty()) null else entries[manifestKey(url)]

        /** True when the pack is older than [maxAgeDays] (0 disables expiry). */
        fun isExpired(maxAgeDays: Int, nowMs: Long = System.currentTimeMillis()): Boolean {
            if (maxAgeDays <= 0 || scrapedAt <= 0L) return false
            val maxAgeMs = maxAgeDays.toLong() * 24L * 3600L * 1000L
            return nowMs - scrapedAt > maxAgeMs
        }
    }
}
