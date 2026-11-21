package com.webtoapp.core.activation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.apkbuilder.ApkConfigJsonFactory
import com.webtoapp.core.apkbuilder.toApkConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.RemoteActivationConfig
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import com.webtoapp.data.model.RemoteActivationTrustMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RemoteActivationPinRoundTripTest {

    private val pin = "AB".repeat(32)

    private fun shellJsonOf(remote: RemoteActivationConfig): String {
        val app = WebApp(
            name = "t",
            url = "https://t.example.com",
            activationRemoteConfig = remote
        )
        val apk = app.toApkConfig("com.example.test")
        return ApkConfigJsonFactory.toShellConfigJson(apk)
    }

    @Test
    fun `pinned certificate fields survive the export round trip`() {
        val json = shellJsonOf(
            RemoteActivationConfig(
                enabled = true,
                verifyUrl = "https://103.236.93.69:10443/activation/verify",
                publicKeyBase64 = "PUBKEY",
                trustMode = RemoteActivationTrustMode.PINNED,
                pinnedCertificate = pin,
                pinnedCertHost = "103.236.93.69"
            )
        )
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
        assertThat(shell.activationRemoteEnabled).isTrue()
        assertThat(shell.activationRemoteTrustMode).isEqualTo("PINNED")
        assertThat(shell.activationRemotePinnedCertificate).isEqualTo(pin)
        assertThat(shell.activationRemotePinnedCertHost).isEqualTo("103.236.93.69")
    }

    @Test
    fun `system trust mode is the default`() {
        val json = shellJsonOf(
            RemoteActivationConfig(
                enabled = true,
                verifyUrl = "https://example.com/verify",
                publicKeyBase64 = "PUBKEY"
            )
        )
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
        assertThat(shell.activationRemoteTrustMode).isEqualTo("SYSTEM")
        assertThat(shell.activationRemotePinnedCertificate).isEmpty()
        assertThat(shell.activationRemotePinnedCertHost).isEmpty()
    }

    @Test
    fun `legacy stored config without trust fields loads with system defaults`() {
        val oldJson =
            """{"enabled":true,"verifyUrl":"https://example.com/verify","publicKeyBase64":"PUBKEY","offlinePolicy":"ALLOW_CACHED"}"""
        val config = com.webtoapp.data.converter.Converters().toRemoteActivationConfig(oldJson)!!
        assertThat(config.trustMode).isEqualTo(RemoteActivationTrustMode.SYSTEM)
        assertThat(config.pinnedCertificate).isEmpty()
        assertThat(config.pinnedCertHost).isEmpty()
        assertThat(
            com.webtoapp.data.converter.Converters().toRemoteActivationConfig("null")
        ).isNull()
    }

    @Test
    fun `buildRemoteRequest forwards the pin fields`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activation = ActivationManager(context)
        val request = activation.buildRemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = "PUBKEY",
            offlinePolicy = RemoteActivationOfflinePolicy.ALLOW_CACHED,
            trustMode = RemoteActivationTrustMode.PINNED,
            pinnedCertificate = pin,
            pinnedCertHost = "example.com"
        )
        assertThat(request.trustMode).isEqualTo(RemoteActivationTrustMode.PINNED)
        assertThat(request.pinnedCertificate).isEqualTo(pin)
        assertThat(request.pinnedCertHost).isEqualTo("example.com")
    }

    @Test
    fun `buildRemoteRequest forwards a preview target package`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activation = ActivationManager(context)
        val request = activation.buildRemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = "PUBKEY",
            offlinePolicy = RemoteActivationOfflinePolicy.ALLOW_CACHED,
            packageName = "com.example.target"
        )
        assertThat(request.packageName).isEqualTo("com.example.target")
        assertThat(request.deviceId)
            .isEqualTo(DeviceIdGenerator.previewDeviceId(context, "com.example.target"))
    }

    @Test
    fun `buildRemoteRequest defaults to the host package`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activation = ActivationManager(context)
        val request = activation.buildRemoteRequest(
            verifyUrl = "https://example.com/verify",
            publicKeyBase64 = "PUBKEY",
            offlinePolicy = RemoteActivationOfflinePolicy.ALLOW_CACHED
        )
        assertThat(request.packageName).isEqualTo(context.packageName)
        assertThat(request.deviceId).isEqualTo(DeviceIdGenerator.getDeviceId(context))
    }

    @Test
    fun `export rejects a pinned certificate with a mismatched host`() {
        val app = WebApp(
            name = "t",
            url = "https://t.example.com",
            activationRemoteConfig = RemoteActivationConfig(
                enabled = true,
                verifyUrl = "https://example.com/verify",
                publicKeyBase64 = "PUBKEY",
                trustMode = RemoteActivationTrustMode.PINNED,
                pinnedCertificate = pin,
                pinnedCertHost = "other.example"
            )
        )
        val apk = app.toApkConfig("com.example.test")
        assertThrows(IllegalArgumentException::class.java) {
            ApkConfigJsonFactory.create(apk)
        }
    }

    @Test
    fun `export rejects an invalid pinned fingerprint`() {
        val app = WebApp(
            name = "t",
            url = "https://t.example.com",
            activationRemoteConfig = RemoteActivationConfig(
                enabled = true,
                verifyUrl = "https://example.com/verify",
                publicKeyBase64 = "PUBKEY",
                trustMode = RemoteActivationTrustMode.PINNED,
                pinnedCertificate = "not-a-pin",
                pinnedCertHost = "example.com"
            )
        )
        val apk = app.toApkConfig("com.example.test")
        assertThrows(IllegalArgumentException::class.java) {
            ApkConfigJsonFactory.create(apk)
        }
    }
}
