package com.webtoapp.core.appmodifier

import com.webtoapp.data.model.SplashType
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.util.GsonProvider

object CloneConfigBuilder {

    fun buildJson(config: AppModifyConfig): String {
        val splashConfig = config.splashConfig
        val splashType = if (splashConfig.type == SplashType.VIDEO) "VIDEO" else "IMAGE"
        val splashOrientation = if (splashConfig.orientation == SplashOrientation.LANDSCAPE) "LANDSCAPE" else "PORTRAIT"

        val activationDialog = mapOf(
            "title" to config.activationDialogConfig.title,
            "subtitle" to config.activationDialogConfig.subtitle,
            "inputLabel" to config.activationDialogConfig.inputLabel,
            "buttonText" to config.activationDialogConfig.buttonText
        )

        val remoteConfig = config.activationRemoteConfig

        val announcement = config.announcement

        val cloneConfig = mapOf(
            "targetPackage" to config.originalApp.packageName,
            "splashEnabled" to (config.splashEnabled && !splashConfig.mediaPath.isNullOrBlank()),
            "splashType" to splashType,
            "splashDuration" to splashConfig.duration,
            "splashClickToSkip" to splashConfig.clickToSkip,
            "splashOrientation" to splashOrientation,
            "splashFillScreen" to splashConfig.fillScreen,
            "splashEnableAudio" to splashConfig.enableAudio,
            "splashVideoStartMs" to splashConfig.videoStartMs,
            "splashVideoEndMs" to splashConfig.videoEndMs,
            "activationEnabled" to config.activationEnabled,
            "activationCodes" to config.activationCodes.map { it.code },
            "activationRequireEveryTime" to config.activationRequireEveryTime,
            "activationDialog" to activationDialog,
            "remoteActivationEnabled" to remoteConfig.enabled,
            "remoteVerifyUrl" to remoteConfig.verifyUrl,
            "remoteOfflinePolicy" to remoteConfig.offlinePolicy.name,
            "announcementEnabled" to config.announcementEnabled,
            "announcementTitle" to announcement.title,
            "announcementContent" to announcement.content,
            "announcementContentIsHtml" to announcement.contentIsHtml,
            "announcementLinkUrl" to (announcement.linkUrl ?: ""),
            "announcementLinkText" to (announcement.linkText ?: "")
        )

        return GsonProvider.gson.toJson(cloneConfig)
    }

    fun needsSplashMedia(config: AppModifyConfig): Boolean {
        return config.splashEnabled && !config.splashConfig.mediaPath.isNullOrBlank()
    }

    fun getSplashMediaPath(config: AppModifyConfig): String? {
        return config.splashConfig.mediaPath
    }

    fun getSplashType(config: AppModifyConfig): String {
        return if (config.splashConfig.type == SplashType.VIDEO) "VIDEO" else "IMAGE"
    }

    fun hasAnyModification(config: AppModifyConfig): Boolean {
        val hasSplash = config.splashEnabled && !config.splashConfig.mediaPath.isNullOrBlank()
        val hasActivation = config.activationEnabled &&
            (config.activationCodes.isNotEmpty() || config.activationRemoteConfig.enabled)
        val hasAnnouncement = config.announcementEnabled && config.announcement.title.isNotBlank()
        return hasSplash || hasActivation || hasAnnouncement
    }
}
