package com.webtoapp.core.engine

import android.os.Handler
import android.os.Looper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeckoViewEngineSeedTest {

    @After
    fun restoreHandler() {
        org.mozilla.gecko.util.ThreadUtils.sGeckoHandler = null
    }

    @Test
    fun `seed installs main handler when sGeckoHandler is null`() {
        org.mozilla.gecko.util.ThreadUtils.sGeckoHandler = null

        GeckoViewEngine.ensureGeckoHandlerSeeded()

        val handler = org.mozilla.gecko.util.ThreadUtils.sGeckoHandler
        assertNotNull(handler)
        assertEquals(Looper.getMainLooper(), handler!!.looper)
    }

    @Test
    fun `seed keeps an already-assigned handler untouched`() {
        val preset = Handler(Looper.getMainLooper())
        org.mozilla.gecko.util.ThreadUtils.sGeckoHandler = preset

        GeckoViewEngine.ensureGeckoHandlerSeeded()

        assertSame(preset, org.mozilla.gecko.util.ThreadUtils.sGeckoHandler)
    }

    @Test
    fun `seed is idempotent and postable before native assignment`() {
        org.mozilla.gecko.util.ThreadUtils.sGeckoHandler = null

        GeckoViewEngine.ensureGeckoHandlerSeeded()
        val first = org.mozilla.gecko.util.ThreadUtils.sGeckoHandler
        GeckoViewEngine.ensureGeckoHandlerSeeded()

        assertSame(first, org.mozilla.gecko.util.ThreadUtils.sGeckoHandler)

        // The ON_RESUME dispatch does exactly this before native boots; must not throw.
        org.mozilla.gecko.util.ThreadUtils.sGeckoHandler!!.post {}
    }
}
