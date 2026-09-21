package com.webtoapp.core.adblock

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Coverage for [AdBlocker.getCosmeticPayloadJson] — the single JSON payload the
 * document-start cosmetic script consumes (issue #998). The document-start path
 * must be able to fetch css + hide batches + procedural rules for a host in one
 * bridge call so the hide stylesheet lands before first paint.
 */
class AdBlockerCosmeticPayloadTest {

    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        adBlocker = AdBlocker()
    }

    @Test
    fun `payload is empty while the blocker is disabled`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.addRule("example.com##.ad-banner-slot")
        adBlocker.setEnabled(false)

        assertThat(adBlocker.getCosmeticPayloadJson("example.com")).isEmpty()
    }

    @Test
    fun `payload carries css and hide batches for a matching host`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com##.ad-banner-slot")

        val payload = adBlocker.getCosmeticPayloadJson("example.com")

        assertThat(payload).startsWith("{")
        assertThat(payload).endsWith("}")
        assertThat(payload).contains("\"css\":\"")
        assertThat(payload).contains(".ad-banner-slot")
        assertThat(payload).contains("\"batches\":[")
        assertThat(payload).contains("\"proc\":[]")
    }

    @Test
    fun `host-scoped selectors do not leak into another host's payload`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com##.ad-banner-slot")

        val other = adBlocker.getCosmeticPayloadJson("other.com")

        assertThat(other).doesNotContain("ad-banner-slot")
        // Built-in hide selectors are generic — they apply on every host.
        assertThat(other).contains("adsbygoogle")
    }

    @Test
    fun `procedural rules surface inside the payload proc array`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com##.item:has-text(Sponsor)")

        val payload = adBlocker.getCosmeticPayloadJson("example.com")

        assertThat(payload).contains("\"proc\":[{\"b\":\".item\"")
        assertThat(payload).contains("t:Sponsor")
    }

    @Test
    fun `payload is valid JSON with escaped selector content`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        // Quotes and backslashes inside selectors must survive JSON.parse on the page.
        adBlocker.addRule("example.com##a[href*=\"promo\"]")

        val payload = adBlocker.getCosmeticPayloadJson("example.com")

        assertThat(payload).contains("\\\"")
        // Raw newlines would terminate the JS string literal the payload is embedded in.
        val cssField = payload.substringAfter("\"css\":\"").substringBefore("\",\"batches\"")
        assertThat(cssField).doesNotContain("\n")
    }
}
