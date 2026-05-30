package com.webtoapp.core.aicoding.prompt.sections

import com.webtoapp.core.aicoding.prompt.PromptLang

internal object EnvironmentSection {
    fun build(
        lang: PromptLang,
        modelName: String,
        sessionDir: String,
        skillName: String?
    ): String {
        val skillLine = skillName?.let { "Active skill: $it" }
        return when (lang) {
            PromptLang.EN -> buildEn(modelName, sessionDir, skillLine)
            PromptLang.ZH -> buildZh(modelName, sessionDir, skillLine)
        }
    }

    private fun buildEn(modelName: String, sessionDir: String, skillLine: String?) = buildString {
        appendLine("# Environment")
        appendLine("- Host: WebToApp on Android (no shell, no git, no package manager)")
        appendLine("- Working directory: $sessionDir (session-scoped, paths in tools are relative to here)")
        appendLine("- Output target: an installable APK that wraps a WebView")
        appendLine("- File operations are sandboxed: paths containing `..`, drive letters, or absolute prefixes are rejected")
        appendLine("- There is no terminal, no Bash, and no remote build server")
        appendLine("- Model: $modelName")
        if (skillLine != null) appendLine("- $skillLine")
    }.trimEnd()

    private fun buildZh(modelName: String, sessionDir: String, skillLine: String?) = buildString {
        appendLine("# 运行环境")
        appendLine("- 宿主：Android 上的 WebToApp（没有 shell，没有 git，没有包管理器）")
        appendLine("- 工作目录：$sessionDir（按会话隔离，工具中的路径都相对此目录）")
        appendLine("- 产出目标：包装 WebView 的可安装 APK")
        appendLine("- 文件操作受沙盒限制：含 `..`、盘符或绝对前缀的路径会被拒绝")
        appendLine("- 没有终端、Bash、远程构建服务器")
        appendLine("- 模型：$modelName")
        if (skillLine != null) appendLine("- $skillLine")
    }.trimEnd()
}
