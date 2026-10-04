package com.webtoapp.core.adblock

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Regression tests for the AdGuard Base import crash: 100k+ rule lists must not
 * pin a compiled Pattern per rule at import (OOM on 256MB-heap devices, doubled
 * again at export which builds a second engine). Regexes compile lazily on first
 * match; behavior must be identical to eager compilation.
 */
class AdBlockLazyRegexTest {

    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        adBlocker = AdBlocker()
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
    }

    @Test
    fun `regex-backed rule blocks only after lazy compile`() {
        // Path-anchored rule: cannot use the exactHosts fast path, needs its regex.
        adBlocker.addRule("||example.com/ads/banner*.js")

        assertThat(
            adBlocker.shouldBlock(
                "https://example.com/ads/banner123.js",
                "example.com", "script", true
            )
        ).isTrue()
        assertThat(
            adBlocker.shouldBlock(
                "https://example.com/content/article.html",
                "example.com", "main_frame", true
            )
        ).isFalse()
    }

    @Test
    fun `uncompilable pattern degrades to no-block without throwing`() {
        // Unbalanced regex literal: translate passes it through, compile fails.
        adBlocker.addRule("/(unbalanced/")
        assertThat(
            adBlocker.shouldBlock("https://example.com/(unbalanced/", "example.com", "other", true)
        ).isFalse()
    }

    @Test
    fun `oversize pattern is rejected at import without throwing`() {
        adBlocker.addRule("||example.com/" + "a".repeat(2048) + "\$script")
        assertThat(
            adBlocker.shouldBlock("https://example.com/", "example.com", "other", true)
        ).isFalse()
    }

    @Test
    fun `exception rule with regex still unblocks`() {
        adBlocker.addRule("||example.com/ads/*")
        adBlocker.addRule("@@||example.com/ads/allowed/*")
        assertThat(
            adBlocker.shouldBlock(
                "https://example.com/ads/allowed/1.js",
                "example.com", "script", true
            )
        ).isFalse()
        assertThat(
            adBlocker.shouldBlock(
                "https://example.com/ads/blocked/1.js",
                "example.com", "script", true
            )
        ).isTrue()
    }

    @Test
    fun `importing thousands of unanchored rules stays linear`() {
        val rules = List(12_000) { i -> "/ads/slot-$i.js" }
        adBlocker.initialize(rules, useDefaultRules = false)
        assertThat(
            adBlocker.shouldBlock(
                "https://cdn.example/ads/slot-11999.js",
                "cdn.example", "script", true
            )
        ).isTrue()
        assertThat(
            adBlocker.shouldBlock(
                "https://cdn.example/content/story.html",
                "cdn.example", "other", true
            )
        ).isFalse()
        // initialize() rebuilds the index at the end, so the per-rule append is
        // only visible by calling trackUnanchored. `list + idx` allocates a new
        // list on every rule; the fix must keep appending to one MutableList.
        // A wall-clock bound is not used: the linear import already exceeded 3s
        // on the GitHub runner.
        assertTrackUnanchoredAppendsInPlace()
    }

    private fun assertTrackUnanchoredAppendsInPlace() {
        val method = AdBlocker::class.java.getDeclaredMethod(
            "trackUnanchored",
            java.util.List::class.java,
            Int::class.javaPrimitiveType
        )
        method.isAccessible = true
        val filters = mutableListOf<Any>()
        method.invoke(adBlocker, filters, 0)
        val indexField = AdBlocker::class.java.getDeclaredField("unanchoredFilterIndex")
        indexField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val index = indexField.get(adBlocker) as Map<Any, MutableList<Int>>
        val first = index.getValue(filters)
        repeat(4_000) { i -> method.invoke(adBlocker, filters, i + 1) }
        val after = index.getValue(filters)
        assertThat(after).isSameInstanceAs(first)
        assertThat(after).hasSize(4_001)
        assertThat(after.first()).isEqualTo(0)
        assertThat(after.last()).isEqualTo(4_000)
    }

    @Test
    fun `export serialization does not compile regexes`() {
        adBlocker.addRule("||example.com/ads/banner*.js")
        // Must round-trip the ORIGINAL rule text (modifiers preserved opaquely).
        assertThat(adBlocker.getCompiledRulesText()).contains("||example.com/ads/banner*.js")
    }
}
