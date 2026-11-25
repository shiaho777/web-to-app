package com.webtoapp.ui.screens

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.appmodifier.InstalledAppInfo
import org.junit.Test

class AppModifierEditStateTest {

    private val app = InstalledAppInfo(
        packageName = "com.example.source",
        appName = "Source",
        versionName = "1.0",
        versionCode = 1,
        icon = null,
        apkPath = "/source.apk",
        isSystemApp = false,
        installedTime = 0,
        updatedTime = 0,
        apkSize = 0
    )

    @Test
    fun `version metadata is copied to the build configuration`() {
        val config = AppModifierEditState(
            newAppName = "Clone",
            newPackageName = "com.example.clone",
            newVersionName = "2.1",
            newVersionCode = "42"
        ).toConfig(app)

        assertThat(config.newVersionName).isEqualTo("2.1")
        assertThat(config.newVersionCode).isEqualTo(42)
    }

    @Test
    fun `invalid version code is rejected by the build configuration`() {
        val config = AppModifierEditState(newVersionCode = "invalid").toConfig(app)

        assertThat(config.newVersionCode).isEqualTo(0)
    }
}
