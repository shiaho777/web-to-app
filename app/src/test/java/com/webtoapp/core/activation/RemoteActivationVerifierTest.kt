package com.webtoapp.core.activation

import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

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

    // -- AES-256-GCM helpers for encryption tests --

    private fun randomAesKeyBase64(): String {
        val key = ByteArray(32)
        java.security.SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.DEFAULT)
    }

    private fun encryptUrl(plaintext: String, aesKeyBase64: String): String {
        val keyBytes = Base64.decode(aesKeyBase64, Base64.DEFAULT)
        val iv = ByteArray(12)
        java.security.SecureRandom().nextBytes(iv)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val gcmSpec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        // Wire format: IV[12] || ciphertext || tag[16] (tag is appended by doFinal in GCM mode)
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.DEFAULT)
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

    // -- AES-256-GCM URL encryption tests (Phase 2) --

    @Test
    fun `decryptUrl roundtrip works with valid key`() {
        val aesKey = randomAesKeyBase64()
        val plaintext = "https://example.com/app/dashboard"
        val encrypted = encryptUrl(plaintext, aesKey)

        val decrypted = verifier.decryptUrl(encrypted, aesKey)
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `decryptUrl returns null with blank inputs`() {
        assertThat(verifier.decryptUrl("", randomAesKeyBase64())).isNull()
        assertThat(verifier.decryptUrl("notblank", "")).isNull()
    }

    @Test
    fun `decryptUrl returns null when key length is wrong`() {
        val shortKey = Base64.encodeToString(ByteArray(16), Base64.DEFAULT) // 16 bytes, not 32
        val encrypted = encryptUrl("https://example.com", randomAesKeyBase64())
        assertThat(verifier.decryptUrl(encrypted, shortKey)).isNull()
    }

    @Test
    fun `decryptUrl returns null for corrupted ciphertext`() {
        val aesKey = randomAesKeyBase64()
        val encrypted = encryptUrl("https://example.com", aesKey)
        // Flip a byte in the middle of the ciphertext
        val corruptedBytes = Base64.decode(encrypted, Base64.DEFAULT)
        corruptedBytes[corruptedBytes.size / 2] = (corruptedBytes[corruptedBytes.size / 2].toInt() xor 0xFF).toByte()
        val corrupted = Base64.encodeToString(corruptedBytes, Base64.DEFAULT)
        assertThat(verifier.decryptUrl(corrupted, aesKey)).isNull()
    }

    @Test
    fun `signature verifies with encrypted url and fails on tampered encrypted blob`() {
        val kpg = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }
        val keyPair = kpg.generateKeyPair()
        val aesKey = randomAesKeyBase64()

        val plainUrl = "https://good.example.com/secret"
        val encryptedUrl = encryptUrl(plainUrl, aesKey)

        // Server signs the canonical payload that includes the encrypted blob
        val payload = verifier.canonicalSignedPayload(response(url = encryptedUrl), includeUrl = true)
        val sig = sign(payload, keyPair.private)

        val goodResponse = response(url = encryptedUrl, sig = sig)
        assertThat(verifier.verifySignature(keyPair.public, goodResponse, "nonce123", includeUrl = true)).isTrue()

        // Tampering the encrypted blob should break the signature
        val tamperedEncrypted = encryptUrl("https://evil.example.com", aesKey)
        val tamperedResponse = response(url = tamperedEncrypted, sig = sig)
        assertThat(verifier.verifySignature(keyPair.public, tamperedResponse, "nonce123", includeUrl = true)).isFalse()
    }

    @Test
    fun `legacy plaintext url still works when encryptUrl is not used`() {
        val kpg = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec("secp256r1")) }
        val keyPair = kpg.generateKeyPair()

        val plainUrl = "https://example.com/legacy"
        val payload = verifier.canonicalSignedPayload(response(url = plainUrl), includeUrl = true)
        val sig = sign(payload, keyPair.private)

        val resp = response(url = plainUrl, sig = sig)
        assertThat(verifier.verifySignature(keyPair.public, resp, "nonce123", includeUrl = true)).isTrue()
    }

    @Test
    fun `decodeAesKey rejects wrong length keys`() {
        val goodKey = randomAesKeyBase64()
        assertThat(verifier.decodeAesKey(goodKey)).isNotNull()
        assertThat(verifier.decodeAesKey(goodKey)!!.size).isEqualTo(32)

        val shortKey = Base64.encodeToString(ByteArray(16), Base64.DEFAULT)
        assertThat(verifier.decodeAesKey(shortKey)).isNull()

        val longKey = Base64.encodeToString(ByteArray(64), Base64.DEFAULT)
        assertThat(verifier.decodeAesKey(longKey)).isNull()

        assertThat(verifier.decodeAesKey("")).isNull()
        assertThat(verifier.decodeAesKey("not-valid-base64!!!")).isNull()
    }
}


@RunWith(RobolectricTestRunner::class)
class RemoteStartupCacheProbeTest {

    @Test
    fun `startup probe with empty code accepts a valid cache`() = kotlinx.coroutines.test.runTest {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val verifier = RemoteActivationVerifier(context)
        val request = RemoteActivationVerifier.RemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = "",
            offlinePolicy = com.webtoapp.data.model.RemoteActivationOfflinePolicy.ALLOW_CACHED,
            code = "",
            deviceId = "device",
            packageName = "pkg",
            deliverUrl = false,
            encryptUrl = false,
            aesKeyBase64 = "",
            deviceBound = false
        )
        // seed a cache exactly the way a successful verification does
        context.activationDataStore.edit { prefs ->
            prefs[stringPreferencesKey("remote_code_-1")] = "ABCD-1234"
            prefs[longPreferencesKey("remote_expires_-1")] = 0L
        }

        assertThat(verifier.resolveCachedStartup(-1L, request)).isTrue()
    }
}
