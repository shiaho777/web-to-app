package com.webtoapp.core.extension

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContentExtensionBridgeTokenTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var runtime: ChromeExtensionRuntime
    private lateinit var bridge: ContentExtensionBridge
    private val extId = "abcdefghijklmnop"
    private val token = "secret-token"

    @Before
    fun setUp() {
        ExtensionStorageSync.initialize(context)
        ChromeExtensionContentScriptRegistry.initialize(context)
        runtime = ChromeExtensionRuntime(context, extId, "", "https://example.com/", "{}")
        bridge = ContentExtensionBridge(
            runtimes = mapOf(extId to runtime),
            tokenProvider = { if (it == extId) token else null }
        )
    }

    @After
    fun tearDown() {
        runtime.destroy()
        ExtensionStorageSync.clear(extId, ExtensionStorageSync.Area.LOCAL)
        ExtensionStorageSync.clear(extId, ExtensionStorageSync.Area.SYNC)
    }

    @Test
    fun `own extension token reads and writes storage`() {
        bridge.storageSetForExt(extId, token, "local", "k", "v")
        assertThat(bridge.storageGetForExt(extId, token, "local", "k")).isEqualTo("v")
    }

    @Test
    fun `forged token cannot read or write storage`() {
        bridge.storageSetForExt(extId, token, "local", "k", "v")
        bridge.storageSetForExt(extId, "forged", "local", "k", "evil")
        assertThat(bridge.storageGetForExt(extId, token, "local", "k")).isEqualTo("v")
        assertThat(bridge.storageGetForExt(extId, "forged", "local", "k")).isEmpty()
    }

    @Test
    fun `foreign page cannot register persistent content scripts`() {
        val scripts = """[{"id":"s1","matches":["<all_urls>"],"js":["evil.js"],"persistAcrossSessions":true}]"""
        bridge.registerContentScripts(extId, "forged", scripts)
        assertThat(bridge.getRegisteredContentScripts(extId, token, "{}")).isEqualTo("[]")
    }

    @Test
    fun `own extension token registers content scripts`() {
        val scripts = """[{"id":"s1","matches":["https://example.com/*"],"js":["ok.js"]}]"""
        bridge.registerContentScripts(extId, token, scripts)
        assertThat(bridge.getRegisteredContentScripts(extId, token, "{}")).contains("s1")
        bridge.unregisterContentScripts(extId, token, """{"ids":["s1"]}""")
    }

    @Test
    fun `unknown extension is refused even with a token`() {
        assertThat(bridge.storageGetForExt("unknownext", token, "local", "k")).isEmpty()
        bridge.registerContentScripts("unknownext", token, "[]")
        assertThat(bridge.getRegisteredContentScripts("unknownext", token, "{}")).isEqualTo("[]")
    }

    @Test
    fun `dnr session rules require the token`() {
        bridge.updateDnrSessionRules(extId, "forged", "[]", "[]")
        assertThat(bridge.getDnrSessionRules(extId, "forged")).isEqualTo("[]")
    }
}
