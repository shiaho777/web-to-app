package com.webtoapp.core.appmodifier

import android.graphics.drawable.Drawable
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.SplashConfig

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable?,
    val apkPath: String,
    val isSystemApp: Boolean,
    val installedTime: Long,
    val updatedTime: Long,
    val apkSize: Long,
    val splitApkPaths: List<String> = emptyList()
) {

    val formattedSize: String
        get() {
            val kb = apkSize / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1 -> String.format(java.util.Locale.getDefault(), "%.1f MB", mb)
                kb >= 1 -> String.format(java.util.Locale.getDefault(), "%.1f KB", kb)
                else -> "$apkSize B"
            }
        }
}

data class AppModifyConfig(
    val originalApp: InstalledAppInfo,
    val newAppName: String,
    /** User-selected clone package. Blank/null falls back to a generated unique name. */
    val newPackageName: String? = null,
    val newIconPath: String? = null,
    val newVersionName: String = originalApp.versionName,
    val newVersionCode: Int = originalApp.versionCode.coerceIn(1, Int.MAX_VALUE.toLong()).toInt(),

    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),

    val activationEnabled: Boolean = false,
    val activationCodes: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.webtoapp.data.model.RemoteActivationConfig = com.webtoapp.data.model.RemoteActivationConfig(),

    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement(),

    val dialogEngineEnabled: Boolean = false,
    val dialogEngineProject: DialogEngineProject = DialogEngineProject()
)

sealed class AppModifyResult {

    data class CloneSuccess(
        val apkPath: String,
        val installerLaunched: Boolean
    ) : AppModifyResult()

    data class Error(val message: String) : AppModifyResult()
}

enum class AppFilterType {
    ALL,
    USER,
    SYSTEM
}
