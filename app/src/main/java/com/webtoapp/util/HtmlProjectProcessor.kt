package com.webtoapp.util

import com.webtoapp.core.logging.AppLogger
import android.util.LruCache
import com.webtoapp.core.i18n.Strings
import java.io.File
import java.nio.charset.Charset

object HtmlProjectProcessor {

    private const val TAG = "HtmlProjectProcessor"

    private const val MAX_ANALYZE_FILE_SIZE = 5L * 1024 * 1024

    private val encodingCache = LruCache<String, String>(50)

    private val cssLinkRegex = Regex("""<link[^>]*\shref\s*=\s*["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
    private val jsScriptRegex = Regex("""<script[^>]*\ssrc\s*=\s*["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
    private val imgSrcRegex = Regex("""<img[^>]*\ssrc\s*=\s*["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
    private val absolutePathRegex = Regex("""(\s)(href|src)\s*=\s*(["'])(/(?!/)[^"']+)\3""", RegexOption.IGNORE_CASE)
    private val protocolRelativeRegex = Regex("""(\s)(href|src)\s*=\s*(["'])(//[^"']+)\3""", RegexOption.IGNORE_CASE)

    private val localCssRegex = Regex("""<link[^>]*\shref\s*=\s*(["'])([^"']*\.css(?:[?#][^"']*)?)\1[^>]*>""", RegexOption.IGNORE_CASE)

    private val localJsRegex = Regex("""(?s)<script[^>]*\ssrc\s*=\s*(["'])([^"']*\.m?js(?:[?#][^"']*)?)\1[^>]*>.*?</script>""", RegexOption.IGNORE_CASE)
    private val charsetRegex = Regex("""charset=["']?([^"'\s>]+)""", RegexOption.IGNORE_CASE)
    private val referenceSchemeRegex = Regex("""^[a-z][a-z0-9+.-]*:""", RegexOption.IGNORE_CASE)
    private val dynamicReferenceMarkers = listOf("${'$'}{", "{{", "}}", "<%", "%>", "||", "&&")

    private val closeHeadRegex = Regex("</head>", RegexOption.IGNORE_CASE)
    private val openBodyRegex = Regex("<body", RegexOption.IGNORE_CASE)
    private val openHtmlTagRegex = Regex("<html[^>]*>", RegexOption.IGNORE_CASE)

    private val closeBodyRegex = Regex("</body>", RegexOption.IGNORE_CASE)
    private val closeHtmlRegex = Regex("</html>", RegexOption.IGNORE_CASE)

    private val openHeadRegex = Regex("<head>", RegexOption.IGNORE_CASE)

    data class ProjectAnalysis(
        val htmlFiles: List<FileInfo>,
        val cssFiles: List<FileInfo>,
        val jsFiles: List<FileInfo>,
        val otherFiles: List<FileInfo>,
        val issues: List<ProjectIssue>,
        val suggestions: List<String>
    )

    data class FileInfo(
        val name: String,
        val path: String,
        val size: Long,
        val encoding: String?,
        val references: List<ResourceReference> = emptyList()
    )

    data class ResourceReference(
        val type: ReferenceType,
        val originalPath: String,
        val resolvedPath: String?,
        val isValid: Boolean,
        val issue: String? = null
    )

    enum class ReferenceType {
        CSS_LINK,
        JS_SCRIPT,
        IMAGE,
        CSS_IMPORT,
        CSS_URL,
        OTHER
    }

    data class ProjectIssue(
        val severity: IssueSeverity,
        val type: IssueType,
        val message: String,
        val file: String? = null,
        val suggestion: String? = null
    )

    enum class IssueSeverity {
        ERROR,
        WARNING,
        INFO
    }

    enum class IssueType {
        MISSING_FILE,
        ABSOLUTE_PATH,
        ENCODING_ISSUE,
        STRUCTURE_ISSUE,
        SYNTAX_ERROR,
        EXTERNAL_RESOURCE
    }

    fun analyzeProject(
        htmlFilePath: String?,
        cssFilePath: String?,
        jsFilePath: String?,
        additionalFiles: List<String> = emptyList()
    ): ProjectAnalysis {
        val issues = mutableListOf<ProjectIssue>()
        val suggestions = mutableListOf<String>()

        val htmlFiles = mutableListOf<FileInfo>()
        val cssFiles = mutableListOf<FileInfo>()
        val jsFiles = mutableListOf<FileInfo>()
        val otherFiles = mutableListOf<FileInfo>()

        htmlFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val encoding = detectEncoding(file)

                val references = if (file.length() <= MAX_ANALYZE_FILE_SIZE) {
                    val content = readFileWithEncoding(file, encoding)
                    analyzeHtmlReferences(content, file.parentFile)
                } else {
                    issues.add(ProjectIssue(
                        severity = IssueSeverity.INFO,
                        type = IssueType.STRUCTURE_ISSUE,
                        message = Strings.htmlFileTooLarge.replace("%s", (file.length() / 1024 / 1024).toString()),
                        file = file.name
                    ))
                    emptyList()
                }

                htmlFiles.add(FileInfo(
                    name = file.name,
                    path = path,
                    size = file.length(),
                    encoding = encoding,
                    references = references
                ))

                references.forEach { ref ->
                    if (!ref.isValid) {
                        issues.add(ProjectIssue(
                            severity = IssueSeverity.WARNING,
                            type = if (ref.originalPath.startsWith("/")) IssueType.ABSOLUTE_PATH else IssueType.MISSING_FILE,
                            message = ref.issue ?: "${Strings.resourceReferenceIssue}: ${ref.originalPath}",
                            file = file.name,
                            suggestion = when {
                                ref.originalPath.startsWith("/") -> Strings.suggestUseRelativePath
                                else -> Strings.suggestEnsureFileImported.replace("%s", ref.originalPath)
                            }
                        ))
                    }
                }
            } else {
                issues.add(ProjectIssue(
                    severity = IssueSeverity.ERROR,
                    type = IssueType.MISSING_FILE,
                    message = "${Strings.htmlFileNotFound}: $path"
                ))
            }
        }

        cssFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val encoding = detectEncoding(file)
                cssFiles.add(FileInfo(
                    name = file.name,
                    path = path,
                    size = file.length(),
                    encoding = encoding
                ))

                if (encoding != "UTF-8" && encoding != null) {
                    issues.add(ProjectIssue(
                        severity = IssueSeverity.WARNING,
                        type = IssueType.ENCODING_ISSUE,
                        message = Strings.cssEncodingWarning.replace("%s", encoding ?: "unknown"),
                        file = file.name,
                        suggestion = Strings.suggestSaveAsUtf8
                    ))
                }
            }
        }

        jsFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val encoding = detectEncoding(file)

                jsFiles.add(FileInfo(
                    name = file.name,
                    path = path,
                    size = file.length(),
                    encoding = encoding
                ))

                if (file.length() <= MAX_ANALYZE_FILE_SIZE) {
                    val content = readFileWithEncoding(file, encoding)

                    checkJsIssues(content, file.name, issues)
                }
            }
        }

