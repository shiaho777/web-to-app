package com.webtoapp.core.dns

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.net.InetAddress

class DnsAddressSelectionTest {

    private val v4a = InetAddress.getByAddress(byteArrayOf(1, 2, 3, 4))
    private val v4b = InetAddress.getByAddress(byteArrayOf(5, 6, 7, 8))
    private val v6 = InetAddress.getByAddress(byteArrayOf(0x20, 0x01, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))
    private val linkLocal = InetAddress.getByAddress(byteArrayOf(0xfe.toByte(), 0x80.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))
    private val ula = InetAddress.getByAddress(byteArrayOf(0xfd.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))
    private val v4LinkLocal = InetAddress.getByAddress(byteArrayOf(0xa9.toByte(), 0xfe.toByte(), 1, 1))

    @Test
    fun `ipv4 only network keeps ipv4 answers in order`() {
        val selected = selectDohConnectAddresses(
            listOf(v6, v4b, v4a),
            ReachableFamilies(ipv4 = true, ipv6 = false)
        )
        assertThat(selected).containsExactly(v4b, v4a).inOrder()
    }

    @Test
    fun `ipv6 only network drops ipv4 answers`() {
        val selected = selectDohConnectAddresses(
            listOf(v4a, v6),
            ReachableFamilies(ipv4 = false, ipv6 = true)
        )
        assertThat(selected).containsExactly(v6)
    }

    @Test
    fun `a family filter can leave nothing, and nothing is final`() {
        // The bridge reads an empty selection as "this resolver returned nothing
        // routable" and fails the connection. It must never be mistaken for a
        // lookup failure that falls back to system DNS.
        assertThat(
            selectDohConnectAddresses(listOf(v6), ReachableFamilies(ipv4 = true, ipv6 = false))
        ).isEmpty()
        assertThat(
            selectDohConnectAddresses(emptyList(), ReachableFamilies(ipv4 = true, ipv6 = true))
        ).isEmpty()
    }

    @Test
    fun `dual stack keeps the resolver order`() {
        val selected = selectDohConnectAddresses(
            listOf(v6, v4a),
            ReachableFamilies(ipv4 = true, ipv6 = true)
        )
        assertThat(selected).containsExactly(v6, v4a).inOrder()
    }

    @Test
    fun `unknown link does not drop a family`() {
        val selected = selectDohConnectAddresses(listOf(v6, v4a), ReachableFamilies.UNKNOWN)
        assertThat(selected).containsExactly(v6, v4a).inOrder()
    }

    @Test
    fun `link local and ula ipv6 do not count as a route`() {
        val families = familiesFromLinkAddresses(listOf(v4a, linkLocal, ula))
        assertThat(families).isEqualTo(ReachableFamilies(ipv4 = true, ipv6 = false))
        assertThat(familiesFromLinkAddresses(listOf(v6))).isEqualTo(ReachableFamilies(ipv4 = false, ipv6 = true))
        assertThat(familiesFromLinkAddresses(emptyList()).known).isFalse()
    }

    @Test
    fun `ipv4 link local does not count as a route`() {
        assertThat(familiesFromLinkAddresses(listOf(v4LinkLocal, linkLocal)).known).isFalse()
    }

    @Test
    fun `unreachable errors are recognized through the cause chain`() {
        val wrapped = java.io.IOException("connect failed", java.net.SocketException("ENETUNREACH (Network is unreachable)"))
        assertThat(isNetworkUnreachable(wrapped)).isTrue()
        assertThat(isNetworkUnreachable(java.net.SocketException("No route to host"))).isTrue()
        assertThat(isNetworkUnreachable(java.net.SocketException("Connection refused"))).isFalse()
    }
}
