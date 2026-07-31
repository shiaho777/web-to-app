package com.webtoapp.core.scraper

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.webview.StaticAssetPack
import org.junit.Test

/**
 * Pure-logic tests for the Static Asset Pack feature: URL normalization, manifest keying /
 * lookup, expiry, and on-disk path layout. No network or Android context required.
 */
class StaticAssetScraperTest {

    @Test
    fun `normalizeUrl lowercases host and keeps path case and query`() {
        assertThat(StaticAssetPack.normalizeUrl("https://Example.COM/Path/File.css?v=2"))
            .isEqualTo("https://example.com/Path/File.css?v=2")
    }

    @Test
    fun `normalizeUrl elides the default port`() {
        assertThat(StaticAssetPack.normalizeUrl("https://example.com:443/a.js"))
            .isEqualTo("https://example.com/a.js")
    }

    @Test
    fun `manifestKey strips the query so cache-busted references still hit`() {
        assertThat(StaticAssetPack.manifestKey("https://example.com/app.css?v=1"))
            .isEqualTo(StaticAssetPack.manifestKey("https://example.com/app.css?v=999"))
    }

    @Test
    fun `extensionOf reads the path extension ignoring any query`() {
        assertThat(StaticAssetPack.extensionOf("https://example.com/fonts/a.woff2?x=1")).isEqualTo("woff2")
        assertThat(StaticAssetPack.extensionOf("https://example.com/page")).isEmpty()
    }

    @Test
    fun `default extension set packs stylesheets scripts fonts and icons but not images`() {
        val defaults = StaticAssetPack.DEFAULT_STATIC_EXTENSIONS
        assertThat(defaults).containsAtLeast("css", "js", "woff2", "ttf", "svg", "ico")
        assertThat(defaults).containsNoneOf("png", "jpg", "webp", "html", "json", "mp4")
    }

    @Test
    fun `manifest lookup hits regardless of query and misses unknown urls`() {
        val manifest = StaticAssetPack.Manifest(
            scrapedAt = System.currentTimeMillis(),
            entries = mapOf("https://example.com/app.css" to "css/app.css")
        )
        assertThat(manifest.lookup("https://example.com/app.css?v=5")).isEqualTo("css/app.css")
        assertThat(manifest.lookup("https://example.com/app.css")).isEqualTo("css/app.css")
        assertThat(manifest.lookup("https://example.com/other.js")).isNull()
        assertThat(StaticAssetPack.Manifest().lookup("https://example.com/app.css")).isNull()
    }

    @Test
    fun `manifest expiry honors maxAgeDays and zero disables expiry`() {
        val now = 1_000_000_000_000L
        val dayMs = 24L * 3600L * 1000L
        val fresh = StaticAssetPack.Manifest(scrapedAt = now - 5 * dayMs)
        assertThat(fresh.isExpired(30, now)).isFalse()
        val stale = StaticAssetPack.Manifest(scrapedAt = now - 40 * dayMs)
        assertThat(stale.isExpired(30, now)).isTrue()
        assertThat(stale.isExpired(0, now)).isFalse()
    }

    @Test
    fun `relativePathFor keeps same-host paths and namespaces cdn hosts`() {
        val sameHost = StaticAssetScraper.relativePathFor("https://example.com/static/app.js", "example.com")
        assertThat(sameHost).isEqualTo("static/app.js")

        val subdomain = StaticAssetScraper.relativePathFor("https://assets.example.com/x/font.woff2", "example.com")
        assertThat(subdomain).isEqualTo("x/font.woff2")

        val cdn = StaticAssetScraper.relativePathFor("https://cdn.other.net/lib/x.woff2", "example.com")
        assertThat(cdn).startsWith("_cdn/cdn.other.net/")
    }
}
