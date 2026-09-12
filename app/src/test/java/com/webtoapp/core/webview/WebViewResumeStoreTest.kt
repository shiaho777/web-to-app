package com.webtoapp.core.webview

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebViewResumeStoreTest {

    private fun newStore() = WebViewResumeStore(ApplicationProvider.getApplicationContext())

    @Test
    fun `session key distinguishes launch targets`() {
        val store = newStore()
        assertThat(store.sessionKey(appId = 7, directUrl = null, previewBaseUrl = null, isTest = false))
            .isEqualTo("app:7")
        assertThat(store.sessionKey(appId = -1, directUrl = "https://a.example", previewBaseUrl = null, isTest = false))
            .isEqualTo("url:https://a.example")
        assertThat(store.sessionKey(appId = -1, directUrl = null, previewBaseUrl = "https://p.example", isTest = false))
            .isEqualTo("preview:https://p.example")
        assertThat(store.sessionKey(appId = 7, directUrl = null, previewBaseUrl = null, isTest = true))
            .isNull()
        assertThat(store.sessionKey(appId = -1, directUrl = null, previewBaseUrl = null, isTest = false))
            .isNull()
    }

    @Test
    fun `persist and resume round trip`() {
        val store = newStore()
        store.persist("app:1", "https://site.example/", "https://site.example/page/2")
        assertThat(store.resumeUrl("app:1", "https://site.example/"))
            .isEqualTo("https://site.example/page/2")
    }

    @Test
    fun `edited base url invalidates the record`() {
        val store = newStore()
        store.persist("app:1", "https://old.example/", "https://old.example/deep")
        assertThat(store.resumeUrl("app:1", "https://new.example/")).isNull()
    }

    @Test
    fun `local runtime urls are never persisted`() {
        val store = newStore()
        for (dead in listOf(
            "http://localhost:8080/index.html",
            "http://127.0.0.1:9000/page",
            "http://10.0.2.2:3000/app",
            "file:///android_asset/site/index.html",
            "about:blank"
        )) {
            store.persist("app:1", "https://site.example/", dead)
            com.google.common.truth.Truth.assertWithMessage("unexpectedly persisted $dead")
                .that(store.resumeUrl("app:1", "https://site.example/")).isNull()
        }
    }

    @Test
    fun `clear drops the record`() {
        val store = newStore()
        store.persist("app:1", "https://site.example/", "https://site.example/page/2")
        store.clear("app:1")
        assertThat(store.resumeUrl("app:1", "https://site.example/")).isNull()
    }

    @Test
    fun `null or blank inputs are ignored`() {
        val store = newStore()
        store.persist(null, "https://site.example/", "https://site.example/x")
        store.persist("app:1", null, "https://site.example/x")
        store.persist("app:1", "https://site.example/", "")
        assertThat(store.resumeUrl("app:1", "https://site.example/")).isNull()
        assertThat(store.resumeUrl(null, "https://site.example/")).isNull()
    }
}
