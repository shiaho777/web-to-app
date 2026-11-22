package com.webtoapp.core.appmodifier

import android.content.Context
import com.webtoapp.util.GsonProvider
import java.io.File
import java.util.UUID

object DialogEngineProjectStore {
    private const val DIRECTORY = "dialog_engine"

    fun projectDirectory(context: Context): File =
        File(context.filesDir, DIRECTORY).apply { mkdirs() }

    fun save(context: Context, project: DialogEngineProject, name: String = "Dialog"): File {
        val safeName = name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "dialog" }
        val file = File(projectDirectory(context), "$safeName.json")
        val payload = mapOf(
            "name" to name,
            "id" to UUID.nameUUIDFromBytes(name.toByteArray()).toString(),
            "project" to project
        )
        file.writeText(GsonProvider.gson.toJson(payload))
        return file
    }

    fun list(context: Context): List<File> =
        projectDirectory(context).listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

    fun load(context: Context, file: File): DialogEngineProject? =
        runCatching {
            val root = GsonProvider.gson.fromJson(file.readText(), Map::class.java)
            val projectJson = GsonProvider.gson.toJson(root["project"])
            DialogEngineProject.fromJson(projectJson)
        }.getOrNull()

    fun delete(context: Context, file: File): Boolean =
        runCatching { file.delete() }.getOrDefault(false)
}
