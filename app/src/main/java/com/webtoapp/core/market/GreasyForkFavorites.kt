package com.webtoapp.core.market

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class GfFavorite(
    val scriptId: Long,
    val name: String,
    val description: String,
    val codeUrl: String,
    val pageUrl: String,
    val version: String,
    val author: String,
    val fanScore: Double,
    val totalInstalls: Long,
    val savedAt: Long
) {
    companion object {
        fun fromResult(result: GfSearchResult): GfFavorite = GfFavorite(
            scriptId = result.id,
            name = result.name,
            description = result.description,
            codeUrl = result.codeUrl,
            pageUrl = result.pageUrl,
            version = result.version,
            author = result.author,
            fanScore = result.fanScore,
            totalInstalls = result.totalInstalls,
            savedAt = System.currentTimeMillis()
        )
    }
}

class GreasyForkFavorites private constructor(private val context: Context) {

    suspend fun load(): List<GfFavorite> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext emptyList()
            val text = file.readText()
            if (text.isBlank()) return@withContext emptyList()
            val type = object : TypeToken<List<GfFavorite>>() {}.type
            gson.fromJson<List<GfFavorite>>(text, type) ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e(TAG, "load favorites failed", e)
            emptyList()
        }
    }

    suspend fun add(favorite: GfFavorite): List<GfFavorite> = mutex.withLock {
        val current = loadSync().toMutableList()
        current.removeAll { it.scriptId == favorite.scriptId }
        current.add(0, favorite)
        saveSync(current)
        current
    }

    suspend fun remove(scriptId: Long): List<GfFavorite> = mutex.withLock {
        val current = loadSync().toMutableList()
        current.removeAll { it.scriptId == scriptId }
        saveSync(current)
        current
    }

    private val gson: Gson by lazy {
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    }
    private val mutex = Mutex()
    private val file: File by lazy { File(context.filesDir, FILE_NAME) }

    private fun loadSync(): List<GfFavorite> {
        return try {
            if (!file.exists()) return emptyList()
            val text = file.readText()
            if (text.isBlank()) return emptyList()
            val type = object : TypeToken<List<GfFavorite>>() {}.type
            gson.fromJson<List<GfFavorite>>(text, type) ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e(TAG, "loadSync failed", e)
            emptyList()
        }
    }

    private fun saveSync(favorites: List<GfFavorite>) {
        try {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, "$FILE_NAME.tmp")
            tmp.writeText(gson.toJson(favorites))
            if (file.exists()) file.delete()
            tmp.renameTo(file)
        } catch (e: Exception) {
            AppLogger.e(TAG, "saveSync failed", e)
        }
    }

    companion object {
        private const val TAG = "GfFavorites"
        private const val FILE_NAME = "greasyfork_favorites.json"

        @Volatile
        private var instance: GreasyForkFavorites? = null

        fun getInstance(context: Context): GreasyForkFavorites {
            val appContext = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: GreasyForkFavorites(appContext).also { instance = it }
            }
        }
    }
}
