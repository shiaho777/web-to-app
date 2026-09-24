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

    @Test
    fun `external jump marker round trips once`() {
        val store = newStore()
        store.persistExternalJump("app:1", "https://site.example/", "https://oauth.example/bounce")
        assertThat(store.consumeExternalJump("app:1", "https://site.example/"))
            .isEqualTo("https://oauth.example/bounce")
        // Consumed: a second read returns nothing.
        assertThat(store.consumeExternalJump("app:1", "https://site.example/")).isNull()
    }

    @Test
    fun `external jump marker respects the base url`() {
        val store = newStore()
        store.persistExternalJump("app:1", "https://old.example/", "https://oauth.example/bounce")
        assertThat(store.consumeExternalJump("app:1", "https://new.example/")).isNull()
    }

    @Test
    fun `the jump-source page is never recorded as a resume target`() {
        val store = newStore()
        val trampoline = "https://oauth.example/bounce"
        store.persistExternalJump("app:1", "https://site.example/", trampoline)
        // onSaveInstanceState persists the current page after the jump marker was
        // written — the trampoline must not become the resume URL.
        store.persist("app:1", "https://site.example/", trampoline)
        assertThat(store.resumeUrl("app:1", "https://site.example/")).isNull()
    }

    @Test
    fun `a stale last url equal to the jump marker never resumes`() {
        val store = newStore()
        val trampoline = "https://oauth.example/bounce"
        // Marker persisted after the URL was already recorded (ordering flip).
        store.persist("app:1", "https://site.example/", trampoline)
        store.persistExternalJump("app:1", "https://site.example/", trampoline)
        assertThat(store.resumeUrl("app:1", "https://site.example/")).isNull()
    }

    @Test
    fun `pages that did not launch an app still resume after a jump`() {
        val store = newStore()
        store.persist("app:1", "https://site.example/", "https://site.example/login")
        store.persistExternalJump("app:1", "https://site.example/", "https://oauth.example/bounce")
        store.persist("app:1", "https://site.example/", "https://oauth.example/bounce")
        // The trampoline write is skipped; the earlier real page survives.
        assertThat(store.resumeUrl("app:1", "https://site.example/"))
            .isEqualTo("https://site.example/login")
    }

    @Test
    fun `clear drops the jump marker too`() {
        val store = newStore()
        store.persistExternalJump("app:1", "https://site.example/", "https://oauth.example/bounce")
        store.clear("app:1")
        assertThat(store.consumeExternalJump("app:1", "https://site.example/")).isNull()
    }
}
