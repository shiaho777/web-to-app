package com.webtoapp.ui.shared

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale

internal data class GalleryWatchEntry(
    val videoId: String,
    val title: String,
    val positionMs: Int,
    val folderIds: List<String>
)

internal class GalleryWatchHistory(context: Context, scope: String) {
    private val preferences = context.getSharedPreferences("gallery_watch_history", Context.MODE_PRIVATE)
    private val prefix = digest(scope)
    private val latestKey = "$prefix/latest"
    private val recent = MutableStateFlow(readLatest())
    val latestEntry = recent.asStateFlow()

    fun position(videoId: String): Int = preferences.getInt(videoKey(videoId), 0)

    private fun readLatest(): GalleryWatchEntry? {
        val pending = preferences.getString(latestKey, null) ?: return null
        return runCatching {
            val json = JSONObject(pending)
            val id = json.getString("id")
            val position = position(id)
            val folders = json.getJSONArray("folders")
            if (position <= 0) null else GalleryWatchEntry(
                id, json.getString("title"), position, List(folders.length()) { folders.getString(it) }
            )
        }.getOrNull()
    }

    fun checkpoint(entry: GalleryWatchEntry) {
        if (entry.positionMs < MIN_POSITION_MS) {
            complete(entry.videoId)
            return
        }
        val key = videoKey(entry.videoId)
        if (recent.value == entry && preferences.getInt(key, 0) == entry.positionMs) return
        preferences.edit()
            .putInt(key, entry.positionMs)
            .putString(latestKey, JSONObject()
                .put("id", entry.videoId)
                .put("title", entry.title)
                .put("folders", JSONArray(entry.folderIds)).toString())
            .apply()
        recent.value = entry
    }

    fun complete(videoId: String) {
        val key = videoKey(videoId)
        if (!preferences.contains(key)) return
        val clearLatest = readLatest()?.videoId == videoId
        preferences.edit().remove(key).apply {
            if (clearLatest) remove(latestKey)
        }.apply()
        if (clearLatest) recent.value = null
    }

    private fun videoKey(videoId: String): String = "$prefix/video/${digest(videoId)}"

    private fun digest(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}

internal class GalleryVideoResume(
    private val history: GalleryWatchHistory,
    private val videoId: String,
    private val title: String,
    private val folderIds: List<String>
) {
    private var active = false
    private var completed = false
    private var lastCheckpoint = 0L

    fun restore(player: MediaPlayer, onReady: () -> Unit) {
        val position = galleryResumePosition(history.position(videoId), player.duration)
        if (position == 0) {
            onReady()
            return
        }
        player.setOnSeekCompleteListener {
            player.setOnSeekCompleteListener(null)
            onReady()
        }
        if (Build.VERSION.SDK_INT >= 26) {
            player.seekTo(position.toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            @Suppress("DEPRECATION")
            player.seekTo(position)
        }
    }

    fun setActive(player: MediaPlayer, current: Boolean, playing: Boolean) {
        if (active && (!current || !playing)) save(player)
        if (current && playing) completed = false
        active = current
    }

    fun tick(player: MediaPlayer) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastCheckpoint >= 5_000) {
            save(player)
            lastCheckpoint = now
        }
    }

    fun save(player: MediaPlayer) {
        if (!active || completed) return
        val position = runCatching { player.currentPosition }.getOrNull() ?: return
        history.checkpoint(GalleryWatchEntry(videoId, title, position, folderIds))
    }

    fun complete() {
        if (!active) return
        completed = true
        history.complete(videoId)
    }
}

private const val MIN_POSITION_MS = 1000

internal fun galleryResumePosition(savedMs: Int, durationMs: Int): Int =
    savedMs.takeIf { it >= MIN_POSITION_MS && it < durationMs } ?: 0

internal fun galleryMatchesSearch(name: String, query: String): Boolean =
    name.contains(query.trim(), ignoreCase = true)

internal fun galleryWatchTime(positionMs: Int): String {
    val seconds = positionMs / 1000
    return if (seconds >= 3600) {
        String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
    } else {
        String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
    }
}

@androidx.compose.runtime.Composable
internal fun SaveGalleryPlaybackOnStop(resume: GalleryVideoResume?, player: MediaPlayer?, ready: Boolean) {
    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
    androidx.compose.runtime.DisposableEffect(lifecycle, resume, player, ready) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP && ready && player != null) {
                resume?.save(player)
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}
