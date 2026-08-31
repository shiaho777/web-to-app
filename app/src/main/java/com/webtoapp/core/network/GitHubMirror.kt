package com.webtoapp.core.network

/**
 * Single source of truth for GitHub release / npm-registry mirror expansion.
 * Every runtime downloader funnels through here so mirror ordering (probed by
 * measured latency via [CnMirrorProbe]) and the direct-URL fallback stay
 * consistent across Node / Python / PHP / WordPress / Go toolchains.
 */
object GitHubMirror {

    /**
     * One route to a GitHub URL. [prefix] is prepended to the full URL
     * (prefix-style accelerators take the whole URL as a path); an empty
     * prefix means a direct hit, which resolves through [GitHubHostsDns] and
     * therefore tries a pinned IP before the system resolver.
     */
    data class MirrorChannel(val id: String, val prefix: String) {
        fun rewrite(url: String): String = prefix + url

        companion object {
            val DIRECT = MirrorChannel("direct", "")
        }
    }

    /**
     * Candidate prefix accelerators. This is deliberately a wide net: the list
     * is re-measured at runtime by [CnMirrorProbe] and anything slow or dead is
     * dropped before the first download attempt, so a stale entry costs one
     * probe request and nothing else.
     */
    val CN_PROXIES: List<MirrorChannel> = listOf(
        MirrorChannel("ghfast", "https://ghfast.top/"),
        MirrorChannel("gh-proxy", "https://gh-proxy.com/"),
        MirrorChannel("ghproxy.net", "https://ghproxy.net/"),
        MirrorChannel("gh.llkk.cc", "https://gh.llkk.cc/"),
        MirrorChannel("ghps.cc", "https://ghps.cc/"),
        MirrorChannel("gh-proxy.ygxz.in", "https://gh-proxy.ygxz.in/"),
        MirrorChannel("gh.jiasu.in", "https://gh.jiasu.in/"),
        MirrorChannel("gh.zwy.one", "https://gh.zwy.one/"),
        MirrorChannel("gh.idayer.com", "https://gh.idayer.com/"),
        MirrorChannel("ghproxy.1888866.xyz", "https://ghproxy.1888866.xyz/"),
        MirrorChannel("gh.con.sh", "https://gh.con.sh/"),
        MirrorChannel("mirror.ghproxy.com", "https://mirror.ghproxy.com/"),
        MirrorChannel("github.91chi.fun", "https://github.91chi.fun/")
    )

    /** Everything a download may try, direct route last in declaration order. */
    val ALL_CHANNELS: List<MirrorChannel> = CN_PROXIES + MirrorChannel.DIRECT

    const val NPM_REGISTRY = "https://registry.npmjs.org"
    const val NPM_MIRROR_REGISTRY = "https://registry.npmmirror.com"

    /**
     * Latency-ordered candidate URLs for a GitHub release asset, with the
     * plain URL as the final fallback. Non-CN or non-GitHub URLs come back
     * unchanged so callers can pass any URL through.
     *
     * The first call of a session blocks for one bounded probe round
     * ([CnMirrorProbe.ACCEPTABLE_LATENCY_MS] gate, cached for five minutes);
     * later calls read the cached order.
     */
    fun proxiedCn(url: String): List<String> {
        if (!url.startsWith("https://github.com/")) return listOf(url)
        val ordered = CnMirrorProbe.orderedChannels()
        return (ordered.map { it.rewrite(url) } + url).distinct()
    }

    /**
     * Same routing as [proxiedCn], but for the smaller GitHub-hosted JSON
     * fetches that never touch a release asset: `api.github.com` for update
     * checks and `raw.githubusercontent.com` for the module catalogue.
     *
     * Kept separate from [proxiedCn] on purpose. Release downloads are the
     * paths that were measured and shipped; widening that function's host
     * match would quietly change where they fetch from. Anything that is not
     * a GitHub host (jsDelivr, for instance) comes back unchanged.
     */
    fun proxiedCnGitHubHost(url: String): List<String> {
        if (!isGitHubHost(url)) return listOf(url)
        val ordered = CnMirrorProbe.orderedChannels()
        return (ordered.map { it.rewrite(url) } + url).distinct()
    }

    private fun isGitHubHost(url: String): Boolean {
        val host = url
            .substringAfter("https://", "")
            .substringBefore("/")
            .lowercase()
        if (host.isEmpty()) return false
        return host == "github.com" ||
            host == "api.github.com" ||
            host.endsWith(".github.com") ||
            host.endsWith("githubusercontent.com")
    }

    /**
     * npm registry tarball URLs with the npmmirror CN variant first when
     * [cn] is set; anything not hosted on registry.npmjs.org passes through.
     */
    fun npmTarballUrls(url: String, cn: Boolean): List<String> {
        if (!url.startsWith("$NPM_REGISTRY/")) return listOf(url)
        if (!cn) return listOf(url)
        val mirrored = NPM_MIRROR_REGISTRY + url.removePrefix(NPM_REGISTRY)
        return listOf(mirrored, url)
    }
}
