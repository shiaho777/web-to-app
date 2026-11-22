package com.webtoapp.ui.screens

import androidx.compose.runtime.Stable
import com.webtoapp.core.appmodifier.AppModifyConfig
import com.webtoapp.core.appmodifier.InstalledAppInfo

@Stable
data class AppModifierEditState(
    val newAppName: String = "",
    val newPackageName: String = ""
) {
    fun toConfig(originalApp: InstalledAppInfo): AppModifyConfig {
        return AppModifyConfig(
            originalApp = originalApp,
            newAppName = newAppName,
            newPackageName = newPackageName.trim().takeIf { it.isNotBlank() }
        )
    }
}
