package com.webtoapp.core.apkbuilder

import com.webtoapp.data.model.WebApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockExportWiringTest {

    @Test
    fun buildAdBlockBlockHonorsEnabledFlagWithoutRules() {
        val app = WebApp(
            name = "AdBlock",
            url = "https://example.com",
            adBlockEnabled = true,
            adBlockRules = emptyList(),
            adBlockSubscriptions = emptyList()
        )
        val config = app.toApkConfig("com.example.adblock")
        assertTrue(config.adBlockEnabled)
        assertTrue(config.adBlock.rules.isEmpty())
        assertTrue(config.adBlock.subscriptions.isEmpty())
    }

    @Test
    fun buildAdBlockBlockHonorsDisabledEvenIfRulesExist() {
        val app = WebApp(
            name = "AdBlock",
            url = "https://example.com",
            adBlockEnabled = false,
            adBlockRules = listOf("||ads.example.com^"),
            adBlockSubscriptions = listOf("https://example.com/list.txt")
        )
        val config = app.toApkConfig("com.example.adblock")
        assertFalse(config.adBlockEnabled)
        assertEquals(listOf("||ads.example.com^"), config.adBlockRules)
        assertEquals(listOf("https://example.com/list.txt"), config.adBlockSubscriptions)
    }
}
