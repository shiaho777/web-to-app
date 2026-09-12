package com.webtoapp.core.activation

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

/**
 * Guards the remembered-card relaunch behaviour: an app activated once must be
 * able to reuse the remembered card on the next launch instead of demanding it
 * again, while revoked/expired/exhausted cards still land on the code prompt.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ActivationRelaunchTest {

    private lateinit var manager: ActivationManager
    private lateinit var context: android.content.Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = ActivationManager(context)
        runBlocking { manager.resetActivation(APP_ID) }
    }

    private fun permanent(code: String) =
        ActivationCode(code = code, type = ActivationCodeType.PERMANENT)

    @Test
    fun `relaunch passes silently with the remembered card`() = runTest {
        val codes = listOf(permanent("CARD-AAAA"), permanent("CARD-BBBB"))
        val result = manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)
        assertThat(result).isInstanceOf(ActivationResult.Success::class.java)

        // Second launch ("verify every launch" gate) reuses the stored card.
        assertThat(manager.resolveRelaunchActivation(APP_ID, codes)).isTrue()
    }

    @Test
    fun `relaunch fails when the remembered card was removed from the config`() = runTest {
        val codes = listOf(permanent("CARD-AAAA"))
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)

        // Developer revoked the card: the grant exists but the code is gone.
        assertThat(manager.resolveRelaunchActivation(APP_ID, emptyList())).isFalse()
        assertThat(manager.resolveRelaunchActivation(APP_ID, listOf(permanent("OTHER-999")))).isFalse()
    }

    @Test
    fun `relaunch fails when the grant expired`() = runTest {
        val codes = listOf(permanent("CARD-AAAA"))
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)

        // Force the persisted grant into the past.
        context.activationDataStore.edit { prefs ->
            prefs[longPreferencesKey("expire_time_$APP_ID")] = System.currentTimeMillis() - 1_000
        }

        assertThat(manager.resolveRelaunchActivation(APP_ID, codes)).isFalse()
    }

    @Test
    fun `relaunch consumes usage and stops when exhausted`() = runTest {
        val code = ActivationCode(
            code = "CARD-AAAA",
            type = ActivationCodeType.USAGE_LIMITED,
            usageLimit = 2
        )
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", listOf(code))

        // Each app launch is a fresh process (fresh manager), so each consumes one use.
        fun relaunch() = kotlinx.coroutines.runBlocking {
            ActivationManager(context).resolveRelaunchActivation(APP_ID, listOf(code))
        }
        // usage_count starts at 0, limit 2 → two launches then the card is done.
        assertThat(relaunch()).isTrue()
        assertThat(relaunch()).isTrue()
        assertThat(relaunch()).isFalse()
    }

    @Test
    fun `relaunch fails when nothing was ever activated`() = runTest {
        assertThat(manager.resolveRelaunchActivation(APP_ID, listOf(permanent("CARD-AAAA"))))
            .isFalse()
    }

    @Test
    fun `entering a different listed code switches the card`() = runTest {
        val codes = listOf(permanent("CARD-AAAA"), permanent("CARD-BBBB"))
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)

        val switch = manager.verifyActivationCodeWithObjects(APP_ID, "CARD-BBBB", codes)
        assertThat(switch).isInstanceOf(ActivationResult.Success::class.java)

        // The grant now belongs to CARD-BBBB: relaunch must still pass.
        assertThat(manager.resolveRelaunchActivation(APP_ID, codes)).isTrue()
    }

    @Test
    fun `re-entering the backing code reports AlreadyActivated`() = runTest {
        val codes = listOf(permanent("CARD-AAAA"))
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)

        val again = manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)
        assertThat(again).isEqualTo(ActivationResult.AlreadyActivated)
    }

    @Test
    fun `a lapsed grant does not block activating a new card`() = runTest {
        val codes = listOf(permanent("CARD-AAAA"), permanent("CARD-BBBB"))
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", codes)

        context.activationDataStore.edit { prefs ->
            prefs[longPreferencesKey("expire_time_$APP_ID")] = System.currentTimeMillis() - 1_000
        }

        // Regression: previously ANY new code was rejected with Expired because the
        // stale grant short-circuited validation — the user could never switch cards.
        val result = manager.verifyActivationCodeWithObjects(APP_ID, "CARD-BBBB", codes)
        assertThat(result).isInstanceOf(ActivationResult.Success::class.java)
        assertThat(manager.resolveStartupActivation(APP_ID)).isTrue()
    }

    @Test
    fun `re-entering the same expired card still reports Expired`() = runTest {
        val code = ActivationCode(
            code = "CARD-AAAA",
            type = ActivationCodeType.TIME_LIMITED,
            timeLimitMs = 60_000
        )
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", listOf(code))

        context.activationDataStore.edit { prefs ->
            prefs[longPreferencesKey("expire_time_$APP_ID")] = System.currentTimeMillis() - 1_000
        }

        val result = manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", listOf(code))
        assertThat(result).isEqualTo(ActivationResult.Expired)
    }

    // ---- remote startup gate ----

    private fun remoteRequest(
        policy: RemoteActivationOfflinePolicy
    ): RemoteActivationVerifier.RemoteRequest {
        val kpg = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }
        val publicKey = android.util.Base64.encodeToString(
            kpg.generateKeyPair().public.encoded, android.util.Base64.DEFAULT
        )
        // Unroutable-but-https URL: connection refuses instantly, so verify()
        // lands in the offline-policy branch without depending on real network.
        return RemoteActivationVerifier.RemoteRequest(
            verifyUrl = "https://127.0.0.1:1/verify",
            publicKeyBase64 = publicKey,
            offlinePolicy = policy,
            code = "",
            deviceId = "device",
            packageName = "pkg"
        )
    }

    // saveCache persists the normalized request code, so seeds use that form.
    private suspend fun seedRemoteGrant(code: String = "REMOTE1") {
        context.activationDataStore.edit { prefs ->
            prefs[stringPreferencesKey("remote_code_$APP_ID")] = code
            prefs[longPreferencesKey("remote_expires_$APP_ID")] = 0L
        }
        manager.saveActivationStatus(APP_ID, true)
    }

    @Test
    fun `remote startup passes on the cached result without reverify`() = runTest {
        seedRemoteGrant()
        val request = remoteRequest(RemoteActivationOfflinePolicy.ALLOW_CACHED)

        assertThat(manager.resolveRemoteStartup(APP_ID, request, reverifyEveryLaunch = false))
            .isTrue()
    }

    @Test
    fun `remote startup with DENY re-verifies the remembered code`() = runTest {
        seedRemoteGrant()
        val request = remoteRequest(RemoteActivationOfflinePolicy.DENY)

        // Offline + DENY → the launch is refused, but it was refused by POLICY
        // after an actual re-verification attempt — not by a missing prompt.
        assertThat(manager.resolveRemoteStartup(APP_ID, request, reverifyEveryLaunch = false))
            .isFalse()
    }

    @Test
    fun `remote every-launch reuses the cached code offline under ALLOW_CACHED`() = runTest {
        seedRemoteGrant()
        val request = remoteRequest(RemoteActivationOfflinePolicy.ALLOW_CACHED)

        // "Verify every launch" path: re-verifies the remembered code; offline the
        // ALLOW_CACHED policy still honours the cached server result.
        assertThat(manager.resolveRemoteStartup(APP_ID, request, reverifyEveryLaunch = true))
            .isTrue()
    }

    @Test
    fun `remote startup with no remembered code asks for one`() = runTest {
        val request = remoteRequest(RemoteActivationOfflinePolicy.ALLOW_CACHED)
        assertThat(manager.resolveRemoteStartup(APP_ID, request, reverifyEveryLaunch = true))
            .isFalse()
        assertThat(manager.resolveRemoteStartup(APP_ID, request, reverifyEveryLaunch = false))
            .isFalse()
    }

    @Test
    fun `remote grant clears remembered local card`() = runTest {
        manager.verifyActivationCodeWithObjects(APP_ID, "CARD-AAAA", listOf(permanent("CARD-AAAA")))
        manager.saveActivationStatus(APP_ID, true) // remote grant overwrites

        // The grant is now remote-backed; the stale local card must not relaunch.
        assertThat(manager.resolveRelaunchActivation(APP_ID, listOf(permanent("CARD-AAAA"))))
            .isFalse()
    }

    companion object {
        private const val APP_ID = 4242L
    }
}
