package com.webtoapp.core.appmodifier

import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Round-trip and forgery-resistance tests for [PayloadIntegrity]: the tag that
 * authenticates SplashLauncherActivity's caller-controlled intent extras.
 */
@RunWith(RobolectricTestRunner::class)
class PayloadIntegrityTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `sign then verify round-trips`() {
        val payload = """{"targetPackage":"com.example.app","splashEnabled":true}"""
        val signature = PayloadIntegrity.sign(context, payload)
        assertThat(signature).isNotEmpty()
        assertThat(PayloadIntegrity.verify(context, payload, signature)).isTrue()
    }

    @Test
    fun `tampered payload is rejected`() {
        val signature = PayloadIntegrity.sign(context, """{"a":1}""")
        assertThat(PayloadIntegrity.verify(context, """{"a":2}""", signature)).isFalse()
    }

    @Test
    fun `tampered signature is rejected`() {
        val payload = """{"a":1}"""
        val signature = PayloadIntegrity.sign(context, payload)
        val forged = String(Base64.decode(signature, Base64.DEFAULT)).let {
            Base64.encodeToString(
                ByteArray(Base64.decode(signature, Base64.DEFAULT).size) { i -> (i % 251).toByte() },
                Base64.DEFAULT
            )
        }
        assertThat(PayloadIntegrity.verify(context, payload, forged)).isFalse()
    }

    @Test
    fun `missing or blank signature is rejected`() {
        val payload = """{"a":1}"""
        assertThat(PayloadIntegrity.verify(context, payload, null)).isFalse()
        assertThat(PayloadIntegrity.verify(context, payload, "")).isFalse()
        assertThat(PayloadIntegrity.verify(context, payload, "   ")).isFalse()
    }

    @Test
    fun `missing or empty payload is rejected`() {
        assertThat(PayloadIntegrity.verify(context, null, "c2ln")).isFalse()
        assertThat(PayloadIntegrity.verify(context, "", "c2ln")).isFalse()
    }

    @Test
    fun `malformed base64 signature is rejected`() {
        assertThat(
            PayloadIntegrity.verify(context, """{"a":1}""", "!!!not-base64!!!")
        ).isFalse()
    }

    @Test
    fun `key persists across calls so a second verify still passes`() {
        val payload = """{"b":2}"""
        val signature = PayloadIntegrity.sign(context, payload)
        // A second sign/verify must reuse the persisted key — otherwise pinned
        // shortcuts created before a process restart would be rejected.
        assertThat(PayloadIntegrity.verify(context, payload, signature)).isTrue()
        assertThat(PayloadIntegrity.sign(context, payload)).isEqualTo(signature)
    }
}
