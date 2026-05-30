package com.webtoapp.core.nodejs

import android.content.Context
import com.webtoapp.core.i18n.PreviewHtmlSupport.escapeText
import com.webtoapp.core.i18n.PreviewHtmlSupport.htmlLang
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NodeRuntime(private val context: Context) {

    companion object {
        private const val TAG = "NodeRuntime"
        private val PROJECT_EXCLUDE_DIRS = setOf(".git", ".cache")
    }

    sealed class ServerState {
        object Stopped : ServerState()
        object Starting : ServerState()
        data class Running(val port: Int) : ServerState()
        data class Error(val message: String) : ServerState()
    }

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState

    @Volatile private var currentPort: Int = 0
    @Volatile private var isRunning = false

    fun isNodeAvailable(): Boolean = NodeDependencyManager.isNodeReady(context)

    suspend fun startServer(
        projectDir: String,
        entryFile: String = "index.js",
        port: Int = 0,
        envVars: Map<String, String> = emptyMap()
    ): Int = withContext(Dispatchers.IO) {
        try {
            if (!isNodeAvailable()) {
                _serverState.value = ServerState.Error("Node.js 运行时未就绪，请先下载依赖")
                return@withContext -1
            }

            _serverState.value = ServerState.Starting

            val attempt1 = NodeServiceClient.startServer(
                context = context,
                projectDir = projectDir,
                entryFile = entryFile,
                portPref = port,
                envVars = envVars
            )

            val result = if (attempt1 is NodeServiceClient.StartResult.Failure && attempt1.v8Exhausted) {

                AppLogger.w(TAG, "V8 已耗尽（上次脚本崩了），重启 :nodejs 子进程后重试")
                ShellLogger.w(TAG, "V8 已耗尽，重启 :nodejs 子进程后重试")
                NodeServiceClient.restartEngine(context)
                NodeServiceClient.startServer(
                    context = context,
                    projectDir = projectDir,
                    entryFile = entryFile,
                    portPref = port,
                    envVars = envVars
                )
            } else {
                attempt1
            }

            when (result) {
                is NodeServiceClient.StartResult.Success -> {
                    currentPort = result.port
                    isRunning = true
                    _serverState.value = ServerState.Running(result.port)
                    AppLogger.i(TAG, "Node.js 服务器已启动 (子进程): ${result.url}")
                    ShellLogger.i(TAG, "Node.js 服务器已启动 (子进程): ${result.url}")
                    result.port
                }
                is NodeServiceClient.StartResult.Failure -> {
                    AppLogger.e(TAG, "Node.js 启动失败: ${result.message}")
                    ShellLogger.e(TAG, "Node.js 启动失败: ${result.message}")
                    _serverState.value = ServerState.Error(result.message)
                    isRunning = false
                    currentPort = 0
                    -1
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "启动 Node.js 服务器失败", e)
            ShellLogger.e(TAG, "启动 Node.js 服务器失败: ${e.message}")
            _serverState.value = ServerState.Error("启动失败: ${e.message}")
            isRunning = false
            currentPort = 0
            -1
        }
    }

    fun stopServer() {
        try {
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            GlobalScope.launch(Dispatchers.IO) {
                NodeServiceClient.stopServer(context)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "停止 Node.js 服务器异常: ${e.message}")
        } finally {
            currentPort = 0
            isRunning = false
            _serverState.value = ServerState.Stopped
        }
    }

    fun isServerRunning(): Boolean = isRunning && currentPort > 0

    fun getCurrentPort(): Int = currentPort

    fun getServerUrl(): String? {
        return if (isServerRunning() && currentPort > 0) {
            "http://127.0.0.1:$currentPort"
        } else null
    }

    fun generatePreviewHtml(projectDir: File, framework: String, entryFile: String): String {
        val S = com.webtoapp.core.i18n.Strings
        val entryFileObj = File(projectDir, entryFile)
        val sourceCode = if (entryFileObj.exists()) {
            try { entryFileObj.readText().take(8000) } catch (_: Exception) { "// ${S.previewFileUnreadable}" }
        } else { "// ${S.previewFileNotFound.replace("%s", entryFile)}" }

        val pkgFile = File(projectDir, "package.json")
        val packageJson = if (pkgFile.exists()) {
            try { pkgFile.readText().trim() } catch (_: Exception) { "" }
        } else ""

        val fileList = projectDir.walkTopDown()
            .filter { it.isFile }.take(30)
            .map { it.relativeTo(projectDir).path }.toList()

        val frameworkLabel = framework.replaceFirstChar { it.uppercase() }
        val escapedSource = sourceCode.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        val escapedPkg = packageJson.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val filesHtml = fileList.joinToString("\n") { "  <li>$it</li>" }

        val nodeReady = isNodeAvailable()

        val statusBadge = buildString {
            append("<span class=\"badge muted\">${escapeText(S.previewNotRunningBadge)}</span>")
            if (nodeReady) {
                append("<span class=\"badge\" style=\"background:#238636\">${escapeText(S.previewRuntimeReadyBadge)}</span>")
            } else {
                append("<span class=\"badge warn\">${escapeText(S.previewNodeNeedRuntime)}</span>")
            }
        }
        val footerTip = if (nodeReady) {
            S.previewNodeTipReady
        } else {
            S.previewNodeTipNotReady.replace("%s", frameworkLabel)
        }
        val notRunningNote = escapeText(S.previewNotRunningNote)
        val projectFilesTitle = "${escapeText(S.previewProjectFilesLabel)} (${fileList.size}${if (fileList.size >= 30) "+" else ""})"
        val lang = htmlLang()

        return """<!DOCTYPE html>
<html lang="$lang"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>$frameworkLabel - ${escapeText(S.previewProjectSuffix)}</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}body{font-family:-apple-system,system-ui,sans-serif;background:#0d1117;color:#c9d1d9;padding:16px;line-height:1.6}
.header{text-align:center;padding:24px 0;border-bottom:1px solid #30363d;margin-bottom:20px}.header h1{font-size:22px;color:#58a6ff;margin-bottom:8px}
.badge{display:inline-block;background:#1f6feb;color:#fff;padding:4px 12px;border-radius:12px;font-size:13px;margin:4px}.badge.warn{background:#d29922}.badge.muted{background:#6e7681}
.section{background:#161b22;border:1px solid #30363d;border-radius:8px;margin-bottom:16px;overflow:hidden}
.section-title{padding:12px 16px;background:#21262d;font-weight:600;font-size:14px;color:#8b949e;border-bottom:1px solid #30363d}
pre{padding:16px;overflow-x:auto;font-size:13px;font-family:'SF Mono',Consolas,monospace;white-space:pre-wrap;word-break:break-all;color:#c9d1d9;max-height:400px;overflow-y:auto}
ul{padding:12px 16px 12px 32px;font-size:13px}li{padding:2px 0;color:#8b949e}
.tip{background:#1a2332;border:1px solid #1f6feb;border-radius:8px;padding:16px;margin-top:16px;font-size:13px;color:#58a6ff}
.note{background:#21262d;border:1px solid #30363d;border-radius:8px;padding:12px 16px;margin-bottom:16px;font-size:12px;color:#8b949e}
</style></head><body>
<div class="header"><h1>$frameworkLabel · ${escapeText(S.previewProjectSuffix)}</h1><span class="badge">$frameworkLabel</span>$statusBadge</div>
<div class="note">$notRunningNote</div>
<div class="section"><div class="section-title">$entryFile</div><pre>$escapedSource</pre></div>
${if (escapedPkg.isNotBlank()) """<div class="section"><div class="section-title">package.json</div><pre>$escapedPkg</pre></div>""" else ""}
<div class="section"><div class="section-title">$projectFilesTitle</div><ul>
$filesHtml
</ul></div>
<div class="tip">${escapeText(footerTip)}</div>
</body></html>"""
    }

    suspend fun createProject(
        projectId: String,
        sourceDir: File,
        cleanTarget: Boolean = true
    ): File = withContext(Dispatchers.IO) {
        require(sourceDir.exists() && sourceDir.isDirectory) { "源项目目录不存在: ${sourceDir.absolutePath}" }

        val projectDir = File(NodeDependencyManager.getNodeProjectsDir(context), projectId)
        if (cleanTarget && projectDir.exists()) {
            projectDir.deleteRecursively()
        }
        projectDir.mkdirs()

        sourceDir.walkTopDown()
            .onEnter { dir -> dir == sourceDir || dir.name !in PROJECT_EXCLUDE_DIRS }
            .filter { it.isFile }
            .forEach { file ->
                val relativePath = file.relativeTo(sourceDir).path
                val destFile = File(projectDir, relativePath)
                destFile.parentFile?.mkdirs()
                file.copyTo(destFile, overwrite = true)
            }

        AppLogger.i(TAG, "项目文件已同步到: ${projectDir.absolutePath}")
        projectDir
    }

    suspend fun syncProjectFromSource(
        projectId: String,
        sourceDir: File
    ): File = createProject(projectId = projectId, sourceDir = sourceDir, cleanTarget = true)

    fun resolveSourceProjectDir(sourceProjectPath: String?): File? {
        val path = sourceProjectPath?.trim().orEmpty()
        if (path.isEmpty()) return null
        val file = File(path)
        return file.takeIf { it.exists() && it.isDirectory }
    }

    fun getProjectDir(projectId: String): File {
        return File(NodeDependencyManager.getNodeProjectsDir(context), projectId)
    }

    fun detectEntryFile(projectDir: File): String? {
        val packageJson = File(projectDir, "package.json")
        if (packageJson.exists()) {
            try {
                val content = packageJson.readText()
                val gson = com.webtoapp.util.GsonProvider.gson
                val json = gson.fromJson(content, com.google.gson.JsonObject::class.java)

                json.get("main")?.asString?.let { main ->
                    if (File(projectDir, main).exists()) return main
                }

                json.getAsJsonObject("scripts")?.get("start")?.asString?.let { startCmd ->
                    val nodeFileRegex = Regex("""node\s+(\S+\.(?:js|mjs|cjs))""")
                    nodeFileRegex.find(startCmd)?.groupValues?.get(1)?.let { entryFile ->
                        if (File(projectDir, entryFile).exists()) return entryFile
                    }
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "解析 package.json 失败", e)
            }
        }

        val candidates = listOf(
            "server.js", "server/index.js", "src/server.js",
            "app.js", "src/app.js",
            "index.js", "src/index.js",
            "main.js", "src/main.js",
            "server.mjs", "index.mjs"
        )

        for (candidate in candidates) {
            if (File(projectDir, candidate).exists()) return candidate
        }

        return null
    }
}
