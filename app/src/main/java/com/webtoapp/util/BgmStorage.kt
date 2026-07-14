package com.webtoapp.util

import android.content.Context
import android.net.Uri
import com.webtoapp.data.model.BgmItem
import com.webtoapp.data.model.LrcData
import com.webtoapp.data.model.LrcLine
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object BgmStorage {

    private const val TAG = "BgmStorage"

    private const val BGM_DIR = "bgm"

    private const val ASSETS_BGM_DIR = "bgm"

    private val LRC_TIME_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
    private val LRC_META_REGEX = Regex("""\[(ti|ar|al):(.*)]""", RegexOption.IGNORE_CASE)

    private val SAFE_NAME_REGEX = Regex("[^a-zA-Z0-9\u4e00-\u9fa5_-]")

    private val IMAGE_COVER_EXTENSIONS = setOf("png", "jpg", "jpeg", "jpe", "jfif", "webp", "bmp", "gif", "heic", "heif")

    fun getBgmDir(context: Context): File {
        val dir = File(context.filesDir, BGM_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun scanAllBgm(context: Context): List<BgmItem> {
        val result = mutableListOf<BgmItem>()

        result.addAll(scanAssetsBgm(context))

        result.addAll(scanUserBgm(context))

        return result
    }

    fun scanAssetsBgm(context: Context): List<BgmItem> {
        val result = mutableListOf<BgmItem>()

        try {
            val assetManager = context.assets
            val files = assetManager.list(ASSETS_BGM_DIR) ?: return emptyList()

            val mp3Files = files.filter { it.lowercase().endsWith(".mp3") }

            for (mp3File in mp3Files) {
                val nameWithoutExt = mp3File.substringBeforeLast(".")

                val coverFile = files.find { file ->
                    val fileNameWithoutExt = file.substringBeforeLast(".")
                    val ext = normalizeCoverExtension(file.substringAfterLast(".").lowercase())
                    fileNameWithoutExt.equals(nameWithoutExt, ignoreCase = true) &&
                        ext in IMAGE_COVER_EXTENSIONS
                }

                val bgmPath = "asset:///$ASSETS_BGM_DIR/$mp3File"
                val userLrcFile = File(getBgmDir(context), "$nameWithoutExt.lrc")
                val lrcData = if (userLrcFile.exists()) {

                    loadLrcFromFile(userLrcFile)
                } else {

                    val assetLrcFile = files.find { file ->
                        val fileNameWithoutExt = file.substringBeforeLast(".")
                        val ext = file.substringAfterLast(".").lowercase()
                        fileNameWithoutExt == nameWithoutExt && ext == "lrc"
                    }
                    assetLrcFile?.let { loadLrcFromAssets(context, "$ASSETS_BGM_DIR/$it") }
                }

                result.add(BgmItem(
                    name = nameWithoutExt,
                    path = bgmPath,
                    coverPath = coverFile?.let { "asset:///$ASSETS_BGM_DIR/$it" },
                    isAsset = true,
                    lrcData = lrcData
                ))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
        }

        return result
    }

    fun scanUserBgm(context: Context): List<BgmItem> {
        val result = mutableListOf<BgmItem>()
        val bgmDir = getBgmDir(context)

        if (!bgmDir.exists()) return emptyList()

        val files = bgmDir.listFiles() ?: return emptyList()

        val mp3Files = files.filter { it.extension.lowercase() == "mp3" }

        for (mp3File in mp3Files) {
            val nameWithoutExt = mp3File.nameWithoutExtension

            val coverFile = files.find { file ->
                file.nameWithoutExtension.equals(nameWithoutExt, ignoreCase = true) &&
                    normalizeCoverExtension(file.extension.lowercase()) in IMAGE_COVER_EXTENSIONS
            }

            val lrcFile = files.find { file ->
                file.nameWithoutExtension == nameWithoutExt &&
                file.extension.lowercase() == "lrc"
            }

            val lrcData = lrcFile?.let { loadLrcFromFile(it) }

            result.add(BgmItem(
                name = nameWithoutExt,
                path = mp3File.absolutePath,
                coverPath = coverFile?.absolutePath,
                isAsset = false,
                lrcData = lrcData
            ))
        }

        return result
    }

    fun saveBgm(context: Context, uri: Uri, customName: String? = null): String? {
        return try {
            val bgmDir = getBgmDir(context)

            val safeName = sanitizeBgmName(customName)
            val fileName = "${safeName}.mp3"
            val destFile = File(bgmDir, fileName)

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                AppLogger.e(TAG, "无法打开音频文件: $uri")
                return null
            }

            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    val bytes = input.copyTo(output)
                    AppLogger.d(TAG, "音频文件已保存: ${destFile.absolutePath}, 大小: $bytes bytes")
                }
            }

            if (!destFile.exists() || destFile.length() == 0L) {
                AppLogger.e(TAG, "音频文件保存失败或为空: ${destFile.absolutePath}")
                return null
            }

            destFile.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "保存音频文件异常", e)
            null
        }
    }

    fun saveCover(context: Context, uri: Uri, bgmName: String): String? {
        return try {
            val bgmDir = getBgmDir(context)
            val safeName = sanitizeBgmName(bgmName)
            val extension = resolveCoverExtension(context, uri)

            IMAGE_COVER_EXTENSIONS.forEach { ext ->
                val stale = File(bgmDir, "$safeName.$ext")
                if (stale.exists()) {
                    stale.delete()
                }
            }

            val destFile = File(bgmDir, "$safeName.$extension")
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                AppLogger.e(TAG, "无法打开封面文件: $uri")
                return null
            }

            inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!destFile.exists() || destFile.length() == 0L) {
                AppLogger.e(TAG, "封面文件保存失败或为空: ${destFile.absolutePath}")
                return null
            }

            destFile.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "保存封面失败", e)
            null
        }
    }

    private fun sanitizeBgmName(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) {
            return "bgm_${UUID.randomUUID()}"
        }
        return value.replace(SAFE_NAME_REGEX, "_")
    }

    internal fun resolveCoverExtension(context: Context, uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)?.lowercase().orEmpty()
        extensionFromMime(mimeType)?.let { return it }

        val pathHint = sequenceOf(
            uri.lastPathSegment,
            uri.path
        ).filterNotNull()
            .map { it.substringAfterLast('/') }
            .map { it.substringAfterLast('.', missingDelimiterValue = "") }
            .map { it.lowercase() }
            .firstOrNull { it in IMAGE_COVER_EXTENSIONS }
        if (pathHint != null) {
            return normalizeCoverExtension(pathHint)
        }

        val magic = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val header = ByteArray(12)
                val read = input.read(header)
                if (read <= 0) null else header.copyOf(read)
            }
        }.getOrNull()
        extensionFromMagic(magic)?.let { return it }

        return "jpg"
    }

    private fun extensionFromMime(mimeType: String): String? {
        return when {
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            mimeType.contains("gif") -> "gif"
            mimeType.contains("bmp") -> "bmp"
            mimeType.contains("heic") || mimeType.contains("heif") -> "heic"
            else -> null
        }
    }

    private fun extensionFromMagic(header: ByteArray?): String? {
        if (header == null || header.isEmpty()) return null
        if (header.size >= 3 &&
            header[0] == 0xFF.toByte() &&
            header[1] == 0xD8.toByte() &&
            header[2] == 0xFF.toByte()
        ) {
            return "jpg"
        }
        if (header.size >= 8 &&
            header[0] == 0x89.toByte() &&
            header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() &&
            header[3] == 0x47.toByte()
        ) {
            return "png"
        }
        if (header.size >= 6) {
            val gif = String(header, 0, 6, Charsets.US_ASCII)
            if (gif == "GIF87a" || gif == "GIF89a") return "gif"
        }
        if (header.size >= 12) {
            val riff = String(header, 0, 4, Charsets.US_ASCII)
            val webp = String(header, 8, 4, Charsets.US_ASCII)
            if (riff == "RIFF" && webp == "WEBP") return "webp"
        }
        if (header.size >= 2 && header[0] == 0x42.toByte() && header[1] == 0x4D.toByte()) {
            return "bmp"
        }
        return null
    }

    private fun normalizeCoverExtension(ext: String): String {
        return when (ext.lowercase()) {
            "jpeg", "jpe", "jfif" -> "jpg"
            else -> ext.lowercase()
        }
    }

    fun deleteBgm(context: Context, bgmItem: BgmItem): Boolean {
        if (bgmItem.isAsset) return false

        return try {

            File(bgmItem.path).delete()

            bgmItem.coverPath?.let { File(it).delete() }

            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            false
        }
    }

    fun getBgmInputStream(context: Context, path: String): java.io.InputStream? {
        return try {
            if (path.startsWith("asset:///")) {
                val assetPath = path.removePrefix("asset:///")
                context.assets.open(assetPath)
            } else {
                File(path).inputStream()
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            null
        }
    }

    fun copyBgmToDir(context: Context, bgmItem: BgmItem, destDir: File): File? {
        return try {
            val destFile = File(destDir, "${bgmItem.name}.mp3")

            getBgmInputStream(context, bgmItem.path)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            null
        }
    }

    fun saveLrc(context: Context, bgmPath: String, lrcData: LrcData): Boolean {
        return try {

            val lrcPath = getLrcPathForBgm(context, bgmPath)
            val lrcFile = File(lrcPath)

            lrcFile.parentFile?.mkdirs()

            val lrcContent = buildString {
                appendLine("[ti:${lrcData.title ?: ""}]")
                appendLine("[ar:${lrcData.artist ?: ""}]")
                appendLine("[al:${lrcData.album ?: ""}]")
                appendLine()

                lrcData.lines.forEach { line ->
                    val minutes = (line.startTime / 60000).toInt()
                    val seconds = ((line.startTime % 60000) / 1000).toInt()
                    val millis = ((line.startTime % 1000) / 10).toInt()
                    appendLine("[%02d:%02d.%02d]%s".format(minutes, seconds, millis, line.text))
                }
            }

            lrcFile.writeText(lrcContent, Charsets.UTF_8)
            AppLogger.d(TAG, "LRC 保存成功: $lrcPath")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "保存 LRC 失败", e)
            false
        }
    }

    fun loadLrcFromFile(lrcFile: File): LrcData? {
        return try {
            if (!lrcFile.exists()) return null
            parseLrcText(lrcFile.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            AppLogger.e(TAG, "加载 LRC 失败: ${lrcFile.path}", e)
            null
        }
    }

    fun loadLrcFromAssets(context: Context, assetPath: String): LrcData? {
        return try {
            val text = context.assets.open(assetPath).bufferedReader().readText()
            parseLrcText(text)
        } catch (e: Exception) {
            AppLogger.e(TAG, "从 assets 加载 LRC 失败: $assetPath", e)
            null
        }
    }

    private fun parseLrcText(text: String): LrcData? {
        val lines = mutableListOf<LrcLine>()
        var title: String? = null
        var artist: String? = null
        var album: String? = null

        val timeRegex = LRC_TIME_REGEX
        val metaRegex = LRC_META_REGEX

        text.lines().forEach { line ->

            metaRegex.find(line)?.let { match ->
                when (match.groupValues[1].lowercase()) {
                    "ti" -> title = match.groupValues[2].trim()
                    "ar" -> artist = match.groupValues[2].trim()
                    "al" -> album = match.groupValues[2].trim()
                }
                return@forEach
            }

            timeRegex.find(line)?.let { match ->
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

        return if (lines.isNotEmpty()) {
            LrcData(lines = lines, title = title, artist = artist, album = album)
        } else null
    }

    fun getLrcPathForBgm(context: Context, bgmPath: String): String {
        return if (bgmPath.startsWith("asset:///")) {

            val assetPath = bgmPath.removePrefix("asset:///")
            val name = File(assetPath).nameWithoutExtension
            File(getBgmDir(context), "$name.lrc").absolutePath
        } else {

            val musicFile = File(bgmPath)
            File(musicFile.parent, "${musicFile.nameWithoutExtension}.lrc").absolutePath
        }
    }

    fun hasLrc(context: Context, bgmPath: String): Boolean {
        val lrcPath = getLrcPathForBgm(context, bgmPath)
        return File(lrcPath).exists()
    }

    fun loadLrc(context: Context, bgmPath: String): LrcData? {
        val lrcPath = getLrcPathForBgm(context, bgmPath)
        val lrcFile = File(lrcPath)
        return if (lrcFile.exists()) loadLrcFromFile(lrcFile) else null
    }
}
