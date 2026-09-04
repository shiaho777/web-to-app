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
    fun `export serialization does not compile regexes`() {
        adBlocker.addRule("||example.com/ads/banner*.js")
        // Must round-trip the ORIGINAL rule text (modifiers preserved opaquely).
        assertThat(adBlocker.getCompiledRulesText()).contains("||example.com/ads/banner*.js")
    }
}
