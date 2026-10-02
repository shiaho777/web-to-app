package com.webtoapp.core.linux

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.*

/**
 * Host-side export optimizer: recompresses/minifies project assets while the APK
 * stream is being written. Uses the esbuild toolchain when present, so it stays
 * host-only and is excluded from the shell source sync.
 */
object PerformanceOptimizerApk {

    private const val TAG = "PerformanceOptimizerApk"

    private const val CACHE_DIR = "perf_optimize_cache"

    private const val IMAGE_COMPRESS_THRESHOLD = 10 * 1024L

    private const val CODE_MINIFY_THRESHOLD = 512L

    suspend fun optimizeBytesForApk(
        context: Context,
        fileName: String,
        data: ByteArray,
        config: PerformanceOptimizer.OptimizeConfig = PerformanceOptimizer.OptimizeConfig()
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            when (ext) {
                "png", "jpg", "jpeg" -> {
                    if (config.compressImages && data.size > IMAGE_COMPRESS_THRESHOLD) {
                        compressImageBytes(data, ext, config) ?: data
                    } else data
                }
                "js" -> {
                    if (config.minifyCode && data.size > CODE_MINIFY_THRESHOLD && !fileName.endsWith(".min.js")) {
                        minifyCodeBytes(context, data, "js", config) ?: data
                    } else data
                }
                "css" -> {
                    if (config.minifyCode && data.size > CODE_MINIFY_THRESHOLD && !fileName.endsWith(".min.css")) {
                        minifyCodeBytes(context, data, "css", config) ?: data
                    } else data
                }
                "svg" -> {
                    if (config.minifySvg) {
                        minifySvgBytes(data) ?: data
                    } else data
                }
                else -> data
            }
        } catch (e: Exception) {
            AppLogger.d(TAG, "字节流优化失败: $fileName", e)
            data
        }
    }

    fun getCacheDir(context: Context): File {
        return File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
    }

    fun getRemovableEntries(entryName: String, appType: String): Boolean {

        if (entryName.startsWith("res/") && entryName.contains("-")) {
            val dirName = entryName.substringAfter("res/").substringBefore("/")

            if (dirName.startsWith("values-") && !dirName.startsWith("values-zh") &&
                !dirName.startsWith("values-en") && !dirName.startsWith("values-ar")) {
                return true
            }
        }

        if (entryName.startsWith("assets/template/") && appType != "FRONTEND") {
            return true
        }

        if (entryName.startsWith("assets/bgm/default") && appType != "WEB") {
            return true
        }

        return false
    }

    private fun compressImageBytes(data: ByteArray, ext: String, config: PerformanceOptimizer.OptimizeConfig): ByteArray? {
        // Full resolution is the point here (recompression); contain OOM instead:
        // a hostile image skips optimization rather than killing the export (#779).
        val bitmap = try {
            BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (t: Throwable) {
            null
        } ?: return null
        try {
            val format = when {
                config.convertToWebP -> Bitmap.CompressFormat.WEBP
                ext == "png" -> Bitmap.CompressFormat.PNG
                else -> Bitmap.CompressFormat.JPEG
            }
            val baos = ByteArrayOutputStream()
            bitmap.compress(format, config.imageQuality, baos)
            val result = baos.toByteArray()
            return if (result.size < data.size) result else null
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun minifyWithEsbuild(context: Context, file: File): Boolean {
        val tempOutput = File(file.parentFile, "${file.nameWithoutExtension}.min.${file.extension}")
        try {
            val args = listOf(
                file.absolutePath,
                "--outfile=${tempOutput.absolutePath}",
                "--minify",
                "--allow-overwrite"
            )
            val result = NativeNodeEngine.executeEsbuild(
                context = context,
                args = args,
                workingDir = file.parentFile ?: file,
                timeout = 30_000
            )
            if (result.exitCode == 0 && tempOutput.exists() && tempOutput.length() > 0 && tempOutput.length() < file.length()) {
                tempOutput.copyTo(file, overwrite = true)
                tempOutput.delete()
                return true
            } else {
                tempOutput.delete()
                return false
            }
        } catch (e: Exception) {
            tempOutput.delete()
            return false
        }
    }

    private suspend fun minifyCodeBytes(
        context: Context,
        data: ByteArray,
        type: String,
        config: PerformanceOptimizer.OptimizeConfig
    ): ByteArray? {
        val esbuildAvailable = NativeNodeEngine.isAvailable(context)
        val content = String(data, Charsets.UTF_8)

        if (esbuildAvailable) {
            val tempDir = getCacheDir(context)
            val tempFile = File(tempDir, "temp_minify_${System.nanoTime()}.$type")
            try {
                tempFile.writeText(content)
                if (minifyWithEsbuild(context, tempFile)) {
                    val result = tempFile.readBytes()
                    tempFile.delete()
                    return if (result.size < data.size) result else null
                }
                tempFile.delete()
            } catch (e: Exception) {
                tempFile.delete()
            }
        }

        val minified = when (type) {
            "js" -> minifyJsContent(content)
            "css" -> minifyCssContent(content)
            else -> null
        }
        return minified?.let {
            val bytes = it.toByteArray(Charsets.UTF_8)
            if (bytes.size < data.size) bytes else null
        }
    }

    private fun minifyJsContent(content: String): String? {
        AppLogger.w(TAG, "Pure-Kotlin JS minify disabled (regex minify corrupts JS: regex literals, ASI, strings); JS is only minified via esbuild")
        return null
    }

    private fun minifyCssContent(content: String): String? {
        if (content.length < 100) return null
        var result = content

        result = result.replace(Regex("""/\*[\s\S]*?\*/"""), "")

        result = result.replace(Regex("""\s+"""), " ")

        result = result.replace(Regex("""\s*([{}:;,])\s*"""), "$1")

        result = result.replace(Regex(""";}"""), "}")
        return result.trim()
    }

    private fun minifySvgBytes(data: ByteArray): ByteArray? {
        val content = String(data, Charsets.UTF_8)
        val minified = minifySvgContent(content) ?: return null
        val result = minified.toByteArray(Charsets.UTF_8)
        return if (result.size < data.size) result else null
    }

    private fun minifySvgContent(content: String): String? {
        if (content.length < 100) return null
        var result = content

        result = result.replace(Regex("""<!--[\s\S]*?-->"""), "")

        result = result.replace(Regex("""\s+"""), " ")

        result = result.replace(Regex(""">\s+<"""), "><")

        result = result.replace(Regex("""<metadata[\s\S]*?</metadata>""", RegexOption.IGNORE_CASE), "")
        return result.trim()
    }
}
