package com.webtoapp.core.adblock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Coverage for subscription auto-refresh: `! Expires:` headers drive a per-source
 * refresh interval (clamped), due sources re-download and rebuild the engine,
 * not-due sources are untouched, and failed downloads back off without losing
 * the previously ingested content.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdBlockSubscriptionRefreshTest {

    private lateinit var context: Context
    private val url = "https://filters.example.test/refreshable.txt"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AdBlockFilterCache.clearCache(context)
    }

    @Test
    fun `expires header parses into a clamped refresh interval`() {
        assertThat(AdBlocker.expiresIntervalMs("! Title: X\n! Expires: 5 days\n||a.example^"))
            .isEqualTo(5 * 24 * 60 * 60 * 1000L)
        assertThat(AdBlocker.expiresIntervalMs("! Expires: 12 hours\n||a.example^"))
            .isEqualTo(12 * 60 * 60 * 1000L)
        // Clamped to the 6h floor and 14d ceiling.
        assertThat(AdBlocker.expiresIntervalMs("! Expires: 1 hours\n||a.example^"))
            .isEqualTo(AdBlocker.MIN_REFRESH_INTERVAL_MS)
        assertThat(AdBlocker.expiresIntervalMs("! Expires: 30 days\n||a.example^"))
            .isEqualTo(AdBlocker.MAX_REFRESH_INTERVAL_MS)
        // Missing header falls back to 24h.
        assertThat(AdBlocker.expiresIntervalMs("! Title: X\n||a.example^"))
            .isEqualTo(AdBlocker.DEFAULT_REFRESH_INTERVAL_MS)
    }

    @Test
    fun `due source re-downloads and rebuilds the engine`() = runBlocking {
        val v1 = "! Expires: 1 days\n||ads.example.test^"
        AdBlockFilterCache.cacheUrlContent(context, url, v1)
        val importer = AdBlocker()
        assertThat(importer.importHostsFromUrl(url, context).isSuccess).isTrue()
        importer.saveHostsRules(context)

        // Age the registry timestamp past the 1 day interval.
        val registry = File(context.filesDir, "adblock_hosts_sources.txt")
        registry.writeText("$url\t1\t1\t\t${System.currentTimeMillis() - 2 * 86400000L}\t86400000")

        val fresh = AdBlocker()
        fresh.loadHostsRules(context)
        fresh.setEnabled(true)
        assertThat(fresh.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()

        // Updated list content arrives through the URL cache (forceNetwork = false).
        val v2 = "! Expires: 1 days\n||newads.example.test^"
        AdBlockFilterCache.cacheUrlContent(context, url, v2)

        val changed = fresh.refreshDueSources(context, listOf(url), forceNetwork = false)

        assertThat(changed).isTrue()
        assertThat(fresh.shouldBlock("https://newads.example.test/banner.js", resourceType = "script")).isTrue()
        assertThat(fresh.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isFalse()
    }

    @Test
    fun `not-due source keeps its ingested content`() = runBlocking {
        val v1 = "! Expires: 1 days\n||ads.example.test^"
        AdBlockFilterCache.cacheUrlContent(context, url, v1)
        val importer = AdBlocker()
        importer.importHostsFromUrl(url, context)
        importer.saveHostsRules(context)

        val fresh = AdBlocker()
        fresh.loadHostsRules(context)
        fresh.setEnabled(true)

        // Fresh import timestamp: interval not elapsed, so the newer cached copy must
        // NOT be picked up.
        AdBlockFilterCache.cacheUrlContent(context, url, "! Expires: 1 days\n||newads.example.test^")

        val changed = fresh.refreshDueSources(context, listOf(url), forceNetwork = false)

        assertThat(changed).isFalse()
        assertThat(fresh.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()
        assertThat(fresh.shouldBlock("https://newads.example.test/banner.js", resourceType = "script")).isFalse()
    }

    @Test
    fun `failed download backs off and keeps previous content`() = runBlocking {
        val v1 = "! Expires: 1 days\n||ads.example.test^"
        AdBlockFilterCache.cacheUrlContent(context, url, v1)
        val importer = AdBlocker()
        importer.importHostsFromUrl(url, context)
        importer.saveHostsRules(context)

        val registry = File(context.filesDir, "adblock_hosts_sources.txt")
        registry.writeText("$url\t1\t1\t\t${System.currentTimeMillis() - 2 * 86400000L}\t86400000")

        val fresh = AdBlocker()
        fresh.loadHostsRules(context)
        fresh.setEnabled(true)

        // No cached update and no network in Robolectric: the download fails.
        val changed = fresh.refreshDueSources(context, listOf(url), forceNetwork = true)

        assertThat(changed).isFalse()
        assertThat(fresh.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()

        // Second call within the backoff window skips the source without retrying.
        assertThat(fresh.refreshDueSources(context, listOf(url), forceNetwork = true)).isFalse()
    }
}
