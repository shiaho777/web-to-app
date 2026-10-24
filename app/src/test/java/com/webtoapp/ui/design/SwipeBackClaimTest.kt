package com.webtoapp.ui.design

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SwipeBackClaimTest {

    @Test
    fun `a press away from the left edge is left for the control under it`() {
        assertThat(swipeBackClaimsDown(downX = 200f, edgeWidthPx = 48f, dismissed = false)).isFalse()
        assertThat(swipeBackClaimsDown(downX = 49f, edgeWidthPx = 48f, dismissed = false)).isFalse()
    }

    @Test
    fun `a press on the left edge is a swipe-back and a committed dismiss is not claimed again`() {
        assertThat(swipeBackClaimsDown(downX = 10f, edgeWidthPx = 48f, dismissed = false)).isTrue()
        assertThat(swipeBackClaimsDown(downX = 48f, edgeWidthPx = 48f, dismissed = false)).isTrue()
        assertThat(swipeBackClaimsDown(downX = 10f, edgeWidthPx = 48f, dismissed = true)).isFalse()
    }
}
