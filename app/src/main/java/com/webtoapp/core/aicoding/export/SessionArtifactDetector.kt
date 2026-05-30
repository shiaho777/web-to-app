package com.webtoapp.core.aicoding.export

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.aicoding.files.ProjectFileManager

data class DetectedArtifact(
    val id: String,
    val kind: Kind,

    val displayName: String,

    val rootPath: String,

    val entryFile: String,
    val fileCount: Int,
    val totalSizeBytes: Long
) {

    enum class Kind(val target: Target) {
        Html(Target.App),
        FrontendReact(Target.App),
        FrontendVue(Target.App),
        NodeJs(Target.App),
        Php(Target.App),
        Python(Target.App),
        Go(Target.App),
        MultiWeb(Target.App),
        Gallery(Target.App),
        JsModule(Target.Module),
        StyleModule(Target.Module),
        UserScript(Target.Module),
        ChromeExtension(Target.Module);

        enum class Target { App, Module }
    }
}

class SessionArtifactDetector(
    private val files: ProjectFileManager
) {

    fun detect(sessionId: String): List<DetectedArtifact> {
        val all = files.listAll(sessionId)
        if (all.isEmpty()) return emptyList()
        val sizeByDir = computeDirSizes(all)
        val countByDir = computeDirCounts(all)
        val artifacts = mutableListOf<DetectedArtifact>()

        artifacts += detectChromeExtensions(sessionId, all, sizeByDir, countByDir)
        artifacts += detectJsModules(sessionId, all, sizeByDir, countByDir)
        artifacts += detectUserScripts(all)
        artifacts += detectNodeJsRoots(all, sizeByDir, countByDir)
        artifacts += detectByExtension(
            all, sizeByDir, countByDir,
            ext = "php",
            kind = DetectedArtifact.Kind.Php,
            preferredEntries = listOf("index.php", "public/index.php")
        )
        artifacts += detectByExtension(
            all, sizeByDir, countByDir,
            ext = "py",
            kind = DetectedArtifact.Kind.Python,
            preferredEntries = listOf("app.py", "main.py", "manage.py")
        )
        artifacts += detectByExtension(
            all, sizeByDir, countByDir,
            ext = "go",
            kind = DetectedArtifact.Kind.Go,
            preferredEntries = listOf("main.go")
        )
        artifacts += detectFileArtifact(
            sessionId = sessionId,
            files = all,
            fileName = "multi-web.json",
            kind = DetectedArtifact.Kind.MultiWeb,
            displayNameFromJson = ::multiWebDisplayName
        )
        artifacts += detectFileArtifact(
            sessionId = sessionId,
            files = all,
            fileName = "gallery.json",
            kind = DetectedArtifact.Kind.Gallery,
            displayNameFromJson = ::galleryDisplayName
        )

        artifacts += detectHtmlRoots(
            sessionId, all, sizeByDir, countByDir,
            claimedDirs = artifacts.map { it.rootPath }.toSet()
        )
        return artifacts.sortedBy { it.rootPath }
    }

    private fun detectChromeExtensions(
        sessionId: String,
        files: List<ProjectFileManager.FileInfo>,
        sizeByDir: Map<String, Long>,
        countByDir: Map<String, Int>
    ): List<DetectedArtifact> {
        val out = mutableListOf<DetectedArtifact>()
        val claimed = mutableSetOf<String>()
        files.filter { it.relativePath.endsWith("manifest.json", ignoreCase = true) }
            .sortedBy { it.relativePath.count { c -> c == '/' } }
            .forEach { f ->
                val parent = f.relativePath.parentDir()
                if (claimed.any { parent == it || parent.startsWith("$it/") }) return@forEach
                val text = this.files.readText(sessionId, f.relativePath) ?: return@forEach
                val obj = runCatching { JsonParser.parseString(text).asJsonObject }.getOrNull()
                    ?: return@forEach
                if (!obj.has("manifest_version")) return@forEach
                val name = obj.optStringOrNull("name") ?: parent.lastSegmentOrSession()
                claimed += parent
                out += DetectedArtifact(
                    id = "chrome:$parent",
                    kind = DetectedArtifact.Kind.ChromeExtension,
                    displayName = name,
                    rootPath = parent,
                    entryFile = f.relativePath,
                    fileCount = countByDir.descendantCount(parent),
                    totalSizeBytes = sizeByDir.descendantSize(parent)
                )
            }
        return out
    }

    private fun detectJsModules(
        sessionId: String,
        files: List<ProjectFileManager.FileInfo>,
        sizeByDir: Map<String, Long>,
        countByDir: Map<String, Int>
    ): List<DetectedArtifact> {
        val out = mutableListOf<DetectedArtifact>()
        val claimed = mutableSetOf<String>()
        val moduleJsons = files.filter {
            it.relativePath.endsWith("/module.json", ignoreCase = true) ||
                it.relativePath.equals("module.json", ignoreCase = true)
        }
        val byDir = files.groupBy { it.relativePath.parentDir() }
        moduleJsons
            .sortedBy { it.relativePath.count { c -> c == '/' } }
            .forEach { f ->
                val dir = f.relativePath.parentDir()
                if (claimed.any { dir == it || dir.startsWith("$it/") }) return@forEach
                val sibs = byDir[dir].orEmpty()
                if (sibs.none { it.relativePath.endsWith("main.js") }) return@forEach
                val text = this.files.readText(sessionId, f.relativePath) ?: return@forEach
                val obj = runCatching { JsonParser.parseString(text).asJsonObject }.getOrNull()
                    ?: return@forEach
                val name = obj.optStringOrNull("name") ?: dir.lastSegmentOrSession()

                val perms = obj.getAsJsonArray("permissions")
                    ?.mapNotNull { (it as? com.google.gson.JsonPrimitive)?.asString }
                    .orEmpty()
                val kind = if (perms == listOf("CSS_INJECT")) DetectedArtifact.Kind.StyleModule
                else DetectedArtifact.Kind.JsModule
                claimed += dir
                out += DetectedArtifact(
                    id = "module:$dir",
                    kind = kind,
                    displayName = name,
                    rootPath = dir,
                    entryFile = f.relativePath,
                    fileCount = countByDir.descendantCount(dir),
                    totalSizeBytes = sizeByDir.descendantSize(dir)
                )
            }
        return out
    }

    private fun detectUserScripts(
        files: List<ProjectFileManager.FileInfo>
    ): List<DetectedArtifact> = files
        .filter { it.relativePath.endsWith(".user.js", ignoreCase = true) }
        .map { f ->
            DetectedArtifact(
                id = "userscript:${f.relativePath}",
                kind = DetectedArtifact.Kind.UserScript,
                displayName = f.relativePath.substringAfterLast('/').removeSuffix(".user.js"),
                rootPath = f.relativePath.parentDir(),
                entryFile = f.relativePath,
                fileCount = 1,
                totalSizeBytes = f.sizeBytes
            )
        }

    private fun detectNodeJsRoots(
        files: List<ProjectFileManager.FileInfo>,
        sizeByDir: Map<String, Long>,
        countByDir: Map<String, Int>
    ): List<DetectedArtifact> {
        val out = mutableListOf<DetectedArtifact>()
        val claimed = mutableSetOf<String>()
        files.filter {
            it.relativePath.endsWith("package.json", ignoreCase = true) &&
                !it.relativePath.contains("/node_modules/") &&
                it.relativePath.count { c -> c == '/' } < 4
        }
            .sortedBy { it.relativePath.count { c -> c == '/' } }
            .forEach { f ->
                val dir = f.relativePath.parentDir()
                if (claimed.any { dir == it || dir.startsWith("$it/") }) return@forEach
                claimed += dir
                out += DetectedArtifact(
                    id = "node:$dir",
                    kind = DetectedArtifact.Kind.NodeJs,
                    displayName = dir.lastSegmentOrSession(),
                    rootPath = dir,
                    entryFile = f.relativePath,
                    fileCount = countByDir.descendantCount(dir),
                    totalSizeBytes = sizeByDir.descendantSize(dir)
                )
            }

        if (claimed.isEmpty()) {
            val stray = files.firstOrNull {
                it.relativePath.equals("index.js", ignoreCase = true) ||
                    it.relativePath.equals("server.js", ignoreCase = true)
            }
            if (stray != null) {
                out += DetectedArtifact(
                    id = "node:",
                    kind = DetectedArtifact.Kind.NodeJs,
                    displayName = "Node.js App",
                    rootPath = "",
                    entryFile = stray.relativePath,
                    fileCount = countByDir.descendantCount(""),
                    totalSizeBytes = sizeByDir.descendantSize("")
                )
            }
        }
        return out
    }

    private fun detectByExtension(
        files: List<ProjectFileManager.FileInfo>,
        sizeByDir: Map<String, Long>,
        countByDir: Map<String, Int>,
        ext: String,
        kind: DetectedArtifact.Kind,
        preferredEntries: List<String>
    ): List<DetectedArtifact> {
        val out = mutableListOf<DetectedArtifact>()
        val claimed = mutableSetOf<String>()
        val candidateDirs = files
            .filter { it.relativePath.endsWith(".$ext", ignoreCase = true) }
            .map { it.relativePath.parentDir() }
            .distinct()
            .sortedBy { it.count { c -> c == '/' } }
        candidateDirs.forEach { dir ->
            if (claimed.any { dir == it || dir.startsWith("$it/") }) return@forEach
            claimed += dir
            val entry = preferredEntries
                .firstOrNull { entry -> files.any { it.relativePath == joinPath(dir, entry) } }
                ?: files.firstOrNull {
                    it.relativePath.parentDir() == dir &&
                        it.relativePath.endsWith(".$ext", ignoreCase = true)
                }?.relativePath
                ?: return@forEach
            out += DetectedArtifact(
                id = "$ext:$dir",
                kind = kind,
                displayName = dir.lastSegmentOrSession(),
                rootPath = dir,
                entryFile = entry,
                fileCount = countByDir.descendantCount(dir),
                totalSizeBytes = sizeByDir.descendantSize(dir)
            )
        }
        return out
    }

    private fun detectFileArtifact(
        sessionId: String,
        files: List<ProjectFileManager.FileInfo>,
        fileName: String,
        kind: DetectedArtifact.Kind,
        displayNameFromJson: (String) -> String?
    ): List<DetectedArtifact> = files
        .filter {
            it.relativePath.equals(fileName, ignoreCase = true) ||
                it.relativePath.endsWith("/$fileName", ignoreCase = true)
        }
        .mapNotNull { f ->
            val text = this.files.readText(sessionId, f.relativePath) ?: return@mapNotNull null
            DetectedArtifact(
                id = "$kind:${f.relativePath}",
                kind = kind,
                displayName = displayNameFromJson(text)
                    ?: f.relativePath.parentDir().lastSegmentOrSession(),
                rootPath = f.relativePath.parentDir(),
                entryFile = f.relativePath,
                fileCount = 1,
                totalSizeBytes = f.sizeBytes
            )
        }

    private fun detectHtmlRoots(
        sessionId: String,
        files: List<ProjectFileManager.FileInfo>,
        sizeByDir: Map<String, Long>,
        countByDir: Map<String, Int>,
        claimedDirs: Set<String>
    ): List<DetectedArtifact> {
        val out = mutableListOf<DetectedArtifact>()
        val claimed = claimedDirs.toMutableSet()

        val htmlByDir = files
            .filter {
                it.relativePath.endsWith(".html", ignoreCase = true) ||
                    it.relativePath.endsWith(".htm", ignoreCase = true)
            }
            .groupBy { it.relativePath.parentDir() }
        htmlByDir.keys
            .sortedBy { it.count { c -> c == '/' } }
            .forEach { dir ->
                if (claimed.any { dir == it || dir.startsWith("$it/") }) return@forEach
                claimed += dir
                val entries = htmlByDir.getValue(dir)
                val entryFile = entries
                    .firstOrNull { it.relativePath.endsWith("index.html", ignoreCase = true) }
                    ?.relativePath
                    ?: entries.first().relativePath

                val displayName = htmlTitle(sessionId, entryFile)
                    ?: dir.lastSegmentOrSession()

                out += DetectedArtifact(
                    id = "html:$dir",
                    kind = DetectedArtifact.Kind.Html,
                    displayName = displayName,
                    rootPath = dir,
                    entryFile = entryFile,
                    fileCount = countByDir.descendantCount(dir),
                    totalSizeBytes = sizeByDir.descendantSize(dir)
                )
            }
        return out
    }

    private fun htmlTitle(sessionId: String, relativePath: String): String? {
        val text = files.readText(sessionId, relativePath)?.take(8 * 1024) ?: return null
        val match = Regex(
            "<title[^>]*>([\\s\\S]*?)</title>",
            RegexOption.IGNORE_CASE
        ).find(text) ?: return null
        return match.groupValues[1].trim().ifBlank { null }
    }

    private fun multiWebDisplayName(json: String): String? = runCatching {
        val obj = JsonParser.parseString(json).asJsonObject
        obj.optStringOrNull("title") ?: obj.optStringOrNull("name")
    }.getOrNull()

    private fun galleryDisplayName(json: String): String? = runCatching {
        val obj = JsonParser.parseString(json).asJsonObject
        obj.optStringOrNull("title") ?: obj.optStringOrNull("name")
    }.getOrNull()

    private fun computeDirSizes(files: List<ProjectFileManager.FileInfo>): Map<String, Long> {
        val out = HashMap<String, Long>()
        files.forEach { f ->
            ancestorsOf(f.relativePath).forEach { dir ->
                out.merge(dir, f.sizeBytes) { a, b -> a + b }
            }
        }
        return out
    }

    private fun computeDirCounts(files: List<ProjectFileManager.FileInfo>): Map<String, Int> {
        val out = HashMap<String, Int>()
        files.forEach { f ->
            ancestorsOf(f.relativePath).forEach { dir ->
                out.merge(dir, 1) { a, b -> a + b }
            }
        }
        return out
    }

    private fun ancestorsOf(path: String): Sequence<String> = sequence {
        var cur = path.parentDir()
        while (true) {
            yield(cur)
            if (cur.isEmpty()) return@sequence
            cur = cur.parentDir()
        }
    }

    private fun Map<String, Long>.descendantSize(dir: String): Long = this[dir] ?: 0L
    private fun Map<String, Int>.descendantCount(dir: String): Int = this[dir] ?: 0
}

private fun String.parentDir(): String {
    val idx = lastIndexOf('/')
    return if (idx < 0) "" else substring(0, idx)
}

private fun String.lastSegmentOrSession(): String =
    substringAfterLast('/').ifBlank { "Session" }

private fun joinPath(dir: String, child: String): String =
    if (dir.isEmpty()) child else "$dir/$child"

private fun JsonObject.optStringOrNull(key: String): String? =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
