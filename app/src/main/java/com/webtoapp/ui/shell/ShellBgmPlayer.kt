package com.webtoapp.ui.shell

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.*
import com.webtoapp.core.bgm.BgmMediaNotifier
import com.webtoapp.core.bgm.BgmPreviousAction
import com.webtoapp.core.bgm.BgmTransport
import com.webtoapp.core.bgm.bgmControlEnabled
import com.webtoapp.core.bgm.bgmManualNextIndex
import com.webtoapp.core.bgm.bgmPreviousAction
import com.webtoapp.core.bgm.bgmPreviousLinearIndex
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
    private val _currentPosition: MutableLongState,
    private val _title: MutableState<String>,
    private val transport: BgmTransport
) {
    var player: MediaPlayer? by _player
    var currentIndex: Int by _currentIndex
    var isPlaying: Boolean by _isPlaying
    var currentLrcData: LrcData? by _currentLrcData
    var currentLrcLineIndex: Int by _currentLrcLineIndex
    var currentPosition: Long by _currentPosition
    var title: String by _title

    fun toggle() {
        if (isPlaying) transport.pause() else transport.play()
    }

    fun next() = transport.next()

    fun previous() = transport.previous()
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

private class ShellBgmSession(
    private val context: Context,
    initialConfig: ShellConfig,
    private val secureAssetLoader: SecureAssetLoader,
    private val playerState: MutableState<MediaPlayer?>,
    private val indexState: MutableIntState,
    private val playingState: MutableState<Boolean>,
    private val lrcState: MutableState<LrcData?>,
    private val lrcLineState: MutableIntState,
    private val positionState: MutableLongState,
    private val titleState: MutableState<String>,
    private val tempFiles: MutableMap<String, File>
) {
    var config: ShellConfig = initialConfig
    private var order: BgmShuffleOrder = BgmShuffleOrder()

    fun startInitial(autoPlay: Boolean) {
        val playlist = config.bgmPlaylist
        if (playlist.isEmpty()) return
        order = initialBgmOrder(playlist.size, config.bgmPlayMode == "SHUFFLE")
        val index = order.currentIndex().coerceIn(0, playlist.lastIndex)
        preparePlayer(index, autoPlay)
    }

    fun play() {
        val mp = playerState.value
        if (mp == null) {
            startInitial(autoPlay = true)
            return
        }
        try {
            mp.start()
            playingState.value = true
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "恢复 BGM 失败", e)
            startInitial(autoPlay = true)
        }
    }

    fun pause() {
        try {
            playerState.value?.pause()
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "暂停 BGM 失败", e)
        }
        playingState.value = false
    }

    fun next() {
        val size = config.bgmPlaylist.size
        if (size == 0) return
        val index = if (config.bgmPlayMode == "SHUFFLE") {
            if (order.order.isEmpty()) order = initialBgmOrder(size, true)
            order = advanceBgmOrder(order, size)
            order.currentIndex()
        } else {
            bgmManualNextIndex(indexState.intValue, size)
        }
        preparePlayer(index, autoStart = true)
    }

    fun previous() {
        val size = config.bgmPlaylist.size
        if (size == 0) return
        val position = try {
            playerState.value?.currentPosition?.toLong() ?: positionState.longValue
        } catch (e: Exception) {
            positionState.longValue
        }
        if (bgmPreviousAction(position) == BgmPreviousAction.RESTART) {
            seek(0L)
            return
        }
        val index = if (config.bgmPlayMode == "SHUFFLE") {
            if (order.order.isEmpty()) order = initialBgmOrder(size, true)
            order = order.copy(pos = bgmPreviousLinearIndex(order.pos, order.order.size))
            order.currentIndex()
        } else {
            bgmPreviousLinearIndex(indexState.intValue, size)
        }
        preparePlayer(index, autoStart = true)
    }

    fun seek(positionMs: Long) {
        try {
            playerState.value?.seekTo(positionMs.coerceAtLeast(0L).toInt())
            positionState.longValue = positionMs.coerceAtLeast(0L)
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "BGM 拖动进度失败", e)
        }
    }

    fun release() {
        playerState.value?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
            }
            runCatching { player.release() }
        }
        playerState.value = null
        playingState.value = false
        tempFiles.values.forEach { file ->
            runCatching { if (file.exists()) file.delete() }
        }
        tempFiles.clear()
    }

    private fun preparePlayer(index: Int, autoStart: Boolean) {
        val item = config.bgmPlaylist.getOrNull(index) ?: return
        val player = playerState.value ?: MediaPlayer().also { playerState.value = it }
        try {
            player.reset()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setBgmDataSource(player, item.assetPath)
            player.setVolume(config.bgmVolume, config.bgmVolume)
            player.isLooping = config.bgmPlayMode == "LOOP" && config.bgmPlaylist.size == 1
            player.setOnCompletionListener { onCompleted() }
            player.prepare()
            indexState.intValue = index
            titleState.value = item.name
            positionState.longValue = 0L
            loadLrc(index)
            if (autoStart) {
                player.start()
                playingState.value = true
            } else {
                playingState.value = false
            }
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "播放 BGM 失败: ${item.name}", e)
            playingState.value = false
        }
    }

    private fun onCompleted() {
        val size = config.bgmPlaylist.size
        if (size == 0) {
            playingState.value = false
            return
        }
        val nextIndex = when (config.bgmPlayMode) {
            "SHUFFLE" -> {
                order = advanceBgmOrder(order, size)
                order.currentIndex()
            }
            "SEQUENTIAL" -> {
                val current = indexState.intValue
                if (current + 1 < size) current + 1 else -1
            }
            else -> bgmManualNextIndex(indexState.intValue, size)
        }
        if (nextIndex in 0 until size) {
            preparePlayer(nextIndex, autoStart = true)
        } else {
            playingState.value = false
        }
    }

    private fun normalizeBgmAssetPath(path: String): String {
        return path.removePrefix("assets/").removePrefix("asset:///")
    }

    private fun setBgmDataSource(player: MediaPlayer, assetPath: String) {
        val normalizedPath = normalizeBgmAssetPath(assetPath)
        if (secureAssetLoader.isEncrypted(normalizedPath)) {
            val cachedFile = tempFiles[normalizedPath]
            if (cachedFile != null && cachedFile.exists()) {
                player.setDataSource(cachedFile.absolutePath)
                return
            }

            val decryptedData = secureAssetLoader.loadAsset(normalizedPath)
            val tempFile = File(context.cacheDir, "shell_bgm_${normalizedPath.hashCode()}.mp3")
            tempFile.writeBytes(decryptedData)
            tempFiles[normalizedPath] = tempFile
            player.setDataSource(tempFile.absolutePath)
            AppLogger.d("ShellActivity", "BGM 解密加载成功: $normalizedPath (${decryptedData.size} bytes)")
            return
        }

        val afd: AssetFileDescriptor = context.assets.openFd(normalizedPath)
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
    }

    private fun loadLrc(bgmIndex: Int) {
        if (!config.bgmShowLyrics) {
            lrcState.value = null
            lrcLineState.intValue = -1
            return
        }
        val bgmItem = config.bgmPlaylist.getOrNull(bgmIndex) ?: return
        val lrcPath = bgmItem.lrcAssetPath
        if (lrcPath == null) {
            lrcState.value = null
            lrcLineState.intValue = -1
            return
        }
        try {
            val lrcText = secureAssetLoader.loadAssetAsString(normalizeBgmAssetPath(lrcPath))
            lrcState.value = parseLrcText(lrcText)
            lrcLineState.intValue = -1
            AppLogger.d("ShellActivity", "LRC 加载成功: $lrcPath, ${lrcState.value?.lines?.size} 行")
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "加载 LRC 失败: $lrcPath", e)
            lrcState.value = null
            lrcLineState.intValue = -1
        }
    }
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
    val isBgmPlayingState = remember { mutableStateOf(false) }
    var isBgmPlaying by isBgmPlayingState
    val currentLrcDataState = remember { mutableStateOf<LrcData?>(null) }
    var currentLrcData by currentLrcDataState
    val currentLrcLineIndexState = remember { mutableIntStateOf(-1) }
    var currentLrcLineIndex by currentLrcLineIndexState
    val bgmCurrentPositionState = remember { mutableLongStateOf(0L) }
    var bgmCurrentPosition by bgmCurrentPositionState
    val titleState = remember { mutableStateOf("") }
    val secureAssetLoader = remember(context) { SecureAssetLoader.getInstance(context) }
    val bgmTempFiles = remember { mutableMapOf<String, File>() }
    val transport = remember { BgmTransport() }
    val notifier = remember(context) { BgmMediaNotifier(context, transport) }
    val session = remember(context) {
        ShellBgmSession(
            context = context,
            initialConfig = config,
            secureAssetLoader = secureAssetLoader,
            playerState = bgmPlayerState,
            indexState = currentBgmIndexState,
            playingState = isBgmPlayingState,
            lrcState = currentLrcDataState,
            lrcLineState = currentLrcLineIndexState,
            positionState = bgmCurrentPositionState,
            titleState = titleState,
            tempFiles = bgmTempFiles
        )
    }
    session.config = config
    transport.play = { session.play() }
    transport.pause = { session.pause() }
    transport.next = { session.next() }
    transport.previous = { session.previous() }
    transport.seek = { session.seek(it) }

    LaunchedEffect(config.bgmEnabled, enabled) {
        if (!enabled || !session.config.bgmEnabled || session.config.bgmPlaylist.isEmpty()) return@LaunchedEffect
        try {
            session.startInitial(autoPlay = session.config.bgmAutoPlay)
            AppLogger.d("ShellActivity", "BGM 播放器初始化成功: ${session.config.bgmPlaylist.getOrNull(currentBgmIndexState.intValue)?.name}")
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "初始化 BGM 播放器失败", e)
        }
    }

    LaunchedEffect(isBgmPlaying) {
        if (!isBgmPlaying) return@LaunchedEffect
        while (isBgmPlaying) {
            val mp = bgmPlayer
            if (mp != null) {
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

    val showNotification = enabled && config.bgmEnabled && bgmControlEnabled(config.bgmShowNotificationPlayer)
    LaunchedEffect(showNotification) {
        if (!showNotification) {
            notifier.hide()
            return@LaunchedEffect
        }
        while (true) {
            val mp = bgmPlayerState.value
            if (mp != null) {
                val playing = try {
                    mp.isPlaying
                } catch (e: Exception) {
                    false
                }
                if (playing != isBgmPlayingState.value) {
                    isBgmPlayingState.value = playing
                }
                val position = try {
                    mp.currentPosition.toLong()
                } catch (e: Exception) {
                    0L
                }
                val duration = try {
                    mp.duration.toLong()
                } catch (e: Exception) {
                    0L
                }
                notifier.publish(titleState.value, playing, position, duration)
            }
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            notifier.release()
            session.release()
        }
    }

    return BgmPlayerState(
        _player = bgmPlayerState,
        _currentIndex = currentBgmIndexState,
        _isPlaying = isBgmPlayingState,
        _currentLrcData = currentLrcDataState,
        _currentLrcLineIndex = currentLrcLineIndexState,
        _currentPosition = bgmCurrentPositionState,
        _title = titleState,
        transport = transport
    )
}
