package com.webtoapp.core.webview

internal class OfflineNavigationRetry(
    private val maxDistinctUrls: Int = 8
) {
    private val retried = LinkedHashSet<String>()

    fun shouldSilentRetry(url: String?, errorCode: Int, description: String?): Boolean {
        if (url.isNullOrBlank() || url == "about:blank") return false
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        if (!isOfflineNavigationError(errorCode, description)) return false
        if (url in retried) return false
        if (retried.size >= maxDistinctUrls) return false
        retried.add(url)
        return true
    }

    companion object {
        private val OFFLINE_ERROR_CODES = setOf(-1, -2, -6, -7, -8)

        private val OFFLINE_ERROR_TOKENS = listOf(
            "ERR_INTERNET_DISCONNECTED",
            "ERR_NETWORK_CHANGED",
            "ERR_NAME_NOT_RESOLVED",
            "ERR_ADDRESS_UNREACHABLE",
            "ERR_CONNECTION",
            "ERR_DNS",
            "ERR_TIMED_OUT",
            "ERR_OFFLINE"
        )

        fun isOfflineNavigationError(errorCode: Int, description: String?): Boolean {
            if (errorCode in OFFLINE_ERROR_CODES) return true
            val text = description?.uppercase().orEmpty()
            return OFFLINE_ERROR_TOKENS.any { text.contains(it) }
        }
    }
}