        if (htmlFiles.isNotEmpty()) {
            val references = htmlFiles.flatMap { it.references }
            val needsCss = cssFiles.isEmpty() && references.any { it.type == ReferenceType.CSS_LINK }
            val needsJs = jsFiles.isEmpty() && references.any { it.type == ReferenceType.JS_SCRIPT }
            if (needsCss || needsJs) {
                suggestions.add(Strings.suggestExternalFilesDetected)
            }
        }

        if (issues.any { it.type == IssueType.ABSOLUTE_PATH }) {
            suggestions.add(Strings.suggestUseRelativePathsForAll)
        }

        return ProjectAnalysis(
            htmlFiles = htmlFiles,
            cssFiles = cssFiles,
            jsFiles = jsFiles,
            otherFiles = otherFiles,
            issues = issues,
            suggestions = suggestions
        )
    }

    fun processHtmlContent(
        htmlContent: String,
        cssContent: String?,
        jsContent: String?,
        fixPaths: Boolean = true,
        removeLocalRefs: Boolean = true
    ): String {
        var result = htmlContent

        if (fixPaths) {
            result = fixResourcePaths(result)
        }

        if (removeLocalRefs) {
            result = removeLocalResourceReferences(result,
                hasCssContent = !cssContent.isNullOrBlank(),
                hasJsContent = !jsContent.isNullOrBlank())
        }

        if (!cssContent.isNullOrBlank()) {
            result = inlineCss(result, cssContent)
        }

        if (!jsContent.isNullOrBlank()) {
            result = inlineJs(result, jsContent)
        }

        if (!result.contains("viewport", ignoreCase = true)) {
            result = addViewportMeta(result)
        }

        return result
    }

    private fun fixResourcePaths(html: String): String {
        var result = html

        result = protocolRelativeRegex.replace(result) { match ->
            val prefix = match.groupValues[1]
            val attr = match.groupValues[2]
            val quote = match.groupValues[3]
            val path = match.groupValues[4]
            if (isDynamicReference(path)) match.value else "$prefix$attr=${quote}https:$path$quote"
        }

        result = absolutePathRegex.replace(result) { match ->
            val prefix = match.groupValues[1]
            val attr = match.groupValues[2]
            val quote = match.groupValues[3]
            val path = match.groupValues[4]
            if (isDynamicReference(path)) match.value else "$prefix$attr=$quote.$path$quote"
        }

        return result
    }

    private fun removeLocalResourceReferences(
        html: String,
        hasCssContent: Boolean = true,
        hasJsContent: Boolean = true
    ): String {
        var result = html

        if (hasCssContent) {
            result = localCssRegex.replace(result) { match ->
                if (shouldAnalyzeLocalReference(match.groupValues[2])) "<!-- CSS inlined -->" else match.value
            }
        }

        if (hasJsContent) {
            result = localJsRegex.replace(result) { match ->
                if (shouldAnalyzeLocalReference(match.groupValues[2])) "<!-- JS inlined -->" else match.value
            }
        }

        return result
    }

    private fun inlineCss(html: String, css: String): String {
        val styleTag = "<style>\n/* Inlined CSS */\n$css\n</style>"
        val escapedStyleTag = Regex.escapeReplacement(styleTag)

        return when {
            html.contains("</head>", ignoreCase = true) -> {
                html.replaceFirst(closeHeadRegex, "$escapedStyleTag\n</head>")
            }
            html.contains("<body", ignoreCase = true) -> {
                html.replaceFirst(openBodyRegex, "$escapedStyleTag\n<body")
            }
            html.contains("<html", ignoreCase = true) -> {
                val match = openHtmlTagRegex.find(html)
                if (match != null) {
                    html.substring(0, match.range.last + 1) +
                        "\n<head>\n$styleTag\n</head>" +
                        html.substring(match.range.last + 1)
                } else {
                    "$styleTag\n$html"
                }
            }
            else -> "$styleTag\n$html"
        }
    }

    private fun inlineJs(html: String, js: String): String {
        val wrappedJs = wrapJsForSafeExecution(js)
        val scriptTag = "<script>\n/* Inlined JS */\n$wrappedJs\n</script>"
        val escapedScriptTag = Regex.escapeReplacement(scriptTag)

        return when {
            html.contains("</body>", ignoreCase = true) -> {
                html.replaceFirst(closeBodyRegex, "$escapedScriptTag\n</body>")
            }
            html.contains("</html>", ignoreCase = true) -> {
                html.replaceFirst(closeHtmlRegex, "$escapedScriptTag\n</html>")
            }
            else -> "$html\n$scriptTag"
        }
    }

    private fun wrapJsForSafeExecution(js: String): String {
        val trimmed = js.trim()
        if (trimmed.isEmpty()) return ""

        val hasWrapper = trimmed.contains("DOMContentLoaded", ignoreCase = true) ||
                        trimmed.contains("window.onload", ignoreCase = true) ||
                        trimmed.contains("addEventListener('load'", ignoreCase = true) ||
                        trimmed.contains("addEventListener(\"load\"", ignoreCase = true) ||
                        trimmed.contains("\$(document).ready", ignoreCase = true) ||
                        trimmed.contains("\$(function()", ignoreCase = true)

        return if (hasWrapper) {
            trimmed
        } else {
            """
(function() {
    'use strict';

    function initApp() {
        try {
$trimmed
        } catch (e) {
            console.error('[WebToApp] JS execution error:', e);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initApp);
    } else {
        initApp();
    }
})();
            """.trimIndent()
        }
    }

    private fun addViewportMeta(html: String): String {
        val viewportMeta = """<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">"""

        return when {
            html.contains("<head>", ignoreCase = true) -> {
                html.replaceFirst(openHeadRegex, "<head>\n$viewportMeta")
            }
            html.contains("<html", ignoreCase = true) -> {
                val match = openHtmlTagRegex.find(html)
                if (match != null) {
                    html.substring(0, match.range.last + 1) +
                        "\n<head>\n$viewportMeta\n</head>" +
                        html.substring(match.range.last + 1)
                } else {
                    "$viewportMeta\n$html"
                }
            }
            else -> "$viewportMeta\n$html"
        }
    }

    private fun analyzeHtmlReferences(html: String, baseDir: File?): List<ResourceReference> {
        val references = mutableListOf<ResourceReference>()

        cssLinkRegex.findAll(html).forEach { match ->
            val path = match.groupValues[1]
            if (shouldAnalyzeLocalReference(path) && stripReferenceSuffix(path).endsWith(".css", ignoreCase = true)) {
                references.add(analyzeReference(path, ReferenceType.CSS_LINK, baseDir))
            }
        }

        jsScriptRegex.findAll(html).forEach { match ->
            val path = match.groupValues[1]
            if (shouldAnalyzeLocalReference(path)) {
                references.add(analyzeReference(path, ReferenceType.JS_SCRIPT, baseDir))
            }
        }

        imgSrcRegex.findAll(html).forEach { match ->
            val path = match.groupValues[1]
            if (shouldAnalyzeLocalReference(path)) {
                references.add(analyzeReference(path, ReferenceType.IMAGE, baseDir))
            }
        }

        return references
    }

    private fun analyzeReference(path: String, type: ReferenceType, baseDir: File?): ResourceReference {
        val localPath = stripReferenceSuffix(path)
        val isAbsolute = localPath.startsWith("/")
        val resolvedPath = if (baseDir != null && !isAbsolute) {
            File(baseDir, localPath.removePrefix("./")).absolutePath
        } else null

        val exists = resolvedPath?.let { File(it).exists() } ?: false

        val issue = when {
            isAbsolute -> Strings.absolutePathWarning
            !exists && resolvedPath != null -> Strings.referencedFileNotExist
            else -> null
        }

        return ResourceReference(
            type = type,
            originalPath = path,
            resolvedPath = resolvedPath,
            isValid = !isAbsolute && (exists || resolvedPath == null),
            issue = issue
        )
    }

    private fun shouldAnalyzeLocalReference(rawPath: String): Boolean {
        val path = rawPath.trim()
        if (path.isBlank() || path.startsWith("#") || path.startsWith("//")) return false
        if (referenceSchemeRegex.containsMatchIn(path)) return false
        if (isDynamicReference(path)) return false
        if (path.any { it == '{' || it == '}' || it == '<' || it == '>' || it == '`' }) return false
        val localPath = stripReferenceSuffix(path)
        if (localPath.isBlank() || localPath.endsWith("/")) return false
        return true
    }

    private fun isDynamicReference(path: String): Boolean {
        return dynamicReferenceMarkers.any { path.contains(it) } ||
            path.any { it == '{' || it == '}' || it == '<' || it == '>' || it == '`' }
    }

    private fun stripReferenceSuffix(path: String): String {
        return path.trim().substringBefore("#").substringBefore("?")
    }

    private fun detectEncoding(file: File): String? {
        val cacheKey = file.absolutePath + "_" + file.lastModified()

        encodingCache.get(cacheKey)?.let { return it }

        return try {

            val bytes = ByteArray(minOf(1000, file.length().toInt().coerceAtLeast(0)))
            val bytesRead = file.inputStream().use { it.read(bytes) }
            val header = if (bytesRead < bytes.size) bytes.copyOf(bytesRead) else bytes

            val encoding = when {

                header.size >= 3 && header[0] == 0xEF.toByte() && header[1] == 0xBB.toByte() && header[2] == 0xBF.toByte() -> "UTF-8"
                header.size >= 2 && header[0] == 0xFE.toByte() && header[1] == 0xFF.toByte() -> "UTF-16BE"
                header.size >= 2 && header[0] == 0xFF.toByte() && header[1] == 0xFE.toByte() -> "UTF-16LE"
                else -> {

                    val content = String(header, Charsets.ISO_8859_1)
                    charsetRegex.find(content)?.groupValues?.get(1)?.uppercase() ?: "UTF-8"
                }
            }

            encodingCache.put(cacheKey, encoding)
            encoding
        } catch (e: Exception) {
            AppLogger.e(TAG, "检测编码失败: ${file.path}", e)
            "UTF-8"
        }
    }

    fun clearEncodingCache() {
        encodingCache.evictAll()
    }

    fun readFileWithEncoding(file: File, encoding: String?): String {
        return try {
            val charset = when (encoding?.uppercase()) {
                "UTF-8", "UTF8" -> Charsets.UTF_8
                "GBK", "GB2312", "GB18030" -> Charset.forName("GBK")
                "UTF-16", "UTF-16BE" -> Charsets.UTF_16BE
                "UTF-16LE" -> Charsets.UTF_16LE
                "ISO-8859-1", "LATIN1" -> Charsets.ISO_8859_1
                else -> Charsets.UTF_8
            }
            file.readText(charset)
        } catch (e: Exception) {
            AppLogger.e(TAG, "读取文件失败: ${file.path}", e)

            try {
                file.readText(Charsets.UTF_8)
            } catch (e2: Exception) {
                file.readText(Charsets.ISO_8859_1)
            }
        }
    }

    private fun checkJsIssues(content: String, fileName: String, issues: MutableList<ProjectIssue>) {

        if (content.contains("document.write", ignoreCase = true)) {
            issues.add(ProjectIssue(
                severity = IssueSeverity.WARNING,
                type = IssueType.SYNTAX_ERROR,
                message = Strings.documentWriteWarning,
                file = fileName,
                suggestion = Strings.suggestUseDomMethods
            ))
        }

        if (shouldSkipJsBraceCheck(fileName, content)) {
            return
        }

        if (!areJsBracesBalanced(content)) {
            issues.add(ProjectIssue(
                severity = IssueSeverity.WARNING,
                type = IssueType.SYNTAX_ERROR,
                message = Strings.possiblyUnclosedBraces,
                file = fileName,
                suggestion = Strings.suggestCheckBracesPaired
            ))
        }
    }

    private fun shouldSkipJsBraceCheck(fileName: String, content: String): Boolean {
        val lower = fileName.lowercase()
        if (lower.endsWith(".min.js") ||
            lower.endsWith(".min.mjs") ||
            lower.endsWith(".min.cjs") ||
            lower.contains(".min.") ||
            lower.endsWith(".umd.js") ||
            lower.endsWith(".umd.mjs") ||
            lower.endsWith(".bundle.js") ||
            lower.endsWith(".prod.js") ||
            lower.endsWith(".production.js")
        ) {
            return true
        }
        if (content.length >= 80_000) {
            return true
        }
        if (content.length < 400) {
            return false
        }
        var lines = 0
        var maxLine = 0
        var total = 0
        var start = 0
        val n = content.length
        var i = 0
        while (i <= n) {
            if (i == n || content[i] == '\n') {
                val len = i - start
                if (len > 0) {
                    lines++
                    total += len
                    if (len > maxLine) maxLine = len
                }
                start = i + 1
            }
            i++
        }
        if (lines == 0) return false
        val avg = total.toDouble() / lines
        return maxLine >= 500 || (content.length >= 8_000 && avg >= 180)
    }

    private fun areJsBracesBalanced(content: String): Boolean {
        var depth = 0
        var i = 0
        val n = content.length
        while (i < n) {
            val c = content[i]
            when {
                c == '/' && i + 1 < n -> {
                    when (content[i + 1]) {
                        '/' -> {
                            i += 2
                            while (i < n && content[i] != '\n') i++
                        }
                        '*' -> {
                            i += 2
                            while (i + 1 < n && !(content[i] == '*' && content[i + 1] == '/')) i++
                            i = (i + 2).coerceAtMost(n)
                        }
                        else -> i++
                    }
                }
                c == '"' || c == '\'' -> {
                    val quote = c
                    i++
                    while (i < n) {
                        val ch = content[i]
                        when {
                            ch == '\\' -> i += 2
                            ch == quote -> {
                                i++
                                break
                            }
                            else -> i++
                        }
                    }
                }
                c == '`' -> {
                    i++
                    while (i < n) {
                        val ch = content[i]
                        when {
                            ch == '\\' -> i += 2
                            ch == '`' -> {
                                i++
                                break
                            }
                            ch == '$' && i + 1 < n && content[i + 1] == '{' -> {
                                i += 2
                                var nested = 1
                                while (i < n && nested > 0) {
                                    val nc = content[i]
                                    when {
                                        nc == '\\' -> i += 2
                                        nc == '"' || nc == '\'' -> {
                                            val quote = nc
                                            i++
                                            while (i < n) {
                                                val x = content[i]
                                                when {
                                                    x == '\\' -> i += 2
                                                    x == quote -> {
                                                        i++
                                                        break
                                                    }
                                                    else -> i++
                                                }
                                            }
                                        }
                                        nc == '{' -> {
                                            nested++
                                            i++
                                        }
                                        nc == '}' -> {
                                            nested--
                                            i++
                                        }
                                        else -> i++
                                    }
                                }
                            }
                            else -> i++
                        }
                    }
                }
                c == '{' -> {
                    depth++
                    i++
                }
                c == '}' -> {
                    depth--
                    if (depth < 0) return false
                    i++
                }
                else -> i++
            }
        }
        return depth == 0
    }
}
