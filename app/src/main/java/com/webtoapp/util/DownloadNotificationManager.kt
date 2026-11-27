package com.webtoapp.util

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.webtoapp.core.logging.AppLogger
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("StaticFieldLeak")
class DownloadNotificationManager(private val context: Context) {

    companion object {
        private const val TAG = "DownloadNotification"
        private const val CHANNEL_ID = "download_channel"
        private val CHANNEL_NAME get() = Strings.notifDownloadChannel
        private const val PROGRESS_NOTIFICATION_ID = 1001
        private const val COMPLETE_NOTIFICATION_ID_BASE = 2000
        private const val CUSTOM_NOTIFICATION_ID_BASE = 3000

        @Volatile
        private var INSTANCE: DownloadNotificationManager? = null

        fun getInstance(context: Context): DownloadNotificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DownloadNotificationManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun release() {
            synchronized(this) {
                INSTANCE?.cleanup()
                INSTANCE = null
            }
        }
    }

    private val customNotificationIdCounter = AtomicInteger(0)

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val pollingScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val activeDownloads = mutableMapOf<Long, DownloadInfo>()
    private var pollingJob: Job? = null
    private var lastQueryErrorAt: Long? = null

    private var downloadReceiver: BroadcastReceiver? = null

    init {
        createNotificationChannel()
        registerDownloadReceiver()
    }

    private fun createNotificationChannel() {
        SafeNotificationChannels.ensure(
            context = context,
            id = CHANNEL_ID,
            name = CHANNEL_NAME,
            importance = NotificationManager.IMPORTANCE_LOW,
            description = Strings.notifDownloadChannelDesc
        ) { setShowBadge(false) }
    }

    private fun registerDownloadReceiver() {
        if (downloadReceiver != null) return

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId in activeDownloads) startProgressPolling()
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun trackDownload(downloadId: Long, fileName: String, mimeType: String) {
        pollingScope.launch {
            activeDownloads[downloadId] = DownloadInfo(
                id = downloadId,
                fileName = fileName,
                mimeType = mimeType
            )
            showProgressNotification()
            startProgressPolling()
        }
    }

