package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImeDocumentScrollTest {

    @Test
    fun `a field already above the keyboard does not scroll`() {
        val scroll = ImeDocumentScroll()
        val generation = scroll.begin()
        scroll.capture(topCss = 100f, bottomCss = 180f, viewportHeightCss = 800f, webViewHeightPx = 800, generation = generation)

        assertThat(scroll.scrollDelta(webViewHeightPx = 800, overlapPx = 300)).isEqualTo(0)
        assertThat(scroll.scrollDelta(webViewHeightPx = 800, overlapPx = 300)).isEqualTo(0)
    }

    @Test
    fun `a covered field scrolls only the covered amount and then stops`() {
        val scroll = ImeDocumentScroll()
        val generation = scroll.begin()
        scroll.capture(topCss = 620f, bottomCss = 700f, viewportHeightCss = 800f, webViewHeightPx = 800, generation = generation)

        assertThat(scroll.scrollDelta(800, overlapPx = 0)).isEqualTo(0)
        assertThat(scroll.scrollDelta(800, overlapPx = 50)).isEqualTo(0)
        assertThat(scroll.scrollDelta(800, overlapPx = 150)).isEqualTo(50)
        assertThat(scroll.scrollDelta(800, overlapPx = 200)).isEqualTo(50)
        assertThat(scroll.scrollDelta(800, overlapPx = 200)).isEqualTo(0)
    }

    @Test
    fun `a field taller than the visible band pins its top once`() {
        val scroll = ImeDocumentScroll()
        val generation = scroll.begin()
        scroll.capture(topCss = 80f, bottomCss = 900f, viewportHeightCss = 800f, webViewHeightPx = 800, generation = generation)

        assertThat(scroll.scrollDelta(800, overlapPx = 200)).isEqualTo(80)
        assertThat(scroll.scrollDelta(800, overlapPx = 400)).isEqualTo(0)
    }

    @Test
    fun `a snapshot from an older animation is ignored`() {
        val scroll = ImeDocumentScroll()
        val first = scroll.begin()
        val second = scroll.begin()
        scroll.capture(620f, 700f, 800f, 800, first)

        assertThat(scroll.wantsSnapshot).isTrue()
        assertThat(scroll.requestSnapshot()).isEqualTo(second)
        assertThat(scroll.requestSnapshot()).isNull()
        scroll.capture(620f, 700f, 800f, 800, second)
        assertThat(scroll.scrollDelta(800, overlapPx = 200)).isEqualTo(100)
    }

    @Test
    fun `a missed snapshot can be requested again`() {
        val scroll = ImeDocumentScroll()
        scroll.begin()
        val generation = scroll.requestSnapshot()
        scroll.snapshotMissed(generation!!)

        assertThat(scroll.wantsSnapshot).isTrue()
        assertThat(scroll.requestSnapshot()).isEqualTo(generation)
    }

    @Test
    fun `scroll stops being tracked after end`() {
        val scroll = ImeDocumentScroll()
        val generation = scroll.begin()
        scroll.capture(0f, 700f, 800f, 800, generation)
        scroll.end()

        assertThat(scroll.scrollDelta(800, overlapPx = 400)).isEqualTo(0)
        scroll.capture(0f, 700f, 800f, 800, generation)
        assertThat(scroll.scrollDelta(800, overlapPx = 400)).isEqualTo(0)
    }

    @Test
    fun `overlap is the part of the web view the keyboard covers`() {
        assertThat(keyboardOverlapPx(
            webTop = 0, webHeight = 2000, contentTop = 0, contentHeight = 2000, imeBottomPx = 300,
        )).isEqualTo(300)
        assertThat(keyboardOverlapPx(
            webTop = 100, webHeight = 1500, contentTop = 0, contentHeight = 2000, imeBottomPx = 300,
        )).isEqualTo(0)
        assertThat(keyboardOverlapPx(
            webTop = 0, webHeight = 1800, contentTop = 0, contentHeight = 2000, imeBottomPx = 400,
        )).isEqualTo(200)
        assertThat(keyboardOverlapPx(
            webTop = 0, webHeight = 800, contentTop = 0, contentHeight = 800, imeBottomPx = 0,
        )).isEqualTo(0)
    }

    @Test
    fun `snapshot parser accepts the quoted javascript callback`() {
        assertThat(parseFocusedFieldSnapshot("\"12.5,340,800\""))
            .isEqualTo(FocusedFieldSnapshot(12.5f, 340f, 800f))
        assertThat(parseFocusedFieldSnapshot("\"\"")).isNull()
        assertThat(parseFocusedFieldSnapshot("null")).isNull()
        assertThat(parseFocusedFieldSnapshot("340,800")).isNull()
    }
}
