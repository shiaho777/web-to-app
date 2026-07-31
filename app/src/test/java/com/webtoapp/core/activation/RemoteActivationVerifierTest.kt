package com.webtoapp.core.activation

import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

/**
 * Guards the dynamic-URL delivery contract (issue #314): when deliverUrl is enabled the delivered
 * URL is part of the signed payload, so a MITM cannot swap it while keeping a valid signature.
 */
@RunWith(RobolectricTestRunner::class)
class RemoteActivationVerifierTest {

    private val verifier = RemoteActivationVerifier(ApplicationProvider.getApplicationContext())

    private fun response(url: String?, sig: String = "", nonce: String = "nonce123") =
        RemoteActivationVerifier.RemoteResponse(
            ok = true,
            expiresAt = 1735862400000L,
            remainingUses = 5,
            message = "",
            nonce = nonce,
            signature = sig,
            url = url
        )

    private fun sign(payload: String, privateKey: PrivateKey): String {
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey)
        sig.update(payload.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(sig.sign(), Base64.DEFAULT)
    }

    @Test
    fun `canonical payload includes url only when deliverUrl enabled`() {
        val resp = response(url = "https://example.com/app")
        val withUrl = verifier.canonicalSignedPayload(resp, includeUrl = true)
        val withoutUrl = verifier.canonicalSignedPayload(resp, includeUrl = false)

        assertThat(withUrl).contains("\"url\":\"https://example.com/app\"")
        assertThat(withoutUrl).doesNotContain("url")
        // url is the fifth key, appended after nonce
        assertThat(withUrl).endsWith("\"nonce\":\"nonce123\",\"url\":\"https://example.com/app\"}")
    }

    @Test
    fun `signature verifies when url matches and fails when tampered`() {
        val kpg = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }
        val keyPair = kpg.generateKeyPair()

        val goodUrl = "https://good.example.com"
        val payload = verifier.canonicalSignedPayload(response(url = goodUrl), includeUrl = true)
        val sig = sign(payload, keyPair.private)

        val goodResponse = response(url = goodUrl, sig = sig)
        assertThat(verifier.verifySignature(keyPair.public, goodResponse, "nonce123", includeUrl = true)).isTrue()

        // Swapping the URL while keeping the signature must fail verification.
        val tampered = response(url = "https://evil.example.com", sig = sig)
        assertThat(verifier.verifySignature(keyPair.public, tampered, "nonce123", includeUrl = true)).isFalse()
    }

    @Test
    fun `legacy payload without url still verifies for servers not delivering a url`() {
        val kpg = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }
        val keyPair = kpg.generateKeyPair()

        val payload = verifier.canonicalSignedPayload(response(url = null), includeUrl = false)
        val sig = sign(payload, keyPair.private)

        val resp = response(url = null, sig = sig)
        assertThat(verifier.verifySignature(keyPair.public, resp, "nonce123", includeUrl = false)).isTrue()
    }
}
