package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import java.io.File
import java.security.MessageDigest
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The `.build.json` release sidecar: stable schema, correct APK SHA-256, and the signing
 * identity recorded next to it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BuildMetadataWriterTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun fakeApk(): File {
        val dir = context.cacheDir
        val apk = File(dir, "fixture-${System.nanoTime()}.apk")
        apk.writeBytes(byteArrayOf(1, 2, 3, 4, 5) + "apk-bytes".toByteArray())
        return apk
    }

    private fun fakeConfig(): ApkConfig = ApkConfig(
        meta = MetaBlock(
            appName = "MetaTest",
            packageName = "com.example.meta",
            targetUrl = "https://example.com",
            versionCode = 7,
            versionName = "7.1",
            appType = AppType.WEB.name,
            engineType = "SYSTEM_WEBVIEW",
            themeType = "STANDARD",
            language = "en"
        )
    )

    @Test
    fun `sidecar records apk hash signing and build facts`() {
        val apk = fakeApk()
        val expectedSha = MessageDigest.getInstance("SHA-256")
            .digest(apk.readBytes())
            .joinToString("") { "%02x".format(it) }

        val out = BuildMetadataWriter.write(
            context = context,
            webApp = WebApp(name = "MetaTest", url = "https://example.com"),
            config = fakeConfig(),
            apkFile = apk,
            signerType = "ANDROID_KEYSTORE",
            certSha256Hex = "AA:BB:CC",
            buildMode = "FULL",
            buildReason = "templateChanged",
            durationMs = 1234L,
            logPath = "/tmp/build.log"
        )

        assertThat(out).isNotNull()
        out!!
        assertThat(out.exists()).isTrue()
        assertThat(out.name).isEqualTo(apk.nameWithoutExtension + ".build.json")
        assertThat(out.parentFile).isEqualTo(apk.parentFile)

        val json = JSONObject(out.readText())
        assertThat(json.getInt("schema")).isEqualTo(1)
        assertThat(json.getLong("generatedAtMs")).isGreaterThan(0L)

        val app = json.getJSONObject("app")
        assertThat(app.getString("packageName")).isEqualTo("com.example.meta")
        assertThat(app.getInt("versionCode")).isEqualTo(7)
        assertThat(app.getString("versionName")).isEqualTo("7.1")
        assertThat(app.getString("appType")).isEqualTo("WEB")

        val apkJson = json.getJSONObject("apk")
        assertThat(apkJson.getString("fileName")).isEqualTo(apk.name)
        assertThat(apkJson.getLong("sizeBytes")).isEqualTo(apk.length())
        assertThat(apkJson.getString("sha256")).isEqualTo(expectedSha)

        val signing = json.getJSONObject("signing")
        assertThat(signing.getString("signerType")).isEqualTo("ANDROID_KEYSTORE")
        assertThat(signing.getBoolean("perAppIdentity")).isFalse()
        assertThat(signing.getString("certificateSha256")).isEqualTo("AA:BB:CC")

        val build = json.getJSONObject("build")
        assertThat(build.getString("mode")).isEqualTo("FULL")
        assertThat(build.getString("reason")).isEqualTo("templateChanged")
        assertThat(build.getLong("durationMs")).isEqualTo(1234L)

        assertThat(json.getJSONObject("host").getInt("deviceSdk")).isGreaterThan(0)

        apk.delete()
        out.delete()
    }

    @Test
    fun `unwritable location yields null not a crash`() {
        val apk = File("/proc/nonexistent-dir-x/fake.apk")
        val out = BuildMetadataWriter.write(
            context = context,
            webApp = WebApp(name = "x", url = "https://x.example"),
            config = fakeConfig(),
            apkFile = apk,
            signerType = "ANDROID_KEYSTORE",
            certSha256Hex = null,
            buildMode = "FULL",
            buildReason = "",
            durationMs = 1L,
            logPath = null
        )
        assertThat(out).isNull()
    }
}
