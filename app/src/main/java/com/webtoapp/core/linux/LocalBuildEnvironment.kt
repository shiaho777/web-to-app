package com.webtoapp.core.linux

import android.content.Context
import com.google.gson.JsonObject
import com.webtoapp.core.frontend.PackageManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.nodejs.NodeDependencyManager
import com.webtoapp.util.GsonProvider
import com.webtoapp.util.destroyForciblyCompat
import com.webtoapp.util.waitForCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

object LocalBuildEnvironment {

    private const val TAG = "LocalBuildEnv"
    private const val NPM_VERSION = "10.9.0"
    private const val PNPM_VERSION = "9.15.9"
    private const val YARN_VERSION = "1.22.22"
    private const val PACKAGED_LAUNCHER_NAME = "libnode_launcher.so"
    private const val LEGACY_LAUNCHER_NAME = "node"
    private const val HTTP_CONNECT_TIMEOUT_MS = 30_000
    private const val HTTP_READ_TIMEOUT_MS = 120_000
    private val installMutex = Mutex()

    private val gson = GsonProvider.gson

    fun getRootDir(context: Context): File = File(context.filesDir, "local_build_env").also { it.mkdirs() }

    fun getBinDir(context: Context): File = File(getRootDir(context), "bin").also { it.mkdirs() }

    fun getToolDir(context: Context): File = File(getRootDir(context), "tools").also { it.mkdirs() }

    fun getNpmCacheDir(context: Context): File = File(getRootDir(context), "cache/npm").also { it.mkdirs() }

    fun getNpmPrefixDir(context: Context): File = File(getRootDir(context), "prefix").also { it.mkdirs() }

    fun getProjectsRoot(context: Context): File = File(context.filesDir, "frontend_builds").also { it.mkdirs() }

    fun getWorkRoot(context: Context): File = File(context.cacheDir, "frontend_build_work").also { it.mkdirs() }

    private fun getPackagedLauncherPath(context: Context): File =
        File(context.applicationInfo.nativeLibraryDir, PACKAGED_LAUNCHER_NAME)

    private fun getLegacyLauncherPath(context: Context): File =
        File(getBinDir(context), LEGACY_LAUNCHER_NAME)

    fun getLauncherPath(context: Context): File {
        val packagedLauncher = getPackagedLauncherPath(context)
        return if (packagedLauncher.exists()) packagedLauncher else getLegacyLauncherPath(context)
    }

    fun getNpmCliPath(context: Context): File =
        File(getToolDir(context), "npm/package/bin/npm-cli.js")

    fun getPnpmCliPath(context: Context): File =
        File(getToolDir(context), "pnpm/package/bin/pnpm.cjs")

    fun getYarnCliPath(context: Context): File =
        File(getToolDir(context), "yarn/package/bin/yarn.js")

    fun getNodeLibPath(context: Context): String? = NodeDependencyManager.getNodeLibraryPath(context)

    fun hasNodeLauncher(context: Context): Boolean = getLauncherPath(context).exists()

    fun isNodeReady(context: Context): Boolean = hasNodeLauncher(context) && getNodeLibPath(context) != null

    fun isNpmReady(context: Context): Boolean = getNpmCliPath(context).exists() && isNodeReady(context)

    fun isPnpmReady(context: Context): Boolean = getPnpmCliPath(context).exists() && isNodeReady(context)

    fun isYarnReady(context: Context): Boolean = getYarnCliPath(context).exists() && isNodeReady(context)

    fun isPhpReady(context: Context): Boolean =
        com.webtoapp.core.wordpress.WordPressDependencyManager.isPhpReady(context)

    fun getPhpExecutablePath(context: Context): String? {
        return if (isPhpReady(context)) {
            com.webtoapp.core.wordpress.WordPressDependencyManager.getPhpExecutablePath(context)
        } else null
    }

    fun isPythonReady(context: Context): Boolean =
        com.webtoapp.core.python.PythonDependencyManager.isPythonReady(context)

    fun getPythonExecutablePath(context: Context): String? {
        if (!isPythonReady(context)) return null
        return com.webtoapp.core.python.PythonDependencyManager.getPythonExecutablePath(context).takeIf { it.isNotBlank() }
    }

