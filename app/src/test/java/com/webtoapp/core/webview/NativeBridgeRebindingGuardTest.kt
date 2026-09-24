package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import java.net.InetAddress
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The CORS-bypass URL gate classifies hostname strings; a public-looking name
 * can resolve to a private address (DNS rebinding). [NativeBridge.isRebindingBlockedAddress]
 * is the resolved-address counterpart — pinned here against every address class
 * a hostile resolver could return.
 */
@RunWith(RobolectricTestRunner::class)
class NativeBridgeRebindingGuardTest {

    private fun blocked(literal: String): Boolean =
        NativeBridge.isRebindingBlockedAddress(InetAddress.getByName(literal))

    @Test
    fun `loopback and any-local addresses are blocked`() {
        assertThat(blocked("127.0.0.1")).isTrue()
        assertThat(blocked("127.53.0.9")).isTrue()
        assertThat(blocked("::1")).isTrue()
        assertThat(blocked("0.0.0.0")).isTrue()
        assertThat(blocked("::")).isTrue()
    }

    @Test
    fun `site-local RFC1918 ranges are blocked`() {
        assertThat(blocked("10.0.0.1")).isTrue()
        assertThat(blocked("10.255.255.254")).isTrue()
        assertThat(blocked("192.168.1.1")).isTrue()
        assertThat(blocked("172.16.0.1")).isTrue()
        assertThat(blocked("172.31.255.254")).isTrue()
    }

    @Test
    fun `link-local and metadata addresses are blocked`() {
        assertThat(blocked("169.254.169.254")).isTrue()
        assertThat(blocked("169.254.0.1")).isTrue()
        assertThat(blocked("fe80::1")).isTrue()
    }

    @Test
    fun `IPv6 unique-local is blocked`() {
        // fc00::/7 ULA — not covered by InetAddress.isSiteLocalAddress (fec0::/10
        // only), which is why the classifier also runs isPrivateNetworkHost.
        assertThat(blocked("fd00::1")).isTrue()
        assertThat(blocked("fc00::1")).isTrue()
        assertThat(blocked("fd12:3456::1")).isTrue()
    }

    @Test
    fun `multicast is blocked`() {
        assertThat(blocked("224.0.0.1")).isTrue()
    }

    @Test
    fun `public addresses pass`() {
        assertThat(blocked("8.8.8.8")).isFalse()
        assertThat(blocked("93.184.216.34")).isFalse()
        assertThat(blocked("1.1.1.1")).isFalse()
        // Just outside the 172.16/12 private range.
        assertThat(blocked("172.15.0.1")).isFalse()
        assertThat(blocked("172.32.0.1")).isFalse()
        // Public IPv6.
        assertThat(blocked("2606:4700:4700::1111")).isFalse()
    }
}
