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
import com.webtoapp.data.model.Announcement
import com.webtoapp.ui.components.announcement.toUiTemplate
import com.webtoapp.ui.splash.ActivationDialog
import kotlinx.coroutines.launch

@Composable
fun ShellActivationDialog(
    config: ShellConfig,
    onDismiss: () -> Unit,
    onActivated: (String?) -> Unit
) {
    val context = LocalContext.current
    val activation = WebToAppApplication.activation
    val announcement = WebToAppApplication.announcement
    var activationError by remember { mutableStateOf<String?>(null) }

    ActivationDialog(
        onDismiss = onDismiss,
        customTitle = config.activationDialogTitle,
        customSubtitle = config.activationDialogSubtitle,
        customInputLabel = config.activationDialogInputLabel,
        customButtonText = config.activationDialogButtonText,
        errorMessage = activationError,
        onActivate = { code ->
            val scope = (context as? AppCompatActivity)?.lifecycleScope
            activationError = null
            scope?.launch {
                val result = if (config.activationRemoteEnabled) {
                    activation.verifyRemoteActivation(
                        -1L,
                        code,
                        activation.buildRemoteRequest(
                            verifyUrl = config.activationRemoteVerifyUrl,
                            publicKeyBase64 = config.activationRemotePublicKey,
                            offlinePolicy = parseOfflinePolicy(config.activationRemoteOfflinePolicy),
                            deliverUrl = config.activationRemoteDeliverUrl,
                            encryptUrl = config.activationRemoteEncryptUrl,
                            aesKeyBase64 = config.activationRemoteAesKey
                        )
                    )
                } else {
                    activation.verifyActivationCode(
                        -1L,
                        code,
                        config.activationCodes
                    )
                }
                when (result) {
                    is ActivationResult.Success -> {
                        onActivated(result.url)
                    }
                    is ActivationResult.Invalid -> {
                        activationError = result.message.ifBlank {
                            com.webtoapp.core.i18n.Strings.invalidActivationCode
                        }
                    }
                    else -> {
                        activationError = com.webtoapp.core.i18n.Strings.invalidActivationCode
                    }
                }
            }
        }
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
        allowNeverShow = config.announcementAllowNeverShow,
        showIcon = config.announcementShowIcon
    )

    val customIconBitmap = if (config.announcementHasCustomIcon) {
        try {
            val bytes = com.webtoapp.core.crypto.AssetDecryptor(context).loadAsset("announcement_icon.png")
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) { null }
    } else null

    com.webtoapp.ui.components.announcement.AnnouncementDialog(
        config = com.webtoapp.ui.components.announcement.AnnouncementConfig(
            announcement = shellAnnouncement,
            template = shellAnnouncement.template.toUiTemplate(),
            customIconBitmap = customIconBitmap
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