    private const val COMPOSER_VERSION = "2.8.5"

    private val COMPOSER_PHAR_URLS = listOf(
        "https://getcomposer.org/download/$COMPOSER_VERSION/composer.phar",
        "https://github.com/composer/composer/releases/download/$COMPOSER_VERSION/composer.phar"
    )

    private val IGNORED_PLATFORM_REQS = listOf(
        "ext-session",
        "ext-fileinfo",
        "ext-mbstring",
        "ext-tokenizer",
        "ext-xml",
        "ext-dom",
        "ext-simplexml",
        "ext-iconv",
        "ext-bcmath",
        "ext-gmp",
        "ext-intl",
        "ext-zip",
        "ext-pdo",
        "ext-pdo_mysql",
        "ext-pdo_sqlite",
        "ext-mysqli",
        "ext-curl",
        "ext-openssl",
        "ext-gd",
        "ext-exif"
    )

    fun getComposerPharPath(context: Context): File =
        File(getToolDir(context), "composer/composer.phar")

    private fun getComposerVersionMarker(context: Context): File =
        File(getToolDir(context), "composer/.installed-version")

    fun isComposerReady(context: Context): Boolean {
        if (!isPhpReady(context)) return false
        val phar = getComposerPharPath(context)
        if (!phar.exists()) return false
        val marker = getComposerVersionMarker(context)

        if (!marker.exists()) return false
        return runCatching { marker.readText().trim() }.getOrNull() == COMPOSER_VERSION
    }

    suspend fun ensureComposer(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (!isPhpReady(context)) {
            throw IOException(Strings.composerNeedsPhp)
        }
        val target = getComposerPharPath(context)
        val marker = getComposerVersionMarker(context)
        val installed = runCatching { marker.readText().trim() }.getOrNull()

        if (target.exists() && installed == COMPOSER_VERSION && verifyComposerVersion(context, target)) {
            return@withContext
        }

        var lastError: Exception? = null
        for ((idx, url) in COMPOSER_PHAR_URLS.withIndex()) {
            val sourceLabel = if (idx == 0) Strings.localBuildComposerSourceOfficial else Strings.localBuildComposerSourceMirror
            onProgress(Strings.localBuildDownloadComposer.format(COMPOSER_VERSION, sourceLabel), 0.5f)
            target.parentFile?.mkdirs()

            if (target.exists()) target.delete()
            marker.delete()
            try {
                downloadFile(url, target)
            } catch (e: Exception) {
                lastError = e
                continue
            }
            if (target.length() < 1_000_000) {
                lastError = IOException("$sourceLabel 下载内容过小（${target.length()} bytes），可能是错误页")
                target.delete()
                continue
            }

            if (!verifyComposerVersion(context, target)) {
                lastError = IOException("$sourceLabel 下载到的 phar 版本不匹配（期望 $COMPOSER_VERSION）")
                target.delete()
                continue
            }
            marker.writeText(COMPOSER_VERSION)
            return@withContext
        }
        throw IOException(
            "Composer 下载失败：所有镜像都未能给出 $COMPOSER_VERSION 的 phar。" +
                (lastError?.message?.let { "（$it）" } ?: "")
        )
    }

    private fun verifyComposerVersion(context: Context, phar: File): Boolean {
        return readComposerVersion(context, phar)?.contains(COMPOSER_VERSION) == true
    }

