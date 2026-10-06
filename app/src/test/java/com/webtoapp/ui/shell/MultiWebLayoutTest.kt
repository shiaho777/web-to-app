package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MultiWebLayoutTest {

    @Test
    fun `missing or zero columns keep the historical grid`() {
        assertThat(resolvedGridColumns(0, MULTI_WEB_DEFAULT_CARD_COLUMNS, MULTI_WEB_MAX_CARD_COLUMNS)).isEqualTo(2)
        assertThat(resolvedGridColumns(-1, MULTI_WEB_DEFAULT_DRAWER_COLUMNS, MULTI_WEB_MAX_DRAWER_COLUMNS)).isEqualTo(1)
        assertThat(resolvedGridColumns(3, MULTI_WEB_DEFAULT_CARD_COLUMNS, MULTI_WEB_MAX_CARD_COLUMNS)).isEqualTo(3)
        assertThat(resolvedGridColumns(9, MULTI_WEB_DEFAULT_CARD_COLUMNS, MULTI_WEB_MAX_CARD_COLUMNS)).isEqualTo(4)
        assertThat(resolvedGridColumns(9, MULTI_WEB_DEFAULT_DRAWER_COLUMNS, MULTI_WEB_MAX_DRAWER_COLUMNS)).isEqualTo(3)
    }

    @Test
    fun `missing aspect stays tall and a larger aspect is a shorter card`() {
        assertThat(resolvedCardAspect(0f)).isEqualTo(MULTI_WEB_DEFAULT_CARD_ASPECT)
        assertThat(resolvedCardAspect(Float.NaN)).isEqualTo(MULTI_WEB_DEFAULT_CARD_ASPECT)
        assertThat(resolvedCardAspect(2.4f)).isEqualTo(2.4f)
        assertThat(resolvedCardAspect(8f)).isEqualTo(MULTI_WEB_MAX_CARD_ASPECT)
    }

    @Test
    fun `drawer sheet grows with columns and cards tighten when short or dense`() {
        assertThat(drawerSheetWidthDp(0)).isEqualTo(300)
        assertThat(drawerSheetWidthDp(1)).isEqualTo(300)
        assertThat(drawerSheetWidthDp(2)).isEqualTo(440)
        assertThat(drawerSheetWidthDp(3)).isEqualTo(560)
        assertThat(siteCardMetrics(2, 1.2f).iconDp).isEqualTo(44)
        assertThat(siteCardMetrics(2, 1.2f).paddingDp).isEqualTo(16)
        assertThat(siteCardMetrics(4, 1.2f).iconDp).isEqualTo(28)
        assertThat(siteCardMetrics(2, 2.6f).paddingDp).isEqualTo(10)
    }

    @Test
    fun `start tab resumes the last site unless a chosen site is pinned`() {
        val ids = listOf("a", "b", "c")
        assertThat(resolveMultiWebStartSiteId("LAST", "", ids, "b")).isEqualTo("b")
        assertThat(resolveMultiWebStartSiteId("LAST", "c", ids, null)).isNull()
        assertThat(resolveMultiWebStartSiteId("", "", ids, "gone")).isNull()
        assertThat(resolveMultiWebStartSiteId("SITE", "c", ids, "a")).isEqualTo("c")
        assertThat(resolveMultiWebStartSiteId("SITE", "gone", ids, "a")).isEqualTo("a")
        assertThat(resolveMultiWebStartSiteId("SITE", "", ids, null)).isEqualTo("a")
        assertThat(resolveMultiWebStartSiteId("SITE", "a", emptyList(), "a")).isNull()
    }

    @Test
    fun `moveListItem reorders inside bounds and ignores a bad index`() {
        assertThat(moveListItem(listOf("a", "b", "c"), 0, 2)).containsExactly("b", "c", "a").inOrder()
        assertThat(moveListItem(listOf("a", "b", "c"), 2, 0)).containsExactly("c", "a", "b").inOrder()
        assertThat(moveListItem(listOf("a", "b"), 1, 1)).containsExactly("a", "b").inOrder()
        assertThat(moveListItem(listOf("a"), 0, 3)).containsExactly("a").inOrder()
        assertThat(moveListItem(listOf("a"), -1, 0)).containsExactly("a").inOrder()
    }
}
