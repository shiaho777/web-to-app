package com.webtoapp.ui.shell

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.activation.ActivationResult
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.shell.ShellPreviewSession
import com.webtoapp.core.forcedrun.ForcedRunManager
import com.webtoapp.core.forcedrun.ForcedRunPermissionDialog
import com.webtoapp.data.model.Announcement
import com.webtoapp.ui.components.announcement.toUiTemplate
import kotlinx.coroutines.launch

@Composable
fun ShellActivationDialog(
    config: ShellConfig,
    onDismiss: () -> Unit,
    onActivated: () -> Unit
) {
    val activation = WebToAppApplication.activation
    var activationStatus by remember {
        mutableStateOf<com.webtoapp.core.activation.ActivationStatus?>(null)
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        activationStatus = try {
            activation.getActivationStatus(ShellPreviewSession.activationAppId())
        } catch (_: Exception) {
            null
        }
    }

    com.webtoapp.ui.components.EnhancedActivationDialog(
        onDismiss = onDismiss,
        onActivate = { code ->
            val result = if (config.activationRemoteEnabled) {
                activation.verifyRemoteActivation(
                    ShellPreviewSession.activationAppId(),
                    code,
                    activation.buildRemoteRequest(
                        verifyUrl = config.activationRemoteVerifyUrl,
                        publicKeyBase64 = config.activationRemotePublicKey,
                        offlinePolicy = parseOfflinePolicy(config.activationRemoteOfflinePolicy)
                    )
                )
            } else {
                activation.verifyActivationCode(
                    ShellPreviewSession.activationAppId(),
                    code,
                    config.activationCodes
                )
            }
            if (result is ActivationResult.Success || result is ActivationResult.AlreadyActivated) {
                onActivated()
            }
            result
        },
        activationStatus = activationStatus,
        customTitle = config.activationDialogTitle,
        customSubtitle = config.activationDialogSubtitle,
        customInputLabel = config.activationDialogInputLabel,
        customButtonText = config.activationDialogButtonText
    )
}

internal fun parseOfflinePolicy(
    raw: String
): com.webtoapp.data.model.RemoteActivationOfflinePolicy {
    return try {
        com.webtoapp.data.model.RemoteActivationOfflinePolicy.valueOf(raw)
    } catch (e: Exception) {
        com.webtoapp.data.model.RemoteActivationOfflinePolicy.ALLOW_CACHED
    }
}

@Composable
fun ShellAnnouncementDialog(
    config: ShellConfig,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val announcement = WebToAppApplication.announcement

    val shellAnnouncement = Announcement(
        title = config.announcementTitle,
        content = config.announcementContent,
        contentIsHtml = config.announcementContentIsHtml,
        linkUrl = config.announcementLink.ifEmpty { null },
        linkText = config.announcementLinkText.ifEmpty { null },
        template = try {
            com.webtoapp.data.model.AnnouncementTemplateType.valueOf(config.announcementTemplate).toUiTemplate().type
        } catch (e: Exception) {
            com.webtoapp.data.model.AnnouncementTemplateType.MINIMAL
        },
        requireConfirmation = config.announcementRequireConfirmation,
        allowNeverShow = config.announcementAllowNeverShow
    )

    com.webtoapp.ui.components.announcement.AnnouncementDialog(
        config = com.webtoapp.ui.components.announcement.AnnouncementConfig(
            announcement = shellAnnouncement,
            template = shellAnnouncement.template.toUiTemplate()
        ),
        onDismiss = {
            onDismiss()
            val scope = (context as? AppCompatActivity)?.lifecycleScope
            scope?.launch {
                announcement.markAnnouncementShown(-1L, 1)
            }
        },
        onLinkClick = { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizeExternalUrlForIntent(url)))
            context.startActivity(intent)
        },
        onNeverShowChecked = { checked ->
            if (checked) {
                val scope = (context as? AppCompatActivity)?.lifecycleScope
                scope?.launch {
                    announcement.markNeverShow(-1L)
                }
            }
        }
    )
}

@Composable
fun ShellForcedRunPermissionDialog(
    config: ShellConfig,
    forcedRunActive: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val forcedRunManager = ForcedRunManager.getInstance(context)

    ForcedRunPermissionDialog(
        protectionLevel = config.forcedRunConfig!!.protectionLevel,
        onDismiss = onDismiss,
        onContinueAnyway = {

            onDismiss()
            AppLogger.w("ShellActivity", "User skipped permission, forced run protection degraded")
        },
        onAllPermissionsGranted = {

            onDismiss()
            AppLogger.d("ShellActivity", "Forced run permissions all granted")

            if (forcedRunActive) {
                forcedRunManager.stopForcedRunMode()
                config.forcedRunConfig?.let { cfg ->
                    forcedRunManager.startForcedRunMode(cfg, -1L)
                }
            }
        }
    )
}
