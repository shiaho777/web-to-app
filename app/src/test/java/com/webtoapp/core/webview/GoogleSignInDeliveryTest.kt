package com.webtoapp.core.webview

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GoogleSignInDeliveryTest {

    private val bridge = NativeBridge(
        context = ApplicationProvider.getApplicationContext<Context>(),
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
    )

    @Test
    fun `a blank url while the picker is open waits instead of dropping`() {
        assertThat(
            bridge.googleSignInDeliveryDecision(
                currentPageUrl = null,
                callerOrigin = "http://127.0.0.1:18248",
                giveUpWaiting = false,
            )
        ).isEqualTo(GoogleSignInDelivery.WAIT)
    }

    @Test
    fun `a local caller still receives the result if the url never returns`() {
        assertThat(
            bridge.googleSignInDeliveryDecision(
                currentPageUrl = "",
                callerOrigin = "http://127.0.0.1:18248",
                giveUpWaiting = true,
            )
        ).isEqualTo(GoogleSignInDelivery.DELIVER)
    }

    @Test
    fun `a foreign caller is dropped when the page url stays blank`() {
        assertThat(
            bridge.googleSignInDeliveryDecision(
                currentPageUrl = null,
                callerOrigin = "https://evil.test",
                giveUpWaiting = true,
            )
        ).isEqualTo(GoogleSignInDelivery.DROP)
    }

    @Test
    fun `the loopback page and a different site are classified`() {
        assertThat(
            bridge.googleSignInDeliveryDecision(
                "http://127.0.0.1:18248/index.html",
                "http://127.0.0.1:18248",
                giveUpWaiting = false,
            )
        ).isEqualTo(GoogleSignInDelivery.DELIVER)
        assertThat(
            bridge.googleSignInDeliveryDecision(
                "https://other.test/home",
                "https://app.test",
                giveUpWaiting = false,
            )
        ).isEqualTo(GoogleSignInDelivery.DROP)
    }

    @Test
    fun `a paused webview holds a local result instead of delivering into the paused page`() {
        assertThat(
            bridge.googleSignInDispatchAction(
                currentPageUrl = "http://127.0.0.1:18123/index.html",
                callerOrigin = "http://127.0.0.1:18123",
                giveUpWaiting = true,
                webViewPaused = true,
            )
        ).isEqualTo(GoogleSignInDelivery.WAIT)
        assertThat(
            bridge.googleSignInDispatchAction(
                currentPageUrl = "http://127.0.0.1:18123/index.html",
                callerOrigin = "http://127.0.0.1:18123",
                giveUpWaiting = true,
                webViewPaused = false,
            )
        ).isEqualTo(GoogleSignInDelivery.DELIVER)
    }

    @Test
    fun `a paused webview still drops a foreign page`() {
        assertThat(
            bridge.googleSignInDispatchAction(
                currentPageUrl = "https://other.test/home",
                callerOrigin = "https://app.test",
                giveUpWaiting = true,
                webViewPaused = true,
            )
        ).isEqualTo(GoogleSignInDelivery.DROP)
    }
}
