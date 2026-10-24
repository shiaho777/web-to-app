package com.webtoapp.core.webview

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebView

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
 *
 * The [ValueCallback] and the WebView that owns it cannot be rebuilt after the
 * view is destroyed. While a picker is open they are held here, so an activity
 * recreation in the same process can deliver the result to the original page
 * instead of reloading the start URL with an empty file input (#1319).
 */
object FileChooserLifetime {

    @Volatile
    private var open = false

    private var permissionHolds = 0

    private var resumeProbeSkips = 0

    private var parkedWebView: WebView? = null

    private var parkedCallback: ValueCallback<Array<Uri>>? = null

    private var parkedCameraUri: Uri? = null

    private val reattached = HashSet<Int>()

    fun markOpen() {
        open = true
    }

    fun markClosed() {
        open = false
        parkedCallback = null
        parkedCameraUri = null
        val wv = parkedWebView
        if (wv == null || wv.parent != null) {
            parkedWebView = null
        }
    }

    /** Remember the page that opened the picker. Destroying it drops the callback. */
    fun parkWebView(webView: WebView) {
        parkedWebView = webView
        open = true
    }

    fun holdCallback(callback: ValueCallback<Array<Uri>>?) {
        parkedCallback = callback
        if (callback != null) open = true
    }

    fun holdCamera(uri: Uri?) {
        parkedCameraUri = uri
    }

    fun callback(): ValueCallback<Array<Uri>>? = parkedCallback

    fun cameraUri(): Uri? = parkedCameraUri

    /**
     * The parked WebView after its old window released it. Null while it is still
     * attached, so a result delivered to the same activity does not steal the view.
     */
    fun takeDetachedWebView(): WebView? {
        val wv = parkedWebView ?: return null
        if (wv.parent != null) return null
        parkedWebView = null
        reattached.add(System.identityHashCode(wv))
        return wv
    }

    fun consumeReattached(webView: WebView): Boolean =
        reattached.remove(System.identityHashCode(webView))

    /** True when [onRelease] must detach this WebView without destroying it. */
    fun shouldRetain(webView: WebView): Boolean = open && parkedWebView === webView

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
