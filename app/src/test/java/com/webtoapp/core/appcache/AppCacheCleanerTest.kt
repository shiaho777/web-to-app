package com.webtoapp.core.appcache

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.WebApp
import org.junit.Test

class AppCacheCleanerTest {

    @Test
    fun `web app origin keeps scheme host and explicit port`() {
        val app = WebApp(id = 1, name = "a", url = "https://example.com", appType = com.webtoapp.data.model.AppType.WEB)
        assertThat(AppCacheCleaner.originsFor(app)).containsExactly("https://example.com")

        val withPort = app.copy(url = "http://127.0.0.1:8080/index.html")
        assertThat(AppCacheCleaner.originsFor(withPort)).containsExactly("http://127.0.0.1:8080")
    }

    @Test
    fun `scheme-less saved urls fall back to http like the editor normalizer`() {
        val app = WebApp(
            id = 5,
            name = "bare",
            url = "10.0.2.2:8898/media.html",
            appType = com.webtoapp.data.model.AppType.WEB
        )
        assertThat(AppCacheCleaner.originsFor(app)).containsExactly("http://10.0.2.2:8898")
    }

    @Test
    fun `multi web apps clear every site origin, deduplicated`() {
        val app = WebApp(
            id = 2,
            name = "m",
            url = "",
            appType = com.webtoapp.data.model.AppType.MULTI_WEB,
            multiWebConfig = MultiWebConfig(
                sites = listOf(
                    MultiWebSite(id = "1", name = "a", url = "https://a.example.com"),
                    MultiWebSite(id = "2", name = "b", url = "https://b.example.com/x"),
                    MultiWebSite(id = "3", name = "dup", url = "https://a.example.com/other")
                )
            )
        )
        assertThat(AppCacheCleaner.originsFor(app)).containsExactly(
            "https://a.example.com",
            "https://b.example.com"
        ).inOrder()
    }

    @Test
    fun `non-http urls and blanks yield no origins`() {
        val app = WebApp(
            id = 3,
            name = "h",
            url = "file:///data/index.html",
            appType = com.webtoapp.data.model.AppType.HTML
        )
        assertThat(AppCacheCleaner.originsFor(app)).isEmpty()
        assertThat(AppCacheCleaner.originsFor(app.copy(url = ""))).isEmpty()
        assertThat(AppCacheCleaner.originsFor(app.copy(url = "not a url"))).isEmpty()
    }

    @Test
    fun `byte formatting stays compact and locale-stable`() {
        assertThat(AppCacheCleaner.formatBytes(0)).isEqualTo("0 B")
        assertThat(AppCacheCleaner.formatBytes(512)).isEqualTo("512 B")
        assertThat(AppCacheCleaner.formatBytes(2048)).isEqualTo("2.0 KB")
        assertThat(AppCacheCleaner.formatBytes(5 * 1024 * 1024)).isEqualTo("5.0 MB")
        assertThat(AppCacheCleaner.formatBytes((1.5 * 1024 * 1024 * 1024).toLong())).isEqualTo("1.50 GB")
    }
}
