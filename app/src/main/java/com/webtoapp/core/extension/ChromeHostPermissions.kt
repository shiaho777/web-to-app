package com.webtoapp.core.extension

/**
 * Host-permission gate for the MV3 polyfill bridge (WtaExtBridge / ContentExtensionBridge).
 *
 * The bridge objects are added to the main WebView via addJavascriptInterface, so every
 * page and iframe in the app can reach them — not just pages the extension declared in
 * its manifest. Without this gate, `getCookies(url)` and the credentialed `nativeFetch`
 * (it attaches and stores the WebView's cookies) let arbitrary embedded content read
 * cookies for any origin, far beyond what the user agreed to when installing the
 * extension. Every bridge method that acts on a URL must verify the target against the
 * host permissions of the installed extension(s).
 */
object ChromeHostPermissions {

    /**
     * Extract the URL match patterns an extension declared: `host_permissions` entries
     * plus the URL-pattern entries found in `permissions` (MV2 style manifests also put
     * hosts there). API permission strings without a scheme are ignored.
     */
    fun declaredPatterns(manifestJson: String): List<String> = runCatching {
        val manifest = org.json.JSONObject(manifestJson)
        val out = mutableListOf<String>()
        listOf("host_permissions", "permissions", "optional_permissions").forEach { key ->
            manifest.optJSONArray(key)?.let { arr ->
                for (i in 0 until arr.length()) {
                    val entry = arr.optString(i) ?: continue
                    if (entry == "<all_urls>" || entry.contains("://")) out.add(entry)
                }
            }
        }
        out
    }.getOrDefault(emptyList())

    /**
     * Chrome match pattern matching: `<all_urls>`, `scheme://host/path` where scheme may
     * be `*`, host may be `*` or `*.example.com`, and the path may contain `*`. Parsing is
     * deliberately manual: `java.net.URI` rejects `*` schemes and Android's Uri is not
     * available under plain-JVM unit tests.
     */
    fun matches(pattern: String, url: String): Boolean {
        if (pattern == "<all_urls>") return true

        val pParts = splitPattern(pattern) ?: return false
        val uParts = splitPattern(url) ?: return false

        if (pParts.scheme != "*" && pParts.scheme != uParts.scheme) return false
        if (!hostMatches(pParts.host, uParts.host)) return false
        return pathMatches(pParts.path, uParts.path)
    }

    private data class UrlParts(val scheme: String, val host: String, val port: String, val path: String)

    private fun splitPattern(raw: String): UrlParts? {
        val sep = raw.indexOf("://")
        if (sep <= 0) return null
        val scheme = raw.substring(0, sep).lowercase()
        val rest = raw.substring(sep + 3)
        if (rest.isEmpty()) return null

        val pathStart = rest.indexOf('/')
        val authority = if (pathStart >= 0) rest.substring(0, pathStart) else rest
        val path = if (pathStart >= 0) rest.substring(pathStart) else "/"

        // authority = host[:port] (userinfo@ not valid in match patterns; brackets for IPv6)
        val host: String
        val port: String
        if (authority.startsWith("[")) {
            val close = authority.indexOf(']')
            if (close < 0) return null
            host = authority.substring(0, close + 1)
            port = authority.substring(close + 1).removePrefix(":")
        } else {
            val colon = authority.lastIndexOf(':')
            if (colon > authority.indexOf(']') || colon == -1) {
                host = authority
                port = ""
            } else {
                host = authority.substring(0, colon)
                port = authority.substring(colon + 1)
            }
        }
        if (host.isEmpty()) return null
        return UrlParts(scheme, host.lowercase(), port, path)
    }

    private fun hostMatches(patternHost: String, host: String): Boolean {
        if (patternHost == "*" || patternHost == host) return true
        if (patternHost.startsWith("*.")) {
            val suffix = patternHost.substring(1) // ".example.com"
            if (host.endsWith(suffix)) return true
            // `*.example.com` also matches `example.com` itself in Chrome.
            return host == suffix.removePrefix(".")
        }
        return false
    }

    private fun pathMatches(patternPath: String, path: String): Boolean {
        if (patternPath == "/*" || patternPath == "*" || patternPath == path) return true
        if (!patternPath.contains('*')) return false
        val regex = buildString {
            append('^')
            patternPath.forEach { ch ->
                when (ch) {
                    '*' -> append(".*")
                    else -> {
                        if ("\\.[]{}()+?^$|".contains(ch)) append('\\')
                        append(ch)
                    }
                }
            }
            append('$')
        }
        return runCatching { Regex(regex).matches(path) }.getOrDefault(false)
    }

    /** True iff [url] is allowed by at least one extension's declared host patterns. */
    fun anyRuntimeAllows(runtimes: Map<String, ChromeExtensionRuntime>, url: String): Boolean {
        return runtimes.values.any { runtime -> runtime.declaredHostPatterns().any { matches(it, url) } }
    }
}
