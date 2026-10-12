package com.webtoapp.core.engine

import android.os.Looper
import android.os.SystemClock
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.geckoview.GeckoResult
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * GeckoView delivers [org.mozilla.geckoview.WebExtension.MessageDelegate.onMessage]
 * on the UI thread. The CORS bridge's OkHttp call has to leave that thread
 * before it touches a socket, or Android throws NetworkOnMainThreadException
 * and the page's fetch fails (#1283).
 */
@RunWith(RobolectricTestRunner::class)
class GeckoNativeBridgeDispatchTest {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Before
    fun seedGeckoHandler() {
        // GeckoResult captures the UI handler when it is created on the main thread.
        GeckoViewEngine.ensureGeckoHandlerSeeded()
    }

    @After
    fun cancelScope() {
        scope.cancel()
        org.mozilla.gecko.util.ThreadUtils.sGeckoHandler = null
    }

    @Test
    fun `native http returns while the request is still running and stays off the main thread`() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val worker = AtomicReference<Thread>()
        val result = try {
            GeckoViewEngine.dispatchGeckoNativeHttp(scope) {
                worker.set(Thread.currentThread())
                entered.countDown()
                release.await(5, TimeUnit.SECONDS)
                """{"ok":true}"""
            }
        } finally {
            // Released below, after the assertions that must observe the parked request.
        }
        try {
            assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue()
            assertThat(worker.get()).isNotEqualTo(Looper.getMainLooper().thread)
            assertThat(worker.get()).isNotEqualTo(Thread.currentThread())
        } finally {
            release.countDown()
        }
        assertThat(awaitResult(result)).isEqualTo("""{"ok":true}""")
    }

    @Test
    fun `native http reports a thrown request as a failed bridge payload`() {
        val result = GeckoViewEngine.dispatchGeckoNativeHttp(scope) {
            throw IllegalStateException("boom")
        }
        val payload = JSONObject(awaitResult(result) as String)
        assertThat(payload.getBoolean("ok")).isFalse()
        assertThat(payload.getString("error")).isEqualTo("REQUEST_FAILED")
        assertThat(payload.getString("message")).contains("boom")
    }

    private fun awaitResult(result: GeckoResult<Any>): Any? {
        val latch = CountDownLatch(1)
        val value = AtomicReference<Any>()
        val error = AtomicReference<Throwable>()
        result.accept(
            { delivered ->
                value.set(delivered)
                latch.countDown()
            },
            { thrown ->
                error.set(thrown)
                latch.countDown()
            }
        )
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (!latch.await(20, TimeUnit.MILLISECONDS)) {
            shadowOf(Looper.getMainLooper()).idle()
            if (SystemClock.uptimeMillis() > deadline) {
                error("timed out waiting for the native bridge result")
            }
        }
        error.get()?.let { throw it }
        return value.get()
    }
}
