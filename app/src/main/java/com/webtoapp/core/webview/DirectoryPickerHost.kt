package com.webtoapp.core.webview

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.webtoapp.core.logging.AppLogger

/**
 * System folder picker for [LocalDirectoryStore].
 *
 * The activity-result launcher has to be registered before the activity is
 * started, so each hosting activity owns one and publishes it for as long as
 * it is alive. [NativeBridge] reaches the foreground activity through
 * [DirectoryPickerRegistry] instead of holding the launcher itself.
 */
class DirectoryPickerHost(activity: ComponentActivity) {
    private var pending: ((Uri?) -> Unit)? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        val callback = pending
        pending = null
        callback?.invoke(uri)
    }

    fun pick(onResult: (Uri?) -> Unit) {
        pending?.invoke(null)
        pending = onResult
        try {
            launcher.launch(null)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Folder picker failed to open", e)
            pending = null
            onResult(null)
        }
    }

    fun cancelPending() {
        val callback = pending
        pending = null
        callback?.invoke(null)
    }

    private companion object {
        private const val TAG = "DirectoryPicker"
    }
}

object DirectoryPickerRegistry {
    @Volatile
    var host: DirectoryPickerHost? = null
        private set

    fun install(host: DirectoryPickerHost) {
        this.host = host
    }

    fun uninstall(host: DirectoryPickerHost) {
        if (this.host === host) this.host = null
    }
}
