package com.webtoapp.core.adblock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Issue #1076: a user could not refresh a URL-based filter list — the visible
 * refresh button went through the cached import path (24h URL cache hit), and
 * removing a source left its URL cache entry behind, so delete-and-re-add
 * resurrected the stale copy. The user-facing paths must bypass the cache and
 * removal must evict it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdBlockManualRefreshTest {

    private lateinit var context: Context
    private val url = "https://filters.example.test/custom.txt"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AdBlockFilterCache.clearCache(context)
    }

    @Test
    fun `removing a source evicts its URL cache entry`() = runBlocking {
        AdBlockFilterCache.cacheUrlContent(context, url, "||ads.example.test^")
        val blocker = AdBlocker()
        assertThat(blocker.importHostsFromUrl(url, context).isSuccess).isTrue()

        blocker.removeHostsSource(context, url)

        assertThat(AdBlockFilterCache.getCachedUrlContent(context, url)).isNull()
        assertThat(AdBlockFilterCache.getSourceContent(context, url)).isNull()
    }

    @Test
    fun `re-added source cannot restore the deleted snapshot`() = runBlocking {
        AdBlockFilterCache.cacheUrlContent(context, url, "||stale.example.test^")
        val blocker = AdBlocker()
        blocker.importHostsFromUrl(url, context)
        blocker.removeHostsSource(context, url)

        // The same URL re-imported through the cached path must not resolve any
        // stored content — with no network in tests it can only fail, never
        // silently resurrect the deleted list.
        val reimport = blocker.importHostsFromUrl(url, context)
        assertThat(reimport.isFailure).isTrue()
    }

    @Test
    fun `user-facing refresh and manual import bypass the URL cache`() {
        val screen = readSanitized("com/webtoapp/ui/screens/HostsAdBlockScreen.kt")
        val importCalls = Regex("importHostsFromUrl\\([^\\n]*forceNetwork = true")
            .findAll(screen).count()
        assertWithMessage(
            "HostsAdBlockScreen must pass forceNetwork = true on its " +
                "refresh button and its add-by-URL dialog so user-initiated " +
                "fetches bypass the 24h URL cache"
        ).that(importCalls).isAtLeast(2)
    }

    @Test
    fun `source removal also evicts the URL cache in the blocker`() {
        val src = readSanitized("com/webtoapp/core/adblock/AdBlocker.kt")
        val removal = src.substringAfter("suspend fun removeHostsSource")
            .substringBefore("private suspend fun rebuildHostsFromSourceContents")
        assertWithMessage("removeHostsSource must evict the stale URL cache entry")
            .that(removal).contains("AdBlockFilterCache.removeUrlContent")
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Test harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
