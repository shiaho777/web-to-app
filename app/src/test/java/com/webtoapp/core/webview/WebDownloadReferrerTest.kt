package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WebDownloadReferrerTest {
    @Test
    fun `same origin retains source path and query but strips credentials and fragment`() {
        val source = "https://user:secret@example.com/releases?page=2#android"
        val referrer = WebDownloadReferrer.fromPage("https://example.com:443/app.apk", source)

        assertThat(referrer).isEqualTo("https://example.com/releases?page=2")
        assertThat(WebDownloadReferrer.containsPageDetails(referrer)).isTrue()
    }

    @Test
    fun `cross origin redirects reduce referrer to the origin`() {
        val referrer = WebDownloadReferrer.fromPage(
            "https://cdn.example.com/app.apk",
            "https://example.com/releases?private=1"
        )

        assertThat(referrer).isEqualTo("https://example.com/")
        assertThat(WebDownloadReferrer.containsPageDetails(referrer)).isFalse()
    }

    @Test
    fun `policy and downgrade can suppress the referrer`() {
        val source = "https://example.com/releases"

        assertThat(WebDownloadReferrer.fromPage("http://example.com/app.apk", source)).isNull()
        assertThat(WebDownloadReferrer.fromPage("https://example.com/app.apk", source, "no-referrer")).isNull()
        assertThat(WebDownloadReferrer.fromPage("https://cdn.example.com/app.apk", source, "same-origin")).isNull()
        assertThat(WebDownloadReferrer.fromPage("https://cdn.example.com/app.apk", "file:///index.html")).isNull()
    }
}
