package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GalleryPlaybackTest {

    @Test
    fun `advances within list without reshuffle`() {
        assertThat(galleryAutoAdvanceTarget(0, 4, loop = true, shuffleOnLoop = true))
            .isEqualTo(GalleryAdvance(1, reshuffle = false))
    }

    @Test
    fun `wrap reshuffles only when shuffle on loop`() {
        assertThat(galleryAutoAdvanceTarget(3, 4, loop = true, shuffleOnLoop = true))
            .isEqualTo(GalleryAdvance(0, reshuffle = true))
        assertThat(galleryAutoAdvanceTarget(3, 4, loop = true, shuffleOnLoop = false))
            .isEqualTo(GalleryAdvance(0, reshuffle = false))
    }

    @Test
    fun `end without loop stops playback`() {
        assertThat(galleryAutoAdvanceTarget(3, 4, loop = false, shuffleOnLoop = true)).isNull()
    }

    @Test
    fun `empty list stops playback`() {
        assertThat(galleryAutoAdvanceTarget(0, 0, loop = true, shuffleOnLoop = true)).isNull()
    }

    @Test
    fun `overshot index wraps instead of crashing pager`() {
        assertThat(galleryAutoAdvanceTarget(9, 4, loop = true, shuffleOnLoop = false))
            .isEqualTo(GalleryAdvance(0, reshuffle = false))
    }
}
