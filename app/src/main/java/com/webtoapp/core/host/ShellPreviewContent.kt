package com.webtoapp.core.host

import android.content.Context
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.nodejs.NodeRuntime
import com.webtoapp.core.php.PhpAppRuntime
import com.webtoapp.core.python.PythonRuntime
import com.webtoapp.core.wordpress.WordPressManager
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import java.io.File

object ShellPreviewContent {
    fun resolve(context: Context, webApp: WebApp): String? {
        return when (webApp.appType) {
            AppType.WEB, AppType.MULTI_WEB -> null
            AppType.HTML, AppType.FRONTEND -> resolveHtmlDir(context, webApp)
            AppType.NODEJS_APP -> resolveNodeDir(context, webApp)
            AppType.PHP_APP -> resolvePhpDir(context, webApp)
            AppType.PYTHON_APP -> resolvePythonDir(context, webApp)
            AppType.GO_APP -> resolveGoDir(context, webApp)
            AppType.WORDPRESS -> resolveWordpressDir(context, webApp)
            AppType.IMAGE, AppType.VIDEO -> webApp.mediaConfig?.mediaPath?.takeIf { File(it).exists() }
            AppType.GALLERY -> null
        }
    }

    private fun resolveHtmlDir(context: Context, webApp: WebApp): String? {
        val configured = webApp.htmlConfig?.projectDir?.trim().orEmpty()
        if (configured.isNotBlank()) {
            val file = File(configured)
            if (file.isDirectory) return file.absolutePath
        }
        val projectId = webApp.htmlConfig?.projectId?.trim().orEmpty()
        if (projectId.isNotBlank()) {
            val file = File(context.filesDir, "html_projects/$projectId")
            if (file.isDirectory) return file.absolutePath
        }
        return null
    }

    private fun resolveNodeDir(context: Context, webApp: WebApp): String? {
        val source = webApp.nodejsConfig?.sourceProjectPath?.trim().orEmpty()
        if (source.isNotBlank() && File(source).isDirectory) return File(source).absolutePath
        val projectId = webApp.nodejsConfig?.projectId?.trim().orEmpty()
        if (projectId.isBlank()) return null
        val dir = NodeRuntime(context).getProjectDir(projectId)
        return dir.takeIf { it.isDirectory }?.absolutePath
    }

    private fun resolvePhpDir(context: Context, webApp: WebApp): String? {
        val doc = webApp.phpAppConfig?.documentRoot?.trim().orEmpty()
        if (doc.isNotBlank() && File(doc).isDirectory) return File(doc).absolutePath
        val projectId = webApp.phpAppConfig?.projectId?.trim().orEmpty()
        if (projectId.isBlank()) return null
        val dir = PhpAppRuntime(context).getProjectDir(projectId)
        return dir.takeIf { it.isDirectory }?.absolutePath
    }

    private fun resolvePythonDir(context: Context, webApp: WebApp): String? {
        val source = webApp.pythonAppConfig?.sourceProjectPath?.trim().orEmpty()
        if (source.isNotBlank() && File(source).isDirectory) return File(source).absolutePath
        val projectId = webApp.pythonAppConfig?.projectId?.trim().orEmpty()
        if (projectId.isBlank()) return null
        val dir = PythonRuntime(context).getProjectDir(projectId)
        return dir.takeIf { it.isDirectory }?.absolutePath
    }

    private fun resolveGoDir(context: Context, webApp: WebApp): String? {
        val projectId = webApp.goAppConfig?.projectId?.trim().orEmpty()
        if (projectId.isBlank()) return null
        val dir = GoRuntime(context).getProjectDir(projectId)
        return dir.takeIf { it.isDirectory }?.absolutePath
    }

    private fun resolveWordpressDir(context: Context, webApp: WebApp): String? {
        val projectId = webApp.wordpressConfig?.projectId?.trim().orEmpty()
        if (projectId.isBlank()) return null
        val dir = WordPressManager.getProjectDir(context, projectId)
        return dir.takeIf { it.isDirectory }?.absolutePath
    }
}
