package com.webtoapp.core.webview

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object WebDownloadReferrer {

    fun fromPage(downloadUrl: String, pageUrl: String?, policy: String = ""): String? {
        val target = downloadUrl.toHttpUrlOrNull() ?: return null
        val page = pageUrl?.toHttpUrlOrNull() ?: return null
        val source = page.newBuilder().username("").password("").fragment(null).build()
        val origin = source.newBuilder().encodedPath("/").query(null).build()
        val sameOrigin = source.scheme == target.scheme && source.host == target.host && source.port == target.port
        val downgrade = source.isHttps && !target.isHttps
        return when (policy.lowercase()) {
            "no-referrer" -> null
            "same-origin" -> source.toString().takeIf { sameOrigin }
            "origin" -> origin.toString()
            "strict-origin" -> origin.toString().takeUnless { downgrade }
            "origin-when-cross-origin" -> (if (sameOrigin) source else origin).toString()
            "no-referrer-when-downgrade" -> (if (sameOrigin) source else origin).toString().takeUnless { downgrade }
            "unsafe-url" -> (if (sameOrigin) source else origin).toString()
            else -> (if (sameOrigin) source else origin).toString().takeUnless { downgrade }
        }
    }

    fun containsPageDetails(referrer: String?): Boolean {
        val url = referrer?.toHttpUrlOrNull() ?: return false
        return url.encodedPath != "/" || url.encodedQuery != null
    }
}
