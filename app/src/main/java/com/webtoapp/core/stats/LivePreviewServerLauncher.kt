package com.webtoapp.core.stats

import android.content.Context
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.nodejs.NodeRuntime
import com.webtoapp.core.php.PhpAppRuntime
import com.webtoapp.core.python.PythonRuntime
import com.webtoapp.core.wordpress.WordPressDependencyManager
import com.webtoapp.core.wordpress.WordPressManager
import com.webtoapp.core.wordpress.WordPressPhpRuntime
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

object LivePreviewServerLauncher {

    private const val TAG = "LivePreviewServer"

    private const val START_TIMEOUT_MS = 40_000L
    private const val START_TIMEOUT_WORDPRESS_MS = 90_000L

    private val LIVE_TYPES = setOf(
        AppType.NODEJS_APP,
        AppType.PYTHON_APP,
        AppType.GO_APP,
        AppType.PHP_APP,
        AppType.WORDPRESS,
    )

    private val launchMutex = Mutex()

    fun supports(appType: AppType): Boolean = appType in LIVE_TYPES

    private class ServerHandle(val url: String, private val onStop: () -> Unit) {
        fun stop() {
            runCatching { onStop() }
                .onFailure { AppLogger.w(TAG, "stop server failed: ${it.message}") }
        }
    }

    suspend fun withLiveServer(
        context: Context,
        app: WebApp,
        block: suspend (url: String) -> Unit,
    ): Boolean {
        if (!supports(app.appType)) return false

        return launchMutex.withLock {
            val timeout = if (app.appType == AppType.WORDPRESS) START_TIMEOUT_WORDPRESS_MS else START_TIMEOUT_MS
            val handle = try {
                withTimeoutOrNull(timeout) { startServer(context, app) }
            } catch (e: Exception) {
                AppLogger.e(TAG, "start server threw for appId=${app.id}, type=${app.appType}", e)
                null
            }

            if (handle == null) {
                AppLogger.i(TAG, "live server unavailable for appId=${app.id}, type=${app.appType}; falling back to static preview")
                return@withLock false
            }

            try {
                AppLogger.i(TAG, "live server up for appId=${app.id}: ${handle.url}")
                block(handle.url)
                true
            } finally {
                handle.stop()
                AppLogger.i(TAG, "live server stopped for appId=${app.id}")
            }
        }
    }

    private suspend fun startServer(context: Context, app: WebApp): ServerHandle? {
        return when (app.appType) {
            AppType.NODEJS_APP -> startNode(context, app)
            AppType.PYTHON_APP -> startPython(context, app)
            AppType.GO_APP -> startGo(context, app)
            AppType.PHP_APP -> startPhp(context, app)
            AppType.WORDPRESS -> startWordPress(context, app)
            else -> null
        }
    }

    private suspend fun startNode(context: Context, app: WebApp): ServerHandle? {
        val config = app.nodejsConfig ?: return null
        val runtime = NodeRuntime(context)
        if (!runtime.isNodeAvailable()) return null

        val sourceDir = runtime.resolveSourceProjectDir(config.sourceProjectPath)
        val storedDir = config.projectId.takeIf { it.isNotBlank() }
            ?.let(runtime::getProjectDir)
            ?.takeIf { it.exists() }
        val projectDir = sourceDir ?: storedDir ?: return null

        val port = runtime.startServer(
            projectDir = projectDir.absolutePath,
            entryFile = config.entryFile.ifBlank { "index.js" },
            port = config.serverPort,
            envVars = config.envVars,
        )
        if (port <= 0) return null
        return ServerHandle("http://127.0.0.1:$port") { runtime.stopServer() }
    }

    private suspend fun startPython(context: Context, app: WebApp): ServerHandle? {
        val config = app.pythonAppConfig ?: return null
        val runtime = PythonRuntime(context)
        if (!runtime.isPythonAvailable()) return null

        val projectDir = config.projectId.takeIf { it.isNotBlank() }
            ?.let(runtime::getProjectDir)
            ?.takeIf { it.exists() } ?: return null

        val framework = config.framework.ifBlank { runtime.detectFramework(projectDir) }
        val entryFile = config.entryFile.ifBlank { runtime.detectEntryFile(projectDir, framework) }

        val port = runtime.startServer(
            projectDir = projectDir.absolutePath,
            entryFile = entryFile,
            framework = framework,
            port = config.serverPort,
            envVars = config.envVars,
            installDeps = config.hasPipDeps,
        )
        if (port <= 0) return null
        return ServerHandle("http://127.0.0.1:$port") { runtime.stopServer() }
    }

