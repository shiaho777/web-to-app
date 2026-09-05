package com.webtoapp.ui.shell

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.activation.ActivationResult
import com.webtoapp.core.activation.ActivationStatus
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.ui.components.announcement.toUiTemplate
import kotlinx.coroutines.launch

@Composable
fun ShellActivationDialog(
    config: ShellConfig,
    onDismiss: () -> Unit,
    onActivated: (String?) -> Unit
) {
    val activation = WebToAppApplication.activation

    val activationStatus by produceState<ActivationStatus?>(initialValue = null) {
        value = try {
            activation.getActivationStatus(-1L)
        } catch (e: Exception) {
            null
        }
    }

    com.webtoapp.ui.components.EnhancedActivationDialog(
        onDismiss = onDismiss,
        onActivate = { code ->
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
                        aesKeyBase64 = config.activationRemoteAesKey,
                        deviceBound = config.activationRemoteDeviceBound
                    )
                )
            } else {
                activation.verifyActivationCode(
                    -1L,
                    code,
                    config.activationCodes
                )
            }
            if (result is ActivationResult.Success) {
                onActivated(result.url)
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
        allowNeverShow = config.announcementAllowNeverShow,
        showIcon = config.announcementShowIcon
    )

    val customIconBitmap = if (config.announcementHasCustomIcon) {
        try {
            val bytes = com.webtoapp.core.crypto.AssetDecryptor(context).loadAsset("announcement_icon.png")
            com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapBytes(bytes)
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
                announcement.markAnnouncementShown(-1L, config.announcementVersion)
            }
        },
        onLinkClick = { url ->
            // Guarded like the preview's announcement dialog: normalize, reject blank targets,
            // and survive a missing browser app instead of crashing inside composition.
            try {
                val safeUrl = normalizeExternalUrlForIntent(url)
                if (safeUrl.isBlank()) {
                    android.widget.Toast.makeText(context, Strings.cannotOpenLink, android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, Strings.cannotOpenLink, android.widget.Toast.LENGTH_SHORT).show()
            }
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
