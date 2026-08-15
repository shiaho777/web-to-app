package com.webtoapp.data.repository

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.webtoapp.data.converter.Converters
import com.webtoapp.data.model.WebViewConfig
import java.io.File

/**
 * Named snapshots of [WebViewConfig] ("common config") that users save once and apply
 * to any app, so a heavily customized browser setup does not have to be re-configured
 * for every new app. Stored as a single JSON file in app storage — deliberately not a
 * Room entity, to keep the store schema-free as WebViewConfig evolves.
 */
object ConfigTemplateStore {

    data class ConfigTemplate(
        val name: String,
        val createdAt: Long,
        val webViewConfig: WebViewConfig
    )

    private const val FILE_NAME = "config_templates.json"
    private const val MAX_NAME_LENGTH = 40

    private val templatesType = object : TypeToken<List<ConfigTemplate>>() {}.type

    private fun storeFile(context: Context) = File(context.filesDir, FILE_NAME)

    fun list(context: Context): List<ConfigTemplate> {
        val file = storeFile(context)
        if (!file.exists()) return emptyList()
        return runCatching {
            Converters.gson.fromJson<List<ConfigTemplate>>(file.readText(), templatesType) ?: emptyList()
        }.getOrDefault(emptyList()).sortedBy { it.name.lowercase() }
    }

    fun get(context: Context, name: String): ConfigTemplate? =
        list(context).firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }

    /** Creates or overwrites (case-insensitive name match) a template. Returns false on an invalid name. */
    fun save(context: Context, name: String, config: WebViewConfig): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) return false
        val templates = list(context).filterNot { it.name.equals(trimmed, ignoreCase = true) } +
            ConfigTemplate(trimmed, System.currentTimeMillis(), config)
        return write(context, templates)
    }

    fun delete(context: Context, name: String): Boolean {
        val templates = list(context)
        val remaining = templates.filterNot { it.name.equals(name.trim(), ignoreCase = true) }
        if (remaining.size == templates.size) return false
        return write(context, remaining)
    }

    fun rename(context: Context, oldName: String, newName: String): Boolean {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) return false
        val templates = list(context)
        val target = templates.firstOrNull { it.name.equals(oldName.trim(), ignoreCase = true) } ?: return false
        if (templates.any { it.name.equals(trimmed, ignoreCase = true) && it !== target }) return false
        val updated = templates.map { if (it === target) it.copy(name = trimmed) else it }
        return write(context, updated)
    }

    private fun write(context: Context, templates: List<ConfigTemplate>): Boolean = runCatching {
        storeFile(context).writeText(Converters.gson.toJson(templates))
        true
    }.getOrDefault(false)
}
