package com.webtoapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object MediaStorage {

    private const val TAG = "MediaStorage"
    private const val MEDIA_DIR = "media_apps"
    private const val GALLERY_DIR = "gallery_apps"
    private const val THUMBNAIL_DIR = "thumbnails"

    private const val THUMBNAIL_WIDTH = 300
    private const val THUMBNAIL_HEIGHT = 300
    private const val THUMBNAIL_QUALITY = 80

    fun saveMedia(context: Context, uri: Uri, isVideo: Boolean): String? {
        val mediaDir = File(context.filesDir, MEDIA_DIR)
        val extension = if (isVideo) "mp4" else "png"
        val destination = File(mediaDir, "media_${UUID.randomUUID()}.$extension")
        var complete = false
        return try {
            mediaDir.mkdirs()
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: 0L
            if (copied <= 0L) return null
            complete = true
            destination.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save media", e)
            null
        } finally {
            if (!complete) destination.delete()
        }
    }

    fun deleteMedia(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getMediaFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        return if (file.exists()) file else null
    }

    private fun getGalleryDir(context: Context): File {
        val dir = File(context.filesDir, GALLERY_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getThumbnailDir(context: Context): File {
        val dir = File(context.filesDir, THUMBNAIL_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun importGalleryMedia(context: Context, uri: Uri): GalleryMediaImport {
        var imported: GalleryItem? = null
        return try {
            withContext(Dispatchers.IO) {
                val name = runCatching { getDisplayNameFromUri(context, uri) }
                    .getOrDefault(uri.lastPathSegment.orEmpty())
                val type = runCatching { getMediaType(context, uri) }.getOrNull()
                val item = type?.let { saveGalleryMedia(context, uri, it, name) }
                imported = item
                GalleryMediaImport(name, item)
            }
        } catch (error: CancellationException) {
            imported?.let { item ->
                withContext(NonCancellable + Dispatchers.IO) { deleteGalleryMedia(item) }
            }
            throw error
        }
    }

    private suspend fun saveGalleryMedia(
        context: Context,
        uri: Uri,
        type: GalleryItemType,
        name: String
    ): GalleryItem? {
        var destination: File? = null
        var thumbnailPath: String? = null
        var complete = false
        try {
            val extension = getExtensionFromUri(context, uri, type)
            val file = File(getGalleryDir(context), "gallery_${UUID.randomUUID()}.$extension")
            destination = file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                }
            } ?: return null

            if (file.length() == 0L) return null
            coroutineContext.ensureActive()
            val mediaInfo = getMediaInfo(file, type)
            thumbnailPath = generateThumbnail(context, file, type)
            coroutineContext.ensureActive()

            val item = GalleryItem(
                path = file.absolutePath,
                type = type,
                name = name,
                thumbnailPath = thumbnailPath,
                width = mediaInfo.width,
                height = mediaInfo.height,
                duration = mediaInfo.duration,
                fileSize = file.length()
            )
            complete = true
            return item
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            AppLogger.e(TAG, "Failed to save gallery media", error)
            return null
        } finally {
            if (!complete) {
                destination?.delete()
                thumbnailPath?.let { File(it).delete() }
            }
        }
    }

    fun deleteGalleryMedia(item: GalleryItem): Boolean {
        var success = true

        try {
            File(item.path).delete()
        } catch (e: Exception) {
            success = false
        }

        item.thumbnailPath?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {

            }
        }

        return success
    }

    fun deleteGalleryMediaBatch(items: List<GalleryItem>): Int {
        var count = 0
        items.forEach { item ->
            if (deleteGalleryMedia(item)) count++
        }
        return count
    }

    private fun generateThumbnail(
        context: Context,
        file: File,
        type: GalleryItemType
    ): String? {
        return try {
            val thumbnailDir = getThumbnailDir(context)
            val thumbnailFile = File(thumbnailDir, "thumb_${file.nameWithoutExtension}.jpg")

            val bitmap = when (type) {
                GalleryItemType.IMAGE -> generateImageThumbnail(file)
                GalleryItemType.VIDEO -> generateVideoThumbnail(file)
            }

            bitmap?.let {
                FileOutputStream(thumbnailFile).use { fos ->
                    it.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, fos)
                }
                it.recycle()
                thumbnailFile.absolutePath
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to generate thumbnail: ${e.message}", e)
            null
        }
    }

    private fun generateImageThumbnail(file: File): Bitmap? {

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        val sampleSize = calculateInSampleSize(options.outWidth, options.outHeight)

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

        return scaleBitmap(bitmap)
    }

    private fun generateVideoThumbnail(file: File): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)

            val frame = retriever.getFrameAtTime(1000000)
                ?: retriever.getFrameAtTime(0)
            frame?.let { scaleBitmap(it) }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to generate video thumbnail: ${e.message}", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {

            }
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var inSampleSize = 1
        if (height > THUMBNAIL_HEIGHT || width > THUMBNAIL_WIDTH) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= THUMBNAIL_HEIGHT &&
                   (halfWidth / inSampleSize) >= THUMBNAIL_WIDTH) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val ratio = minOf(
            THUMBNAIL_WIDTH.toFloat() / bitmap.width,
            THUMBNAIL_HEIGHT.toFloat() / bitmap.height
        )
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (scaled != bitmap) {
            bitmap.recycle()
        }
        return scaled
    }

    private fun getMediaInfo(file: File, type: GalleryItemType): MediaInfo {
        return when (type) {
            GalleryItemType.IMAGE -> getImageInfo(file)
            GalleryItemType.VIDEO -> getVideoInfo(file)
        }
    }

    private fun getImageInfo(file: File): MediaInfo {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return MediaInfo(
            width = options.outWidth,
            height = options.outHeight,
            duration = 0
        )
    }

    private fun getVideoInfo(file: File): MediaInfo {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0

            MediaInfo(width, height, duration)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get video info: ${e.message}", e)
            MediaInfo(0, 0, 0)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {

            }
        }
    }

    private fun getExtensionFromUri(
        context: Context,
        uri: Uri,
        type: GalleryItemType
    ): String {

        val mimeType = context.contentResolver.getType(uri)
        val extension = mimeType?.let {
            when {
                it.contains("jpeg") || it.contains("jpg") -> "jpg"
                it.contains("png") -> "png"
                it.contains("gif") -> "gif"
                it.contains("webp") -> "webp"
                it.contains("mp4") -> "mp4"
                it.contains("webm") -> "webm"
                it.contains("avi") -> "avi"
                it.contains("mkv") -> "mkv"
                it.contains("mov") -> "mov"
                else -> null
            }
        }

        return extension ?: when (type) {
            GalleryItemType.IMAGE -> "jpg"
            GalleryItemType.VIDEO -> "mp4"
        }
    }

    private fun getDisplayNameFromUri(context: Context, uri: Uri): String {

        context.contentResolver.query(
            uri,
            arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) {

                        return name.substringBeforeLast(".")
                    }
                }
            }
        }

        return uri.lastPathSegment?.substringBeforeLast(".") ?: "Media"
    }

    fun isVideoUri(context: Context, uri: Uri): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("video/") == true
    }

    fun isImageUri(context: Context, uri: Uri): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true
    }

    fun getMediaType(context: Context, uri: Uri): GalleryItemType? {
        return when {
            isVideoUri(context, uri) -> GalleryItemType.VIDEO
            isImageUri(context, uri) -> GalleryItemType.IMAGE
            else -> null
        }
    }

    fun clearAllGalleryFiles(context: Context) {
        getGalleryDir(context).deleteRecursively()
        getThumbnailDir(context).deleteRecursively()
    }
}

data class GalleryMediaImport(val name: String, val item: GalleryItem?)

private data class MediaInfo(
    val width: Int,
    val height: Int,
    val duration: Long
)