    private suspend fun startGo(context: Context, app: WebApp): ServerHandle? {
        val config = app.goAppConfig ?: return null
        val runtime = GoRuntime(context)

        val projectDir = config.projectId.takeIf { it.isNotBlank() }
            ?.let(runtime::getProjectDir)
            ?.takeIf { it.exists() } ?: return null

        if (runtime.detectBinary(projectDir) == null) {
            AppLogger.i(TAG, "Go app ${app.id} has no prebuilt binary; skip live preview")
            return null
        }

        val port = runtime.startServer(
            projectDir = projectDir.absolutePath,
            binaryName = config.binaryName,
            port = config.serverPort,
            envVars = config.envVars,
        )
        if (port <= 0) return null
        return ServerHandle("http://127.0.0.1:$port") { runtime.stopServer() }
    }

    private suspend fun startPhp(context: Context, app: WebApp): ServerHandle? {
        val config = app.phpAppConfig ?: return null
        val runtime = PhpAppRuntime(context)
        if (!runtime.isPhpAvailable()) return null

        val projectDir = config.projectId.takeIf { it.isNotBlank() }
            ?.let(runtime::getProjectDir)
            ?.takeIf { it.exists() } ?: return null

        val framework = config.framework.ifBlank { runtime.detectFramework(projectDir) }
        val documentRoot = config.documentRoot.ifBlank { runtime.detectDocumentRoot(projectDir, framework) }
        val entryFile = config.entryFile.ifBlank { runtime.detectEntryFile(projectDir, documentRoot) }

        val port = runtime.startServer(
            projectDir = projectDir.absolutePath,
            documentRoot = documentRoot,
            entryFile = entryFile,
            port = config.phpPort,
            envVars = config.envVars,
            phpExtensions = config.phpExtensions,
        )
        if (port <= 0) return null
        return ServerHandle("http://127.0.0.1:$port/") { runtime.stopServer() }
    }

    private suspend fun startWordPress(context: Context, app: WebApp): ServerHandle? {
        val config = app.wordpressConfig ?: return null
        if (!WordPressDependencyManager.isAllReady(context)) return null

        val projectId = config.projectId.takeIf { it.isNotBlank() } ?: return null
        val projectDir: File = WordPressManager.getProjectDir(context, projectId).takeIf { it.exists() } ?: return null

        WordPressManager.ensureDbPhpExists(context, projectDir)

        val runtime = WordPressPhpRuntime(context)
        val port = runtime.startServer(projectDir.absolutePath, config.phpPort)
        if (port <= 0) return null

        val base = "http://127.0.0.1:$port"

        try {
            WordPressManager.autoInstallIfNeeded(
                baseUrl = base,
                siteTitle = config.siteTitle.ifBlank { "My Site" },
                adminUser = config.adminUser.ifBlank { "admin" },
                adminPassword = config.adminPassword.ifBlank { "admin" },
                adminEmail = config.adminEmail.ifBlank { "admin@localhost.local" },
                siteLanguage = config.siteLanguage.ifBlank { "en_US" },
            )
            WordPressManager.applyRuntimeConfig(
                phpBinary = runtime.getPhpBinaryPath(),
                projectDir = projectDir,
                siteTitle = config.siteTitle.ifBlank { "My Site" },
                permalinkStructure = config.permalinkStructure.ifBlank { "/%postname%/" },
                siteLanguage = config.siteLanguage.ifBlank { "en_US" },
                themeName = config.themeName,
                activePlugins = config.activePlugins,
            )
        } catch (e: Exception) {

            AppLogger.w(TAG, "WordPress auto-install/config failed for appId=${app.id}: ${e.message}")
        }

        return ServerHandle("$base/") { runtime.stopServer() }
    }
}
