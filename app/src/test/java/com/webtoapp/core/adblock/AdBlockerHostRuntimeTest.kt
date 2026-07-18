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
}
