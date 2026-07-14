package com.webtoapp.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Stable
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.core.actions.DeviceActionsConfig
import com.webtoapp.core.appearance.DeviceDisguiseConfig
import com.webtoapp.core.appearance.DisguiseConfig
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.AdConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.AutoStartConfig
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.MediaConfig
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.TranslateConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig

@Stable
data class EditState(
    val name: String = "",
    val url: String = "",
    val iconUri: Uri? = null,
    val savedIconPath: String? = null,
    val iconBitmap: Bitmap? = null,
    val appType: AppType = AppType.WEB,
    val mediaConfig: MediaConfig? = null,
    val htmlConfig: HtmlConfig? = null,
    val activationEnabled: Boolean = false,
    val activationCodeList: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.webtoapp.data.model.RemoteActivationConfig? = null,
    val adsEnabled: Boolean = false,
    val adConfig: AdConfig = AdConfig(),
    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement(),
    val adBlockEnabled: Boolean = false,
    val adBlockRules: List<String> = emptyList(),
    val adBlockSubscriptions: List<String> = emptyList(),
    val webViewConfig: WebViewConfig = WebViewConfig(),
    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),
    val splashMediaUri: Uri? = null,
    val savedSplashPath: String? = null,
    val bgmEnabled: Boolean = false,
    val bgmConfig: BgmConfig = BgmConfig(),
    val apkExportConfig: ApkExportConfig = ApkExportConfig(),
    val themeType: String = "AURORA",
    val translateEnabled: Boolean = false,
    val translateConfig: TranslateConfig = TranslateConfig(),
    val extensionModuleEnabled: Boolean = false,
    val extensionModuleIds: Set<String> = emptySet(),
    val extensionFabIcon: String = "",
    val autoStartConfig: AutoStartConfig? = null,
    val forcedRunConfig: ForcedRunConfig? = null,
    val blackTechConfig: DeviceActionsConfig? = null,
    val disguiseConfig: DisguiseConfig? = null,
    val deviceDisguiseConfig: DeviceDisguiseConfig = DeviceDisguiseConfig(),
)

fun WebApp.toEditState(): EditState = EditState(
    name = name,
    url = url,
    iconUri = iconPath?.let(Uri::parse),
    savedIconPath = iconPath,
    appType = appType,
    mediaConfig = mediaConfig,
    htmlConfig = htmlConfig,
    activationEnabled = activationEnabled,
    activationCodeList = activationCodeList,
    activationRequireEveryTime = activationRequireEveryTime,
    activationDialogConfig = activationDialogConfig ?: ActivationDialogConfig(),
    activationRemoteConfig = activationRemoteConfig,
    adsEnabled = adsEnabled,
    adConfig = adConfig ?: AdConfig(),
    announcementEnabled = announcementEnabled,
    announcement = announcement ?: Announcement(),
    adBlockEnabled = adBlockEnabled,
    adBlockRules = adBlockRules,
    adBlockSubscriptions = adBlockSubscriptions,
    webViewConfig = webViewConfig,
    splashEnabled = splashEnabled,
    splashConfig = splashConfig ?: SplashConfig(),
    splashMediaUri = splashConfig?.mediaPath?.let(Uri::parse),
    savedSplashPath = splashConfig?.mediaPath,
    bgmEnabled = bgmEnabled,
    bgmConfig = bgmConfig ?: BgmConfig(),
    apkExportConfig = apkExportConfig ?: ApkExportConfig(),
    themeType = themeType,
    translateEnabled = translateEnabled,
    translateConfig = translateConfig ?: TranslateConfig(),
    extensionModuleEnabled = extensionEnabled,
    extensionModuleIds = extensionModuleIds.toSet(),
    extensionFabIcon = extensionFabIcon ?: "",
    autoStartConfig = autoStartConfig,
    forcedRunConfig = forcedRunConfig,
    blackTechConfig = blackTechConfig,
    disguiseConfig = disguiseConfig,
    deviceDisguiseConfig = deviceDisguiseConfig ?: DeviceDisguiseConfig(),
)


fun EditState.hasPreviewableContent(): Boolean = when (appType) {
    AppType.WEB -> url.isNotBlank()
    AppType.HTML, AppType.FRONTEND -> htmlConfig?.files?.isNotEmpty() == true
    AppType.IMAGE, AppType.VIDEO -> url.isNotBlank()
    else -> true
}
