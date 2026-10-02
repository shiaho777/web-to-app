package com.webtoapp.ui.shell

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import androidx.compose.runtime.*
import com.webtoapp.core.crypto.SecureAssetLoader
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.LrcData
import com.webtoapp.data.model.LrcLine
import java.io.File
import kotlinx.coroutines.delay

private val BGM_LRC_TIME_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")

class BgmPlayerState internal constructor(
    private val _player: MutableState<MediaPlayer?>,
    private val _currentIndex: MutableIntState,
    private val _isPlaying: MutableState<Boolean>,
    private val _currentLrcData: MutableState<LrcData?>,
    private val _currentLrcLineIndex: MutableIntState,
    private val _currentPosition: MutableLongState
) {
    var player: MediaPlayer? by _player
    var currentIndex: Int by _currentIndex
    var isPlaying: Boolean by _isPlaying
    var currentLrcData: LrcData? by _currentLrcData
    var currentLrcLineIndex: Int by _currentLrcLineIndex
    var currentPosition: Long by _currentPosition
}

/**
 * Shuffle order state. Mirrors the host [com.webtoapp.core.bgm.BgmPlayer]
 * semantics: a full permutation per cycle (no repeats), reshuffled on wrap.
 */
internal data class BgmShuffleOrder(
    val order: List<Int> = emptyList(),
    val pos: Int = 0
) {
    fun currentIndex(): Int = order.getOrElse(pos) { 0 }
}

internal fun initialBgmOrder(size: Int, shuffle: Boolean): BgmShuffleOrder {
    if (size <= 0) return BgmShuffleOrder()
    val order = if (shuffle) (0 until size).shuffled() else (0 until size).toList()
    return BgmShuffleOrder(order, 0)
}

internal fun advanceBgmOrder(state: BgmShuffleOrder, size: Int): BgmShuffleOrder {
    if (size <= 0) return BgmShuffleOrder()
    val nextPos = state.pos + 1
    if (nextPos < state.order.size && nextPos < size) {
        return state.copy(pos = nextPos)
    }
    return BgmShuffleOrder((0 until size).shuffled(), 0)
}

internal fun parseLrcText(text: String): LrcData? {
    val lines = mutableListOf<LrcLine>()

    text.lines().forEach { line ->
        BGM_LRC_TIME_REGEX.find(line)?.let { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: 0
            val seconds = match.groupValues[2].toLongOrNull() ?: 0
            val millis = match.groupValues[3].let {
                if (it.length == 2) it.toLong() * 10 else it.toLong()
            }
            val lyricText = match.groupValues[4].trim()

            if (lyricText.isNotEmpty()) {
                val startTime = minutes * 60000 + seconds * 1000 + millis
                lines.add(LrcLine(startTime = startTime, endTime = startTime + 5000, text = lyricText))
            }
        }
    }

    for (i in 0 until lines.size - 1) {
        lines[i] = lines[i].copy(endTime = lines[i + 1].startTime)
    }

    return if (lines.isNotEmpty()) LrcData(lines = lines) else null
}

