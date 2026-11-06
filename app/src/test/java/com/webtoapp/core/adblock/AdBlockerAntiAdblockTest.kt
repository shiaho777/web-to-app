package com.webtoapp.core.adblock

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class AdBlockerAntiAdblockTest {

    private lateinit var adBlocker: AdBlocker

    @Before
    fun setUp() {
        adBlocker = AdBlocker()
    }

    @Test
    fun `remote host without scriptlets still receives the universal anti-adblock script`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)

        val script = adBlocker.getAntiAdblockScript("example.com")

        assertThat(script).isNotEmpty()
        assertThat(script).contains("__wta_aab_injected__")
        assertThat(script).contains("blockAdBlock")
        assertThat(script).contains("adsbygoogle")
    }

    @Test
    fun `anti-adblock script is empty when the blocker is disabled`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(false)

        assertThat(adBlocker.getAntiAdblockScript("example.com")).isEmpty()
    }

    @Test
    fun `matching scriptlet is prepended without suppressing the universal script`() {
        adBlocker.initialize(useDefaultRules = false)
        adBlocker.setEnabled(true)
        adBlocker.addRule("example.com##+js(set-constant, wtaTestFlag, true)")

        val matching = adBlocker.getAntiAdblockScript("example.com")
        val other = adBlocker.getAntiAdblockScript("other.com")

        assertThat(matching).contains("wtaTestFlag")
        assertThat(matching).contains("blockAdBlock")
        assertThat(other).doesNotContain("wtaTestFlag")
        assertThat(other).contains("blockAdBlock")
    }

    @Test
    fun `universal script is emitted for every host when defaults are loaded`() {
        adBlocker.initialize(useDefaultRules = true)
        adBlocker.setEnabled(true)

        assertThat(adBlocker.getAntiAdblockScript("news.example")).contains("blockAdBlock")
        assertThat(adBlocker.getAntiAdblockScript("shop.example")).contains("blockAdBlock")
    }
}
