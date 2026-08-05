package com.webtoapp.core.webview

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PageZoomStoreTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val pkgA = "com.example.appA"
    private val pkgB = "com.example.appB"

    @Before
    fun clearAll() {
        // Start from a clean store so tests are order-independent.
        PageZoomStore.clearZoom(ctx, pkgA)
        PageZoomStore.clearZoom(ctx, pkgB)
    }

    @Test
    fun `unset zoom returns zero override`() {
        assertThat(PageZoomStore.getZoomPercent(ctx, pkgA)).isEqualTo(0)
    }

    @Test
    fun `stored zoom is read back`() {
        PageZoomStore.setZoomPercent(ctx, pkgA, 125)
        assertThat(PageZoomStore.getZoomPercent(ctx, pkgA)).isEqualTo(125)
    }

    @Test
    fun `zoom is isolated per app`() {
        PageZoomStore.setZoomPercent(ctx, pkgA, 75)
        PageZoomStore.setZoomPercent(ctx, pkgB, 150)
        assertThat(PageZoomStore.getZoomPercent(ctx, pkgA)).isEqualTo(75)
        assertThat(PageZoomStore.getZoomPercent(ctx, pkgB)).isEqualTo(150)
    }

    @Test
    fun `clear removes the override`() {
        PageZoomStore.setZoomPercent(ctx, pkgA, 90)
        PageZoomStore.clearZoom(ctx, pkgA)
        assertThat(PageZoomStore.getZoomPercent(ctx, pkgA)).isEqualTo(0)
    }

    @Test
    fun `storing zero clears the override`() {
        PageZoomStore.setZoomPercent(ctx, pkgA, 110)
        PageZoomStore.setZoomPercent(ctx, pkgA, 0)
        assertThat(PageZoomStore.getZoomPercent(ctx, pkgA)).isEqualTo(0)
    }

    @Test
    fun `blank package name is a no-op`() {
        PageZoomStore.setZoomPercent(ctx, "", 100)
        assertThat(PageZoomStore.getZoomPercent(ctx, "")).isEqualTo(0)
    }
}
