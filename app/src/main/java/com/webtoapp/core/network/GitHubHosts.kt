package com.webtoapp.core.network

import com.webtoapp.core.logging.AppLogger
import okhttp3.Dns
import java.net.InetAddress

/**
 * Pinned GitHub IP mappings (GitHub520 hosts list, snapshot 2026-08-29).
 *
 * On networks where DNS is poisoned or slow, resolving `github.com` and its
 * CDN hosts to a known-good IP short-circuits the failing lookup. The pinned
 * list is only ever tried *first*: [GitHubHostsDns] always appends the system
 * resolver's answer behind it, so when a pinned IP goes stale (GitHub rotates
 * these regularly) OkHttp silently moves on to the real one.
 *
 * Nothing here is a hard dependency — it is a fast path with an automatic
 * fallback, which is what makes shipping static IPs safe.
 */
object GitHubHosts {

    private const val TAG = "GitHubHosts"

    private val ENTRIES: Map<String, List<String>> = mapOf(
        "alive.github.com" to listOf("140.82.114.26"),
        "api.github.com" to listOf("20.205.243.168"),
        "api.individual.githubcopilot.com" to listOf("140.82.113.21"),
        "avatars.githubusercontent.com" to listOf("185.199.110.133"),
        "avatars0.githubusercontent.com" to listOf("185.199.110.133"),
        "avatars1.githubusercontent.com" to listOf("185.199.110.133"),
        "avatars2.githubusercontent.com" to listOf("185.199.110.133"),
        "avatars3.githubusercontent.com" to listOf("185.199.110.133"),
        "avatars4.githubusercontent.com" to listOf("185.199.110.133"),
        "avatars5.githubusercontent.com" to listOf("185.199.110.133"),
        "camo.githubusercontent.com" to listOf("185.199.110.133"),
        "central.github.com" to listOf("140.82.114.21"),
        "cloud.githubusercontent.com" to listOf("185.199.110.133"),
        "codeload.github.com" to listOf("20.205.243.165"),
        "collector.github.com" to listOf("140.82.113.21"),
        "desktop.githubusercontent.com" to listOf("185.199.110.133"),
        "favicons.githubusercontent.com" to listOf("185.199.110.133"),
        "gist.github.com" to listOf("37.61.54.158"),
        "github-cloud.s3.amazonaws.com" to listOf("52.217.141.105"),
        "github-com.s3.amazonaws.com" to listOf("52.217.136.9"),
        "github-production-release-asset-2e65be.s3.amazonaws.com" to listOf("54.231.129.185"),
        "github-production-repository-file-5c1aeb.s3.amazonaws.com" to listOf("52.216.25.68"),
        "github-production-user-asset-6210df.s3.amazonaws.com" to listOf("52.217.37.124"),
        "github.blog" to listOf("192.0.66.2"),
        "github.com" to listOf("20.205.243.166"),
        "github.community" to listOf("140.82.113.17"),
        "github.githubassets.com" to listOf("185.199.110.215"),
        "github.global.ssl.fastly.net" to listOf("108.160.165.189"),
        "github.io" to listOf("185.199.111.153"),
        "github.map.fastly.net" to listOf("185.199.110.133"),
        "githubstatus.com" to listOf("185.199.111.153"),
        "live.github.com" to listOf("140.82.113.26"),
        "media.githubusercontent.com" to listOf("185.199.110.133"),
        "objects.githubusercontent.com" to listOf("185.199.110.133"),
        "pipelines.actions.githubusercontent.com" to listOf("13.107.42.16"),
        "raw.githubusercontent.com" to listOf("185.199.110.133"),
        "user-images.githubusercontent.com" to listOf("185.199.110.133"),
        "private-user-images.githubusercontent.com" to listOf("185.199.110.133"),
        "vscode.dev" to listOf("150.171.110.103"),
        "education.github.com" to listOf("140.82.112.21")
    )

    private val resolved: Map<String, List<InetAddress>> by lazy {
        ENTRIES.mapValues { (_, ips) ->
            ips.mapNotNull { ip -> runCatching { InetAddress.getByName(ip) }.getOrNull() }
        }.filterValues { it.isNotEmpty() }
            .also { AppLogger.i(TAG, "Pinned ${it.size} GitHub hosts across ${it.values.sumOf { v -> v.size }} addresses") }
    }

    /** Pinned addresses for [hostname], empty when it is not in the table. */
    fun lookup(hostname: String): List<InetAddress> = resolved[hostname].orEmpty()

    fun covers(hostname: String): Boolean = resolved.containsKey(hostname)

    val pinnedHosts: Set<String> get() = resolved.keys
}

/**
 * OkHttp [Dns] that tries the pinned IP first and keeps the system answer
 * behind it as a fallback. OkHttp walks the returned list in order, so a stale
 * pinned IP costs one failed connect attempt and nothing more.
 */
object GitHubHostsDns : Dns {

    override fun lookup(hostname: String): List<InetAddress> {
        val pinned = GitHubHosts.lookup(hostname)
        if (pinned.isEmpty()) return Dns.SYSTEM.lookup(hostname)
        val system = runCatching { Dns.SYSTEM.lookup(hostname) }.getOrDefault(emptyList())
        val merged = pinned + system.filter { candidate -> pinned.none { it.hostAddress == candidate.hostAddress } }
        if (merged.size > pinned.size) {
            AppLogger.d(GitHubHostsDnsTag, "$hostname: pinned ${pinned.map { it.hostAddress }} + ${merged.size - pinned.size} system")
        }
        return merged
    }

    private const val GitHubHostsDnsTag = "GitHubHostsDns"
}
