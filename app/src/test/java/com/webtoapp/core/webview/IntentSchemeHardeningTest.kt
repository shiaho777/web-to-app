package com.webtoapp.core.webview

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pins the `intent://` hardening contract: page-supplied intents lose their
 * explicit component/selector (direct addressing of other apps' exported
 * activities) while keeping data, package targeting, and extras.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class IntentSchemeHardeningTest {

    @Test
    fun `explicit component is stripped, package targeting preserved`() {
        val intent = Intent.parseUri(
            "intent://scan/#Intent;scheme=zxing;component=com.foo/.InternalActivity;package=com.foo;end",
            Intent.URI_INTENT_SCHEME
        )
        assertThat(intent.component).isNotNull()

        WebViewManager.hardenIntentSchemeIntent(intent)

        assertThat(intent.component).isNull()
        assertThat(intent.`package`).isEqualTo("com.foo")
        assertThat(intent.categories).contains(Intent.CATEGORY_BROWSABLE)
    }

    @Test
    fun `selector is stripped`() {
        // SEL. extras are not accepted by this platform's Intent.parseUri, so
        // attach a selector directly — the hardening contract is the same.
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://x/")).apply {
            selector = Intent(Intent.ACTION_VIEW).setType("text/plain")
        }
        assertThat(intent.selector).isNotNull()

        WebViewManager.hardenIntentSchemeIntent(intent)

        assertThat(intent.selector).isNull()
    }

    @Test
    fun `implicit intent keeps data action and extras`() {
        val intent = Intent.parseUri(
            "intent://pay/qr#Intent;scheme=alipay;package=com.eg.android.AlipayGphone;" +
                "S.browser_fallback_url=https%3A%2F%2Fexample.com%2Fdl;end",
            Intent.URI_INTENT_SCHEME
        )

        WebViewManager.hardenIntentSchemeIntent(intent)

        assertThat(intent.component).isNull()
        assertThat(intent.selector).isNull()
        assertThat(intent.dataString).isEqualTo("alipay://pay/qr")
        assertThat(intent.`package`).isEqualTo("com.eg.android.AlipayGphone")
        assertThat(intent.getStringExtra("browser_fallback_url"))
            .isEqualTo("https://example.com/dl")
        assertThat(intent.categories).contains(Intent.CATEGORY_BROWSABLE)
    }
}