    private fun startProgressPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = pollingScope.launch {
            while (activeDownloads.isNotEmpty()) {
                val expected = activeDownloads.keys.toList()
                val rows = withContext(Dispatchers.IO) { queryProgress(expected) }
                if (rows != null) {
                    for (id in expected) {
                        val tracked = activeDownloads[id] ?: continue
                        val row = rows[id]
                        if (row == null) activeDownloads.remove(id)
                        else applyProgress(tracked, row)
                    }
                    showProgressNotification()
                }
                if (activeDownloads.isNotEmpty()) delay(if (rows == null) 2_000 else 500)
            }
        }
    }

    private data class DownloadRow(
        val status: Int,
        val reason: Int,
        val bytes: Long,
        val total: Long,
        val uri: String?,
        val mime: String?
    )

    private fun queryProgress(ids: List<Long>): Map<Long, DownloadRow>? {
        return try {
            val rows = mutableMapOf<Long, DownloadRow>()
            for (batch in ids.chunked(100)) {
                val query = DownloadManager.Query().setFilterById(*batch.toLongArray())
                val cursor = downloadManager.query(query) ?: return null
                cursor.use {
                    val id = it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
                    val status = it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    val reason = it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
                    val bytes = it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val total = it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val uri = it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
                    val mime = it.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE)
                    while (it.moveToNext()) {
                        rows[it.getLong(id)] = DownloadRow(
                            it.getInt(status), it.getInt(reason), it.getLong(bytes), it.getLong(total),
                            it.getString(uri), it.getString(mime)
                        )
                    }
                }
            }
            rows
        } catch (e: RuntimeException) {
            val now = SystemClock.elapsedRealtime()
            if (lastQueryErrorAt?.let { now - it < 30_000 } != true) {
                lastQueryErrorAt = now
                AppLogger.w(TAG, "Could not query system downloads", e)
            }
            null
        }
    }

    private fun applyProgress(info: DownloadInfo, row: DownloadRow) {
        if (row.status != info.status || row.reason != info.reason) {
            AppLogger.d(TAG, "Download id=${info.id} status=${row.status} reason=${row.reason} bytes=${row.bytes} total=${row.total}")
        }
        info.status = row.status
        info.reason = row.reason
        info.downloadedBytes = row.bytes
        info.totalBytes = row.total
        info.progress = if (row.total > 0) (row.bytes.toDouble() * 100 / row.total).toInt().coerceIn(0, 100) else 0
        val mime = row.mime ?: info.mimeType
        when (row.status) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                activeDownloads.remove(info.id)
                if (row.bytes == 0L && DownloadArtifact.isApk(info.fileName, mime)) {
                    showDownloadFailedNotification(info.id, info.fileName, Strings.notifDownloadEmptyResponse)
                } else {
                    showDownloadCompleteNotification(info.id, info.fileName, row.uri, mime)
                }
            }
            DownloadManager.STATUS_FAILED -> {
                activeDownloads.remove(info.id)
                val message = if (row.reason in 400..599) Strings.httpErrorCode(row.reason) else "#${row.reason}"
                showDownloadFailedNotification(info.id, info.fileName, message)
            }
        }
    }

    private fun showProgressNotification() {
        if (activeDownloads.isEmpty()) {
            notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
            return
        }

        val totalProgress = activeDownloads.values.sumOf { it.progress }
        val avgProgress = totalProgress / activeDownloads.size

        val downloadingCount = activeDownloads.size
        val title = if (downloadingCount == 1) {
            Strings.notifDownloadingFile.format(activeDownloads.values.first().fileName)
        } else {
            Strings.notifDownloadingMultipleFiles.format(downloadingCount)
        }

        val totalDownloaded = activeDownloads.values.sumOf { it.downloadedBytes }
        val totalSize = activeDownloads.values.sumOf { it.totalBytes }
        val progressText = transferText(totalDownloaded, totalSize)
        val waiting = activeDownloads.values.firstNotNullOfOrNull { info ->
            when (info.status) {
                DownloadManager.STATUS_PENDING -> Strings.notifDownloadQueued
                DownloadManager.STATUS_PAUSED -> when (info.reason) {
                    DownloadManager.PAUSED_WAITING_TO_RETRY -> Strings.notifDownloadRetrying
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> Strings.notifDownloadWaitingWifi
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> Strings.notifDownloadWaitingNetwork
                    else -> Strings.notifDownloadQueued
                }
                else -> null
            }
        }.takeIf { activeDownloads.values.none { it.status == DownloadManager.STATUS_RUNNING } }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(if (waiting == null) progressText else "$waiting · $progressText")
            .setProgress(100, avgProgress, totalSize <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(PendingIntent.getActivity(
                context, PROGRESS_NOTIFICATION_ID, Intent(DownloadManager.ACTION_VIEW_DOWNLOADS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(PROGRESS_NOTIFICATION_ID, notification)
    }

    private fun showDownloadCompleteNotification(
        downloadId: Long,
        fileName: String,
        localUri: String?,
        mimeType: String
    ) {
        val notificationId = (COMPLETE_NOTIFICATION_ID_BASE + downloadId).toInt()

        val openIntent = createOpenFileIntent(localUri, mimeType)
        val openPendingIntent = if (openIntent != null) {
            PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val shareIntent = createShareFileIntent(localUri, mimeType)
        val sharePendingIntent = if (shareIntent != null) {
            PendingIntent.getActivity(
                context,
                notificationId + 10000,
                Intent.createChooser(shareIntent, Strings.notifShare),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(Strings.notifDownloadComplete)
            .setContentText(fileName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (openPendingIntent != null) {
            builder.setContentIntent(openPendingIntent)
            builder.addAction(
                android.R.drawable.ic_menu_view,
                Strings.notifOpen,
                openPendingIntent
            )
        }

        if (sharePendingIntent != null) {
            builder.addAction(
                android.R.drawable.ic_menu_share,
                Strings.notifShare,
                sharePendingIntent
            )
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun showDownloadFailedNotification(downloadId: Long, fileName: String, reason: String) {
        val notificationId = (COMPLETE_NOTIFICATION_ID_BASE + downloadId).toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(Strings.notifDownloadFailed)
            .setContentText(Strings.notifSaveFailedWithReason.format(fileName, reason))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun createOpenFileIntent(localUri: String?, mimeType: String): Intent? {
        if (localUri == null) return null
        return try {
            val uri = Uri.parse(localUri)
            val fileUri = if (uri.scheme == "file") {
                val file = File(uri.path ?: return null)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                uri
            }
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to create open intent", e)
            null
        }
    }

    private fun createShareFileIntent(localUri: String?, mimeType: String): Intent? {
        if (localUri == null) return null
        return try {
            val uri = Uri.parse(localUri)
            val fileUri = if (uri.scheme == "file") {
                val file = File(uri.path ?: return null)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                uri
            }
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to create share intent", e)
            null
        }
    }

    fun showIndeterminateProgress(fileName: String): Int {
        val notificationId = CUSTOM_NOTIFICATION_ID_BASE + customNotificationIdCounter.incrementAndGet()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(Strings.notifDownloading)
            .setContentText(fileName)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(notificationId, notification)
        return notificationId
    }

    fun updateTransferProgress(notificationId: Int, fileName: String, downloaded: Long, total: Long) {
        val progress = if (total > 0) ((downloaded * 100) / total).toInt().coerceIn(0, 100) else 0
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(Strings.notifDownloadingFile.format(fileName))
            .setContentText(transferText(downloaded, total))
            .setProgress(100, progress, total <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun transferText(downloaded: Long, total: Long): String =
        if (total > 0) "${formatFileSize(downloaded)} / ${formatFileSize(total)}" else formatFileSize(downloaded)

    fun updateProgress(notificationId: Int, fileName: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(fileName)
            .setContentText("$progress%")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showMediaSaveComplete(
        fileName: String,
        uri: android.net.Uri,
        mimeType: String,
        isImage: Boolean,
        progressNotificationId: Int
    ) {

        notificationManager.cancel(progressNotificationId)

        val notificationId = CUSTOM_NOTIFICATION_ID_BASE + customNotificationIdCounter.incrementAndGet()
        val typeText = if (isImage) Strings.image else Strings.video

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(Strings.savedToGallery.replace("%s", typeText))
            .setContentText(fileName)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showSaveComplete(
        fileName: String,
        filePath: String,
        mimeType: String,
        progressNotificationId: Int,
        contentUri: String? = null
    ) {
        notificationManager.cancel(progressNotificationId)

        val notificationId = CUSTOM_NOTIFICATION_ID_BASE + customNotificationIdCounter.incrementAndGet()

        val openIntent = createOpenFileIntent(contentUri ?: "file://$filePath", mimeType)
        val pendingIntent = if (openIntent != null) {
            PendingIntent.getActivity(
                context, notificationId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(Strings.notifDownloadComplete)
            .setContentText(fileName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun showSaveFailed(fileName: String, reason: String, progressNotificationId: Int) {
        notificationManager.cancel(progressNotificationId)

        val notificationId = CUSTOM_NOTIFICATION_ID_BASE + customNotificationIdCounter.incrementAndGet()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(Strings.notifDownloadFailed)
            .setContentText(Strings.notifSaveFailedWithReason.format(fileName, reason))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun formatFileSize(bytes: Long): String {
        return android.text.format.Formatter.formatFileSize(context, bytes)
    }

    private fun cleanup() {
        try {
            pollingScope.cancel()
            pollingJob = null
            downloadReceiver?.let {
                context.unregisterReceiver(it)
            }
            downloadReceiver = null
            activeDownloads.clear()
            notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Cleanup failed", e)
        }
    }

    data class DownloadInfo(
        val id: Long,
        val fileName: String,
        val mimeType: String,
        var progress: Int = 0,
        var totalBytes: Long = 0,
        var downloadedBytes: Long = 0,
        var status: Int = DownloadManager.STATUS_PENDING,
        var reason: Int = 0
    )
}
