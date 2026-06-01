package com.webtoapp.core.aicoding.skill

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File

class SkillLoader(private val context: Context) {

    fun loadBundled(): List<Skill> {
        val am = context.assets
        val root = "skills"
        val out = mutableListOf<Skill>()
        val entries = try { am.list(root)?.toList().orEmpty() } catch (_: Exception) { emptyList() }
        for (folder in entries) {
            val skillMdPath = "$root/$folder/SKILL.md"
            val text = runCatching {
                am.open(skillMdPath).bufferedReader().use { it.readText() }
            }.getOrElse {
                AppLogger.w(TAG, "skipping bundled skill $folder: SKILL.md missing")
                continue
            }
            val skill = parseSkillMd(
                text = text,
                folderName = folder,
                source = Skill.Source.Bundled,
                rootDir = null
            ) ?: continue
            out += skill
        }
        AppLogger.i(TAG, "Loaded ${out.size} bundled skills")
        return out
    }

    fun loadUser(): List<Skill> = loadFromDir(userSkillsDir(), Skill.Source.User)
    fun loadMarket(): List<Skill> = loadFromDir(marketSkillsDir(), Skill.Source.Market)

    fun userSkillsDir(): File = ensureDir(File(context.filesDir, "aicoding/user_skills"))
    fun marketSkillsDir(): File = ensureDir(File(context.filesDir, "aicoding/market_skills"))

    private fun loadFromDir(root: File, source: Skill.Source): List<Skill> {
        if (!root.exists() || !root.isDirectory) return emptyList()
        val out = mutableListOf<Skill>()
        root.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val mdFile = File(dir, "SKILL.md").takeIf { it.exists() } ?: return@forEach
            val text = runCatching { mdFile.readText() }.getOrElse {
                AppLogger.w(TAG, "skipping ${source.name} skill ${dir.name}: read failed (${it.message})")
                return@forEach
            }
            val skill = parseSkillMd(text, dir.name, source, dir.absolutePath) ?: return@forEach
            out += skill
        }
        AppLogger.i(TAG, "Loaded ${out.size} ${source.name} skills from $root")
        return out
    }

    private fun parseSkillMd(
        text: String,
        folderName: String,
        source: Skill.Source,
        rootDir: String?
    ): Skill? {
        val (front, body) = SkillFrontmatter.split(text)
        val name = (front["name"] as? String)?.takeIf { it.isNotBlank() } ?: folderName
        val description = (front["description"] as? String).orEmpty()
        if (body.isBlank()) {
            AppLogger.w(TAG, "skill $name has empty body; skipping")
            return null
        }

        val allowedTools = readStringList(front["allowed_tools"])
        val implicitlyActiveFor = readStringList(front["implicitly_active_for"])
        val context = when ((front["context"] as? String)?.lowercase()) {
            "fork" -> Skill.Context.Fork
            else -> Skill.Context.Inline
        }
        val category = when ((front["category"] as? String)?.lowercase()) {
            "app" -> Skill.Category.App
            "module" -> Skill.Category.Module
            "tool" -> Skill.Category.Tool
            "custom" -> Skill.Category.Custom
            else -> when (source) {
                Skill.Source.User -> Skill.Category.Custom
                else -> Skill.Category.Tool
            }
        }
        val pinned = (front["pinned"] as? Boolean) ?: false
        val hidden = (front["hidden"] as? Boolean) ?: false
        val modelInvokable = (front["model_invokable"] as? Boolean) ?: true
        val userInvokable = (front["user_invokable"] as? Boolean) ?: true

        val starterName = (front["starter"] as? String)?.trim()?.takeIf { it.isNotBlank() }
        val starterDir = starterName
            ?.takeIf { rootDir != null }
            ?.let { File(rootDir, it).absolutePath }
        val starterAssetDir = starterName
            ?.takeIf { source == Skill.Source.Bundled }
            ?.let { "skills/$folderName/$it" }

        return Skill(
            name = name,
            description = description,
            whenToUse = (front["when_to_use"] as? String).orEmpty(),
            icon = (front["icon"] as? String) ?: "auto_awesome",
            iconColor = (front["icon_color"] as? String) ?: "9CA3AF",
            allowedTools = allowedTools,
            implicitlyActiveFor = implicitlyActiveFor,
            context = context,
            argumentHint = (front["arguments"] as? String).orEmpty(),
            promptText = body.trim(),
            source = source,
            hidden = hidden,
            pinned = pinned,
            rootDir = rootDir,
            modelInvokable = modelInvokable,
            userInvokable = userInvokable,
            starterDir = starterDir,
            starterAssetDir = starterAssetDir,
            category = category
        )
    }

    private fun readStringList(value: Any?): List<String> = when (value) {
        is List<*> -> value.filterIsInstance<String>().filter { it.isNotBlank() }
        is String -> value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        else -> emptyList()
    }

    private fun ensureDir(d: File): File { if (!d.exists()) d.mkdirs(); return d }

    companion object { private const val TAG = "SkillLoader" }
}
