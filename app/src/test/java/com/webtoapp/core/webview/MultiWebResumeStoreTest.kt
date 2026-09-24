package com.webtoapp.core.webview

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MultiWebResumeStoreTest {

    private fun newStore() = MultiWebResumeStore(ApplicationProvider.getApplicationContext())

    @Test
    fun `persisted site id round trips`() {
        val store = newStore()
        store.persistSelectedSiteId("app", "site_deepseek")
        assertThat(store.resumeSiteId("app")).isEqualTo("site_deepseek")
    }

    @Test
    fun `null clears the record so card mode reopens on the grid`() {
        val store = newStore()
        store.persistSelectedSiteId("app", "site_deepseek")
        store.persistSelectedSiteId("app", null)
        assertThat(store.resumeSiteId("app")).isNull()
    }

    @Test
    fun `blank id clears the record`() {
        val store = newStore()
        store.persistSelectedSiteId("app", "site_a")
        store.persistSelectedSiteId("app", "")
        assertThat(store.resumeSiteId("app")).isNull()
    }

    @Test
    fun `keys isolate records between apps`() {
        val store = newStore()
        store.persistSelectedSiteId("appOne", "site_a")
        store.persistSelectedSiteId("appTwo", "site_b")
        assertThat(store.resumeSiteId("appOne")).isEqualTo("site_a")
        assertThat(store.resumeSiteId("appTwo")).isEqualTo("site_b")
        assertThat(store.resumeSiteId("appThree")).isNull()
    }

    @Test
    fun `latest write wins`() {
        val store = newStore()
        store.persistSelectedSiteId("app", "site_first")
        store.persistSelectedSiteId("app", "site_second")
        assertThat(store.resumeSiteId("app")).isEqualTo("site_second")
    }
}
