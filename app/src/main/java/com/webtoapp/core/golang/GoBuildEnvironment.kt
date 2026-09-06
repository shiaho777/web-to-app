package com.webtoapp.core.golang

import android.content.Context
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.linux.LocalDnsBridgeProxy
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.destroyForciblyCompat
import com.webtoapp.util.waitForCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

data class GoExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Long,
)

object GoBuildEnvironment {

    private const val TAG = "GoBuildEnvironment"

    fun isGoReady(context: Context): Boolean = GoToolchainManager.isGoReady(context)

    suspend fun executeGo(
        context: Context,
        arguments: List<String>,
        workingDir: File,
        env: Map<String, String> = emptyMap(),
        timeout: Long = TimeUnit.MINUTES.toMillis(30),
        onOutput: (String) -> Unit = {},
    ): GoExecutionResult = withContext(Dispatchers.IO) {
        if (!isGoReady(context)) {
            return@withContext GoExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = Strings.goBuildStreamToolchainNotReady,
                durationMs = 0,
            )
        }

        if (!workingDir.exists() || !workingDir.isDirectory) {

            return@withContext GoExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = Strings.workingDirNotFound.format(workingDir.absolutePath),
                durationMs = 0,
            )
        }

        val start = System.currentTimeMillis()
        val goBin = GoToolchainManager.getGoBinary(context)
        val command = buildList {
            add(goBin.absolutePath)
            addAll(arguments)
        }
        AppLogger.d(TAG, "exec: ${command.joinToString(" ")} (cwd=${workingDir.absolutePath})")

        // Route through HostProcessLauncher: plain fork+exec where the
        // platform allows it (generated APKs, targetSdk 28), otherwise the
        // user-mode static exec loader (host targetSdk>=29 W^X). A raw
        // ProcessBuilder.start() here throws error=13 and crashed the host
        // (FATAL in GoBuildInAppCard); the launcher degrades to a diagnostic.
        val launchEnv = mutableMapOf<String, String>()
        configureEnvironment(context, launchEnv)
        return@withContext runCommand(
            context, command, workingDir, launchEnv, env, timeout, start, onOutput
        )
    }

    /**
     * Launch a toolchain TOOL binary directly (compile/asm/link), bypassing
     * the `go` driver prepend. Same launcher/plumbing as [executeGo]; used by
     * the on-device direct build driver where every launch must stay
     * single-shot (no child ever execs).
     */
    suspend fun executeTool(
        context: Context,
        tool: File,
        arguments: List<String>,
        workingDir: File,
        env: Map<String, String> = emptyMap(),
        timeout: Long = TimeUnit.MINUTES.toMillis(30),
        onOutput: (String) -> Unit = {},
    ): GoExecutionResult = withContext(Dispatchers.IO) {
        if (!isGoReady(context)) {
            return@withContext GoExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = Strings.goBuildStreamToolchainNotReady,
                durationMs = 0,
            )
        }
        if (!workingDir.exists() || !workingDir.isDirectory) {
            return@withContext GoExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = Strings.workingDirNotFound.format(workingDir.absolutePath),
                durationMs = 0,
            )
        }
        val start = System.currentTimeMillis()
        val command = buildList {
            add(tool.absolutePath)
            addAll(arguments)
        }
        AppLogger.d(TAG, "exec tool: ${tool.name} (cwd=${workingDir.absolutePath})")
        val launchEnv = mutableMapOf<String, String>()
        configureEnvironment(context, launchEnv)
        return@withContext runCommand(
            context, command, workingDir, launchEnv, env, timeout, start, onOutput
        )
    }

    private suspend fun runCommand(
        context: Context,
        command: List<String>,
        workingDir: File,
        launchEnv: MutableMap<String, String>,
        extraEnv: Map<String, String>,
        timeout: Long,
        start: Long,
        onOutput: (String) -> Unit,
    ): GoExecutionResult {
        val proxyPort = LocalDnsBridgeProxy.start()
        if (proxyPort > 0) {
            LocalDnsBridgeProxy.proxyEnvFor(proxyPort).forEach { (k, v) -> launchEnv[k] = v }
            onOutput("[env] DNS bridge proxy=http://127.0.0.1:$proxyPort")
        } else {
            // Without the bridge, toolchain network ops (module/sumdb fetch)
            // have no working DNS: stock Go only reads /etc/resolv.conf,
            // which does not exist on Android. Surface it instead of failing
            // later with a cryptic "connection refused".
            AppLogger.w(TAG, "DNS bridge unavailable; toolchain network ops may fail")
            onOutput("[go] warning: local DNS bridge unavailable, network operations may fail")
        }
        extraEnv.forEach { (k, v) -> launchEnv[k] = v }
        return try {
            val launch = com.webtoapp.core.linux.HostProcessLauncher.start(
                context, command, launchEnv, workingDir, "Go"
            )
            val proc = launch.process
            if (proc == null) {
                val msg = launch.error
                    ?: Strings.goBuildStreamToolchainNotReady
                AppLogger.e(TAG, "go launch refused: $msg")
                return GoExecutionResult(
                    exitCode = -1,
                    stdout = "",
                    stderr = msg,
                    durationMs = System.currentTimeMillis() - start,
                )
            }
            val stdoutBuf = StringBuilder()
            val stderrBuf = StringBuilder()
            val tOut = Thread {
                proc.inputStream.bufferedReader().forEachLine { line ->
                    stdoutBuf.appendLine(line)
                    onOutput(line)
                }
            }
            val tErr = Thread {
                proc.errorStream.bufferedReader().forEachLine { line ->
                    stderrBuf.appendLine(line)
                    onOutput(line)
                }
            }
            tOut.start(); tErr.start()
            val finished = proc.waitForCompat(timeout)
            tOut.join(2_000); tErr.join(2_000)
            val exit = if (finished) proc.exitValue() else {
                proc.destroyForciblyCompat()
                -1
            }
            GoExecutionResult(
                exitCode = exit,
                stdout = stdoutBuf.toString(),
                stderr = stderrBuf.toString(),
                durationMs = System.currentTimeMillis() - start,
            )
        } catch (e: Exception) {
            // Never let a spawn failure escape: callers (build cards, preview)
            // treat a null/exit!=0 result as a build error, but an uncaught
            // IOException here used to FATAL the host process.
            AppLogger.e(TAG, "go exec failed: ${e.message}", e)
            GoExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = (e.message ?: "go exec failed"),
                durationMs = System.currentTimeMillis() - start,
            )
        } finally {
            if (proxyPort > 0) {
                LocalDnsBridgeProxy.stop()
            }
        }
    }

    fun configureEnvironment(context: Context, processEnv: MutableMap<String, String>) {
        val goRoot = GoToolchainManager.getGoRoot(context)
        val goPath = GoToolchainManager.getGoPath(context)
        val buildCache = GoToolchainManager.getBuildCacheDir(context)

        processEnv["GOROOT"] = goRoot.absolutePath
        processEnv["GOPATH"] = goPath.absolutePath
        processEnv["GOCACHE"] = buildCache.absolutePath
        processEnv["GOMODCACHE"] = GoToolchainManager.getModCacheDir(context).absolutePath
        processEnv["HOME"] = context.filesDir.absolutePath
        val tempDir = GoToolchainManager.selectTempDir(context)
        processEnv["TMPDIR"] = tempDir.absolutePath
        processEnv["GOTMPDIR"] = tempDir.absolutePath

        processEnv["GOOS"] = processEnv["GOOS"] ?: "android"
        processEnv["GOARCH"] = processEnv["GOARCH"] ?: "arm64"

        processEnv["CGO_ENABLED"] = processEnv["CGO_ENABLED"] ?: "0"

        processEnv["GO111MODULE"] = processEnv["GO111MODULE"] ?: "on"

        processEnv["GOPROXY"] = processEnv["GOPROXY"] ?: "https://goproxy.cn,direct"
        processEnv["GOSUMDB"] = processEnv["GOSUMDB"] ?: "sum.golang.google.cn"
        GoDependencyManager.ensureTrustedCaBundle(context)?.let { bundle ->
            processEnv["SSL_CERT_FILE"] = processEnv["SSL_CERT_FILE"] ?: bundle.absolutePath
        }

        processEnv["GOBIN"] = File(goPath, "bin").absolutePath

        val originalPath = processEnv["PATH"].orEmpty()
        processEnv["PATH"] = listOf(
            File(goRoot, "bin").absolutePath,
            File(goPath, "bin").absolutePath,
            originalPath,
        ).filter { it.isNotBlank() }.joinToString(File.pathSeparator)
    }

    fun looksLikeNoSpace(stderr: String, stdout: String = ""): Boolean {
        val blob = (stderr + "\n" + stdout).lowercase()
        return "no space left on device" in blob ||
            "enospc" in blob ||
            "not enough space" in blob ||
            "disk quota exceeded" in blob
    }

    suspend fun buildProject(
        context: Context,
        projectDir: File,
        binaryName: String,
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {},
    ): File? = withContext(Dispatchers.IO) {
        val name = binaryName.ifBlank { projectDir.name }

        val tempDir = GoToolchainManager.prepareForBuild(context)
        onOutput("[env] TMPDIR=${tempDir.absolutePath} free≈${GoToolchainManager.usableBytes(tempDir) / 1024 / 1024}MB")
        if (GoToolchainManager.usableBytes(tempDir) < 64L * 1024 * 1024) {
            onOutput("[go] ${Strings.goBuildNoSpace}")
            AppLogger.e(TAG, "insufficient free space for Go build under ${tempDir.absolutePath}")
            return@withContext null
        }

        val lastStderr = StringBuilder()
        val wrapOutput: (String) -> Unit = { line ->
            lastStderr.appendLine(line)
            onOutput(line)
        }

        val first = buildProjectOnce(context, projectDir, name, env, wrapOutput)
        if (first != null) return@withContext first

        if (!looksLikeNoSpace(lastStderr.toString())) {
            return@withContext null
        }

        onOutput("[go] ${Strings.goBuildNoSpace}")
        val freed = GoToolchainManager.clearBuildArtifactCaches(context)
        onOutput("[go] ${Strings.goBuildStreamCleaningCache.format((freed / 1024 / 1024).coerceAtLeast(0).toInt())}")
        onOutput("[go] ${Strings.goBuildStreamRetryAfterClean}")
        lastStderr.clear()
        val second = buildProjectOnce(context, projectDir, name, env, wrapOutput)
        if (second == null && looksLikeNoSpace(lastStderr.toString())) {
            onOutput("[go] ${Strings.goBuildNoSpace}")
        }
        second
    }

    private suspend fun buildProjectOnce(
        context: Context,
        projectDir: File,
        name: String,
        env: Map<String, String>,
        onOutput: (String) -> Unit,
    ): File? {
        val envProbe = executeGo(
            context = context,
            arguments = listOf("env", "GOPROXY", "GOSUMDB", "GO111MODULE", "GOFLAGS"),
            workingDir = projectDir,
            env = env,
        )
        envProbe.stdout.lineSequence()
            .filter { it.isNotBlank() }
            .forEachIndexed { idx, line ->
                val key = listOf("GOPROXY", "GOSUMDB", "GO111MODULE", "GOFLAGS").getOrNull(idx) ?: "ENV"
                onOutput("[env] $key=$line")
            }

        val goMod = File(projectDir, "go.mod")

        val vendorDir = File(projectDir, "vendor")
        val hasVendor = vendorDir.isDirectory && vendorDir.list().isNullOrEmpty().not()
        if (hasVendor) {
            onOutput("[go] vendor/ found, building offline (-mod=vendor)")
            AppLogger.i(TAG, "Go 项目带 vendor/，走离线 build 路径")
        } else if (goMod.exists()) {

            onOutput("[go] 1/3 解析依赖图 (go mod tidy)")
            val tidyResult = executeGo(
                context = context,
                arguments = listOf("mod", "tidy"),
                workingDir = projectDir,
                env = env,
                onOutput = onOutput,
            )
            if (tidyResult.exitCode != 0) {

                AppLogger.e(TAG, "go mod tidy failed exit=${tidyResult.exitCode}\n${tidyResult.stderr}")
                onOutput("[go] ${Strings.goBuildStreamGoModTidyFailed.format(tidyResult.exitCode)}")
                tidyResult.stderr.lineSequence()
                    .filter { it.isNotBlank() }
                    .forEach { onOutput("[stderr] $it") }

                onOutput("[hint] 网络受限时可在电脑上跑 `go mod vendor`，把生成的 vendor/ 一并导入项目即可离线构建")
                return null
            }
            onOutput("[go] 2/3 预下载依赖 (go mod download)")
            val downloadResult = executeGo(
                context = context,
                arguments = listOf("mod", "download"),
                workingDir = projectDir,
                env = env,
                onOutput = onOutput,
            )
            if (downloadResult.exitCode != 0) {
                AppLogger.w(
                    TAG,
                    "go mod download 退出码 ${downloadResult.exitCode},将交给后续 go build 重试:\n${downloadResult.stderr}"
                )
                onOutput("[go] (mod download 非阻塞失败,继续 build 阶段)")
            }
        } else {
            onOutput("[go] ${Strings.goBuildStreamNoGoMod}")
        }

        val binDir = File(projectDir, "bin").also { it.mkdirs() }
        val output = File(binDir, name)

        // Host builds (targetSdk>=29) cannot fork+exec the toolchain at all,
        // so the multi-process `go build` is unreachable there: replay its
        // tool invocations directly (single-shot launches only).
        if (!com.webtoapp.core.linux.RuntimeExecPolicy.canExecAppDataBinaries(context)) {
            onOutput("[go] 3/3 编译 (on-device direct driver)")
            val produced = GoDirectBuilder.build(
                context = context,
                projectDir = projectDir,
                binaryName = name,
                env = env,
                onOutput = onOutput,
            )
            if (produced == null || !produced.exists()) {
                onOutput("[go] ${Strings.goBuildStreamFailedExit.format(-1)}")
                return null
            }
            // Normalize to the historical output location.
            if (produced.absolutePath != output.absolutePath) {
                produced.copyTo(output, overwrite = true)
            }
            output.setExecutable(true, false)
            AppLogger.i(TAG, "Go 项目构建成功: ${output.absolutePath} (${output.length() / 1024} KB)")
            return output
        }

        val buildArgs = if (hasVendor) {
            listOf("build", "-mod=vendor", "-o", output.absolutePath, "./...")
        } else {
            listOf("build", "-o", output.absolutePath, "./...")
        }
        onOutput("[go] 3/3 编译 (go build ${buildArgs.joinToString(" ")})")
        val buildResult = executeGo(
            context = context,
            arguments = buildArgs,
            workingDir = projectDir,
            env = env,
            onOutput = onOutput,
        )
        if (buildResult.exitCode != 0 || !output.exists()) {
            AppLogger.e(TAG, "go build failed: exit=${buildResult.exitCode}\n${buildResult.stderr}")

            if (looksLikeNoSpace(buildResult.stderr, buildResult.stdout)) {
                onOutput("[go] ${Strings.goBuildNoSpace}")
            }
            onOutput("[go] ${Strings.goBuildStreamFailedExit.format(buildResult.exitCode)}")
            buildResult.stderr.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { onOutput("[stderr] $it") }
            return null
        }
        output.setExecutable(true, false)
        AppLogger.i(TAG, "Go 项目构建成功: ${output.absolutePath} (${output.length() / 1024} KB)")
        return output
    }
}
