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
class AdBlockerHostRuntimeTest {

    private lateinit var context: Context
    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        adBlocker = AdBlocker()
        AdBlockFilterCache.clearCache(context)
    }

    @Test
    fun prepareRuntimeFiltersReappliesCachedSubscriptionAfterInitializeWipe() = runBlocking {
        val source = "https://example.test/easylist.txt"
        val content = """
            [Adblock Plus 2.0]
            ||ads.example.test^
            ||tracker.example.test^
        """.trimIndent()
        AdBlockFilterCache.saveSourceContent(context, source, content)
        AdBlockFilterCache.cacheUrlContent(context, source, content)

        adBlocker.prepareRuntimeFilters(
            context = context,
            enabled = true,
            customRules = emptyList(),
            subscriptionUrls = listOf(source)
        )
        assertThat(adBlocker.isEnabled()).isTrue()
        assertThat(adBlocker.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()

        adBlocker.initialize(emptyList(), useDefaultRules = false)
        assertThat(adBlocker.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isFalse()

        adBlocker.prepareRuntimeFilters(
            context = context,
            enabled = true,
            customRules = listOf("||custom.example.test^"),
            subscriptionUrls = listOf(source)
        )
        assertThat(adBlocker.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()
        assertThat(adBlocker.shouldBlock("https://custom.example.test/x.js", resourceType = "script")).isTrue()
    }

    @Test
    fun compileRulesTextIncludesSelectedSubscriptionAndCustomRules() = runBlocking {
        val source = "https://example.test/list.txt"
        val content = """
            [Adblock Plus 2.0]
            ||compiled-ad.example.test^
        """.trimIndent()
        AdBlockFilterCache.saveSourceContent(context, source, content)
        AdBlockFilterCache.cacheUrlContent(context, source, content)

        val compiled = adBlocker.compileRulesText(
            context = context,
            subscriptionUrls = listOf(source),
            customRules = listOf("||manual.example.test^")
        )
        assertThat(compiled).contains("compiled-ad.example.test")
        assertThat(compiled).contains("manual.example.test")
    }

    @Test
    fun compileRulesTextKeepsAbpLinesAndConvertsHostsWithoutBuildingAnEngine() = runBlocking {
        val abp = "https://example.test/ublock.txt"
        AdBlockFilterCache.saveSourceContent(
            context,
            abp,
            """
            [Adblock Plus 2.0]
            ! Title: uBlock filters
            ||ads.example.test^
            example.com##.ad-slot
            example.com##+js(set, ads, false)
            # comment kept out
            """.trimIndent()
        )
        val hosts = "https://example.test/hosts.txt"
        AdBlockFilterCache.saveSourceContent(
            context,
            hosts,
            """
            # hosts list
            0.0.0.0 tracker.example.test
            127.0.0.1 localhost
            """.trimIndent()
        )

        val started = System.nanoTime()
        val compiled = adBlocker.compileRulesText(
            context = context,
            subscriptionUrls = listOf(abp, hosts),
            customRules = listOf("! ignored", "||custom.example.test^")
        )
        val elapsedMs = (System.nanoTime() - started) / 1_000_000

        assertThat(compiled).contains("||ads.example.test^")
        assertThat(compiled).contains("example.com##.ad-slot")
        assertThat(compiled).contains("example.com##+js(set, ads, false)")
        assertThat(compiled).contains("||tracker.example.test^")
        assertThat(compiled).contains("||custom.example.test^")
        assertThat(compiled).doesNotContain("Title:")
        assertThat(compiled).doesNotContain("localhost")
        assertThat(compiled).doesNotContain("! ignored")
        // Parsing these into a second engine is what froze export. A straight
        // copy of twenty thousand unanchored rules stays well under a second.
        val bulk = "https://example.test/bulk.txt"
        val bulkBody = buildString {
            append("[Adblock Plus 2.0]\n")
            repeat(20_000) { append("/ads/banner-").append(it).append(".js\n") }
        }
        AdBlockFilterCache.saveSourceContent(context, bulk, bulkBody)
        val bulkStarted = System.nanoTime()
        val bulkCompiled = adBlocker.compileRulesText(context, listOf(bulk))
        val bulkMs = (System.nanoTime() - bulkStarted) / 1_000_000
        assertThat(bulkCompiled.lines()).hasSize(20_000)
        assertThat(bulkMs).isLessThan(2_000)
        assertThat(elapsedMs).isLessThan(2_000)
    }

    @Test
    fun prepareRuntimeFiltersDisablesWhenRequested() = runBlocking {
        adBlocker.prepareRuntimeFilters(
            context = context,
            enabled = true,
            customRules = listOf("||ads.example.test^"),
            subscriptionUrls = emptyList()
        )
        assertThat(adBlocker.isEnabled()).isTrue()
        adBlocker.prepareRuntimeFilters(
            context = context,
            enabled = false,
            customRules = emptyList(),
            subscriptionUrls = emptyList()
        )
        assertThat(adBlocker.isEnabled()).isFalse()
        assertThat(adBlocker.shouldBlock("https://ads.example.test/a.js", resourceType = "script")).isFalse()
    }

    @Test
    fun importHostsFromUrlPersistsSourceRegistryBeforeHeavyParseSurvivesProcessDeath() = runBlocking {
        val source = "https://example.test/loop.txt"
        val content = """
            [Adblock Plus 2.0]
            ||ads.example.test^
            ||tracker.example.test^
        """.trimIndent()
        AdBlockFilterCache.cacheUrlContent(context, source, content)

        val first = adBlocker
        val result = first.importHostsFromUrl(source, context, onProgress = null)
        assertThat(result.isSuccess).isTrue()
        assertThat(first.isHostsSourceEnabled(source)).isTrue()

        val deadProcessAdBlocker = AdBlocker()

        deadProcessAdBlocker.loadHostsRules(context)

        assertThat(deadProcessAdBlocker.isHostsSourceEnabled(source)).isTrue()
        assertThat(deadProcessAdBlocker.getEnabledHostsSources()).contains(source)

        deadProcessAdBlocker.prepareRuntimeFilters(
            context = context,
            enabled = true,
            customRules = emptyList(),
            subscriptionUrls = listOf(source)
        )
        assertThat(deadProcessAdBlocker.shouldBlock("https://ads.example.test/banner.js", resourceType = "script")).isTrue()
    }

    @Test
    fun pathologicalAbpPatternDoesNotCrashParserAndSafeRuleStillApplies() {
        val pathological = "||evil" + "*".repeat(5) + ".test^"
        adBlocker.setEnabled(true)
        adBlocker.addRule(pathological)

        adBlocker.addRule("||safe.example.test^")

        assertThat(adBlocker.shouldBlock("https://safe.example.test/ad.js", resourceType = "script")).isTrue()
    }
}
