package com.webtoapp.core.aicoding.export

import android.content.Context
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.extension.ChromeExtensionParser
import com.webtoapp.core.extension.ConfigItemType
import com.webtoapp.core.extension.ExtensionFileManager
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.core.extension.ExtensionModule
import com.webtoapp.core.extension.ModuleAuthor
import com.webtoapp.core.extension.ModuleCategory
import com.webtoapp.core.extension.ModuleConfigItem
import com.webtoapp.core.extension.ModulePermission
import com.webtoapp.core.extension.ModuleRunTime
import com.webtoapp.core.extension.ModuleSourceType
import com.webtoapp.core.extension.ModuleVersion
import com.webtoapp.core.extension.UrlMatchRule
import com.webtoapp.core.extension.UserScriptParser
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SaveSessionAsModuleUseCase(
    private val context: Context,
    private val files: ProjectFileManager,
    private val extensionManager: ExtensionManager,
    private val extensionFiles: ExtensionFileManager
) {

    sealed class Result {

        data class Success(
            val moduleIds: List<String>,
            val moduleNames: List<String>
        ) : Result()
        data class Failure(val message: String) : Result()
    }

    suspend fun save(
        sessionId: String,
        artifact: DetectedArtifact
    ): Result = withContext(Dispatchers.IO) {
        if (artifact.kind.target != DetectedArtifact.Kind.Target.Module) {
            return@withContext Result.Failure(
                "Wrong use case for ${artifact.kind} — use SaveSessionAsAppUseCase"
            )
        }
        try {
            val sessionRoot = files.getSessionRoot(sessionId)
            val artifactRoot = if (artifact.rootPath.isEmpty()) sessionRoot
            else File(sessionRoot, artifact.rootPath)
            when (artifact.kind) {
                DetectedArtifact.Kind.JsModule,
                DetectedArtifact.Kind.StyleModule ->
                    saveAsJsModule(sessionId, artifact)
                DetectedArtifact.Kind.UserScript ->
                    saveAsUserScript(sessionId, artifact.entryFile)
                DetectedArtifact.Kind.ChromeExtension ->
                    saveAsChromeExtension(artifactRoot)
                else -> Result.Failure("Unhandled kind ${artifact.kind}")
            }
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Save artifact as module failed", t)
            Result.Failure(t.message ?: "unknown error")
        }
    }

    private suspend fun saveAsJsModule(
        sessionId: String,
        artifact: DetectedArtifact
    ): Result {
        val dir = artifact.rootPath
        val manifestRel = if (dir.isEmpty()) "module.json" else "$dir/module.json"
        val mainJsRel = if (dir.isEmpty()) "main.js" else "$dir/main.js"
        val styleRel = if (dir.isEmpty()) "style.css" else "$dir/style.css"

        val manifestText = files.readText(sessionId, manifestRel)
            ?: return Result.Failure("module.json not found at $manifestRel")
        val mainJs = files.readText(sessionId, mainJsRel)
            ?: return Result.Failure("main.js not found at $mainJsRel")
        val styleCss = files.readText(sessionId, styleRel).orEmpty()

        val module = parseJsModuleManifest(manifestText)
            ?.copy(
                code = mainJs,
                cssCode = styleCss,
                sourceType = ModuleSourceType.CUSTOM
            )
            ?: return Result.Failure("module.json is not a valid module manifest")

        val saved = extensionManager.addModule(module)
        return saved.fold(
            onSuccess = { ok -> Result.Success(listOf(ok.id), listOf(ok.name)) },
            onFailure = { Result.Failure(it.message ?: "Module rejected") }
        )
    }

    private fun parseJsModuleManifest(text: String): ExtensionModule? = runCatching {
        val obj = JsonParser.parseString(text).asJsonObject
        ExtensionModule(
            id = obj.optString("id").ifBlank {
                java.util.UUID.randomUUID().toString().take(12)
            },
            name = obj.optString("name", "Untitled Module"),
            description = obj.optString("description"),
            icon = obj.optString("icon", "package"),
            category = parseCategory(obj.optString("category")),
            tags = obj.optStringArray("tags"),
            version = obj.getAsJsonObject("version")?.let { v ->
                ModuleVersion(
                    code = v.optInt("code", 1),
                    name = v.optString("name", "1.0.0"),
                    changelog = v.optString("changelog")
                )
            } ?: ModuleVersion(),
            author = obj.getAsJsonObject("author")?.let { a ->
                ModuleAuthor(
                    name = a.optString("name"),
                    url = a.optString("url").ifBlank { null },
                    email = a.optString("email").ifBlank { null }
                )
            },
            runAt = parseRunAt(obj.optString("runAt", "DOCUMENT_END")),
            urlMatches = obj.getAsJsonArray("urlMatches")?.mapNotNull { el ->
                (el as? JsonObject)?.let { rule ->
                    UrlMatchRule(
                        pattern = rule.optString("pattern", "*"),
                        isRegex = rule.optBoolean("isRegex", false),
                        exclude = rule.optBoolean("exclude", false)
                    )
                }
            }.orEmpty(),
            permissions = obj.optStringArray("permissions").mapNotNull { parsePermission(it) },
            configItems = obj.getAsJsonArray("configItems")?.mapNotNull { el ->
                (el as? JsonObject)?.let { c ->
                    ModuleConfigItem(
                        key = c.optString("key"),
                        name = c.optString("name").ifBlank { c.optString("key") },
                        description = c.optString("description"),
                        type = parseConfigItemType(c.optString("type", "TEXT")),
                        defaultValue = c.optString("defaultValue"),
                        options = c.optStringArray("options"),
                        required = c.optBoolean("required", false),
                        placeholder = c.optString("placeholder")
                    )
                }
            }.orEmpty()
        )
    }.getOrNull()

    private fun parseCategory(raw: String) =
        runCatching { ModuleCategory.valueOf(raw.uppercase()) }
            .getOrDefault(ModuleCategory.OTHER)

    private fun parseRunAt(raw: String) = runCatching {
        ModuleRunTime.valueOf(raw.uppercase().replace("-", "_"))
    }.getOrDefault(ModuleRunTime.DOCUMENT_END)

    private fun parsePermission(raw: String): ModulePermission? = runCatching {
        ModulePermission.valueOf(raw.uppercase())
    }.getOrNull()

    private fun parseConfigItemType(raw: String): ConfigItemType = runCatching {
        ConfigItemType.valueOf(raw.uppercase())
    }.getOrDefault(ConfigItemType.TEXT)

    private suspend fun saveAsUserScript(
        sessionId: String,
        relativePath: String
    ): Result {
        val content = files.readText(sessionId, relativePath)
            ?: return Result.Failure("Could not read $relativePath")
        val parse = UserScriptParser.parse(
            scriptContent = content,
            fileName = relativePath.substringAfterLast('/')
        )
        if (!parse.isValid) {
            return Result.Failure(parse.warnings.joinToString(", ").ifBlank {
                "Userscript metadata block is missing or invalid"
            })
        }
        val saved = extensionManager.addModule(parse.module)
        return saved.fold(
            onSuccess = { ok -> Result.Success(listOf(ok.id), listOf(ok.name)) },
            onFailure = { Result.Failure(it.message ?: "Userscript rejected") }
        )
    }

    private suspend fun saveAsChromeExtension(artifactRoot: File): Result {
        val (extensionId, extensionDir) = extensionFiles.allocateChromeExtensionDir()
        try {
            copyTreeIgnoringHidden(artifactRoot, extensionDir)
            val parse = ChromeExtensionParser.parseFromDirectory(
                extensionDir = extensionDir,
                overrideExtensionId = extensionId
            )
            if (!parse.isValid || parse.modules.isEmpty()) {
                extensionDir.deleteRecursively()
                return Result.Failure(
                    parse.warnings.joinToString(", ").ifBlank { "manifest.json is missing or invalid" }
                )
            }
            val ids = mutableListOf<String>()
            val names = mutableListOf<String>()
            parse.modules.forEach { module ->
                extensionManager.addModule(module).onSuccess {
                    ids += it.id
                    names += it.name
                }
            }
            return if (ids.isEmpty()) {
                extensionDir.deleteRecursively()
                Result.Failure("No modules could be saved from this Chrome extension")
            } else Result.Success(ids, names)
        } catch (t: Throwable) {
            extensionDir.deleteRecursively()
            throw t
        }
    }

    private fun copyTreeIgnoringHidden(src: File, dst: File) {
        src.walkTopDown().forEach { f ->
            if (!f.isFile) return@forEach
            val rel = f.relativeTo(src).path.replace(File.separatorChar, '/')
            if (rel.startsWith(".changes/") || rel == ".changes") return@forEach
            val target = File(dst, rel)
            target.parentFile?.mkdirs()
            f.copyTo(target, overwrite = true)
        }
    }

    companion object {
        private const val TAG = "SaveSessionAsModule"
    }
}

private fun JsonObject.optString(key: String, default: String = ""): String =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString ?: default

private fun JsonObject.optInt(key: String, default: Int = 0): Int =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.let {
        runCatching { it.asInt }.getOrDefault(default)
    } ?: default

private fun JsonObject.optBoolean(key: String, default: Boolean = false): Boolean =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.let {
        runCatching { it.asBoolean }.getOrDefault(default)
    } ?: default

private fun JsonObject.optStringArray(key: String): List<String> =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonArray }
        ?.asJsonArray
        ?.mapNotNull { (it as? JsonElement)?.takeIf { e -> !e.isJsonNull && e.isJsonPrimitive }?.asString }
        ?: emptyList()
