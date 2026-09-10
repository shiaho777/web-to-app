package com.webtoapp.core.adblock

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Regression coverage for issue #623: user-imported custom filter sources must carry
 * display metadata (registry v2), be listable via [AdBlocker.getCustomHostsSources]
 * (excluding presets), survive restarts through the registry, be deletable, and be
 * consumable per-app through the same subscription path as URL sources.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdBlockerCustomSourcesTest {

    private lateinit var context: Context
    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        adBlocker = AdBlocker()
        AdBlockFilterCache.clearCache(context)
    }

    @Test
    fun `file import records a display name and lists a custom source`() = runBlocking {
        val uri = Uri.parse("content://media/external/filters/my-blocklist.txt")
        Shadows.shadowOf(context.contentResolver).registerInputStream(
            uri,
            "||ads.example.test^\n||tracker.example.test^".byteInputStream()
        )

        val result = adBlocker.importHostsFromFile(context, uri)

        assertThat(result.isSuccess).isTrue()
        val custom = adBlocker.getCustomHostsSources()
        assertThat(custom).hasSize(1)
        assertThat(custom.first().url).isEqualTo("file:$uri")
        assertThat(custom.first().name).isEqualTo("my-blocklist.txt")
    }

    @Test
    fun `url import with display name is listed and persists across restart`() = runBlocking {
        val url = "https://filters.example.test/custom.txt"
        val content = "||ads.example.test^"
        AdBlockFilterCache.cacheUrlContent(context, url, content)

        adBlocker.importHostsFromUrl(url, context, displayName = "My Custom List")
        adBlocker.saveHostsRules(context)

        assertThat(adBlocker.getCustomHostsSources().map { it.name }).containsExactly("My Custom List")

        // Simulate a restart: a fresh instance rehydrates the registry from disk.
        val fresh = AdBlocker()
        fresh.hydrateSourcesMetadata(context)
        val restored = fresh.getCustomHostsSources()
        assertThat(restored).hasSize(1)
        assertThat(restored.first().name).isEqualTo("My Custom List")
        assertThat(restored.first().url).isEqualTo(url)
    }

    @Test
    fun `preset urls are never listed as custom sources`() = runBlocking {
        val preset = AdBlocker.getPopularHostsSources().first()
        AdBlockFilterCache.cacheUrlContent(context, preset.url, "||ads.example.test^")

        adBlocker.importHostsFromUrl(preset.url, context, displayName = preset.name)

        assertThat(adBlocker.isHostsSourceDownloaded(preset.url)).isTrue()
        assertThat(adBlocker.getCustomHostsSources()).isEmpty()
    }

    @Test
    fun `removeHostsSource drops the custom source and its name`() = runBlocking {
        val url = "https://filters.example.test/custom.txt"
        AdBlockFilterCache.cacheUrlContent(context, url, "||ads.example.test^")
        adBlocker.importHostsFromUrl(url, context, displayName = "Doomed List")
        assertThat(adBlocker.getCustomHostsSources()).hasSize(1)

        adBlocker.removeHostsSource(context, url)
        adBlocker.saveHostsRules(context)

        assertThat(adBlocker.getCustomHostsSources()).isEmpty()
        assertThat(adBlocker.isHostsSourceDownloaded(url)).isFalse()

        val fresh = AdBlocker()
        fresh.hydrateSourcesMetadata(context)
        assertThat(fresh.getCustomHostsSources()).isEmpty()
    }

    @Test
    fun `url import without display name falls back to the list title header`() = runBlocking {
        val url = "https://raw.example.test/giant-filters.txt"
        AdBlockFilterCache.cacheUrlContent(
            context,
            url,
            "! Title: Giant Pharmacy\n! Description: Dark mode rules\n" +
                "||ads.example.test^\nexample.com##.banner:style(background: #121212 !important)"
        )

        adBlocker.importHostsFromUrl(url, context)
        adBlocker.saveHostsRules(context)

        val custom = adBlocker.getCustomHostsSources()
        assertThat(custom).hasSize(1)
        assertThat(custom.first().name).isEqualTo("Giant Pharmacy")

        val fresh = AdBlocker()
        fresh.hydrateSourcesMetadata(context)
        assertThat(fresh.getCustomHostsSources().first().name).isEqualTo("Giant Pharmacy")
    }

    @Test
    fun `compiled state round-trips style overrides`() = runBlocking {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com##.banner:style(background: #121212 !important)")
        adBlocker.addRule("example.com##.slot")
        adBlocker.saveHostsRules(context)

        val fresh = AdBlocker()
        fresh.loadHostsRules(context)
        fresh.setEnabled(true)

        val css = fresh.getCosmeticFilterCss("example.com")
        assertThat(css).contains(".banner { background: #121212 !important; }")
        assertThat(fresh.getCosmeticHideBatches("example.com").any { it.contains(".slot") }).isTrue()
    }

    @Test
    fun `compiled state round-trips injected css rules`() = runBlocking {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com#$#body { background: #121212 !important; }")
        adBlocker.saveHostsRules(context)

        val fresh = AdBlocker()
        fresh.loadHostsRules(context)
        fresh.setEnabled(true)

        assertThat(fresh.getCosmeticFilterCss("example.com"))
            .contains("body { background: #121212 !important; }")
    }

    @Test
    fun `compiled state round-trips procedural rules`() = runBlocking {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com##.item:has-text(Sponsor):upward(1)")
        adBlocker.saveHostsRules(context)

        val fresh = AdBlocker()
        fresh.loadHostsRules(context)
        fresh.setEnabled(true)

        val js = fresh.getCosmeticProceduralRulesJs("example.com")
        assertThat(js).contains("\"b\":\".item\"")
        assertThat(js).contains("t:Sponsor")
        assertThat(js).contains("u:1")
    }

    @Test
    fun `file source key is consumable through the per-app subscription path`() = runBlocking {
        val uri = Uri.parse("content://media/external/filters/block.txt")
        Shadows.shadowOf(context.contentResolver).registerInputStream(
            uri,
            "||ads.example.test^".byteInputStream()
        )
        adBlocker.importHostsFromFile(context, uri)
        val fileKey = "file:$uri"

        adBlocker.prepareRuntimeFilters(
            context = context,
            enabled = true,
            customRules = emptyList(),
            subscriptionUrls = listOf(fileKey)
        )

        assertThat(adBlocker.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()
    }
}
