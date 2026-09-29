package com.webtoapp.core.plugin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `hcj.share` / `hcj.openExternal` / `hcj.clearData` / `hcj.exitApp` are
 * capability-gated host actions: the token must belong to the calling plugin
 * and the plugin must hold the matching [PluginPermission]. These tests pin
 * that contract so a page script can never reach the actions anonymously.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PluginBridgeHostActionsTest {

    private lateinit var context: Context
    private lateinit var bridge: PluginBridge
    private lateinit var events: MutableList<String>
    private var plugin: Plugin = Plugin(
        id = "p1",
        name = "P1",
        packageDir = "p1",
        permissions = listOf(
            PluginPermission.SHARE,
            PluginPermission.OPEN_EXTERNAL,
            PluginPermission.CLEAR_DATA,
            PluginPermission.EXIT_APP
        )
    )
    private var lockedPlugin: Plugin = Plugin(id = "p2", name = "P2", packageDir = "p2")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        events = mutableListOf()
        bridge = PluginBridge(
            configStore = PluginConfigStore(context),
            host = object : PluginBridge.Host {
                override fun evaluatePageJs(js: String) {}
                override fun pluginFor(pluginId: String): Plugin? =
                    when (pluginId) {
                        "p1" -> plugin
                        "p2" -> lockedPlugin
                        else -> null
                    }
                override fun onBadge(pluginId: String, text: String, color: String) {}
                override fun onPanelOpen(pluginId: String) {}
                override fun onPanelClose(pluginId: String) {}
                override fun onPanelMessage(pluginId: String, json: String) {}
                override fun onNotify(pluginId: String, title: String, body: String) {}
                override fun onShare(pluginId: String, title: String, text: String, url: String) {
                    events.add("share:$title:$url")
                }
                override fun onOpenExternal(pluginId: String, url: String) {
                    events.add("open:$url")
                }
                override fun onClearData(pluginId: String) {
                    events.add("clear")
                }
                override fun onExitApp(pluginId: String) {
                    events.add("exit")
                }
            }
        )
    }

    @Test
    fun `granted plugin reaches all host actions`() {
        val token = bridge.issueToken("p1")

        bridge.share("p1", token, "t", "", "https://example.com")
        bridge.openExternal("p1", token, "https://example.com")
        bridge.clearData("p1", token)
        bridge.exitApp("p1", token)

        assertThat(events).containsExactly(
            "share:t:https://example.com",
            "open:https://example.com",
            "clear",
            "exit"
        ).inOrder()
    }

    @Test
    fun `missing permission drops the action`() {
        val token = bridge.issueToken("p2")

        bridge.share("p2", token, "t", "", "u")
        bridge.openExternal("p2", token, "u")
        bridge.clearData("p2", token)
        bridge.exitApp("p2", token)

        assertThat(events).isEmpty()
    }

    @Test
    fun `forged token or foreign plugin id is rejected`() {
        val token = bridge.issueToken("p1")

        bridge.share("p2", token, "t", "", "u") // token belongs to p1
        bridge.exitApp("p1", "bogus-token")
        bridge.clearData("ghost", token)

        assertThat(events).isEmpty()
    }
}
