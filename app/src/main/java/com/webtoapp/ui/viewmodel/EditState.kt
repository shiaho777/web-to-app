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
import com.webtoapp.data.model.withRuntimePermissionsSyncedFromFeatures
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

fun WebApp.toEditState(): EditState {
    val synced = withRuntimePermissionsSyncedFromFeatures()
    return EditState(
        name = synced.name,
        url = synced.url,
        iconUri = synced.iconPath?.let(Uri::parse),
        savedIconPath = synced.iconPath,
        appType = synced.appType,
        mediaConfig = synced.mediaConfig,
        htmlConfig = synced.htmlConfig,
        activationEnabled = synced.activationEnabled,
        activationCodeList = synced.activationCodeList,
        activationRequireEveryTime = synced.activationRequireEveryTime,
        activationDialogConfig = synced.activationDialogConfig ?: ActivationDialogConfig(),
        activationRemoteConfig = synced.activationRemoteConfig,
        adsEnabled = synced.adsEnabled,
        adConfig = synced.adConfig ?: AdConfig(),
        announcementEnabled = synced.announcementEnabled,
        announcement = synced.announcement ?: Announcement(),
        adBlockEnabled = synced.adBlockEnabled,
        adBlockRules = synced.adBlockRules,
        adBlockSubscriptions = synced.adBlockSubscriptions,
        webViewConfig = synced.webViewConfig,
        splashEnabled = synced.splashEnabled,
        splashConfig = synced.splashConfig ?: SplashConfig(),
        splashMediaUri = synced.splashConfig?.mediaPath?.let(Uri::parse),
        savedSplashPath = synced.splashConfig?.mediaPath,
        bgmEnabled = synced.bgmEnabled,
        bgmConfig = synced.bgmConfig ?: BgmConfig(),
        apkExportConfig = synced.apkExportConfig ?: ApkExportConfig(),
        themeType = synced.themeType,
        translateEnabled = synced.translateEnabled,
        translateConfig = synced.translateConfig ?: TranslateConfig(),
        extensionModuleEnabled = synced.extensionEnabled,
        extensionModuleIds = synced.extensionModuleIds.toSet(),
        extensionFabIcon = synced.extensionFabIcon ?: "",
        autoStartConfig = synced.autoStartConfig,
        forcedRunConfig = synced.forcedRunConfig,
        blackTechConfig = synced.blackTechConfig,
        disguiseConfig = synced.disguiseConfig,
        deviceDisguiseConfig = synced.deviceDisguiseConfig ?: DeviceDisguiseConfig(),
    )
}

fun EditState.withRuntimePermissionsSyncedFromFeatures(): EditState {
    val syncedExport = apkExportConfig.withRuntimePermissionsSyncedFromFeatures(
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        forcedRunConfig = forcedRunConfig,
        blackTechConfig = blackTechConfig,
        bgmEnabled = bgmEnabled
    )
    return if (syncedExport === apkExportConfig || syncedExport == apkExportConfig) {
        this
    } else {
        copy(apkExportConfig = syncedExport)
    }
}


fun EditState.hasPreviewableContent(): Boolean = when (appType) {
    AppType.WEB -> url.isNotBlank()
    AppType.HTML, AppType.FRONTEND -> htmlConfig?.files?.isNotEmpty() == true
    AppType.IMAGE, AppType.VIDEO -> url.isNotBlank()
    else -> true
}
