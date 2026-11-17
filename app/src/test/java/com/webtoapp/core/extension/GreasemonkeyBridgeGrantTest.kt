package com.webtoapp.core.extension

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GreasemonkeyBridgeGrantTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `grant none installs no privileged api`() {
        val script = GreasemonkeyBridge.generatePolyfillScript(
            scriptId = "ns_test",
            grants = emptyList(),
            token = "tok"
        )
        assertThat(script).doesNotContain("function GM_xmlhttpRequest")
        assertThat(script).doesNotContain("function GM_getValue")
        assertThat(script).doesNotContain("function GM_setClipboard")
        assertThat(script).doesNotContain("function GM_openInTab")
        assertThat(script).doesNotContain("window.GM_xmlhttpRequest")
        assertThat(script).contains("function GM_addStyle")
        assertThat(script).contains("window.GM_info")
    }

    @Test
    fun `only granted apis are installed`() {
        val script = GreasemonkeyBridge.generatePolyfillScript(
            scriptId = "ns_test",
            grants = listOf("GM_xmlhttpRequest", "GM_getValue", "GM_setValue"),
            token = "tok"
        )
        assertThat(script).contains("function GM_xmlhttpRequest")
        assertThat(script).contains("function GM_getValue")
        assertThat(script).doesNotContain("function GM_setClipboard")
        assertThat(script).doesNotContain("function GM_openInTab")
    }

    @Test
    fun `storage calls require the matching token`() {
        val bridge = GreasemonkeyBridge(context) { null }
        val alias = bridge.storageAliasFor("script-a")
        val token = bridge.registerScript("script-a", listOf("GM_getValue", "GM_setValue"))

        bridge.setValue(alias, token, "k", "v")
        assertThat(bridge.getValue(alias, token, "k", null)).isEqualTo("v")

        bridge.setValue(alias, "forged-token", "k", "evil")
        assertThat(bridge.getValue(alias, token, "k", null)).isEqualTo("v")
        assertThat(bridge.getValue(alias, "forged-token", "k", null)).isNull()
    }

    @Test
    fun `ungranted api is refused even with a valid token`() {
        val bridge = GreasemonkeyBridge(context) { null }
        val alias = bridge.storageAliasFor("script-b")
        val token = bridge.registerScript("script-b", listOf("GM_getValue"))

        bridge.setValue(alias, token, "k", "v")
        assertThat(bridge.getValue(alias, token, "k", null)).isNull()
    }

    @Test
    fun `token is embedded only in the generated polyfill`() {
        val bridge = GreasemonkeyBridge(context) { null }
        val token = bridge.registerScript("script-c", listOf("GM_getValue"))
        val script = GreasemonkeyBridge.generatePolyfillScript(
            scriptId = bridge.storageAliasFor("script-c"),
            grants = listOf("GM_getValue"),
            token = token
        )
        assertThat(script).contains("var _token = '$token'")
        assertThat(token).isNotEmpty()
    }
}
