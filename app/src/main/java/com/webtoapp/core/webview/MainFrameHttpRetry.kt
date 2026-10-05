package com.webtoapp.core.webview

/**
 * One silent reload for a main-frame HTTP 403.
 *
 * Some video hosts reject the first navigation (hotlink / missing Referer / a cookie
 * that the 403 itself just set) and accept the immediate retry. Showing the error page
 * on that first 403 forces the user to tap 重试. A second 403 for the same URL falls
 * through to the normal error page so a real denial is still visible.
 *
 * The set is not cleared when the error document finishes: that document's history URL
 * is the failed URL, and re-arming there would reload forever.
 */
internal class MainFrameHttpRetry(
    private val maxDistinctUrls: Int = 8
) {
    private val retried = LinkedHashSet<String>()

    fun shouldSilentRetry(url: String?, statusCode: Int): Boolean {
        if (statusCode != 403) return false
        if (url.isNullOrBlank() || url == "about:blank") return false
        if (url.startsWith("data:") || url.startsWith("file:")) return false
        if (url in retried) return false
        if (retried.size >= maxDistinctUrls) return false
        retried.add(url)
        return true
    }
}

/**
 * Referer for the silent retry. The previous real page is what a tapped link would
 * have sent. The failed URL itself, about:blank, and non-http schemes are omitted so
 * the retry does not advertise the error document or a file URL.
 */
internal fun mainFrameHttpRetryHeaders(failedUrl: String, referer: String?): Map<String, String> {
    if (referer.isNullOrBlank() || referer == failedUrl || referer == "about:blank") return emptyMap()
    if (!referer.startsWith("http://") && !referer.startsWith("https://")) return emptyMap()
    return mapOf("Referer" to referer)
}
