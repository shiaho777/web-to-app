package com.webtoapp.core.playstore.aab

import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.resolveOutputDir
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.WebApp
import java.io.File
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AabExportCoordinatorTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun app(
        name: String = "Demo",
        packageName: String? = null,
        updatedAt: Long = 1_000L
    ): WebApp = WebApp(
        id = 1L,
        name = name,
        url = "https://example.com",
        updatedAt = updatedAt,
        apkExportConfig = ApkExportConfig(customPackageName = packageName)
    )

    private fun builtApk(
        fileName: String,
        packageName: String?,
        builtAtMs: Long,
        versionCode: Long = 1L,
        sidecar: Boolean = true
    ): File {
        val dir = resolveOutputDir(context)
        val apk = File(dir, fileName)
        apk.writeBytes(byteArrayOf(1, 2, 3))
        if (sidecar && packageName != null) {
            val metadata = JSONObject()
                .put("schema", 1)
                .put("generatedAtMs", builtAtMs)
                .put("app", JSONObject()
                    .put("packageName", packageName)
                    .put("versionCode", versionCode)
                    .put("versionName", "1.0.0"))
            File(dir, apk.nameWithoutExtension + ".build.json").writeText(metadata.toString())
        }
        return apk
    }

    private fun install(packageName: String, versionCode: Long) {
        val info = PackageInfo().apply {
            this.packageName = packageName
            @Suppress("DEPRECATION")
            this.versionCode = versionCode.toInt()
            this.longVersionCode = versionCode
        }
        shadowOf(context.packageManager).installPackage(info)
    }

    @Test
    fun `prefix sibling app is not treated as a built apk`() {
        val demo = app(name = "Demo", packageName = "com.example.demo")
        builtApk("Demo_2_v1.0.0.APK", "com.example.demo2", builtAtMs = 5_000L)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isFalse()
    }

    @Test
    fun `own fresh apk with matching package is reusable`() {
        val demo = app(name = "Demo", packageName = "com.example.demo", updatedAt = 1_000L)
        builtApk("Demo_v1.0.0.APK", "com.example.demo", builtAtMs = 5_000L)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isTrue()
    }

    @Test
    fun `apk older than the latest config edit is stale`() {
        val demo = app(name = "Demo", packageName = "com.example.demo", updatedAt = 9_000L)
        builtApk("Demo_v1.0.0.APK", "com.example.demo", builtAtMs = 5_000L)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isFalse()
    }

    @Test
    fun `apk without metadata sidecar is not reusable`() {
        val demo = app(name = "Demo", packageName = "com.example.demo")
        builtApk("Demo_v1.0.0.APK", packageName = null, builtAtMs = 5_000L, sidecar = false)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isFalse()
    }

    @Test
    fun `corrupt metadata sidecar is not reusable`() {
        val demo = app(name = "Demo", packageName = "com.example.demo")
        val apk = builtApk("Demo_v1.0.0.APK", packageName = null, builtAtMs = 5_000L, sidecar = false)
        File(apk.parentFile, apk.nameWithoutExtension + ".build.json").writeText("{not json")

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isFalse()
    }

    @Test
    fun `apk below the installed version code is stale`() {
        val demo = app(name = "Demo", packageName = "com.example.demo", updatedAt = 1_000L)
        builtApk("Demo_v1.0.0.APK", "com.example.demo", builtAtMs = 5_000L, versionCode = 2L)
        install("com.example.demo", versionCode = 7L)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isFalse()
    }

    @Test
    fun `apk at or above the installed version code is reusable`() {
        val demo = app(name = "Demo", packageName = "com.example.demo", updatedAt = 1_000L)
        builtApk("Demo_v1.0.0.APK", "com.example.demo", builtAtMs = 5_000L, versionCode = 7L)
        install("com.example.demo", versionCode = 7L)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isTrue()
    }

    @Test
    fun `name collision with different packages resolves by identity`() {
        val demo = app(name = "Demo", packageName = "com.example.demo", updatedAt = 1_000L)
        builtApk("Demo_v1.0.0.APK", "com.example.other", builtAtMs = 9_000L)
        builtApk("Demo_v1.0.1.APK", "com.example.demo", builtAtMs = 5_000L)

        assertThat(AabExportCoordinator(context).hasBuiltApk(demo)).isTrue()
        assertThat(ApkBuilder.resolvePackageName(demo)).isEqualTo("com.example.demo")
    }
}