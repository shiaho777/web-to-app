package com.webtoapp.core.activation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.apkbuilder.ApkConfigJsonFactory
import com.webtoapp.core.apkbuilder.toApkConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.RemoteActivationConfig
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression coverage for the device-bound remote activation round trip (#704-era gap):
 * PR #570 removed the local DEVICE_BOUND code type and, with it, the only call site that
 * forwarded `deviceBound` into remote requests — the server's seat enforcement
 * (`maxDevices`, default 1 = one-time code) could never trigger because the client
 * always sent `deviceBound: false`.
 *
 * The editor toggle now flows through model → ApkConfig → JSON → shell config, and
 * every runtime call site forwards it via `buildRemoteRequest(deviceBound = …)`.
 */
@RunWith(RobolectricTestRunner::class)
class RemoteDeviceBoundRoundTripTest {

    private fun shellJsonOf(remote: RemoteActivationConfig): String {
        val app = WebApp(name = "t", url = "https://t.example.com", activationRemoteConfig = remote)
        val apk = app.toApkConfig("com.example.test")
        return ApkConfigJsonFactory.toShellConfigJson(apk)
    }

    @Test
    fun `deviceBound survives the export round trip when enabled`() {
        val json = shellJsonOf(
            RemoteActivationConfig(
                enabled = true,
                verifyUrl = "https://example.com/verify",
                publicKeyBase64 = "PUBKEY",
                deviceBound = true
            )
        )
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
        assertThat(shell.activationRemoteEnabled).isTrue()
        assertThat(shell.activationRemoteDeviceBound).isTrue()
    }

    @Test
    fun `deviceBound stays false by default and when disabled`() {
        val json = shellJsonOf(RemoteActivationConfig(enabled = true, verifyUrl = "https://x"))
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
        assertThat(shell.activationRemoteDeviceBound).isFalse()
    }

    @Test
    fun `buildRemoteRequest forwards the deviceBound flag`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activation = ActivationManager(context)
        val request = activation.buildRemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = "PUBKEY",
            offlinePolicy = RemoteActivationOfflinePolicy.ALLOW_CACHED,
            deviceBound = true
        )
        assertThat(request.deviceBound).isTrue()

        val requestOff = activation.buildRemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = "PUBKEY",
            offlinePolicy = RemoteActivationOfflinePolicy.ALLOW_CACHED
        )
        assertThat(requestOff.deviceBound).isFalse()
    }
}
