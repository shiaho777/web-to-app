package com.webtoapp.core.linux

import android.content.Context
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.*

class PureBuildEngine(private val context: Context) {

    companion object {
        private const val TAG = "PureBuildEngine"

        private val PACKAGE_JSON_MAIN_REGEX = Regex(""""main"\s*:\s*"([^"]+)"""")

        private val IMPORT_FROM_REGEX = Regex("""import\s+.*?from\s+['"][^'"]+['"];?\s*""")
        private val IMPORT_SIDE_EFFECT_REGEX = Regex("""import\s+['"][^'"]+['"];?\s*""")
        private val EXPORT_DEFAULT_REGEX = Regex("""export\s+default\s+""")
        private val EXPORT_NAMED_REGEX = Regex("""export\s+\{[^}]*\};?\s*""")
        private val EXPORT_DECLARATION_REGEX = Regex("""export\s+(const|let|var|function|class)\s+""")

        private val TS_TYPE_IMPORT_REGEX = Regex("""import\s+type\s+.*?;""")
        private val TS_TYPE_ANNOTATION_REGEX = Regex("""\s*:\s*[A-Z][a-zA-Z0-9<>,\s\[\]|&]*(?=\s*[=;,)\]])""")
        private val TS_RETURN_TYPE_REGEX = Regex("""\)\s*:\s*[A-Z][a-zA-Z0-9<>,\s\[\]|&]*\s*(?=\{)""")
        private val TS_GENERICS_REGEX = Regex("""<[A-Z][a-zA-Z0-9<>,\s\[\]|&]*>""")
        private val TS_INTERFACE_TYPE_REGEX = Regex("""(interface|type)\s+\w+\s*[^{]*\{[^}]*\}""")

        private val JSX_SELF_CLOSING_REGEX = Regex("""<(\w+)\s*/>""")
        private val JSX_SIMPLE_TAG_REGEX = Regex("""<(\w+)>([^<]*)</\1>""")
    }

    private val _state = MutableStateFlow<PureBuildState>(PureBuildState.Idle)
    val state: StateFlow<PureBuildState> = _state

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    var allowBuiltinPackagerFallback: Boolean = true

    suspend fun build(
        projectPath: String,
        outputPath: String,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ): Result<BuildResult> = withContext(Dispatchers.IO) {
        try {
            _state.value = PureBuildState.Analyzing
            _logs.value = emptyList()

            log(Strings.pureBuildAnalyzeProject.format(projectPath))

            val projectDir = File(projectPath)
            if (!projectDir.exists()) {
                throw Exception(Strings.pureBuildProjectDirNotFound)
            }

            val existingDist = findExistingDist(projectDir)
            if (existingDist != null) {
                log(Strings.pureBuildFoundDist.format(existingDist.name))
                _state.value = PureBuildState.Copying(0f)

                val outputDir = File(outputPath)
                copyDirectory(existingDist, outputDir) { progress ->
                    _state.value = PureBuildState.Copying(progress)
                    onProgress(Strings.pureBuildCopyFiles, progress)
                }

                _state.value = PureBuildState.Success(outputPath)
                return@withContext Result.success(BuildResult(
                    outputPath = outputPath,
                    method = BuildMethod.EXISTING_DIST,
                    fileCount = countFiles(outputDir),
                    totalSize = calculateSize(outputDir)
                ))
            }

            if (NativeNodeEngine.isAvailable(context)) {
                log(Strings.pureBuildUseEsbuild)
                return@withContext buildWithEsbuild(projectDir, File(outputPath), onProgress)
            }

            log("初始化构建工具...")
            val initResult = NativeNodeEngine.initialize(context) { step, progress ->
                log(step)
                onProgress(step, progress * 0.3f)
            }

            if (initResult.isSuccess && NativeNodeEngine.isAvailable(context)) {
                log(Strings.pureBuildUseEsbuild)
                return@withContext buildWithEsbuild(projectDir, File(outputPath), onProgress)
            }

            if (!allowBuiltinPackagerFallback) {
                throw IllegalStateException(
                    Strings.pureBuildEsbuildUnavailable
                )
            }

            log("使用内置打包器")
            return@withContext buildWithPureKotlin(projectDir, File(outputPath), onProgress)

        } catch (e: Exception) {
            AppLogger.e(TAG, "Build failed", e)
            log(Strings.pureBuildError.format(e.message))
            _state.value = PureBuildState.Error(e.message ?: "未知错误")
            Result.failure(e)
        }
    }

    private fun findExistingDist(projectDir: File): File? {
        val candidates = listOf("dist", "build", "out", ".output", "public")

        for (name in candidates) {
            val dir = File(projectDir, name)
            if (dir.exists() && dir.isDirectory) {

                if (File(dir, "index.html").exists()) {
                    return dir
                }

                dir.listFiles()?.forEach { subDir ->
                    if (subDir.isDirectory && File(subDir, "index.html").exists()) {
                        return subDir
                    }
                }
            }
        }

        if (File(projectDir, "index.html").exists()) {
            return projectDir
        }

        return null
    }

    private suspend fun buildWithEsbuild(
        projectDir: File,
        outputDir: File,
        onProgress: (String, Float) -> Unit
    ): Result<BuildResult> = withContext(Dispatchers.IO) {
        _state.value = PureBuildState.Building("esbuild", 0f)

        val entryFile = detectEntryFile(projectDir)
            ?: throw Exception(Strings.pureBuildEntryNotFound)

        log(Strings.pureBuildEntryFile.format(entryFile.name))

        outputDir.mkdirs()

        val args = mutableListOf(
            entryFile.absolutePath,
            "--bundle",
            "--outdir=${outputDir.absolutePath}",
            "--format=esm",
            "--platform=browser",
            "--target=es2020",
            "--minify",
            "--sourcemap"
        )

        if (entryFile.extension in listOf("jsx", "tsx")) {
            args.add("--loader:.jsx=jsx")
            args.add("--loader:.tsx=tsx")
        }

        log(Strings.pureBuildRunEsbuild)
        onProgress(Strings.pureBuildBuilding, 0.5f)

        val result = NativeNodeEngine.executeEsbuild(
            context = context,
            args = args,
            workingDir = projectDir,
            onOutput = { log(it) }
        )

        if (result.exitCode != 0) {
            throw Exception(Strings.pureBuildEsbuildFailed.format(result.stderr))
        }

        generateIndexHtml(outputDir, entryFile.nameWithoutExtension)

        copyStaticAssets(projectDir, outputDir)

        _state.value = PureBuildState.Success(outputDir.absolutePath)
        onProgress("Done", 1f)

        Result.success(BuildResult(
            outputPath = outputDir.absolutePath,
            method = BuildMethod.ESBUILD,
            fileCount = countFiles(outputDir),
            totalSize = calculateSize(outputDir)
        ))
    }

    private suspend fun buildWithPureKotlin(
        projectDir: File,
        outputDir: File,
        onProgress: (String, Float) -> Unit
    ): Result<BuildResult> = withContext(Dispatchers.IO) {
        _state.value = PureBuildState.Building("内置打包器", 0f)
        log("使用内置打包器（功能有限）")

        outputDir.mkdirs()

        val entryFile = detectEntryFile(projectDir)

        if (entryFile != null) {

            log("收集源文件...")
            onProgress(Strings.pureBuildCollectFiles, 0.3f)

            val sourceFiles = collectSourceFiles(projectDir)
            log(Strings.pureBuildFoundSourceFiles.format(sourceFiles.size))

            log("打包中...")
            onProgress(Strings.pureBuildPackaging, 0.6f)

            val bundleContent = buildSimpleBundle(sourceFiles, entryFile)

            File(outputDir, "bundle.js").writeText(bundleContent)

            generateIndexHtml(outputDir, "bundle", isModule = false)
        }

        log("复制静态资源...")
        onProgress(Strings.pureBuildCopyResources, 0.8f)
        copyStaticAssets(projectDir, outputDir)

        val indexHtml = File(projectDir, "index.html")
        if (indexHtml.exists()) {
            indexHtml.copyTo(File(outputDir, "index.html"), overwrite = true)
        }

        _state.value = PureBuildState.Success(outputDir.absolutePath)
        onProgress("Done", 1f)

        Result.success(BuildResult(
            outputPath = outputDir.absolutePath,
            method = BuildMethod.PURE_KOTLIN,
            fileCount = countFiles(outputDir),
            totalSize = calculateSize(outputDir)
        ))
    }

    private fun detectEntryFile(projectDir: File): File? {
        val candidates = listOf(
            "src/main.ts", "src/main.tsx", "src/main.js", "src/main.jsx",
            "src/index.ts", "src/index.tsx", "src/index.js", "src/index.jsx",
            "src/App.tsx", "src/App.jsx", "src/App.ts", "src/App.js",
            "main.ts", "main.tsx", "main.js", "main.jsx",
            "index.ts", "index.tsx", "index.js", "index.jsx"
        )

        for (candidate in candidates) {
            val file = File(projectDir, candidate)
            if (file.exists()) return file
        }

        val packageJson = File(projectDir, "package.json")
        if (packageJson.exists()) {
            try {
                val content = packageJson.readText()

                val mainMatch = PACKAGE_JSON_MAIN_REGEX.find(content)
                mainMatch?.groupValues?.get(1)?.let { main ->
                    val mainFile = File(projectDir, main)
                    if (mainFile.exists()) return mainFile
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "解析 package.json 失败", e)
            }
        }

        return null
    }

    private fun collectSourceFiles(projectDir: File): List<File> {
        val extensions = setOf("js", "jsx", "ts", "tsx", "mjs")
        val excludeDirs = setOf("node_modules", "dist", "build", ".git", ".cache")

        return projectDir.walkTopDown()
            .filter { it.isFile }
            .filter { it.extension in extensions }
            .filter { file ->
                !excludeDirs.any { excluded ->
                    file.absolutePath.contains("/$excluded/")
                }
            }
            .toList()
    }

    private fun buildSimpleBundle(files: List<File>, entryFile: File): String {
        val sb = StringBuilder()

        sb.appendLine("// Auto-generated bundle")
        sb.appendLine("(function() {")
        sb.appendLine("'use strict';")
        sb.appendLine()

        val sortedFiles = files.sortedBy {
            if (it == entryFile) 1 else 0
        }

        for (file in sortedFiles) {
            sb.appendLine("// === ${file.name} ===")

            var content = file.readText()

            content = content
                .replace(IMPORT_FROM_REGEX, "")
                .replace(IMPORT_SIDE_EFFECT_REGEX, "")
                .replace(EXPORT_DEFAULT_REGEX, "")
                .replace(EXPORT_NAMED_REGEX, "")
                .replace(EXPORT_DECLARATION_REGEX, "$1 ")

            if (file.extension in listOf("ts", "tsx")) {
                content = stripTypeAnnotations(content)
            }

            if (file.extension in listOf("jsx", "tsx")) {
                content = transformJsx(content)
            }

            sb.appendLine(content)
            sb.appendLine()
        }

        sb.appendLine("})();")

        return sb.toString()
    }

    private fun stripTypeAnnotations(code: String): String {
        var result = code

        result = result.replace(TS_TYPE_IMPORT_REGEX, "")

        result = result.replace(TS_TYPE_ANNOTATION_REGEX, "")

        result = result.replace(TS_RETURN_TYPE_REGEX, ") ")

        result = result.replace(TS_GENERICS_REGEX, "")

        result = result.replace(TS_INTERFACE_TYPE_REGEX, "")

        return result
    }

    private fun transformJsx(code: String): String {
        var result = code

        result = result.replace(
            JSX_SELF_CLOSING_REGEX
        ) { match ->
            val tag = match.groupValues[1]
            if (tag[0].isUpperCase()) {
                "React.createElement($tag)"
            } else {
                "React.createElement('$tag')"
            }
        }

        result = result.replace(
            JSX_SIMPLE_TAG_REGEX
        ) { match ->
            val tag = match.groupValues[1]
            val content = match.groupValues[2].trim()
            val tagArg = if (tag[0].isUpperCase()) tag else "'$tag'"
            if (content.isEmpty()) {
                "React.createElement($tagArg)"
            } else {
                "React.createElement($tagArg, null, '$content')"
            }
        }

        return result
    }

    private fun generateIndexHtml(outputDir: File, bundleName: String, isModule: Boolean = true) {
        val moduleAttr = if (isModule) """ type="module"""" else ""
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>App</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div id="root"></div>
    <div id="app"></div>
    <script$moduleAttr src="$bundleName.js"></script>
</body>
</html>
        """.trimIndent()

        val indexFile = File(outputDir, "index.html")
        if (!indexFile.exists()) {
            indexFile.writeText(html)
        }
    }

    private fun copyStaticAssets(projectDir: File, outputDir: File) {
        val assetDirs = listOf("public", "static", "assets")
        val assetExtensions = setOf("css", "png", "jpg", "jpeg", "gif", "svg", "ico", "woff", "woff2", "ttf", "eot")

        for (dirName in assetDirs) {
            val dir = File(projectDir, dirName)
            if (dir.exists() && dir.isDirectory) {
                copyDirectory(dir, File(outputDir, dirName)) { }
            }
        }

        projectDir.listFiles()?.filter {
            it.isFile && it.extension in assetExtensions
        }?.forEach { file ->
            file.copyTo(File(outputDir, file.name), overwrite = true)
        }

        val srcDir = File(projectDir, "src")
        if (srcDir.exists()) {
            srcDir.walkTopDown()
                .filter { it.isFile && it.extension == "css" }
                .forEach { cssFile ->
                    cssFile.copyTo(File(outputDir, cssFile.name), overwrite = true)
                }
        }
    }

    private fun copyDirectory(src: File, dest: File, onProgress: (Float) -> Unit) {
        if (!src.exists()) return

        dest.mkdirs()

        val files = src.walkTopDown().filter { it.isFile }.toList()
        val total = files.size

        files.forEachIndexed { index, file ->
            val relativePath = file.relativeTo(src).path
            val destFile = File(dest, relativePath)
            destFile.parentFile?.mkdirs()
            file.copyTo(destFile, overwrite = true)
            onProgress((index + 1).toFloat() / total)
        }
    }

    private fun countFiles(dir: File): Int {
        return dir.walkTopDown().filter { it.isFile }.count()
    }

    private fun calculateSize(dir: File): Long {
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun log(message: String) {
        AppLogger.d(TAG, message)
        _logs.value = _logs.value + message
    }

    fun reset() {
        _state.value = PureBuildState.Idle
        _logs.value = emptyList()
    }
}

sealed class PureBuildState {
    object Idle : PureBuildState()
    object Analyzing : PureBuildState()
    data class Copying(val progress: Float) : PureBuildState()
    data class Building(val tool: String, val progress: Float) : PureBuildState()
    data class Success(val outputPath: String) : PureBuildState()
    data class Error(val message: String) : PureBuildState()
}

data class BuildResult(
    val outputPath: String,
    val method: BuildMethod,
    val fileCount: Int,
    val totalSize: Long
)

enum class BuildMethod {
    EXISTING_DIST,
    NODE_PACKAGE_SCRIPT,
    ESBUILD,
    PURE_KOTLIN
}
