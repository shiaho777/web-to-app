package com.webtoapp.core.apkbuilder

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Build-screen launch (#1151) goes through [ApkBuilder.launchInstalledPackage]:
 * the installed package's launcher activity, or a clean false when there is none.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class LaunchInstalledPackageTest {

    private val context: Application
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `blank or unknown package does not launch`() {
        assertThat(ApkBuilder.launchInstalledPackage(context, "  ")).isFalse()
        assertThat(ApkBuilder.launchInstalledPackage(context, "com.example.missing")).isFalse()
        assertThat(shadowOf(context).nextStartedActivity).isNull()
    }

    @Test
    fun `starts the launcher activity of an installed package`() {
        val packageName = "com.example.built"
        val component = ComponentName(packageName, "$packageName.MainActivity")
        val activityInfo = ActivityInfo().apply {
            this.packageName = packageName
            name = component.className
            enabled = true
            exported = true
            applicationInfo = ApplicationInfo().apply { this.packageName = packageName }
        }
        val shadowPm = shadowOf(context.packageManager)
        shadowPm.addOrUpdateActivity(activityInfo)
        shadowPm.addIntentFilterForActivity(
            component,
            IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        )

        assertThat(ApkBuilder.launchInstalledPackage(context, packageName)).isTrue()

        val started = shadowOf(context).nextStartedActivity
        assertThat(started).isNotNull()
        assertThat(started.component).isEqualTo(component)
        assertThat(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
    }
}
