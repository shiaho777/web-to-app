package com.webtoapp.core.dns

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * Which IP families the active network can actually route.
 * Both false means the link addresses could not be read; callers then keep
 * the resolver's original order instead of guessing.
 */
internal data class ReachableFamilies(
    val ipv4: Boolean,
    val ipv6: Boolean
) {
    val known: Boolean get() = ipv4 || ipv6

    companion object {
        val UNKNOWN = ReachableFamilies(ipv4 = false, ipv6 = false)
    }
}

/** True for an IPv4 address the network can route. Loopback and 169.254 do not. */
internal fun isRoutableIpv4(address: Inet4Address): Boolean {
    return !address.isLoopbackAddress && !address.isLinkLocalAddress && !address.isAnyLocalAddress
}

/** True for a global IPv6 address. Link-local and ULA do not mean the APN has IPv6. */
internal fun isGlobalUnicastIpv6(address: Inet6Address): Boolean {
    if (address.isLoopbackAddress || address.isLinkLocalAddress || address.isAnyLocalAddress) return false
    val first = address.address[0].toInt() and 0xff
    if ((first and 0xfe) == 0xfc) return false
    if (first == 0xff) return false
    return true
}

internal fun familiesFromLinkAddresses(addresses: List<InetAddress>): ReachableFamilies {
    var ipv4 = false
    var ipv6 = false
    for (address in addresses) {
        when (address) {
            is Inet4Address -> if (isRoutableIpv4(address)) ipv4 = true
            is Inet6Address -> if (isGlobalUnicastIpv6(address)) ipv6 = true
        }
    }
    return ReachableFamilies(ipv4, ipv6)
}

/**
 * Addresses the bridge should try, in resolver order, limited to families the
 * network can route. An IPv4-only APN drops AAAA results.
 *
 * An empty result is a real answer, not a lookup failure: the resolver replied
 * and every address it returned is unroutable here, or it returned nothing at
 * all (an ad/tracker block, or an NXDOMAIN-style empty answer). The caller must
 * fail the connection rather than fall back to system DNS — falling back would
 * reach the host through a resolver the user did not configure, which is exactly
 * what a configured DoH endpoint is there to prevent.
 */
internal fun selectDohConnectAddresses(
    resolved: List<InetAddress>,
    families: ReachableFamilies
): List<InetAddress> {
    if (!families.known) return resolved
    return resolved.filter { address ->
        when (address) {
            is Inet4Address -> families.ipv4
            is Inet6Address -> families.ipv6
            else -> false
        }
    }
}

/**
 * Connect failures that mean "this family has no route", not "the host refused".
 *
 * Matched on the message text, walking the cause chain, because the errno is not
 * reliably reachable: `Socket.connect` reports these as `SocketException` or
 * `ConnectException`, and only some of them are `ErrnoException` with a usable
 * `errno`. `ENETUNREACH` / `EHOSTUNREACH` carry those tokens in their message on
 * Android, so the text is the portable check. Anything unrecognised returns
 * false, which keeps the retry loop fail-safe: an unknown error is rethrown
 * instead of being swallowed as "try the next address".
 */
internal fun isNetworkUnreachable(error: Throwable): Boolean {
    var current: Throwable? = error
    while (current != null) {
        val message = current.message?.lowercase().orEmpty()
        if (
            message.contains("enetunreach") ||
            message.contains("ehostunreach") ||
            message.contains("network is unreachable") ||
            message.contains("no route to host")
        ) {
            return true
        }
        current = current.cause
    }
    return false
}
