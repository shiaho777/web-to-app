package com.webtoapp.core.agent.skill

import android.util.AtomicFile
import com.webtoapp.util.GsonProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.security.MessageDigest

internal data class AgentSkill(
    val id: String,
    val name: String,
    val description: String,
    val instructions: String
) {
    val sizeBytes: Int get() = instructions.toByteArray(Charsets.UTF_8).size
}

internal class AgentSkillLibrary(private val directory: File) {
    fun list(): List<AgentSkill> = directory.listFiles().orEmpty()
        .filter { it.isFile && it.extension == "json" && skillId.matches(it.nameWithoutExtension) }
        .mapNotNull { read(it.nameWithoutExtension) }
        .sortedBy { it.name.lowercase() }

    fun selected(ids: List<String>): List<AgentSkill> = buildList {
        var remaining = maxSelectedBytes
        for (id in ids.distinct()) {
            val skill = read(id) ?: continue
            val size = skill.sizeBytes
            if (size <= remaining) {
                add(skill)
                remaining -= size
            }
        }
    }

    fun import(input: InputStream, displayName: String): AgentSkill {
        val markdown = decode(readBounded(input))
        val skill = parse(markdown, displayName)
        check(directory.isDirectory || directory.mkdirs() || directory.isDirectory)
        val file = AtomicFile(directory.resolve("${skill.id}.json"))
        val output = file.startWrite()
        try {
            output.write(GsonProvider.gson.toJson(skill).toByteArray(Charsets.UTF_8))
            file.finishWrite(output)
        } catch (failure: Exception) {
            file.failWrite(output)
            throw failure
        }
        return skill
    }

    fun remove(id: String) {
        if (skillId.matches(id)) AtomicFile(directory.resolve("$id.json")).delete()
    }

    private fun read(id: String): AgentSkill? {
        if (!skillId.matches(id)) return null
        val file = AtomicFile(directory.resolve("$id.json"))
        return runCatching {
            val data = file.openRead().bufferedReader().use {
                GsonProvider.gson.fromJson(it, AgentSkill::class.java)
            }
            data?.takeIf { it.id == id && it.instructions.isNotBlank() && it.sizeBytes <= maxBytes }
        }.getOrNull()
    }

    companion object {
        const val maxBytes = 24 * 1024
        const val maxSelectedBytes = 32 * 1024
        private val skillId = Regex("[a-f0-9]{64}")
        private val heading = Regex("(?m)^#{1,6} +(.+)$")

        internal fun parse(markdown: String, displayName: String): AgentSkill {
            val text = markdown.removePrefix("\uFEFF").replace("\r\n", "\n").trim()
            require(text.isNotBlank() && '\u0000' !in text)
            val headerEnd = if (text.startsWith("---\n")) text.indexOf("\n---\n", 4) else -1
            val metadata = if (headerEnd >= 0) text.substring(4, headerEnd).lineSequence()
                .filter { it.firstOrNull()?.isWhitespace() == false }
                .filter { ':' in it }
                .associate { it.substringBefore(':') to it.substringAfter(':').trim().trim('"', '\'') }
            else emptyMap()
            val body = if (headerEnd >= 0) text.substring(headerEnd + 5).trim() else text
            require(body.isNotBlank())
            val name = metadata["name"]?.takeIf { it.isNotBlank() }
                ?: heading.find(body)?.groupValues?.get(1)
                ?: displayName.substringBeforeLast('.')
            val id = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            return AgentSkill(id, name.trim().ifBlank { "SKILL.md" }.take(80), metadata["description"].orEmpty().take(300), body)
        }

        private fun readBounded(input: InputStream): ByteArray {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var remaining = maxBytes + 1
            while (remaining > 0) {
                val read = input.read(buffer, 0, minOf(buffer.size, remaining))
                if (read < 0) break
                output.write(buffer, 0, read)
                remaining -= read
            }
            require(remaining > 0)
            return output.toByteArray()
        }

        private fun decode(bytes: ByteArray): String =
            try {
                Charsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString()
            } catch (e: CharacterCodingException) {
                throw IllegalArgumentException(e)
            }
    }
}
