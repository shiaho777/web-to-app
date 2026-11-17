package com.webtoapp.core.adblock

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdBlockerClearHostsPersistenceTest {

    private lateinit var context: Context
    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        adBlocker = AdBlocker()
        AdBlockFilterCache.clearCache(context)
    }

    @Test
    fun `clearHostsFileRules persists across a restart and does not resurrect sources`() = runBlocking {
        val url = "https://filters.example.test/clear-me.txt"
        AdBlockFilterCache.cacheUrlContent(context, url, "||ads.example.test^\n||tracker.example.test^")
        adBlocker.importHostsFromUrl(url, context, displayName = "Clear Me")
        adBlocker.saveHostsRules(context)
        assertThat(adBlocker.getAllDownloadedSourceKeys()).contains(url)
        assertThat(adBlocker.getRuleCount()).isGreaterThan(0)

        val cleared = adBlocker.clearHostsFileRules(context)
        assertThat(cleared.isSuccess).isTrue()
        assertThat(adBlocker.getAllDownloadedSourceKeys()).isEmpty()
        assertThat(adBlocker.getRuleCount()).isEqualTo(0)

        val fresh = AdBlocker()
        fresh.hydrateSourcesMetadata(context)
        assertThat(fresh.getAllDownloadedSourceKeys()).isEmpty()
        assertThat(fresh.getCustomHostsSources()).isEmpty()

        val loaded = AdBlocker()
        loaded.loadHostsRules(context)
        assertThat(loaded.getAllDownloadedSourceKeys()).isEmpty()
        assertThat(loaded.getHostsFileRuleCount()).isEqualTo(0)
        assertThat(loaded.getRuleCount()).isEqualTo(0)
    }

    @Test
    fun `clearHostsFileRules removes cached source content so a re-add downloads fresh`() = runBlocking {
        val url = "https://filters.example.test/content.txt"
        AdBlockFilterCache.cacheUrlContent(context, url, "||ads.example.test^")
        AdBlockFilterCache.saveSourceContent(context, url, "||ads.example.test^")
        adBlocker.importHostsFromUrl(url, context)
        adBlocker.saveHostsRules(context)

        adBlocker.clearHostsFileRules(context)

        assertThat(AdBlockFilterCache.getSourceContent(context, url)).isNull()
        assertThat(AdBlockFilterCache.getCachedUrlContent(context, url)).isNull()
    }
}
