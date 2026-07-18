package com.webtoapp.core.host

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.webtoapp.core.apkbuilder.toPreviewShellConfig
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.GalleryShellItem
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.shell.ShellPreviewSession
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.shell.ShellActivity
import java.io.File

object ShellPreviewLauncher {
    private const val TAG = "ShellPreviewLauncher"

    fun supportsShellPreview(appType: AppType): Boolean = true

    fun start(context: Context, webApp: WebApp): Boolean {
        return try {
            val config = buildPreviewConfig(context, webApp)
            ShellPreviewSession.begin(config, webApp.id)
            context.startActivity(buildIntent(context, webApp.id))
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start shell preview for appId=${webApp.id} type=${webApp.appType}", e)
            ShellPreviewSession.end()
            false
        }
    }

    suspend fun startById(context: Context, appId: Long): Boolean {
        val app = com.webtoapp.WebToAppApplication.repository.getWebApp(appId) ?: return false
        return start(context, app)
    }

    fun buildPreviewConfig(context: Context, webApp: WebApp): ShellConfig {
        val base = webApp.toPreviewShellConfig(context)
        val contentDir = ShellPreviewContent.resolve(context, webApp).orEmpty()
        var config = base.copy(
            previewContentDir = contentDir,
            siteDirName = when {
                contentDir.isNotBlank() && File(contentDir).isDirectory -> "shell_preview_${webApp.id}"
                else -> base.siteDirName
            },
            siteId = base.siteId.ifBlank { "preview_${webApp.id}" }
        )
        if (webApp.appType == AppType.GALLERY) {
            val items = webApp.galleryConfig?.items.orEmpty().map { item ->
                GalleryShellItem(
                    id = item.id,
                    assetPath = item.path,
                    type = item.type.name,
                    name = item.name,
                    duration = item.duration,
                    thumbnailPath = item.thumbnailPath
                )
            }
            config = config.copy(
                galleryConfig = config.galleryConfig.copy(items = items)
            )
        }
        if ((webApp.appType == AppType.HTML || webApp.appType == AppType.FRONTEND) &&
            config.htmlConfig.entryFile.isBlank()
        ) {
            config = config.copy(
                htmlConfig = config.htmlConfig.copy(
                    entryFile = webApp.htmlConfig?.getValidEntryFile() ?: "index.html"
                )
            )
        }
        return config
    }

    private fun buildIntent(context: Context, appId: Long): Intent {
        val separateTasks = try {
            HostRuntimePrefs.getInstance(context).isSeparateTasksEnabledBlocking()
        } catch (_: Exception) {
            false
        }
        return Intent(context, ShellActivity::class.java).apply {
            putExtra(ShellActivity.EXTRA_PREVIEW, true)
            putExtra(ShellActivity.EXTRA_PREVIEW_APP_ID, appId)
            action = Intent.ACTION_VIEW
            if (separateTasks) {
                data = Uri.parse("webtoapp://shell-preview/$appId")
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                )
            } else {
                data = null
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }
    }
}