@Composable
fun rememberBgmPlayerState(
    context: Context,
    config: ShellConfig,
    enabled: Boolean = true
): BgmPlayerState {

    val bgmPlayerState = remember { mutableStateOf<MediaPlayer?>(null) }
    var bgmPlayer by bgmPlayerState
    val currentBgmIndexState = remember { mutableIntStateOf(0) }
    var currentBgmIndex by currentBgmIndexState
    val isBgmPlayingState = remember { mutableStateOf(false) }
    var isBgmPlaying by isBgmPlayingState

    val currentLrcDataState = remember { mutableStateOf<LrcData?>(null) }
    var currentLrcData by currentLrcDataState
    val currentLrcLineIndexState = remember { mutableIntStateOf(-1) }
    var currentLrcLineIndex by currentLrcLineIndexState
    val bgmCurrentPositionState = remember { mutableLongStateOf(0L) }
    var bgmCurrentPosition by bgmCurrentPositionState
    val secureAssetLoader = remember(context) { SecureAssetLoader.getInstance(context) }
    val bgmTempFiles = remember { mutableMapOf<String, File>() }

    fun normalizeBgmAssetPath(path: String): String {
        return path.removePrefix("assets/").removePrefix("asset:///")
    }

    fun setBgmDataSource(player: MediaPlayer, assetPath: String) {
        val normalizedPath = normalizeBgmAssetPath(assetPath)
        if (secureAssetLoader.isEncrypted(normalizedPath)) {
            val cachedFile = bgmTempFiles[normalizedPath]
            if (cachedFile != null && cachedFile.exists()) {
                player.setDataSource(cachedFile.absolutePath)
                return
            }

            val decryptedData = secureAssetLoader.loadAsset(normalizedPath)
            val tempFile = File(context.cacheDir, "shell_bgm_${normalizedPath.hashCode()}.mp3")
            tempFile.writeBytes(decryptedData)
            bgmTempFiles[normalizedPath] = tempFile
            player.setDataSource(tempFile.absolutePath)
            AppLogger.d("ShellActivity", "BGM 解密加载成功: $normalizedPath (${decryptedData.size} bytes)")
            return
        }

        val afd: AssetFileDescriptor = context.assets.openFd(normalizedPath)
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
    }

    fun loadLrcForCurrentBgm(bgmIndex: Int) {
        if (!config.bgmShowLyrics) {
            currentLrcData = null
            return
        }

        val bgmItem = config.bgmPlaylist.getOrNull(bgmIndex) ?: return
        val lrcPath = bgmItem.lrcAssetPath ?: return

        try {
            val lrcAssetPath = normalizeBgmAssetPath(lrcPath)
            val lrcText = secureAssetLoader.loadAssetAsString(lrcAssetPath)
            currentLrcData = parseLrcText(lrcText)
            currentLrcLineIndex = -1
            AppLogger.d("ShellActivity", "LRC 加载成功: $lrcPath, ${currentLrcData?.lines?.size} 行")
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "加载 LRC 失败: $lrcPath", e)
            currentLrcData = null
        }
    }

    LaunchedEffect(config.bgmEnabled, enabled) {
        // Gated on activation resolution by the caller: an activation-gated app must not
        // autoplay BGM behind the activation gate.
        if (!enabled) return@LaunchedEffect
        if (config.bgmEnabled && config.bgmPlaylist.isNotEmpty()) {
            try {
                val isShuffle = config.bgmPlayMode == "SHUFFLE"
                var shuffleOrder = initialBgmOrder(config.bgmPlaylist.size, isShuffle)

                val player = MediaPlayer()
                val firstIndex = shuffleOrder.currentIndex()
                val firstItem = config.bgmPlaylist[firstIndex]

                setBgmDataSource(player, firstItem.assetPath)

                player.setVolume(config.bgmVolume, config.bgmVolume)
                player.isLooping = config.bgmPlayMode == "LOOP" && config.bgmPlaylist.size == 1

                player.setOnCompletionListener {

                    val nextIndex = when (config.bgmPlayMode) {
                        "SHUFFLE" -> {
                            shuffleOrder = advanceBgmOrder(shuffleOrder, config.bgmPlaylist.size)
                            shuffleOrder.currentIndex()
                        }
                        "SEQUENTIAL" -> if (currentBgmIndex + 1 < config.bgmPlaylist.size) currentBgmIndex + 1 else -1
                        else -> (currentBgmIndex + 1) % config.bgmPlaylist.size
                    }

                    if (nextIndex >= 0 && nextIndex < config.bgmPlaylist.size) {
                        currentBgmIndex = nextIndex
                        try {
                            player.reset()
                            val nextItem = config.bgmPlaylist[nextIndex]
                            setBgmDataSource(player, nextItem.assetPath)
                            player.prepare()
                            player.start()

                            loadLrcForCurrentBgm(nextIndex)
                        } catch (e: Exception) {
                            AppLogger.e("ShellActivity", "播放下一首 BGM 失败", e)
                        }
                    }
                }

                player.prepare()

                if (config.bgmAutoPlay) {
                    player.start()
                    isBgmPlaying = true
                }

                bgmPlayer = player
                currentBgmIndex = firstIndex

                loadLrcForCurrentBgm(firstIndex)

                AppLogger.d("ShellActivity", "BGM 播放器初始化成功: ${firstItem.name}")
            } catch (e: Exception) {
                AppLogger.e("ShellActivity", "初始化 BGM 播放器失败", e)
            }
        }
    }

    LaunchedEffect(isBgmPlaying, currentLrcData) {
        if (!isBgmPlaying || currentLrcData == null) return@LaunchedEffect

        while (isBgmPlaying && currentLrcData != null) {
            bgmPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        bgmCurrentPosition = mp.currentPosition.toLong()

                        val lrcData = currentLrcData
                        if (lrcData != null) {
                            val newIndex = lrcData.lines.indexOfLast { it.startTime <= bgmCurrentPosition }
                            if (newIndex != currentLrcLineIndex) {
                                currentLrcLineIndex = newIndex
                            }
                        }
                    }
                } catch (e: Exception) {

                }
            }
            delay(100)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            bgmPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            bgmTempFiles.values.forEach { file ->
                try {
                    if (file.exists()) file.delete()
                } catch (e: Exception) {

                }
            }
            bgmTempFiles.clear()
            bgmPlayer = null
        }
    }

    return BgmPlayerState(
        _player = bgmPlayerState,
        _currentIndex = currentBgmIndexState,
        _isPlaying = isBgmPlayingState,
        _currentLrcData = currentLrcDataState,
        _currentLrcLineIndex = currentLrcLineIndexState,
        _currentPosition = bgmCurrentPositionState
    )
}
