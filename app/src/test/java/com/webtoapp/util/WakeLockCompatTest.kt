package com.webtoapp.util

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WakeLockCompatTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun shadowApp() = shadowOf(ApplicationProvider.getApplicationContext<Application>())

    @Test
    fun `acquire returns a held wake lock when WAKE_LOCK is granted`() {
        shadowApp().grantPermissions(Manifest.permission.WAKE_LOCK)

        val lock = WakeLockCompat.acquire(context, "Test:Held", 60_000L)
        assertThat(lock).isNotNull()
        assertThat(lock!!.isHeld).isTrue()

        WakeLockCompat.release(lock)
        assertThat(lock.isHeld).isFalse()
    }

    @Test
    fun `acquire returns null instead of throwing when WAKE_LOCK is denied`() {
        // Generated APKs may omit WAKE_LOCK (or OEM permission managers revoke
        // it) — a denied acquire must degrade, never crash (#1034).
        shadowApp().denyPermissions(Manifest.permission.WAKE_LOCK)
        try {
            assertThat(WakeLockCompat.acquire(context, "Test:Denied", 60_000L)).isNull()
        } finally {
            shadowApp().grantPermissions(Manifest.permission.WAKE_LOCK)
        }
    }

    @Test
    fun `release tolerates null and unheld locks`() {
        WakeLockCompat.release(null)

        shadowApp().denyPermissions(Manifest.permission.WAKE_LOCK)
        try {
            val denied = WakeLockCompat.acquire(context, "Test:Null", 60_000L)
            WakeLockCompat.release(denied)
        } finally {
            shadowApp().grantPermissions(Manifest.permission.WAKE_LOCK)
        }
    }
}
