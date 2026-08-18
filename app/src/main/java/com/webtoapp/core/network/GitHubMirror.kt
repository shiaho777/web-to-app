package com.webtoapp.core.network

/**
 * Single source of truth for GitHub release / npm-registry mirror expansion.
 * Every runtime downloader funnels through here so mirror ordering (probed by
 * measured bandwidth via [CnMirrorProbe]) and the direct-URL fallback stay
 * consistent across Node / Python / PHP / WordPress / Go toolchains.
 */
object GitHubMirror {

    val CN_PROXIES = listOf(
        "https://ghfast.top/",
        "https://gh-proxy.com/"
    )

    const val NPM_REGISTRY = "https://registry.npmjs.org"
    const val NPM_MIRROR_REGISTRY = "https://registry.npmmirror.com"

    /**
     * Bandwidth-ordered CN proxy URLs for a GitHub release asset, with the
     * direct URL as the final fallback. Non-CN or non-GitHub URLs come back
     * unchanged so callers can pass any URL through.
     */
    fun proxiedCn(url: String): List<String> {
        if (!url.startsWith("https://github.com/")) return listOf(url)
        return CnMirrorProbe.getOrderedProxies(CN_PROXIES).map { proxy -> proxy + url } + url
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