    private fun readComposerVersion(context: Context, phar: File): String? {
        val phpBin = getPhpExecutablePath(context) ?: return null
        return try {
            val execPrefix = com.webtoapp.core.wordpress.WordPressDependencyManager.buildPhpExecPrefix(context)
            val cmd = execPrefix + listOf(phar.absolutePath, "--version")
            val pb = ProcessBuilder(cmd)
            pb.redirectErrorStream(true)

            pb.environment()["USE_ZEND_ALLOC"] = "0"
            pb.environment()["TMPDIR"] = context.cacheDir.absolutePath
            val proc = pb.start()
            val out = proc.inputStream.bufferedReader().readText()
            val finished = proc.waitForCompat(5_000)
            if (!finished) {
                proc.destroyForciblyCompat()
                return null
            }
            out.trim().takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun ensurePhpRuntime(
        context: Context,
        @Suppress("UNUSED_PARAMETER") onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (isPhpReady(context)) return@withContext

        onProgress(Strings.localBuildDownloadPhp, 0.05f)
        val success = com.webtoapp.core.wordpress.WordPressDependencyManager.downloadPhpDependency(context)
        if (!success) {
            throw IOException(Strings.phpRuntimeDownloadFailed)
        }
    }

    suspend fun ensurePythonRuntime(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (isPythonReady(context)) return@withContext
        onProgress(Strings.localBuildDownloadPython, 0.05f)
        val ok = com.webtoapp.core.python.PythonDependencyManager.downloadPythonRuntime(context)
        if (!ok) throw IOException(Strings.pythonRuntimeDownloadFailed)
    }

    suspend fun installPhpDependencies(
        context: Context,
        projectDir: File,
        timeout: Long = TimeUnit.MINUTES.toMillis(15),
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {}
    ): ExecutionResult = withContext(Dispatchers.IO) {

        onOutput(Strings.composerStartInstall.format("BUILD-TAG-2025-05-24-A"))
        val composerJson = File(projectDir, "composer.json")
        if (!composerJson.exists()) {
            onOutput(Strings.composerNoComposerJson)
            return@withContext ExecutionResult(0, "no composer.json", "", 0)
        }
        val phpBin = getPhpExecutablePath(context)
            ?: return@withContext ExecutionResult(-1, "", Strings.composerPhpRuntimeNotReady, 0)
        val composer = getComposerPharPath(context)
        if (!composer.exists()) {
            return@withContext ExecutionResult(-1, "", Strings.composerPharNotInstalled, 0)
        }

        val phpVersion = readComposerVersion(context, composer)
        onOutput(Strings.composerCurrentPharVersion.format(phpVersion ?: Strings.composerVersionUnknown))
        val needUpgrade = phpVersion?.contains(COMPOSER_VERSION) != true
        if (needUpgrade) {
            onOutput(Strings.composerUpgradingPhar.format(COMPOSER_VERSION))
            runCatching { ensureComposer(context) }.onFailure { e ->
                onOutput(Strings.composerAutoUpgradeFailed.format(e.message ?: ""))
            }
        }

        val versionUpgraded = verifyComposerVersion(context, composer)
        onOutput(Strings.composerUpgradeStatus.format(COMPOSER_VERSION, versionUpgraded))
        val arguments = buildList {
            add(composer.absolutePath)
            add("install")
            add("--no-interaction")
            add("--no-progress")
            add("--no-scripts")
            add("--prefer-dist")

            for (req in IGNORED_PLATFORM_REQS) {
                add("--ignore-platform-req=$req")
            }

        }
        onOutput("php composer.phar ${arguments.drop(1).joinToString(" ")}")
        executePhp(
            context = context,
            arguments = arguments,
            workingDir = projectDir,
            env = env,
            timeout = timeout,
            onOutput = onOutput,
        )
    }

    suspend fun installPythonDependencies(
        context: Context,
        projectDir: File,
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {}
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val reqFile = File(projectDir, "requirements.txt")
        if (!reqFile.exists()) {
            onOutput(Strings.pipNoRequirementsTxt)
            return@withContext ExecutionResult(0, "no requirements.txt", "", 0)
        }
        if (!isPythonReady(context)) {
            return@withContext ExecutionResult(-1, "", "Python 运行时未就绪", 0)
        }
        val start = System.currentTimeMillis()
        val ok = com.webtoapp.core.python.PythonDependencyManager.installRequirements(
            context = context,
            projectDir = projectDir,
            extraEnv = env,
            onOutput = onOutput,
        )
        val duration = System.currentTimeMillis() - start
        ExecutionResult(
            exitCode = if (ok) 0 else -1,
            stdout = "",
            stderr = if (ok) "" else "pip install 失败",
            duration = duration,
        )
    }

    suspend fun executePhp(
        context: Context,
        arguments: List<String>,
        workingDir: File,
        env: Map<String, String> = emptyMap(),
        timeout: Long = TimeUnit.MINUTES.toMillis(10),
        onOutput: (String) -> Unit = {},
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val phpBin = getPhpExecutablePath(context)
            ?: return@withContext ExecutionResult(-1, "", "PHP 运行时未就绪", 0)
        val start = System.currentTimeMillis()

        val execPrefix = com.webtoapp.core.wordpress.WordPressDependencyManager.buildPhpExecPrefix(context)
        val command = mutableListOf<String>()
        command.addAll(execPrefix)
        if (env["PHP_INI_OVERRIDES_OFF"] != "1") {
            command += phpIniArgs()
        }
        command.addAll(arguments)

        val pb = ProcessBuilder(command)
        pb.directory(workingDir)
        pb.redirectErrorStream(false)
        val processEnv = pb.environment()
        processEnv["HOME"] = getRootDir(context).absolutePath
        processEnv["TMPDIR"] = context.cacheDir.absolutePath
        processEnv["COMPOSER_HOME"] = File(getRootDir(context), "composer").absolutePath
        processEnv["COMPOSER_CACHE_DIR"] = File(getRootDir(context), "cache/composer").absolutePath
        processEnv["COMPOSER_NO_INTERACTION"] = "1"

        processEnv["COMPOSER_MEMORY_LIMIT"] = "-1"

        processEnv["COMPOSER_DISABLE_XDEBUG_WARN"] = "1"

        processEnv["COMPOSER_ALLOW_SUPERUSER"] = "1"

        processEnv["USE_ZEND_ALLOC"] = "0"
        env.forEach { (k, v) ->
            if (k != "PHP_INI_OVERRIDES_OFF") processEnv[k] = v
        }

        val process = pb.start()
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val tOut = Thread {
            process.inputStream.bufferedReader().forEachLine { line ->
                stdout.appendLine(line)
                onOutput(line)
            }
        }
        val tErr = Thread {
            process.errorStream.bufferedReader().forEachLine { line ->
                stderr.appendLine(line)
                onOutput(line)
            }
        }
        tOut.start(); tErr.start()
        val completed = process.waitForCompat(timeout)
        tOut.join(2_000); tErr.join(2_000)
        val exit = if (completed) process.exitValue() else { process.destroyForciblyCompat(); -1 }
        ExecutionResult(exit, stdout.toString(), stderr.toString(), System.currentTimeMillis() - start)
    }

    private fun phpIniArgs(): List<String> {
        val pairs = listOf(

            "memory_limit" to "2048M",

            "opcache.enable" to "0",
            "opcache.enable_cli" to "0",
            "opcache.jit" to "disable",
            "opcache.jit_buffer_size" to "0",

            "pcre.jit" to "0",

            "zend.max_call_depth" to "0",

            "zend.assertions" to "-1",

            "max_execution_time" to "0",

        )
        val out = mutableListOf<String>()
        for ((k, v) in pairs) {
            out += "-d"
            out += "$k=$v"
        }
        return out
    }

    suspend fun ensureInstalled(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        installMutex.withLock {
            ensureNodeLauncher(context, onProgress)
            ensureNpm(context, onProgress)
            ensurePnpm(context, onProgress)
            ensureYarn(context, onProgress)
        }
    }

    suspend fun ensureNodeLauncher(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (!NodeDependencyManager.isNodeReady(context)) {
            onProgress(Strings.localBuildDownloadNode, 0.05f)
            val success = NodeDependencyManager.downloadNodeRuntime(context)
            if (!success) {
                throw IOException(Strings.nodeRuntimeDownloadFailed)
            }
        }

        val packagedLauncher = getPackagedLauncherPath(context)
        if (packagedLauncher.exists()) {
            return@withContext
        }

        val legacyLauncher = getLegacyLauncherPath(context)
        if (legacyLauncher.exists() && legacyLauncher.canExecute()) {
            AppLogger.w(TAG, "使用旧版 node 启动器路径: ${legacyLauncher.absolutePath}")
            return@withContext
        }

        onProgress(Strings.localBuildPreparingNodeLauncher, 0.12f)
        throw IOException(Strings.nodeLauncherNotPackaged.format(packagedLauncher.absolutePath))
    }

    suspend fun ensureNpm(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (getNpmCliPath(context).exists()) return@withContext
        onProgress(Strings.localBuildInstallingNpm, 0.35f)
        installTarballPackage(
            context = context,
            tarballUrl = "https://registry.npmjs.org/npm/-/npm-$NPM_VERSION.tgz",
            targetDir = File(getToolDir(context), "npm")
        )
    }

    suspend fun ensurePnpm(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (getPnpmCliPath(context).exists()) return@withContext
        onProgress(Strings.localBuildInstallingPnpm, 0.6f)
        installTarballPackage(
            context = context,
            tarballUrl = "https://registry.npmjs.org/pnpm/-/pnpm-$PNPM_VERSION.tgz",
            targetDir = File(getToolDir(context), "pnpm")
        )
    }

    suspend fun ensureYarn(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        if (getYarnCliPath(context).exists()) return@withContext
        onProgress(Strings.localBuildInstallingYarn, 0.8f)
        installTarballPackage(
            context = context,
            tarballUrl = "https://registry.npmjs.org/yarn/-/yarn-$YARN_VERSION.tgz",
            targetDir = File(getToolDir(context), "yarn")
        )
    }

    suspend fun detectToolVersion(context: Context, tool: BuildTool): String? = withContext(Dispatchers.IO) {

        runCatching {
            when (tool) {
                BuildTool.NODE, BuildTool.NPM, BuildTool.PNPM, BuildTool.YARN -> {
                    if (!isNodeReady(context)) return@runCatching null
                    val args = when (tool) {
                        BuildTool.NODE -> listOf("--version")
                        BuildTool.NPM -> listOf(getNpmCliPath(context).absolutePath, "--version")
                        BuildTool.PNPM -> listOf(getPnpmCliPath(context).absolutePath, "--version")
                        BuildTool.YARN -> listOf(getYarnCliPath(context).absolutePath, "--version")
                        else -> return@runCatching null
                    }
                    val result = executeNode(context, args, context.filesDir)
                    if (result.exitCode == 0) result.stdout.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { null } else null
                }
                BuildTool.PHP -> {
                    if (!isPhpReady(context)) return@runCatching null
                    val result = executePhp(context, listOf("-v"), context.filesDir)
                    if (result.exitCode == 0) {
                        Regex("""PHP\s+(\S+)""").find(result.stdout)?.groupValues?.get(1)
                    } else null
                }
                BuildTool.COMPOSER -> {
                    if (!isComposerReady(context)) return@runCatching null
                    val composer = getComposerPharPath(context)
                    val result = executePhp(context, listOf(composer.absolutePath, "--version", "--no-ansi"), context.filesDir)
                    if (result.exitCode == 0) {
                        Regex("""Composer\s+version\s+(\S+)""").find(result.stdout)?.groupValues?.get(1)
                    } else null
                }
                BuildTool.PYTHON -> {
                    if (!isPythonReady(context)) return@runCatching null
                    val bin = getPythonExecutablePath(context) ?: return@runCatching null
                    File(bin).takeIf { it.exists() }?.let { "Python 3.x" }
                }
                BuildTool.PIP -> {
                    if (isPythonReady(context)) "pip (bundled)" else null
                }
            }
        }.getOrElse { e ->
            AppLogger.w(TAG, "detectToolVersion failed for $tool: ${e.message}")
            null
        }
    }

    suspend fun installDependencies(
        context: Context,
        projectDir: File,
        packageManager: PackageManager,
        cleanInstall: Boolean,
        timeout: Long,
        env: Map<String, String>,
        onOutput: (String) -> Unit
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val packageLock = File(projectDir, "package-lock.json")
        val installArgs = when (packageManager) {
            PackageManager.PNPM -> listOf(getPnpmCliPath(context).absolutePath, "install", "--prod=false")
            PackageManager.YARN -> listOf(getYarnCliPath(context).absolutePath, "install")
            else -> {
                if (cleanInstall && packageLock.exists()) {
                    listOf(getNpmCliPath(context).absolutePath, "ci")
                } else {
                    listOf(getNpmCliPath(context).absolutePath, "install")
                }
            }
        }
        executeNode(
            context = context,
            arguments = installArgs,
            workingDir = projectDir,
            env = env,
            timeout = timeout,
            onOutput = onOutput
        )
    }

    suspend fun runPackageScript(
        context: Context,
        projectDir: File,
        packageManager: PackageManager,
        scriptName: String,
        timeout: Long,
        env: Map<String, String>,
        onOutput: (String) -> Unit
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val packageJson = File(projectDir, "package.json")
        if (!packageJson.exists()) {
            return@withContext ExecutionResult(-1, "", "package.json 不存在", 0)
        }
        val script = parsePackageScript(packageJson, scriptName)
            ?: return@withContext ExecutionResult(-1, "", "未找到脚本: $scriptName", 0)
        AppLogger.d(TAG, "run script $scriptName: $script")

        val args = when (packageManager) {
            PackageManager.PNPM -> listOf(getPnpmCliPath(context).absolutePath, "run", scriptName)
            PackageManager.YARN -> listOf(getYarnCliPath(context).absolutePath, "run", scriptName)
            else -> listOf(getNpmCliPath(context).absolutePath, "run", scriptName)
        }
        executeNode(
            context = context,
            arguments = args,
            workingDir = projectDir,
            env = env,
            timeout = timeout,
            onOutput = onOutput
        )
    }

    suspend fun executeCommand(
        context: Context,
        command: String,
        args: List<String>,
        workingDir: File,
        env: Map<String, String>,
        timeout: Long,
        onOutput: (String) -> Unit = {}
    ): ExecutionResult = withContext(Dispatchers.IO) {
        when (command) {
            "node" -> executeNode(context, args, workingDir, env, timeout, onOutput)
            "npm" -> executeNode(context, listOf(getNpmCliPath(context).absolutePath) + args, workingDir, env, timeout, onOutput)
            "pnpm" -> executeNode(context, listOf(getPnpmCliPath(context).absolutePath) + args, workingDir, env, timeout, onOutput)
            "yarn" -> executeNode(context, listOf(getYarnCliPath(context).absolutePath) + args, workingDir, env, timeout, onOutput)
            "esbuild" -> NativeNodeEngine.executeEsbuild(context, args, workingDir, env, timeout, onOutput)
            "php" -> executePhp(context, args, workingDir, env, timeout, onOutput)
            "composer" -> executePhp(
                context,
                listOf(getComposerPharPath(context).absolutePath) + args,
                workingDir, env, timeout, onOutput,
            )
            else -> ExecutionResult(-1, "", "不支持的命令: $command", 0)
        }
    }

    suspend fun executeNode(
        context: Context,
        arguments: List<String>,
        workingDir: File,
        env: Map<String, String> = emptyMap(),
        timeout: Long = TimeUnit.MINUTES.toMillis(10),
        onOutput: (String) -> Unit = {}
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val launcher = getLauncherPath(context)
        val nodeLib = getNodeLibPath(context)
            ?: return@withContext ExecutionResult(-1, "", "Node.js 运行时未就绪", 0)
        if (!launcher.exists()) {
            return@withContext ExecutionResult(-1, "", "node 启动器未安装", 0)
        }

        val start = System.currentTimeMillis()
        val command = mutableListOf(launcher.absolutePath)
        command.addAll(arguments)

        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(workingDir)
        processBuilder.redirectErrorStream(false)

        val processEnv = processBuilder.environment()
        val rootDir = getRootDir(context)
        val originalPath = processEnv["PATH"].orEmpty()
        processEnv["HOME"] = rootDir.absolutePath
        processEnv["TMPDIR"] = context.cacheDir.absolutePath
        processEnv["WTA_NODE_LIB"] = nodeLib

        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        val existingLdPath = processEnv["LD_LIBRARY_PATH"].orEmpty()
        processEnv["LD_LIBRARY_PATH"] = if (existingLdPath.isBlank()) {
            nativeLibDir
        } else {
            "$nativeLibDir${File.pathSeparator}$existingLdPath"
        }
        processEnv["NODE_PATH"] = buildNodePath(context, workingDir)
        processEnv["PATH"] = listOf(
            getBinDir(context).absolutePath,
            File(workingDir, "node_modules/.bin").absolutePath,
            File(getNpmPrefixDir(context), "bin").absolutePath,
            originalPath
        ).filter { it.isNotBlank() }.joinToString(File.pathSeparator)
        processEnv["npm_config_cache"] = getNpmCacheDir(context).absolutePath
        processEnv["npm_config_prefix"] = getNpmPrefixDir(context).absolutePath
        processEnv["npm_config_userconfig"] = File(rootDir, "npmrc").absolutePath
        processEnv["COREPACK_ENABLE_AUTO_PIN"] = "0"
        processEnv["CI"] = "1"
        env.forEach { (key, value) -> processEnv[key] = value }

        AppLogger.d(TAG, "exec: ${command.joinToString(" ")}")
        val process = processBuilder.start()

        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stdoutThread = Thread {
            process.inputStream.bufferedReader().forEachLine { line ->
                stdout.appendLine(line)
                onOutput(line)
            }
        }
        val stderrThread = Thread {
            process.errorStream.bufferedReader().forEachLine { line ->
                stderr.appendLine(line)
                onOutput(line)
            }
        }
        stdoutThread.start()
        stderrThread.start()

        val completed = process.waitForCompat(timeout)
        stdoutThread.join(2_000)
        stderrThread.join(2_000)

        val exitCode = if (completed) process.exitValue() else {
            process.destroyForciblyCompat()
            -1
        }
        ExecutionResult(
            exitCode = exitCode,
            stdout = stdout.toString(),
            stderr = stderr.toString(),
            duration = System.currentTimeMillis() - start
        )
    }

    suspend fun reset(context: Context) = withContext(Dispatchers.IO) {
        getRootDir(context).deleteRecursively()
        getWorkRoot(context).deleteRecursively()
    }

    suspend fun clearCache(context: Context): Long = withContext(Dispatchers.IO) {
        val dirs = listOf(getNpmCacheDir(context), getWorkRoot(context))
        var total = 0L
        dirs.forEach { dir ->
            total += dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            dir.deleteRecursively()
        }
        total
    }

    private fun buildNodePath(context: Context, workingDir: File): String {
        val paths = linkedSetOf<String>()
        paths += File(workingDir, "node_modules").absolutePath
        paths += File(getNpmPrefixDir(context), "lib/node_modules").absolutePath
        listOf("npm", "pnpm", "yarn").forEach { tool ->
            paths += File(getToolDir(context), "$tool/package/node_modules").absolutePath
        }
        return paths.joinToString(File.pathSeparator)
    }

    private fun parsePackageScript(packageJson: File, scriptName: String): String? {
        val json = gson.fromJson(packageJson.readText(), JsonObject::class.java)
        val scripts = json.getAsJsonObject("scripts") ?: return null
        return scripts.get(scriptName)?.asString
    }

    private suspend fun installTarballPackage(
        context: Context,
        tarballUrl: String,
        targetDir: File
    ) = withContext(Dispatchers.IO) {
        targetDir.parentFile?.mkdirs()
        if (targetDir.exists()) targetDir.deleteRecursively()
        val tempFile = File.createTempFile("wta-tool", ".tgz", context.cacheDir)
        try {
            downloadFile(tarballUrl, tempFile)
            extractTarGz(tempFile, targetDir)
        } finally {
            tempFile.delete()
        }
    }

    private fun downloadFile(url: String, targetFile: File) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = HTTP_CONNECT_TIMEOUT_MS
        connection.readTimeout = HTTP_READ_TIMEOUT_MS
        connection.instanceFollowRedirects = true
        connection.inputStream.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun extractTarGz(archive: File, destinationDir: File) {
        destinationDir.mkdirs()
        GZIPInputStream(FileInputStream(archive)).use { gzip ->
            TarArchiveInputStream(gzip).use { tar ->
                var entry = tar.nextTarEntry
                while (entry != null) {
                    val dest = File(destinationDir, entry.name.removePrefix("./"))
                    if (entry.isDirectory) {
                        dest.mkdirs()
                    } else {
                        dest.parentFile?.mkdirs()
                        FileOutputStream(dest).use { output -> tar.copyTo(output) }
                        if ((entry.mode and 0b001_001_001) != 0) {
                            dest.setExecutable(true, false)
                        }
                    }
                    entry = tar.nextTarEntry
                }
            }
        }
    }

}

enum class BuildTool {
    NODE,
    NPM,
    PNPM,
    YARN,
    PHP,
    COMPOSER,
    PYTHON,
    PIP
}
