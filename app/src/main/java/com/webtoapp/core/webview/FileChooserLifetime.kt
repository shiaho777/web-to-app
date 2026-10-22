package com.webtoapp.core.webview

/**
 * True while a WebView is waiting on system UI that will call back into it.
 *
 * The shell activity is singleTask. Opening a file picker or a runtime-permission
 * dialog sends the app to the background. TRIM_MEMORY_COMPLETE then tears the
 * WebView down, and the resume liveness probe does the same when the renderer
 * does not answer within two seconds (#1033, #1319). A camera `getUserMedia`
 * grant is the bad case: the probe destroys the WebView that is opening the
 * camera and the process crashes.
 *
 * [markOpen] covers the file picker, including the camera-permission step that
 * happens before it. [runPermissionDialog] covers a permission dialog that is
 * not already inside a picker (live camera / microphone). It also skips the
 * single resume probe that fires when that dialog returns, because the grant
 * itself keeps the renderer busy.
 */
object FileChooserLifetime {

    @Volatile
    private var open = false

    private var permissionHolds = 0

    private var resumeProbeSkips = 0

    fun markOpen() {
        open = true
    }

    fun markClosed() {
        open = false
    }

    /**
     * Run [launch] while the WebView must stay alive, and skip the resume probe
     * that follows the dialog. Returns false when [launch] throws; the hold is
     * released in that case so a failed request does not pin the WebView.
     */
    fun runPermissionDialog(launch: () -> Unit): Boolean {
        synchronized(this) {
            permissionHolds++
            resumeProbeSkips++
        }
        return try {
            launch()
            true
        } catch (e: Exception) {
            synchronized(this) {
                if (permissionHolds > 0) permissionHolds--
                if (resumeProbeSkips > 0) resumeProbeSkips--
            }
            false
        }
    }

    /** Pair with a successful [runPermissionDialog], from the dialog's result callback. */
    fun endPermissionDialog() {
        synchronized(this) {
            if (permissionHolds > 0) permissionHolds--
        }
    }

    /**
     * One-shot skip for the resume probe armed by [runPermissionDialog].
     * Call this on every resume before deciding to probe, so a leftover skip
     * cannot suppress a later, unrelated resume.
     */
    fun consumeResumeProbeSkip(): Boolean {
        synchronized(this) {
            if (resumeProbeSkips <= 0) return false
            resumeProbeSkips--
            return true
        }
    }

    fun isOpen(): Boolean = open || synchronized(this) { permissionHolds > 0 }
}
