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
    fun `saved site ids round trip and drop blanks`() {
        val store = newStore()
        store.persistSavedSiteIds("app", setOf("site_a", " ", "site_b"))
        assertThat(store.savedSiteIds("app")).containsExactly("site_a", "site_b")
        store.persistSavedSiteIds("app", emptySet())
        assertThat(store.savedSiteIds("app")).isEmpty()
    }

    @Test
    fun `saved ids stay isolated between apps`() {
        val store = newStore()
        store.persistSavedSiteIds("appOne", setOf("site_a"))
        store.persistSavedSiteIds("appTwo", setOf("site_b"))
        assertThat(store.savedSiteIds("appOne")).containsExactly("site_a")
        assertThat(store.savedSiteIds("appTwo")).containsExactly("site_b")
    }

    @Test
    fun `drawer button position survives reopening the store and is scoped to the app and anchor`() {
        val position = MultiWebDrawerButtonPosition(0.25f, 0.75f)
        newStore().persistDrawerButtonPosition("preview/1/topEnd", position)

        val reopened = newStore()
        assertThat(reopened.drawerButtonPosition("preview/1/topEnd")).isEqualTo(position)
        assertThat(reopened.drawerButtonPosition("preview/1/bottomStart")).isNull()
        assertThat(reopened.drawerButtonPosition("preview/2/topEnd")).isNull()
        assertThat(reopened.drawerButtonPosition("shell/topEnd")).isNull()
    }

    @Test
    fun `a drag clamps at the edges and can move back immediately`() {
        val position = MultiWebDrawerButtonPosition(0.5f, 0.5f)
        val edge = position.movedBy(dx = 200f, dy = -400f, availableWidth = 100, availableHeight = 200)
        assertThat(edge).isEqualTo(MultiWebDrawerButtonPosition(1f, 0f))
        assertThat(edge.movedBy(dx = -25f, dy = 50f, availableWidth = 100, availableHeight = 200))
            .isEqualTo(MultiWebDrawerButtonPosition(0.75f, 0.25f))
    }

    @Test
    fun `a collapsed axis retains its position for when the viewport expands`() {
        val position = MultiWebDrawerButtonPosition(0.75f, 0.25f)
        assertThat(position.movedBy(dx = -500f, dy = 25f, availableWidth = 0, availableHeight = 100))
            .isEqualTo(MultiWebDrawerButtonPosition(0.75f, 0.5f))
    }

    @Test
    fun `drawer search matches name host or url`() {
        assertThat(multiWebSiteMatchesQuery("Pixiv", "https://www.pixiv.net/art", "pixiv.net", "")).isTrue()
        assertThat(multiWebSiteMatchesQuery("Pixiv", "https://www.pixiv.net/art", "pixiv.net", "pix")).isTrue()
        assertThat(multiWebSiteMatchesQuery("News", "https://example.com/feed", "example.com", "FEED")).isTrue()
        assertThat(multiWebSiteMatchesQuery("News", "https://example.com/feed", "example.com", "missing")).isFalse()
    }

    @Test
    fun `latest write wins`() {
        val store = newStore()
        store.persistSelectedSiteId("app", "site_first")
        store.persistSelectedSiteId("app", "site_second")
        assertThat(store.resumeSiteId("app")).isEqualTo("site_second")
    }
}
