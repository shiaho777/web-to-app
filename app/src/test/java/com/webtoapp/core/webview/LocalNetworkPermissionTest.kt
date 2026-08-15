package com.webtoapp.core.webview

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocalNetworkPermissionTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `loopback hosts are never private network`() {
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("127.0.0.1")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("127.0.1.5")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("localhost")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("LOCALHOST")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("::1")).isFalse()
    }

    @Test
    fun `private IPv4 ranges are detected`() {
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("192.168.1.188")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("10.0.0.1")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("10.255.255.255")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("172.16.0.1")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("172.31.255.255")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("169.254.10.20")).isTrue()
    }

    @Test
    fun `public IPv4 ranges are not private network`() {
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("8.8.8.8")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("172.15.0.1")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("172.32.0.1")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("192.169.1.1")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("11.0.0.1")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("100.100.100.100")).isFalse()
    }

    @Test
    fun `local names and private IPv6 are private network`() {
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("nas.local")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("printer.LOCAL.")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("fe80::1a2b")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("fd12:3456::1")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("fc00::5")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("2001:db8::1")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkHost("example.com")).isFalse()
    }

    @Test
    fun `urls are classified by their host`() {
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("http://192.168.1.188:3000")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("http://10.1.2.3:8080/path?q=1")).isTrue()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("http://localhost:5000")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("http://127.0.0.1:19500/")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("https://example.com")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl(null)).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("")).isFalse()
        assertThat(LocalNetworkPermission.isPrivateNetworkUrl("not a url")).isFalse()
    }

    @Test
    fun `webview local network errors are recognized`() {
        assertThat(
            LocalNetworkPermission.isLocalNetworkBlockedError("net::ERR_LOCAL_NETWORK_PERMISSION_MISSING")
        ).isTrue()
        assertThat(LocalNetworkPermission.isLocalNetworkBlockedError("ERR_CONNECTION_TIMED_OUT")).isFalse()
        assertThat(LocalNetworkPermission.isLocalNetworkBlockedError(null)).isFalse()
    }

    @Test
    fun `below SDK 36 the permission is implicitly granted and never requested`() {
        // Robolectric runs at SDK 33 here: the permission does not exist on this
        // platform, so access is implicitly granted via INTERNET and no prompt is due.
        assertThat(LocalNetworkPermission.isGranted(context)).isTrue()
        assertThat(LocalNetworkPermission.shouldRequest(context, "http://192.168.1.188:3000")).isFalse()
        assertThat(
            LocalNetworkPermission.shouldRequest(context, "https://example.com", "ERR_LOCAL_NETWORK_PERMISSION_MISSING")
        ).isFalse()
    }
}
