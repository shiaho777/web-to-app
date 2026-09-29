package com.webtoapp.core.appmodifier

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppListProviderTest {

    private lateinit var context: Context
    private lateinit var provider: AppListProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        provider = AppListProvider(context)
    }

    private fun installPackage(packageName: String, system: Boolean = false) {
        val info = PackageInfo().apply {
            this.packageName = packageName
            versionName = "1.0"
            applicationInfo = ApplicationInfo().apply {
                this.packageName = packageName
                sourceDir = "/data/app/$packageName/base.apk"
                nonLocalizedLabel = packageName
                if (system) flags = flags or ApplicationInfo.FLAG_SYSTEM
            }
        }
        shadowOf(context.packageManager).installPackage(info)
    }

    @Test
    fun `installed package without a launcher activity is listed`() = runBlocking {
        // Regression: enumeration used to go through a MAIN+LAUNCHER intent query,
        // which can never resolve packages that have no launcher entry (IMEs,
        // plugin/icon packs, service-only system components, disabled apps).
        installPackage("com.example.headless")

        val names = provider.getInstalledApps(AppFilterType.ALL).map { it.packageName }

        assertThat(names).contains("com.example.headless")
    }

    @Test
    fun `own package is always listed`() = runBlocking {
        val names = provider.getInstalledApps(AppFilterType.ALL).map { it.packageName }

        assertThat(names).contains(context.packageName)
    }

    @Test
    fun `user and system filters split on FLAG_SYSTEM`() = runBlocking {
        installPackage("com.example.userapp")
        installPackage("com.example.sysapp", system = true)

        val userNames = provider.getInstalledApps(AppFilterType.USER).map { it.packageName }
        val systemNames = provider.getInstalledApps(AppFilterType.SYSTEM).map { it.packageName }

        assertThat(userNames).contains("com.example.userapp")
        assertThat(userNames).doesNotContain("com.example.sysapp")
        assertThat(systemNames).contains("com.example.sysapp")
        assertThat(systemNames).doesNotContain("com.example.userapp")
    }

    @Test
    fun `search query filters by name and package`() = runBlocking {
        installPackage("com.example.alpha")
        installPackage("com.example.beta")

        val names = provider.getInstalledApps(AppFilterType.ALL, "alpha").map { it.packageName }

        assertThat(names).contains("com.example.alpha")
        assertThat(names).doesNotContain("com.example.beta")
    }
}
