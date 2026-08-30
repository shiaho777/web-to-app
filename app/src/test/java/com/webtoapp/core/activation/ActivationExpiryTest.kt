package com.webtoapp.core.activation

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

/**
 * Guards the offline half of issue #672.
 *
 * A relative time limit is measured from first activation and only lives in
 * local state, so clearing app data reset the clock and the code activated
 * again. An absolute expiry travels with the code and is checked before local
 * state is read, so it survives the wipe.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ActivationExpiryTest {

    private lateinit var manager: ActivationManager
    private lateinit var context: android.content.Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = ActivationManager(context)
        runBlocking { manager.resetActivation(APP_ID) }
    }

    private fun preferences() = runBlocking {
        context.activationDataStore.data.first()
    }

    @Test
    fun `code without an absolute expiry never expires`() {
        val code = ActivationCode(code = "PLAIN123", type = ActivationCodeType.PERMANENT)
        assertThat(code.isExpiredAt()).isFalse()
        assertThat(code.isExpiredAt(Long.MAX_VALUE)).isFalse()
    }

    @Test
    fun `code past its absolute expiry is rejected with no local state`() = runBlocking {
        val expired = ActivationCode(
            code = "EXPIRED1",
            type = ActivationCodeType.PERMANENT,
            expiresAt = System.currentTimeMillis() - 1_000
        )

        // The bug: local state is empty (as after a data wipe), so the old code
        // path skipped every check and returned Success.
        val result = manager.verifyActivationCodeWithObjects(APP_ID, "EXPIRED1", listOf(expired))

        assertThat(result).isEqualTo(ActivationResult.Expired)
    }

    @Test
    fun `code before its absolute expiry still activates`() = runBlocking {
        val valid = ActivationCode(
            code = "FUTURE12",
            type = ActivationCodeType.PERMANENT,
            expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)
        )

        val result = manager.verifyActivationCodeWithObjects(APP_ID, "FUTURE12", listOf(valid))

        assertThat(result).isInstanceOf(ActivationResult.Success::class.java)
    }

    @Test
    fun `absolute expiry is written into state so the startup check honours it`() = runBlocking {
        val expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)
        val code = ActivationCode(
            code = "STATE123",
            type = ActivationCodeType.PERMANENT,
            expiresAt = expiresAt
        )

        manager.saveActivationStatus(APP_ID, code, "device-under-test")

        assertThat(preferences()[longPreferencesKey("expire_time_$APP_ID")]).isEqualTo(expiresAt)
    }

    @Test
    fun `absolute expiry caps a longer relative window`() = runBlocking {
        val expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3)
        val code = ActivationCode(
            code = "BOTH1234",
            type = ActivationCodeType.TIME_LIMITED,
            timeLimitMs = TimeUnit.DAYS.toMillis(30),
            expiresAt = expiresAt
        )

        manager.saveActivationStatus(APP_ID, code, "device-under-test")

        assertThat(preferences()[longPreferencesKey("expire_time_$APP_ID")]).isEqualTo(expiresAt)
    }

    @Test
    fun `device id is persisted so the stored state is complete`() = runBlocking {
        val code = ActivationCode(code = "DEVICE12", type = ActivationCodeType.PERMANENT)

        manager.saveActivationStatus(APP_ID, code, "device-abc")

        assertThat(preferences()[stringPreferencesKey("device_id_$APP_ID")]).isEqualTo("device-abc")
    }

    @Test
    fun `legacy json without expiresAt stays permanent`() {
        val parsed = ActivationCode.fromJson("""{"code":"LEGACY12","type":"PERMANENT"}""")

        assertThat(parsed).isNotNull()
        assertThat(parsed!!.expiresAt).isNull()
        assertThat(parsed.isExpiredAt()).isFalse()
    }

    @Test
    fun `expiresAt survives a json round trip`() {
        val expiresAt = 1_800_000_000_000L
        val original = ActivationCode(
            code = "ROUND123",
            type = ActivationCodeType.PERMANENT,
            expiresAt = expiresAt
        )

        val restored = ActivationCode.fromJson(original.toJson())

        assertThat(restored?.expiresAt).isEqualTo(expiresAt)
    }

    private companion object {
        const val APP_ID = 7L
    }
}
