package com.webtoapp.core.adblock

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class AdBlockerCacheCollisionTest {

    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        adBlocker = AdBlocker()
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
    }

    @Test
    fun `java hash colliding urls keep independent verdicts`() {
        val blockedUrl = "https://example.com/Aa"
        val allowedUrl = "https://example.com/BB"
        assertThat(blockedUrl.hashCode()).isEqualTo(allowedUrl.hashCode())

        adBlocker.addRule("example.com/Aa")

        assertThat(adBlocker.shouldBlock(blockedUrl, pageHost = "page.example")).isTrue()
        assertThat(adBlocker.shouldBlock(allowedUrl, pageHost = "page.example")).isFalse()
        assertThat(adBlocker.shouldBlock(blockedUrl, pageHost = "page.example")).isTrue()
        assertThat(adBlocker.shouldBlock(allowedUrl, pageHost = "page.example")).isFalse()
    }

    @Test
    fun `same url different page host keeps independent verdicts`() {
        adBlocker.addRule("@@||ads.example.com^\$domain=allowed.page")
        adBlocker.addRule("||ads.example.com^")

        val url = "https://ads.example.com/banner.js"
        assertThat(adBlocker.shouldBlock(url, pageHost = "allowed.page")).isFalse()
        assertThat(adBlocker.shouldBlock(url, pageHost = "other.page")).isTrue()
    }
}
