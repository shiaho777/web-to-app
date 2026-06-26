package com.webtoapp.core.shell

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.shell.buildExtractionToken
import com.webtoapp.ui.shell.extractAssetsRecursive
import com.webtoapp.ui.shell.shouldReextractAssets
import com.webtoapp.ui.shell.writeExtractionMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object ShellServerLauncher {

    private const val TAG = "ShellServerLauncher"

    fun interface RuntimeStopper {
        fun stop()
    }

    data class LaunchResult(

        val resolvedUrl: String,

        val stopper: RuntimeStopper,

        val error: String? = null
    )

    suspend fun resolveServerBackedTargetUrl(
        context: Context,
        config: ShellConfig
    ): LaunchResult = withContext(Dispatchers.IO) {
        when (config.appType.uppercase()) {
            "PHP_APP" -> launchPhp(context, config)
            "PYTHON_APP" -> launchPython(context, config)
            "GO_APP" -> launchGo(context, config)
            "NODEJS_APP" -> launchNodejs(context, config)
            "WORDPRESS" -> launchWordpress(context, config)
            else -> LaunchResult(
                resolvedUrl = config.targetUrl,
                stopper = RuntimeStopper {  }
            )
        }
    }

    private suspend fun launchPhp(context: Context, config: ShellConfig): LaunchResult {
        return try {
            ensurePhpBinaryExtracted(context)

            val projectDir = File(context.filesDir, "php_app_site")
            val marker = File(projectDir, ".php_extracted")
            val extra = "${config.phpAppConfig.documentRoot}|${config.phpAppConfig.entryFile}"
            val token = buildExtractionToken(context, "php_app", config.versionCode, extra)
            if (shouldReextractAssets(marker, token)) {
                projectDir.deleteRecursively()
                extractAssetsRecursive(context, "php_app", projectDir)
                writeExtractionMarker(marker, token)
            }

            val runtime = com.webtoapp.core.php.PhpAppRuntime(context)
            val entryFile = config.phpAppConfig.entryFile.ifBlank { "index.php" }
            val port = runtime.startServer(
                projectDir = projectDir.absolutePath,
                documentRoot = config.phpAppConfig.documentRoot,
                entryFile = entryFile,
                port = config.phpAppConfig.port,
                envVars = config.phpAppConfig.envVars,
                phpExtensions = config.phpAppConfig.phpExtensions,
                customPhpExtensions = config.phpAppConfig.customPhpExtensions
            )
            if (port > 0) {
                LaunchResult(
                    resolvedUrl = "http://127.0.0.1:$port",
                    stopper = RuntimeStopper { runtime.stopServer() }
                )
            } else {
                runtime.stopServer()
                LaunchResult(config.targetUrl, RuntimeStopper {}, error = "PHP server start failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "PHP launch failed", e)
            LaunchResult(config.targetUrl, RuntimeStopper {}, error = e.message ?: "PHP launch error")
        }
    }

    private suspend fun launchPython(context: Context, config: ShellConfig): LaunchResult {
        return try {
            val pyConfig = config.pythonAppConfig

            val projectDir = File(context.filesDir, "python_app_site")
            val marker = File(projectDir, ".python_extracted")
            val token = buildExtractionToken(
                context = context,
                scope = "python_app",
                configVersionCode = config.versionCode,
                extra = "${pyConfig.framework}|${pyConfig.entryFile}|${pyConfig.entryModule}"
            )
            if (shouldReextractAssets(marker, token)) {
                projectDir.deleteRecursively()
                extractAssetsRecursive(context, "python_app", projectDir)
                writeExtractionMarker(marker, token)
            }

            val runtime = com.webtoapp.core.python.PythonRuntime(context)
            if (!runtime.isPythonAvailable()) {
                return LaunchResult(
                    config.targetUrl,
                    RuntimeStopper {},
                    error = "Python runtime unavailable"
                )
            }

            val entryFile = pyConfig.entryFile.ifEmpty { "app.py" }
            val framework = pyConfig.framework.ifEmpty { "flask" }
            val port = runtime.startServer(
                projectDir = projectDir.absolutePath,
                entryFile = entryFile,
                framework = framework,
                port = pyConfig.port,
                envVars = pyConfig.envVars,
                installDeps = true
            )
            if (port > 0) {
                LaunchResult(
                    resolvedUrl = "http://127.0.0.1:$port",
                    stopper = RuntimeStopper { runtime.stopServer() }
                )
            } else {
                runtime.stopServer()
                LaunchResult(config.targetUrl, RuntimeStopper {}, error = "Python server start failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Python launch failed", e)
            LaunchResult(config.targetUrl, RuntimeStopper {}, error = e.message ?: "Python launch error")
        }
    }

    private suspend fun launchGo(context: Context, config: ShellConfig): LaunchResult {
        return try {
            val goConfig = config.goAppConfig
            val projectDir = File(context.filesDir, "go_app_site")
            val marker = File(projectDir, ".go_extracted")
            val token = buildExtractionToken(
                context = context,
                scope = "go_app",
                configVersionCode = config.versionCode,
                extra = "${goConfig.framework}|${goConfig.binaryName}|${goConfig.staticDir}"
            )
            if (shouldReextractAssets(marker, token)) {
                projectDir.deleteRecursively()
                extractAssetsRecursive(context, "go_app", projectDir)
                writeExtractionMarker(marker, token)
            }

            projectDir.walkTopDown()
                .filter { it.isFile && it.length() > 1000 }
                .forEach { file ->
                    val info = com.webtoapp.core.golang.GoDependencyManager.parseElf(file)
                    if (info.isValid) file.setExecutable(true, false)
                }

            val runtime = com.webtoapp.core.golang.GoRuntime(context)
            val binaryName = goConfig.binaryName.ifEmpty {
                runtime.detectBinary(projectDir) ?: ""
            }
            if (binaryName.isEmpty()) {
                return LaunchResult(
                    config.targetUrl,
                    RuntimeStopper {},
                    error = "Go binary not found"
                )
            }

            val port = runtime.startServer(
                projectDir = projectDir.absolutePath,
                binaryName = binaryName,
                port = goConfig.port,
                envVars = goConfig.envVars
            )
            if (port > 0) {
                LaunchResult(
                    resolvedUrl = "http://127.0.0.1:$port",
                    stopper = RuntimeStopper { runtime.stopServer() }
                )
            } else {
                runtime.stopServer()
                LaunchResult(config.targetUrl, RuntimeStopper {}, error = "Go server start failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Go launch failed", e)
            LaunchResult(config.targetUrl, RuntimeStopper {}, error = e.message ?: "Go launch error")
        }
    }

    private suspend fun launchNodejs(context: Context, config: ShellConfig): LaunchResult {

        if (config.nodejsConfig.mode.equals("STATIC", ignoreCase = true)) {
            return LaunchResult(config.targetUrl, RuntimeStopper {})
        }
        return try {
            val nativeNode = File(context.applicationInfo.nativeLibraryDir, "libnode.so")
            if (!nativeNode.exists()) {
                return LaunchResult(
                    config.targetUrl,
                    RuntimeStopper {},
                    error = "libnode.so not found"
                )
            }

            val projectDir = File(context.filesDir, "nodejs_site")
            val marker = File(projectDir, ".nodejs_extracted")
            val token = buildExtractionToken(
                context = context,
                scope = "nodejs",
                configVersionCode = config.versionCode,
                extra = "${config.nodejsConfig.mode}|${config.nodejsConfig.entryFile}"
            )
            if (shouldReextractAssets(marker, token)) {
                projectDir.deleteRecursively()
                extractAssetsRecursive(context, "nodejs_app", projectDir)
                writeExtractionMarker(marker, token)
            }

            val envVars = config.nodejsConfig.envVars.toMutableMap()
            val requestPort = config.nodejsConfig.port.takeIf { it > 0 }
            if (requestPort != null && !envVars.containsKey("PORT")) {
                envVars["PORT"] = requestPort.toString()
            }

            val runtime = com.webtoapp.core.nodejs.NodeRuntime(context)
            val entryFile = config.nodejsConfig.entryFile.ifEmpty { "index.js" }
            val port = runtime.startServer(
                projectDir = projectDir.absolutePath,
                entryFile = entryFile,
                port = config.nodejsConfig.port,
                envVars = envVars
            )
            if (port > 0) {
                LaunchResult(
                    resolvedUrl = "http://127.0.0.1:$port",
                    stopper = RuntimeStopper { runtime.stopServer() }
                )
            } else {
                runtime.stopServer()
                LaunchResult(config.targetUrl, RuntimeStopper {}, error = "Node server start failed")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Node.js launch failed", e)
            LaunchResult(config.targetUrl, RuntimeStopper {}, error = e.message ?: "Node launch error")
        }
    }

    private suspend fun launchWordpress(context: Context, config: ShellConfig): LaunchResult {
        return try {
            ensurePhpBinaryExtracted(context)

            val wpDir = File(context.filesDir, "wordpress_site")
            val marker = File(wpDir, ".wp_extracted")
            val token = buildExtractionToken(
                context,
                "wordpress",
                config.versionCode,
                config.wordpressConfig.siteTitle
            )
            if (shouldReextractAssets(marker, token)) {
                wpDir.deleteRecursively()
                extractAssetsRecursive(context, "wordpress", wpDir)
                writeExtractionMarker(marker, token)
            }

            val runtime = com.webtoapp.core.wordpress.WordPressPhpRuntime(context)
            val port = runtime.startServer(wpDir.absolutePath, config.wordpressConfig.phpPort)
            if (port <= 0) {
                runtime.stopServer()
                return LaunchResult(
                    config.targetUrl,
                    RuntimeStopper {},
                    error = "WordPress PHP server start failed"
                )
            }

            val baseUrl = "http://127.0.0.1:$port"
            if (config.wordpressConfig.autoInstall) {
                runCatching {
                    com.webtoapp.core.wordpress.WordPressManager.autoInstallIfNeeded(
                        baseUrl = baseUrl,
                        siteTitle = config.wordpressConfig.siteTitle,
                        adminUser = config.wordpressConfig.adminUser,
                        adminPassword = config.wordpressConfig.adminPassword,
                        adminEmail = config.wordpressConfig.adminEmail.ifBlank { "admin@localhost.local" },
                        siteLanguage = config.wordpressConfig.siteLanguage
                    )
                }.onFailure { AppLogger.w(TAG, "WordPress auto-install failed", it) }
            }
            runCatching {
                com.webtoapp.core.wordpress.WordPressManager.applyRuntimeConfig(
                    phpBinary = runtime.getPhpBinaryPath(),
                    projectDir = wpDir,
                    siteTitle = config.wordpressConfig.siteTitle,
                    permalinkStructure = config.wordpressConfig.permalinkStructure,
                    siteLanguage = config.wordpressConfig.siteLanguage,
                    themeName = config.wordpressConfig.themeName,
                    activePlugins = config.wordpressConfig.activePlugins
                )
            }.onFailure { AppLogger.w(TAG, "WordPress runtime config apply failed", it) }

            LaunchResult(
                resolvedUrl = baseUrl,
                stopper = RuntimeStopper { runtime.stopServer() }
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "WordPress launch failed", e)
            LaunchResult(config.targetUrl, RuntimeStopper {}, error = e.message ?: "WordPress launch error")
        }
    }

    private fun ensurePhpBinaryExtracted(context: Context) {
        if (com.webtoapp.core.wordpress.WordPressDependencyManager.isPhpReady(context)) return
        throw IOException(
            "PHP runtime missing: nativeLibraryDir/libphp.so 和 " +
                "wordpress_deps/php/<abi>/php 都不存在；用户需要先在 “运行时管理” " +
                "里安装 PHP。"
        )
    }
}
