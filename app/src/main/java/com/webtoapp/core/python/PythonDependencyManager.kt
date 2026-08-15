package com.webtoapp.core.python

import android.content.Context
import android.os.Build
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.download.DependencyDownloadNotification
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.destroyForciblyCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PythonDependencyManager {

    private const val TAG = "PythonDependencyManager"
    private val ANDROID_UNSUPPORTED_REQUIREMENTS = setOf(
        "uvloop",
        "httptools",
        "watchfiles"
    )
    private val UVICORN_EXTRAS_REGEX = Regex(
        """^(\s*)uvicorn\s*\[[^]]+](\s*.*)$""",
        RegexOption.IGNORE_CASE
    )

    /** pip reports this when no wheel matches the interpreter/platform, or the index is unreachable. */
    private val NO_DISTRIBUTION_RE = Regex(
        """(?:No matching distribution found for|Could not find a version that satisfies the requirement)\s+(\S+)"""
    )

    /** Evidence that the package index actually responded (downloads started). */
    private val INDEX_PROOF_RE = Regex("""Collecting \S+|Downloading \S+""")

    /** A source build failed while building a wheel (usually a missing compiler). */
    private val FAILED_BUILD_WHEEL_RE = Regex("""Failed building wheel for (\S+)""")

    /** A compiler invocation failed (no toolchain ships with the on-device Python). */
    private val COMPILER_MISSING_RE = Regex("""error: command '[^']*' failed""")

    const val PYTHON_VERSION = "3.14"
    const val PYTHON_FULL_VERSION = "3.14.6"
    private const val PYTHON_BUILD_TAG = "20260623"

    private const val SITE_CUSTOMIZE_FILE_NAME = "sitecustomize.py"

    /** Bump when [buildSitecustomizeScript] content changes so existing runtimes re-heal. */
    internal const val SITE_CUSTOMIZE_VERSION = 2

    /** Marker file under the runtime root recording the last applied heal. */
    private const val HEAL_MARKER_FILE_NAME = ".w2a-musl-heal"

    /** Cap on pip output retained for failure analysis (pattern matching). */
    private const val MAX_PIP_OUTPUT_CHARS = 512 * 1024

    private const val MUSL_VERSION = "1.2.5-r11"
    private const val MUSL_ALPINE_BRANCH = "v3.21"

    enum class MirrorRegion { CN, GLOBAL }

    private val GITHUB_CN_PROXIES = listOf(
        "https://ghfast.top/",
        "https://gh-proxy.com/"
    )

    private fun getPythonUrl(abi: String): String {

        val tripleMap = mapOf(
            "arm64-v8a"   to "aarch64-unknown-linux-musl",
            "x86_64"      to "x86_64-unknown-linux-musl",
            "armeabi-v7a" to "armv7-unknown-linux-gnueabihf",
            "x86"         to "x86_64-unknown-linux-musl"
        )
        val triple = tripleMap[abi] ?: "aarch64-unknown-linux-musl"

        return "https://github.com/astral-sh/python-build-standalone/releases/download/$PYTHON_BUILD_TAG/cpython-${PYTHON_FULL_VERSION}+${PYTHON_BUILD_TAG}-${triple}-install_only_stripped.tar.gz"
    }

    private fun getMuslLinkerUrl(abi: String): String? {
        val archMap = mapOf(
            "arm64-v8a"   to "aarch64",
            "x86_64"      to "x86_64",
            "x86"         to "x86_64"
        )
        val arch = archMap[abi] ?: return null
        return "https://dl-cdn.alpinelinux.org/alpine/$MUSL_ALPINE_BRANCH/main/$arch/musl-$MUSL_VERSION.apk"
    }

    fun getMuslLinkerName(abi: String): String {
        val archMap = mapOf(
            "arm64-v8a"   to "aarch64",
            "x86_64"      to "x86_64",
            "x86"         to "x86_64",
            "armeabi-v7a" to "armhf"
        )
        val arch = archMap[abi] ?: "aarch64"
        return "ld-musl-$arch.so.1"
    }

    data class MirrorConfig(
        val pythonUrls: List<String>,
        val muslLinkerUrl: String? = null
    )

    private fun getCnMirror(abi: String): MirrorConfig {
        val baseUrl = getPythonUrl(abi)
        val orderedProxies = com.webtoapp.core.network.CnMirrorProbe.getOrderedProxies(GITHUB_CN_PROXIES)
        return MirrorConfig(
            pythonUrls = orderedProxies.map { proxy -> "${proxy}${baseUrl}" } + baseUrl,
            muslLinkerUrl = getMuslLinkerUrl(abi)
        )
    }

    private fun getGlobalMirror(abi: String): MirrorConfig {
        return MirrorConfig(
            pythonUrls = listOf(getPythonUrl(abi)),
            muslLinkerUrl = getMuslLinkerUrl(abi)
        )
    }

    private const val MAX_RETRY_PER_URL = 2
    private const val RETRY_DELAY_MS = 2000L

    fun getVersionedPythonBinaryName(): String = "python$PYTHON_VERSION"

    fun getVersionedPythonLibraryName(): String = "libpython$PYTHON_VERSION.so.1.0"

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Float, val currentFile: String, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
        data class Extracting(val fileName: String) : DownloadState()
        object Complete : DownloadState()
        data class Error(val message: String, val retryable: Boolean = true) : DownloadState()
        data class Paused(val progress: Float, val currentFile: String, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    private var _userMirrorRegion: MirrorRegion? = null

    fun setMirrorRegion(region: MirrorRegion?) {
        _userMirrorRegion = region
    }

    fun getMirrorRegion(): MirrorRegion {
        _userMirrorRegion?.let { return it }
        val lang = Locale.getDefault().language
        return if (lang == "zh") MirrorRegion.CN else MirrorRegion.GLOBAL
    }

    fun getDepsDir(context: Context): File {
        return File(context.filesDir, "python_deps").also { it.mkdirs() }
    }

    fun getPythonDir(context: Context): File {
        return File(getDepsDir(context), "python").also { it.mkdirs() }
    }

    fun getProjectsDir(context: Context): File {
        return File(context.filesDir, "python_projects").also { it.mkdirs() }
    }

    fun getDeviceAbi(): String {
        return Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    }

    private fun resolvePythonBinary(context: Context): File? {
        val nativePython = File(context.applicationInfo.nativeLibraryDir, "libpython3.so")
        if (nativePython.exists() && nativePython.length() > 1024 * 1024) {
            return nativePython
        }

        val versionedBinaryName = getVersionedPythonBinaryName()
        val downloadedVersioned = File(getPythonDir(context), "bin/$versionedBinaryName")
        if (downloadedVersioned.exists() && downloadedVersioned.length() > 1024 * 1024) {
            return downloadedVersioned
        }

        val downloaded = File(getPythonDir(context), "bin/python3")
        if (downloaded.exists() && downloaded.length() > 1024 * 1024) {
            return downloaded
        }

        return null
    }

    private fun resolveMuslLinker(context: Context): File? {
        val nativeLinker = File(context.applicationInfo.nativeLibraryDir, "libmusl-linker.so")
        if (nativeLinker.exists() && nativeLinker.length() > 1024) {
            return nativeLinker
        }

        val abi = getDeviceAbi()
        val linkerName = getMuslLinkerName(abi)
        val downloadedLinker = File(getPythonDir(context), "lib/$linkerName")
        if (downloadedLinker.exists() && downloadedLinker.length() > 1024) {
            return downloadedLinker
        }

        return null
    }

    private fun resolveBuilderMuslLinker(context: Context): File? {

        val nativeLinker = File(context.applicationInfo.nativeLibraryDir, "libmusl-linker.so")
        if (nativeLinker.exists() && nativeLinker.length() > 1024 && nativeLinker.canExecute()) {
            return nativeLinker
        }

        val abi = getDeviceAbi()
        val linkerName = getMuslLinkerName(abi)
        val downloadedLinker = File(getPythonDir(context), "lib/$linkerName")
        if (downloadedLinker.exists() && downloadedLinker.length() > 1024 && downloadedLinker.canExecute()) {
            return downloadedLinker
        }
        return null
    }

    fun isPythonReady(context: Context): Boolean {
        return resolvePythonBinary(context) != null && resolveMuslLinker(context) != null
    }

    fun getPythonExecutablePath(context: Context): String {
        resolvePythonBinary(context)?.let { pythonBinary ->
            AppLogger.d(TAG, "Using Python: ${pythonBinary.absolutePath} (${pythonBinary.length() / 1024} KB)")
            return pythonBinary.absolutePath
        }

        val fallback = File(getPythonDir(context), "bin/python3")
        AppLogger.d(TAG, "Using downloaded Python (fallback): ${fallback.absolutePath}")
        return fallback.absolutePath
    }

    fun getPythonHome(context: Context): String {
        return getPythonDir(context).absolutePath
    }

    fun getMuslLinkerPath(context: Context): String? {
        return resolveMuslLinker(context)?.absolutePath
    }

    fun getBuilderMuslLinkerPath(context: Context): String? {
        return resolveBuilderMuslLinker(context)?.absolutePath
    }

    fun getPipPath(context: Context): String {
        return File(getPythonDir(context), "bin/pip3").absolutePath
    }

    fun hasInstalledPackages(sitePackagesDir: File): Boolean {
        if (!sitePackagesDir.exists() || !sitePackagesDir.isDirectory) return false
        return sitePackagesDir.walkTopDown()
            .drop(1)
            .any { it.isFile }
    }

    suspend fun downloadPythonRuntime(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = DownloadState.Idle
            DependencyDownloadNotification.getInstance(context)
            DependencyDownloadEngine.reset()

            val pythonReady = resolvePythonBinary(context) != null
            val muslReady = resolveMuslLinker(context) != null
            if (pythonReady && muslReady) {
                // Runtimes that persisted across an app update never received
                // the on-device fixes (they only used to run after a fresh
                // download) — heal them in place before declaring ready.
                ensureRuntimePatched(context)
                markComplete()
                return@withContext true
            }

            val abi = getDeviceAbi()
            val mirror = when (getMirrorRegion()) {
                MirrorRegion.CN -> getCnMirror(abi)
                MirrorRegion.GLOBAL -> getGlobalMirror(abi)
            }

            val success = when {
                !pythonReady -> downloadPython(context, mirror, abi)
                !muslReady -> {
                    val muslUrl = mirror.muslLinkerUrl
                    if (muslUrl == null) {
                        markError(String.format(com.webtoapp.core.i18n.Strings.pyRuntimeNoMuslForAbi, abi))
                        false
                    } else {
                        _downloadState.value = DownloadState.Extracting("musl linker")
                        downloadMuslLinker(context, muslUrl, abi)
                    }
                }
                else -> true
            }
            if (!success) return@withContext false

            if (!isPythonReady(context)) {
                markError(com.webtoapp.core.i18n.Strings.pyRuntimeDownloadIncomplete)
                return@withContext false
            }

            markComplete()
            AppLogger.i(TAG, "Python runtime download complete")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Downloading Python runtimefailed", e)
            markError(e.message ?: com.webtoapp.core.i18n.Strings.unknownError)
            false
        }
    }

    suspend fun installRequirements(
        context: Context,
        projectDir: File,
        extraEnv: Map<String, String> = emptyMap(),
        onOutput: ((String) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!com.webtoapp.core.linux.RuntimeExecPolicy.canExecAppDataBinaries(context)) {
            val msg = com.webtoapp.core.linux.RuntimeExecPolicy.hostPreviewBlockedMessage("Python")
            AppLogger.w(TAG, msg)
            onOutput?.invoke(msg)
            return@withContext false
        }

        val reqFile = File(projectDir, "requirements.txt")
        if (!reqFile.exists()) {
            AppLogger.i(TAG, "No requirements.txt, skipping dependency install")
            return@withContext true
        }

        val sitePackages = File(projectDir, ".pypackages")
        if (hasInstalledPackages(sitePackages)) {
            val existingPackages = sitePackages.listFiles()?.size ?: 0
            AppLogger.i(TAG, ".pypackages Already exists (${existingPackages} items)，skipping pip install")
            onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsAlreadyReady)
            return@withContext true
        }

        val pythonBin = getPythonExecutablePath(context)
        val pythonHome = getPythonHome(context)
        val muslLinker = getBuilderMuslLinkerPath(context)
        val installReqFile = prepareRequirementsFileForInstall(context, projectDir, reqFile, onOutput)

        try {
            sitePackages.mkdirs()
            onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsInstalling)

            if (muslLinker == null) {
                AppLogger.w(
                    TAG,
                    "musl linker missing at build time, can't pre-install Python dependencies: nativeLibraryDir=${context.applicationInfo.nativeLibraryDir}"
                )
                onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsBuilderMissingMusl)
                return@withContext false
            }

            // Heal runtimes downloaded by older app versions before pip runs:
            // pip's subprocess spawns and packaging's musl platform probe both
            // depend on the patched interpreter / sitecustomize bridge.
            ensureRuntimePatched(context)

            val result = runPipWithWrapper(context, projectDir, pythonBin, pythonHome, muslLinker,
                sitePackages, installReqFile, extraEnv, onOutput)

            if (result) {
                AppLogger.i(TAG, "Python dependency install succeeded")
                onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsInstallComplete)
            } else {
                AppLogger.e(TAG, "Python dependency install failed")
                onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsInstallFailed)
            }
            result
        } catch (e: Exception) {
            AppLogger.e(TAG, "Python dependency install exception", e)
            onOutput?.invoke(String.format(com.webtoapp.core.i18n.Strings.pyDepsInstallException, e.message ?: ""))
            false
        } finally {
            if (installReqFile != reqFile) {
                installReqFile.delete()
            }
        }
    }

    /**
     * The absolute loader path to bake into PT_INTERP: the loader co-located
     * with the runtime under `filesDir`, which is stable across app updates.
     * The `nativeLibraryDir` loader is deliberately not used — that path is
     * randomized on every app install/update, which would break embedded
     * interpreters after the next update.
     */
    fun resolvedMuslInterpPath(context: Context): String? {
        val linkerName = getMuslLinkerName(getDeviceAbi())
        val coLocated = File(getPythonDir(context), "lib/$linkerName")
        if (coLocated.isFile && coLocated.length() > 1024) return coLocated.absolutePath
        return null
    }

    /**
     * Idempotently applies the on-device compatibility fixes to an extracted
     * Python runtime: patches musl PT_INTERP paths this device cannot resolve
     * (the stock `/lib/ld-musl-*.so.1`, the `$ORIGIN` form written by the
     * 2.4.5 patcher, or a stale absolute path) to [resolvedInterp], and
     * refreshes the `sitecustomize.py` subprocess bridge. A marker file
     * short-circuits repeated runs.
     *
     * @return true when the runtime exists and is (now) healed.
     */
    internal fun healRuntimeInPlace(pythonHome: File, resolvedInterp: String): Boolean {
        val binDir = File(pythonHome, "bin")
        if (!binDir.isDirectory) return false

        val marker = File(pythonHome, HEAL_MARKER_FILE_NAME)
        val markerValue = "$resolvedInterp|v$SITE_CUSTOMIZE_VERSION"
        if (marker.isFile && marker.readText() == markerValue) {
            AppLogger.d(TAG, "Python runtime already healed: ${marker.absolutePath}")
            return true
        }

        val patchedCount = ElfInterpPatcher.patchPythonBinaries(pythonHome, resolvedInterp)
        if (patchedCount > 0) {
            AppLogger.i(TAG, "Patched $patchedCount Python ELF interpreter(s) to $resolvedInterp")
        }

        val siteCustomize = File(pythonHome, "lib/python$PYTHON_VERSION/$SITE_CUSTOMIZE_FILE_NAME")
        siteCustomize.parentFile?.mkdirs()
        siteCustomize.writeText(buildSitecustomizeScript())
        AppLogger.i(TAG, "Installed/refreshed $SITE_CUSTOMIZE_FILE_NAME for subprocess loader bridging: ${siteCustomize.absolutePath}")

        marker.writeText(markerValue)
        return true
    }

    /**
     * Heals a runtime installed by an older app version (or after a partially
     * failed download): the interp patch and sitecustomize bridge used to run
     * only right after a fresh download, so runtimes that persisted across an
     * app update never received them and pip kept failing with
     * `No such file or directory: '/lib/ld-musl-*.so.1'`.
     *
     * @return true when a runtime exists and is (now) healed.
     */
    fun ensureRuntimePatched(context: Context): Boolean {
        return try {
            val pythonHome = getPythonDir(context)
            if (!File(pythonHome, "bin").isDirectory) return false
            val resolvedInterp = resolvedMuslInterpPath(context) ?: run {
                AppLogger.w(TAG, "musl loader not present yet; skipping runtime heal")
                return false
            }
            healRuntimeInPlace(pythonHome, resolvedInterp)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Runtime heal failed: ${e.message}")
            false
        }
    }

    private fun prepareRequirementsFileForInstall(
        context: Context,
        projectDir: File,
        reqFile: File,
        onOutput: ((String) -> Unit)?
    ): File {
        val original = reqFile.readText()
        val sanitized = sanitizeRequirementsForAndroid(original)
        if (sanitized == original) {
            return reqFile
        }

        val tempFile = File.createTempFile("w2a_requirements_android_", ".txt", context.cacheDir)
        tempFile.writeText(sanitized)
        AppLogger.w(
            TAG,
            "Detected Android-incompatible Python dependencies; using sanitised requirements: ${tempFile.absolutePath} (project=${projectDir.absolutePath})"
        )
        onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsAdjustedIncompatible)
        return tempFile
    }

    internal fun sanitizeRequirementsForAndroid(requirementsText: String): String {
        val sanitized = requirementsText
            .lineSequence()
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) {
                    return@mapNotNull line
                }

                val packageName = trimmed.takeWhile { char ->
                    char.isLetterOrDigit() || char == '-' || char == '_' || char == '.'
                }
                if (packageName.lowercase(Locale.US) in ANDROID_UNSUPPORTED_REQUIREMENTS) {
                    return@mapNotNull null
                }
                if (packageName.equals("uvicorn", ignoreCase = true) && trimmed.getOrNull(packageName.length) == '[') {
                    return@mapNotNull line.replaceFirst(UVICORN_EXTRAS_REGEX, "$1uvicorn$2")
                }
                line
            }
            .joinToString("\n")

        return if (requirementsText.endsWith("\n") && !sanitized.endsWith("\n")) {
            "$sanitized\n"
        } else {
            sanitized
        }
    }

    /**
     * Extracts the package name pip could not resolve, when the failure is a
     * missing distribution (no wheel for this Python/platform). Returns null
     * for other failure modes.
     */
    internal fun findNoWheelPackage(output: String): String? {
        NO_DISTRIBUTION_RE.find(output)?.let { return it.groupValues[1] }
        FAILED_BUILD_WHEEL_RE.find(output)?.let { return it.groupValues[1] }
        return null
    }

    /**
     * True when the `--only-binary` pass failed because no wheel matched and
     * the index clearly responded. Retrying without `--only-binary` would try
     * to build from source, which cannot work on-device (no compiler), so we
     * skip it and report an actionable hint instead.
     */
    internal fun shouldSkipSourceBuildRetry(output: String): Boolean {
        if (!NO_DISTRIBUTION_RE.containsMatchIn(output)) return false
        return INDEX_PROOF_RE.containsMatchIn(output)
    }

    /**
     * A `sitecustomize.py` installed into the Python runtime dir. It is imported
     * by *every* Python process (including pip subprocesses and PEP 517 build
     * isolation venv pythons) and routes any python-like executable through the
     * on-device musl loader, because the stock ELF interpreter path
     * (`/lib/ld-musl-<arch>.so.1`) does not exist on Android. This is the safety
     * net for subprocess spawns on runtimes whose PT_INTERP patch did not apply;
     * the primary fix is patching PT_INTERP to the on-device absolute loader
     * path.
     *
     * It also rescues `packaging`'s musl version probe: the probe reads the
     * PT_INTERP string of `sys.executable` and executes it verbatim
     * (`subprocess.run([ld])`) to compute the `musllinux_*` platform tags. On
     * Android that string is never directly executable, so it is re-routed to
     * the bundled loader — without this, compiled wheels (e.g. MarkupSafe for
     * Flask) never match and pip falls back to a source build that cannot work.
     */
    internal fun buildSitecustomizeScript(): String = """
import os
import sys

# WebToApp: route python subprocesses through the on-device musl dynamic linker.
# The stock python ELF interpreter (/lib/ld-musl-*.so.1) does not exist on
# Android, so direct kernel execs of the binary fail. This module is imported
# by every python process via the site machinery and rewrites subprocess/exec
# calls that target python-like executables to go through the bundled loader.

_MUSL = os.environ.get('_WTA_MUSL_LINKER', '')
_LIB = os.environ.get('_WTA_MUSL_LIB_PATH', '')
_PYBIN = os.environ.get('_WTA_PYTHON_BIN', '')
# 'python' covers venv symlinks (pip build isolation creates venv/bin/python).
_PYNAMES = ('python', 'python3', 'python${PYTHON_VERSION}', 'libpython3.so')


def _is_py(p):
    return bool(p) and (p == _PYBIN or os.path.basename(str(p)) in _PYNAMES)


def _is_loader(p):
    b = os.path.basename(str(p))
    return b.startswith('ld-musl-') and '.so.' in b


if _MUSL and _LIB:
    import subprocess

    _OrigPopen = subprocess.Popen

    class _MPopen(_OrigPopen):
        def __init__(self, args, *a, **kw):
            if isinstance(args, (list, tuple)) and len(args) > 0:
                head = str(args[0])
                if _is_py(head):
                    args = [_MUSL, '--library-path', _LIB] + list(args)
                elif head != _MUSL and _is_loader(head):
                    # packaging's musl probe executes the PT_INTERP string of
                    # sys.executable verbatim; run the bundled loader instead
                    # so the musllinux platform tags resolve.
                    args = [_MUSL] + list(args)[1:]
            super().__init__(args, *a, **kw)

    subprocess.Popen = _MPopen

    _oexecv = os.execv

    def _mexecv(p, a):
        if _is_py(p):
            a = [_MUSL, '--library-path', _LIB] + list(a)
            p = _MUSL
        elif p != _MUSL and _is_loader(p):
            a = list(a)
            p = _MUSL
        return _oexecv(p, a)

    os.execv = _mexecv

    if hasattr(os, 'execve'):
        _oexecve = os.execve

        def _mexecve(p, a, e):
            if _is_py(p):
                a = [_MUSL, '--library-path', _LIB] + list(a)
                p = _MUSL
            elif p != _MUSL and _is_loader(p):
                a = list(a)
                p = _MUSL
            return _oexecve(p, a, e)

        os.execve = _mexecve
""".trimIndent()

    private fun runPipWithWrapper(
        context: Context,
        projectDir: File,
        pythonBin: String,
        pythonHome: String,
        muslLinker: String,
        sitePackages: File,
        reqFile: File,
        extraEnv: Map<String, String>,
        onOutput: ((String) -> Unit)?
    ): Boolean {
        val cacheDir = context.cacheDir

        val bootstrapScript = File(cacheDir, "pip_bootstrap.py")
        bootstrapScript.writeText("""
import sys
import os

# ★ sys.executable 必须保持真实的 python ELF 路径，不能改写成 shell wrapper：
# 1) 运行时 PT_INTERP 已被修补为设备上的绝对 musl loader 路径，pip 派生的
#    子进程直接 exec 即可；
# 2) packaging 的 musl 版本探测会读取 sys.executable 的 ELF 头，并把其
#    PT_INTERP 字符串当命令执行，以计算 musllinux 平台标签。若指向非 ELF
#    的 wrapper，探测静默失败 → 编译型 wheel（如 MarkupSafe）全部无法匹配
#    → pip 退回源码构建并在 Android 上失败。补丁未生效的旧运行时由下面的
#    subprocess/os.execv 劫持兜底。

# 安全网: monkey-patch subprocess.Popen 和 os.execv，把 python 目标的调用
# 改道到 musl loader（覆盖 PT_INTERP 补丁未生效的情况）
import subprocess
_OrigPopen = subprocess.Popen
_MUSL = os.environ.get('_WTA_MUSL_LINKER', '')
_LIB = os.environ.get('_WTA_MUSL_LIB_PATH', '')
_PYBIN = os.environ.get('_WTA_PYTHON_BIN', '')
_PYNAMES = ('python', 'python3', '${getVersionedPythonBinaryName()}', 'libpython3.so')

def _is_py(p):
    return p == _PYBIN or os.path.basename(str(p)) in _PYNAMES

def _is_loader(p):
    b = os.path.basename(str(p))
    return b.startswith('ld-musl-') and '.so.' in b

class _MPopen(_OrigPopen):
    def __init__(self, args, *a, **kw):
        if isinstance(args, (list, tuple)) and len(args) > 0 and _MUSL and _LIB:
            head = str(args[0])
            if _is_py(head):
                args = [_MUSL, '--library-path', _LIB] + list(args)
            elif head != _MUSL and _is_loader(head):
                # packaging 的 musl 探测会把 PT_INTERP 字符串当命令执行，
                # 改道到随包 loader 让 musllinux 标签可解析。
                args = [_MUSL] + list(args)[1:]
        super().__init__(args, *a, **kw)

subprocess.Popen = _MPopen

_oexecv = os.execv
def _mexecv(p, a):
    if _MUSL and _LIB:
        if _is_py(p):
            a = [_MUSL, '--library-path', _LIB] + list(a)
            p = _MUSL
        elif p != _MUSL and _is_loader(p):
            a = list(a)
            p = _MUSL
    return _oexecv(p, a)
os.execv = _mexecv

if hasattr(os, 'execve'):
    _oexecve = os.execve
    def _mexecve(p, a, e):
        if _MUSL and _LIB:
            if _is_py(p):
                a = [_MUSL, '--library-path', _LIB] + list(a)
                p = _MUSL
            elif p != _MUSL and _is_loader(p):
                a = list(a)
                p = _MUSL
        return _oexecve(p, a, e)
    os.execve = _mexecve

# 运行 pip
from pip._internal.cli.main import main
sys.exit(main())
""".trimIndent())

        val pipArgs = listOf(
            "install",
            "--target", sitePackages.absolutePath,
            "--no-cache-dir",
            "--disable-pip-version-check",
            "--no-compile",
            "--timeout", "30",
            "--retries", "2",
            "--only-binary", ":all:",
            "-r", reqFile.absolutePath
        )

        val command = listOf(
            muslLinker, "--library-path", "$pythonHome/lib",
            pythonBin, bootstrapScript.absolutePath
        ) + pipArgs

        AppLogger.i(TAG, "Installing Python dependencies (wrapper mode): ${command.joinToString(" ")}")

        val firstResult = executeCommand(command, projectDir, pythonBin, pythonHome, muslLinker,
            context, extraEnv, onOutput)

        if (firstResult.exitCode == 0) return true

        val noWheelPackage = findNoWheelPackage(firstResult.output)
        if (noWheelPackage != null && shouldSkipSourceBuildRetry(firstResult.output)) {
            // A source build needs a compiler, which does not exist on-device.
            // Retrying without --only-binary would only fail at the gcc step
            // with a confusing error, so surface an actionable hint instead.
            AppLogger.w(
                TAG,
                "pip --only-binary found no usable wheel for '$noWheelPackage' on Python $PYTHON_VERSION; skipping source-build retry"
            )
            sitePackages.deleteRecursively()
            onOutput?.invoke(
                String.format(com.webtoapp.core.i18n.Strings.pyDepsNoWheelHint, noWheelPackage, PYTHON_VERSION)
            )
            return false
        }

        AppLogger.w(TAG, "pip --only-binary failed (exitCode=${firstResult.exitCode}), retrying without restrictions...")
        onOutput?.invoke(com.webtoapp.core.i18n.Strings.pyDepsRetrying)
        sitePackages.deleteRecursively()
        sitePackages.mkdirs()

        val retryArgs = listOf(
            "install",
            "--target", sitePackages.absolutePath,
            "--no-cache-dir",
            "--disable-pip-version-check",
            "--no-compile",
            "--timeout", "30",
            "--retries", "2",
            "-r", reqFile.absolutePath
        )

        val retryCommand = listOf(
            muslLinker, "--library-path", "$pythonHome/lib",
            pythonBin, bootstrapScript.absolutePath
        ) + retryArgs

        AppLogger.i(TAG, "Installing Python dependencies (wrapper-mode retry): ${retryCommand.joinToString(" ")}")

        val retryResult = executeCommand(retryCommand, projectDir, pythonBin, pythonHome, muslLinker,
            context, extraEnv, onOutput)
        if (retryResult.exitCode == 0) return true

        // The retry also failed: give an actionable hint when the cause is a
        // missing wheel/compiler rather than a transient network issue.
        val retryPackage = findNoWheelPackage(retryResult.output)
        if (retryPackage != null) {
            onOutput?.invoke(
                String.format(com.webtoapp.core.i18n.Strings.pyDepsNoWheelHint, retryPackage, PYTHON_VERSION)
            )
        } else if (COMPILER_MISSING_RE.containsMatchIn(retryResult.output)) {
            onOutput?.invoke(String.format(com.webtoapp.core.i18n.Strings.pyDepsSourceBuildHint, PYTHON_VERSION))
        }
        return false
    }

    private fun runPipDirect(
        context: Context,
        projectDir: File,
        pythonBin: String,
        pythonHome: String,
        sitePackages: File,
        reqFile: File,
        onOutput: ((String) -> Unit)?
    ): Boolean {
        val command = listOf(
            pythonBin, "-m", "pip", "install",
            "--target", sitePackages.absolutePath,
            "--no-cache-dir",
            "--disable-pip-version-check",
            "--no-compile",
            "-r", reqFile.absolutePath
        )
        AppLogger.i(TAG, "Installing Python dependencies (direct mode): ${command.joinToString(" ")}")
        return executeCommand(command, projectDir, pythonBin, pythonHome, null,
            context, emptyMap(), onOutput).exitCode == 0
    }

    data class CommandResult(val exitCode: Int, val output: String)

    private fun executeCommand(
        command: List<String>,
        workDir: File,
        pythonBin: String,
        pythonHome: String,
        muslLinker: String?,
        context: Context,
        extraEnv: Map<String, String>,
        onOutput: ((String) -> Unit)?
    ): CommandResult {
        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(workDir)
        processBuilder.redirectErrorStream(true)

        val env = processBuilder.environment()
        env["PYTHONHOME"] = pythonHome
        env["PYTHONPATH"] = "$pythonHome/lib/python$PYTHON_VERSION"
        env["LD_LIBRARY_PATH"] = "$pythonHome/lib"
        env["HOME"] = context.filesDir.absolutePath
        env["TMPDIR"] = context.cacheDir.absolutePath
        env["PATH"] = "${File(pythonHome, "bin").absolutePath}:${env["PATH"] ?: "/usr/bin"}"
        env["PYTHONDONTWRITEBYTECODE"] = "1"
        if (muslLinker != null) {
            env["_WTA_MUSL_LINKER"] = muslLinker
            env["_WTA_MUSL_LIB_PATH"] = "$pythonHome/lib"
        }
        env["_WTA_PYTHON_BIN"] = pythonBin

        extraEnv.forEach { (k, v) -> env[k] = v }

        AppLogger.i(TAG, "executeCommand: starting process...")
        val process = processBuilder.start()

        val output = StringBuilder()
        val outputLock = Any()
        val readerThread = Thread {
            try {
                process.inputStream.bufferedReader().forEachLine { line ->
                    AppLogger.d(TAG, "[pip] $line")
                    onOutput?.invoke(line)
                    synchronized(outputLock) {
                        if (output.length < MAX_PIP_OUTPUT_CHARS) {
                            output.append(line).append('\n')
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.d(TAG, "pip output stream closed: ${e.message}")
            }
        }.apply { isDaemon = true; start() }

        val PIP_TIMEOUT_SECONDS = 120L
        val deadline = System.currentTimeMillis() + PIP_TIMEOUT_SECONDS * 1000L
        var completed = false
        while (System.currentTimeMillis() < deadline && !completed) {
            completed = try {
                process.exitValue()
                true
            } catch (_: IllegalThreadStateException) {
                Thread.sleep(200)
                false
            }
        }

        if (!completed) {

            AppLogger.e(TAG, "pip install timed out (${PIP_TIMEOUT_SECONDS}s), force-killing process")
            onOutput?.invoke(String.format(com.webtoapp.core.i18n.Strings.pyDepsInstallTimeout, PIP_TIMEOUT_SECONDS))
            process.destroyForciblyCompat()
            readerThread.interrupt()
            return CommandResult(-1, synchronized(outputLock) { output.toString() })
        }

        readerThread.join(3000)

        val exitCode = process.exitValue()
        val capturedOutput = synchronized(outputLock) { output.toString() }
        if (exitCode != 0) {
            val lastOutput = capturedOutput.lines().takeLast(5).joinToString("\n")
            AppLogger.e(TAG, "pip install failed, exitCode=$exitCode, output=$lastOutput")
            onOutput?.invoke(String.format(com.webtoapp.core.i18n.Strings.pyDepsInstallFailedCode, exitCode))
        }
        return CommandResult(exitCode, capturedOutput)
    }

    fun clearCache(context: Context) {
        getDepsDir(context).deleteRecursively()
        AppLogger.i(TAG, "Python dependency cache cleared")
    }

    fun getCacheSize(context: Context): Long {
        return getDepsDir(context).walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private suspend fun downloadWithRetry(
        urls: List<String>,
        destFile: File,
        displayName: String,
        context: Context?
    ): Boolean {
        for ((urlIndex, url) in urls.withIndex()) {
            val sourceName = if (urls.size > 1) String.format(com.webtoapp.core.i18n.Strings.pyDownloadSourceLabel, displayName, urlIndex + 1, urls.size) else displayName
            AppLogger.i(TAG, "Attempting to download $sourceName: $url")

            for (attempt in 1..MAX_RETRY_PER_URL) {
                val success = DependencyDownloadEngine.downloadFile(url, destFile, sourceName, context)
                if (success) return true

                if (attempt < MAX_RETRY_PER_URL) {
                    AppLogger.i(TAG, "$sourceName download failed, retrying in ${RETRY_DELAY_MS / 1000}s ($attempt/$MAX_RETRY_PER_URL)")
                    kotlinx.coroutines.delay(RETRY_DELAY_MS)
                    DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Idle)
                }
            }

            if (urlIndex < urls.lastIndex) {
                val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp")
                tmpFile.delete()
                AppLogger.i(TAG, "$sourceName failed, trying next source...")
                DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Idle)
            }
        }
        return false
    }

    private suspend fun downloadPython(context: Context, mirror: MirrorConfig, abi: String): Boolean {
        val pythonUrls = mirror.pythonUrls
        val fileName = pythonUrls.first().substringAfterLast("/")
        val destDir = getPythonDir(context)
        val archiveFile = File(getDepsDir(context), fileName)

        AppLogger.i(TAG, "Downloading Python runtime (${pythonUrls.size} sources)")

        val downloaded = downloadWithRetry(pythonUrls, archiveFile, "Python $PYTHON_FULL_VERSION ($abi)", context)
        syncEngineState()
        if (!downloaded) return false

        _downloadState.value = DownloadState.Extracting("Python")
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Extracting("Python"))
        try {

            extractTarGz(archiveFile, destDir, stripPrefix = "python/")

            val versionedBinaryName = getVersionedPythonBinaryName()
            val pythonBinVersioned = File(destDir, "bin/$versionedBinaryName")
            val pythonBin = File(destDir, "bin/python3")
            if (pythonBinVersioned.exists() && pythonBinVersioned.length() > 1024 * 1024) {
                pythonBinVersioned.setExecutable(true, false)
                pythonBinVersioned.setReadable(true, true)
                AppLogger.i(TAG, "Python runtime ready: ${pythonBinVersioned.absolutePath} (${pythonBinVersioned.length() / 1024} KB)")

                if (!pythonBin.exists() || pythonBin.length() < 1024 * 1024) {
                    pythonBinVersioned.copyTo(pythonBin, overwrite = true)
                    pythonBin.setExecutable(true, false)
                    AppLogger.i(TAG, "Copying $versionedBinaryName -> python3 (replacing broken symlink)")
                }
            } else if (pythonBin.exists() && pythonBin.length() > 1024 * 1024) {
                pythonBin.setExecutable(true, false)
                pythonBin.setReadable(true, true)
                AppLogger.i(TAG, "Python runtime ready: ${pythonBin.absolutePath}")
            } else {

                AppLogger.w(TAG, "No valid $versionedBinaryName or python3 found; searching for other binaries")
                val found = destDir.walkTopDown()
                    .filter { it.name.startsWith("python3") && it.isFile && it.length() > 1024 * 1024 }
                    .firstOrNull()

                if (found != null) {
                    val binDir = File(destDir, "bin")
                    binDir.mkdirs()
                    val target = File(binDir, versionedBinaryName)
                    found.copyTo(target, overwrite = true)
                    target.setExecutable(true, false)
                    target.copyTo(File(binDir, "python3"), overwrite = true)
                    File(binDir, "python3").setExecutable(true, false)
                    AppLogger.i(TAG, "Python binary moved to: ${target.absolutePath}")
                } else {
                    AppLogger.e(TAG, "No valid Python binary (>1MB) found after extraction")
                    markError(com.webtoapp.core.i18n.Strings.pyExtractNoBinary)
                    return false
                }
            }

            File(destDir, "bin").listFiles()?.forEach { it.setExecutable(true, false) }

            File(destDir, "lib").listFiles()?.filter { it.name.endsWith(".so") || it.name.contains(".so.") }?.forEach {
                it.setExecutable(true, false)
            }

            // The python binaries ship with an ELF interpreter the kernel
            // cannot resolve on Android. The interp patch + sitecustomize
            // bridge need the musl loader on disk, so they run as an
            // idempotent heal after the loader download below (same path used
            // for runtimes that persisted across an app update).
            archiveFile.delete()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Python extraction failed", e)
            markError(String.format(com.webtoapp.core.i18n.Strings.pyExtractFailed, e.message ?: ""))
            return false
        }

        if (mirror.muslLinkerUrl != null) {
            val muslSuccess = downloadMuslLinker(context, mirror.muslLinkerUrl, abi)
            if (!muslSuccess) {
                AppLogger.w(TAG, "musl dynamic linker download failed; Python may not execute on Android")
            }
        } else {
            AppLogger.w(TAG, "Current ABI ($abi) has no available musl linker")
        }

        // Applies the interp patch + sitecustomize bridge now that the loader
        // is on disk; no-ops safely when the loader download failed.
        ensureRuntimePatched(context)

        return true
    }

    private suspend fun downloadMuslLinker(context: Context, url: String, abi: String): Boolean {
        val linkerName = getMuslLinkerName(abi)
        val destDir = getPythonDir(context)
        val linkerFile = File(destDir, "lib/$linkerName")

        if (linkerFile.exists() && linkerFile.length() > 100 * 1024) {
            AppLogger.i(TAG, "musl linker Already exists: ${linkerFile.absolutePath} (${linkerFile.length() / 1024} KB)")
            return true
        }

        try {
            AppLogger.i(TAG, "Downloading musl linker: $url")
            val apkFile = File(getDepsDir(context), "musl-${abi}.apk")
            val downloaded = downloadWithRetry(listOf(url), apkFile, "musl linker ($abi)", context)
            if (!downloaded) return false

            val gzipStream = java.util.zip.GZIPInputStream(apkFile.inputStream().buffered())
            val tarStream = org.apache.commons.compress.archivers.tar.TarArchiveInputStream(gzipStream)
            var found = false
            tarStream.use { tar ->
                var entry = tar.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && !entry.isSymbolicLink && entry.name.endsWith(linkerName)) {
                        linkerFile.parentFile?.mkdirs()
                        FileOutputStream(linkerFile).use { fos ->
                            tar.copyTo(fos)
                        }
                        linkerFile.setExecutable(true, false)
                        found = true
                        AppLogger.i(TAG, "musl linker extracted: ${linkerFile.absolutePath} (${linkerFile.length() / 1024} KB)")
                        break
                    }
                    entry = tar.nextEntry
                }
            }
            apkFile.delete()

            if (!found) {
                AppLogger.e(TAG, "Alpine APK does not contain $linkerName")
                markError(String.format(com.webtoapp.core.i18n.Strings.pyMuslNotInAlpineApk, linkerName))
            }
            return found
        } catch (e: Exception) {
            AppLogger.e(TAG, "Downloading musl linker failed", e)
            markError(String.format(com.webtoapp.core.i18n.Strings.pyMuslDownloadFailed, e.message ?: ""))
            return false
        }
    }

    private fun extractTarGz(archiveFile: File, destDir: File, stripPrefix: String? = null) {
        AppLogger.i(TAG, "Extracting ${archiveFile.name} to ${destDir.absolutePath}" +
            (if (stripPrefix != null) " (strip prefix: $stripPrefix)" else ""))

        val gzipStream = java.util.zip.GZIPInputStream(archiveFile.inputStream().buffered())
        val tarStream = org.apache.commons.compress.archivers.tar.TarArchiveInputStream(gzipStream)

        val deferredSymlinks = mutableListOf<Pair<String, String>>()
        var fileCount = 0

        tarStream.use { tar ->
            var entry = tar.nextEntry
            while (entry != null) {
                var entryName = entry.name

                if (stripPrefix != null && entryName.startsWith(stripPrefix)) {
                    entryName = entryName.removePrefix(stripPrefix)
                    if (entryName.isEmpty()) {
                        entry = tar.nextEntry
                        continue
                    }
                }

                val outFile = File(destDir, entryName)

                if (!outFile.canonicalPath.startsWith(destDir.canonicalPath)) {
                    AppLogger.w(TAG, "Skipping suspicious path: ${entry.name}")
                    entry = tar.nextEntry
                    continue
                }

                if (entry.isSymbolicLink) {

                    var linkTarget = entry.linkName
                    if (stripPrefix != null && linkTarget.startsWith(stripPrefix)) {
                        linkTarget = linkTarget.removePrefix(stripPrefix)
                    }
                    deferredSymlinks.add(entryName to linkTarget)
                } else if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        tar.copyTo(fos)
                    }

                    if (entry.mode and 0b001_000_000 != 0) {
                        outFile.setExecutable(true, false)
                    }
                    fileCount++
                }
                entry = tar.nextEntry
            }
        }

        var resolvedLinks = 0
        for ((linkName, targetName) in deferredSymlinks) {
            val linkFile = File(destDir, linkName)
            val linkParent = linkFile.parentFile ?: destDir
            val targetFile = File(linkParent, targetName)

            if (targetFile.exists() && targetFile.isFile) {
                try {
                    linkFile.parentFile?.mkdirs()
                    targetFile.copyTo(linkFile, overwrite = true)
                    if (targetFile.canExecute()) {
                        linkFile.setExecutable(true, false)
                    }
                    resolvedLinks++
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to resolve symlink: $linkName -> $targetName: ${e.message}")
                }
            } else {
                AppLogger.d(TAG, "Skipping symlink (target missing): $linkName -> $targetName")
            }
        }

        AppLogger.i(TAG, "Unpack complete: $fileCount files, $resolvedLinks/${deferredSymlinks.size} symlinks resolved")
    }

    private fun syncEngineState() {
        when (val es = DependencyDownloadEngine.state.value) {
            is DependencyDownloadEngine.State.Downloading -> {
                _downloadState.value = DownloadState.Downloading(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes
                )
            }
            is DependencyDownloadEngine.State.Paused -> {
                _downloadState.value = DownloadState.Paused(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes
                )
            }
            is DependencyDownloadEngine.State.Error -> {
                _downloadState.value = DownloadState.Error(es.message)
            }
            else -> {}
        }
    }

    private fun markComplete() {
        _downloadState.value = DownloadState.Complete
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Complete)
    }

    private fun markError(message: String) {
        _downloadState.value = DownloadState.Error(message)
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Error(message))
    }
}
