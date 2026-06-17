package com.webtoapp.core.appmodifier

import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.core.errorpage.ErrorPageConfig
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.util.GsonProvider

data class AppModifyPayload(
    val targetPackage: String,
    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),
    val activationEnabled: Boolean = false,
    val activationCodes: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.webtoapp.data.model.RemoteActivationConfig = com.webtoapp.data.model.RemoteActivationConfig(),
    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement(),

    val errorPageEnabled: Boolean = false,
    val errorPageConfig: ErrorPageConfig = ErrorPageConfig()
) {

    fun toJson(): String = GsonProvider.gson.toJson(this)

    fun needsLauncher(): Boolean {
        val hasSplash = splashEnabled && !splashConfig.mediaPath.isNullOrBlank()
        val hasActivation = activationEnabled &&
            (activationCodes.isNotEmpty() || activationRemoteConfig.enabled)
        val hasAnnouncement = announcementEnabled && announcement.title.isNotBlank()
        val hasErrorPage = errorPageEnabled && errorPageConfig.mode != com.webtoapp.core.errorpage.ErrorPageMode.DEFAULT
        return hasSplash || hasActivation || hasAnnouncement || hasErrorPage
    }

    companion object {
        fun fromJson(json: String?): AppModifyPayload? {
            if (json.isNullOrBlank()) return null
            return try {
                GsonProvider.gson.fromJson(json, AppModifyPayload::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
