package com.webtoapp.core.apkbuilder

import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.WebApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Covers the two rules behind issue #672: the package name has one derivation
 * rule shared by the UI and the builder, and a rebuild over an installed
 * package ends up with a strictly greater versionCode.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApkBuilderVersionTest {

    private fun app(
        name: String = "My Site",
        packageName: String? = null,
        versionCode: Int? = null,
        versionName: String? = null
    ): WebApp = WebApp(
        id = 1L,
        name = name,
        url = "https://example.com",
        apkExportConfig = ApkExportConfig(
            customPackageName = packageName,
            customVersionCode = versionCode,
            customVersionName = versionName
        )
    )

    private fun install(packageName: String, versionCode: Long) {
        val info = PackageInfo().apply {
            this.packageName = packageName
            @Suppress("DEPRECATION")
            this.versionCode = versionCode.toInt()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                this.longVersionCode = versionCode
            }
        }
        shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>().packageManager)
            .installPackage(info)
    }

    @Test
    fun `derived package name is stable for the same app name`() {
        val first = ApkBuilder.resolvePackageName(app("My Site"))
        val second = ApkBuilder.resolvePackageName(app("My Site"))
        assertThat(first).isEqualTo(second)
        assertThat(first).startsWith("com.w2a.")
    }

    @Test
    fun `custom package name wins when it is well formed`() {
        val resolved = ApkBuilder.resolvePackageName(app(packageName = "com.example.demo"))
        assertThat(resolved).isEqualTo("com.example.demo")
    }

    @Test
    fun `malformed custom package name falls back to the derived one`() {
        val resolved = ApkBuilder.resolvePackageName(app(packageName = "not a package name"))
        assertThat(resolved).isEqualTo(ApkBuilder.generatePackageName("My Site"))
    }

    @Test
    fun `nothing is bumped when the target package is not installed`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val webApp = app("Uninstalled App")
        assertThat(ApkBuilder.suggestedVersionForInstall(context, webApp)).isNull()
        assertThat(ApkBuilder.withInstallAwareVersion(context, webApp)).isSameInstanceAs(webApp)
    }

    @Test
    fun `rebuild over an installed derived package bumps code and name`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val webApp = app("My Site")
        install(ApkBuilder.resolvePackageName(webApp), 3)

        val suggested = ApkBuilder.suggestedVersionForInstall(context, webApp)
        assertThat(suggested).isNotNull()
        assertThat(suggested!!.first).isEqualTo(4)
        assertThat(suggested.second).isEqualTo("1.0.1")

        val bumped = ApkBuilder.withInstallAwareVersion(context, webApp)
        assertThat(bumped.apkExportConfig?.customVersionCode).isEqualTo(4)
        assertThat(bumped.apkExportConfig?.customVersionName).isEqualTo("1.0.1")
    }

    @Test
    fun `an explicit higher version set by the user is left alone`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val webApp = app("My Site", versionCode = 100)
        install(ApkBuilder.resolvePackageName(webApp), 3)

        assertThat(ApkBuilder.suggestedVersionForInstall(context, webApp)).isNull()
    }

    @Test
    fun `an explicit version that cannot be installed still gets bumped`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val webApp = app("My Site", versionCode = 5, versionName = "2.1.7")
        install(ApkBuilder.resolvePackageName(webApp), 5)

        val suggested = ApkBuilder.suggestedVersionForInstall(context, webApp)
        assertThat(suggested!!.first).isEqualTo(6)
        assertThat(suggested.second).isEqualTo("2.1.8")
    }

    @Test
    fun `version name bump only touches the last numeric segment`() {
        assertThat(ApkBuilder.bumpVersionName("1.0.0")).isEqualTo("1.0.1")
        assertThat(ApkBuilder.bumpVersionName("1.0")).isEqualTo("1.1")
        assertThat(ApkBuilder.bumpVersionName("v2.9")).isEqualTo("v2.10")
        assertThat(ApkBuilder.bumpVersionName("7")).isEqualTo("8")
    }

    @Test
    fun `version name without a numeric tail is left untouched`() {
        assertThat(ApkBuilder.bumpVersionName("1.0.0-beta")).isEqualTo("1.0.0-beta")
        assertThat(ApkBuilder.bumpVersionName("nightly")).isEqualTo("nightly")
    }
}
