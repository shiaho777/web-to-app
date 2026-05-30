package com.webtoapp.core.aicoding.skill

import com.webtoapp.core.logging.AppLogger
import java.io.File

class UserSkillRepository(private val loader: SkillLoader) {

    fun save(input: SkillEditorInput): Result<File> = runCatching {
        val name = input.name.trim()
        require(name.isNotEmpty()) { "Skill name is required" }
        require(NAME_REGEX.matches(name)) { "Skill name must be kebab-case (letters, digits, hyphens)" }
        val body = input.body.trim()
        require(body.isNotEmpty()) { "Skill body is required" }

        val root = loader.userSkillsDir()
        val dir = File(root, name)
        if (!dir.exists()) dir.mkdirs()
        val skillMd = File(dir, "SKILL.md")
        skillMd.writeText(serialise(input))
        AppLogger.i(TAG, "Saved user skill at ${skillMd.absolutePath}")
        dir
    }

    fun delete(name: String): Boolean {
        val dir = File(loader.userSkillsDir(), name)
        if (!dir.exists()) return false
        return dir.deleteRecursively()
    }

    fun exists(name: String): Boolean {
        val dir = File(loader.userSkillsDir(), name)
        return dir.exists() && File(dir, "SKILL.md").exists()
    }

    fun load(name: String): SkillEditorInput? {
        val dir = File(loader.userSkillsDir(), name)
        val md = File(dir, "SKILL.md").takeIf { it.exists() } ?: return null
        val text = runCatching { md.readText() }.getOrNull() ?: return null
        val (front, body) = SkillFrontmatter.split(text)
        return SkillEditorInput(
            name = (front["name"] as? String) ?: name,
            description = (front["description"] as? String).orEmpty(),
            whenToUse = (front["when_to_use"] as? String).orEmpty(),
            icon = (front["icon"] as? String) ?: "auto_awesome",
            iconColor = (front["icon_color"] as? String) ?: "9CA3AF",
            argumentHint = (front["arguments"] as? String).orEmpty(),
            body = body.trim()
        )
    }

    private fun serialise(input: SkillEditorInput): String = buildString {
        appendLine("---")
        appendLine("name: ${input.name.trim()}")
        if (input.description.isNotBlank()) {
            appendLine("description: ${quote(input.description.trim())}")
        }
        if (input.whenToUse.isNotBlank()) {
            appendLine("when_to_use: ${quote(input.whenToUse.trim())}")
        }
        appendLine("icon: ${input.icon.ifBlank { "auto_awesome" }}")
        appendLine("icon_color: ${input.iconColor.ifBlank { "9CA3AF" }}")
        appendLine("category: custom")
        if (input.argumentHint.isNotBlank()) {
            appendLine("arguments: ${quote(input.argumentHint.trim())}")
        }
        appendLine("---")
        appendLine()
        append(input.body.trim())
        appendLine()
    }

    private fun quote(s: String): String {

        val needsQuotes = s.contains(':') || s.contains('#') ||
            s.startsWith('-') || s.startsWith('[')
        if (!needsQuotes) return s
        val escaped = s.replace("\\", "\\\\").replace("\"", "\\\"")
        return "\"$escaped\""
    }

    companion object {
        private const val TAG = "UserSkillRepo"

        private val NAME_REGEX = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")
    }
}

data class SkillEditorInput(
    val name: String,
    val description: String,
    val whenToUse: String,
    val icon: String = "auto_awesome",
    val iconColor: String = "9CA3AF",
    val argumentHint: String = "",
    val body: String
)
