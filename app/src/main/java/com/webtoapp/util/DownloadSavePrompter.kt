package com.webtoapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContract
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.shell.DownloadSaveActivity

/**
 * Shows the system save dialog (ACTION_CREATE_DOCUMENT) once per download and
 * returns the document the user created. Prompts are queued so a second
 * download waits until the first dialog closes.
 */
object DownloadSavePrompter {

    private const val TAG = "DownloadSavePrompter"

    private val main = android.os.Handler(Looper.getMainLooper())
    private val pending = ArrayDeque<Pending>()
    private var showing = false
    private var appContext: Context? = null

    private class Pending(
        val fileName: String,
        val mimeType: String,
        val onResult: (Uri?) -> Unit
    )

    fun prompt(context: Context, fileName: String, mimeType: String, onResult: (Uri?) -> Unit) {
        val app = context.applicationContext
        val item = Pending(
            fileName.ifBlank { "download" },
            mimeType.ifBlank { "application/octet-stream" },
            onResult
        )
        main.post {
            appContext = app
            pending.addLast(item)
            if (!showing) {
                showing = true
                launch(pending.first())
            }
        }
    }

    fun deliver(uri: Uri?) {
        val run = {
            val item = pending.removeFirstOrNull()
            showing = false
            if (uri != null) {
                val resolver = appContext?.contentResolver
                if (resolver != null) {
                    try {
                        resolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "Persistable grant not available for $uri", e)
                    }
                }
            }
            item?.onResult?.invoke(uri)
            val next = pending.firstOrNull()
            if (next != null) {
                showing = true
                launch(next)
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) run() else main.post(run)
    }

    private fun launch(item: Pending) {
        val ctx = appContext ?: return
        val intent = Intent(ctx, DownloadSaveActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(DownloadSaveActivity.EXTRA_TITLE, item.fileName)
            putExtra(DownloadSaveActivity.EXTRA_MIME, item.mimeType)
        }
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Could not open the system save dialog", e)
            showing = false
            pending.removeFirstOrNull()?.onResult?.invoke(null)
        }
    }
}

/** Suggested title is [Pair.second]; MIME type is [Pair.first]. */
class CreateDownloadDocument : ActivityResultContract<Pair<String, String>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<String, String>): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input.first.ifBlank { "application/octet-stream" }
            putExtra(Intent.EXTRA_TITLE, input.second)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.data
    }
}
