package com.webtoapp.ui.screens

import android.net.Uri
import androidx.compose.runtime.Stable
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.core.appmodifier.AppModifyConfig
import com.webtoapp.core.appmodifier.InstalledAppInfo
import com.webtoapp.core.errorpage.ErrorPageConfig
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.SplashConfig

@Stable
data class AppModifierEditState(
    val newAppName: String = "",
    val newIconUri: Uri? = null,
    val newIconPath: String? = null,

    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),

    val splashMediaUri: Uri? = null,

    val activationEnabled: Boolean = false,
    val activationCodes: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.webtoapp.data.model.RemoteActivationConfig = com.webtoapp.data.model.RemoteActivationConfig(),

    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement(),

    val bgmEnabled: Boolean = false,
    val bgmConfig: BgmConfig = BgmConfig(),

    val errorPageEnabled: Boolean = false,
    val errorPageConfig: ErrorPageConfig = ErrorPageConfig()
) {
    fun toConfig(originalApp: InstalledAppInfo): AppModifyConfig {
        return AppModifyConfig(
            originalApp = originalApp,
            newAppName = newAppName,
            newIconPath = newIconPath ?: newIconUri?.toString(),
            splashEnabled = splashEnabled && !splashConfig.mediaPath.isNullOrBlank(),
            splashConfig = splashConfig,
            activationEnabled = activationEnabled,
            activationCodes = activationCodes,
            activationRequireEveryTime = activationRequireEveryTime,
            activationDialogConfig = activationDialogConfig,
            activationRemoteConfig = activationRemoteConfig,
            announcementEnabled = announcementEnabled,
            announcement = announcement,
            bgmEnabled = bgmEnabled,
            bgmConfig = if (bgmEnabled) bgmConfig else null,
            errorPageEnabled = errorPageEnabled,
            errorPageConfig = errorPageConfig
        )
    }
}
