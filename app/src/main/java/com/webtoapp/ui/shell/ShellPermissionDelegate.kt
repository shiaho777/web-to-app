package com.webtoapp.ui.shell

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.mimeTypeForFileExtension
import com.webtoapp.util.DownloadHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KEY_PENDING_CAMERA_PHOTO = "wta_pending_camera_photo"

class ShellPermissionDelegate(private val activity: AppCompatActivity) {

    private val capturePermission = com.webtoapp.core.webview.CapturePermissionBridge()
    private var pendingGeolocationOrigin: String? = null
    private var pendingGeolocationCallback: GeolocationPermissions.Callback? = null
    private val pendingLocationAccessCallbacks = mutableListOf<(Boolean) -> Unit>()

    private var pendingDownload: PendingDownload? = null

    private data class PendingDownload(
        val url: String,
        val userAgent: String,
        val contentDisposition: String,
        val mimeType: String,
        val contentLength: Long
    )

    private fun resolveDownloadLocationConfig(): Pair<com.webtoapp.data.model.DownloadLocationMode, String> {
        val config = try {
            com.webtoapp.WebToAppApplication.shellMode.getConfig()
        } catch (e: Exception) {
            null
        }
        val mode = try {
            com.webtoapp.data.model.DownloadLocationMode.valueOf(
                config?.webViewConfig?.downloadLocationMode ?: "SYSTEM_DOWNLOAD"
            )
        } catch (e: Exception) {
            com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD
        }
        return mode to (config?.webViewConfig?.customDownloadDirUri ?: "")
    }

    private fun resolveAlbumName(): String {
        val config = try {
            com.webtoapp.WebToAppApplication.shellMode.getConfig()
        } catch (e: Exception) {
            null
        }
        return config?.webViewConfig?.mediaAlbumName ?: ""
    }

    private var cameraPhotoUri: Uri? = null

    private var pendingFilePathCallback: android.webkit.ValueCallback<Array<Uri>>? = null

    /**
     * A pending FilePathCallback cannot survive process death — when the camera
     * app gets us killed, the restored activity re-receives the chooser result
     * with no callback to answer, and the captured temp file would leak in
     * cacheDir. Persist the URI so [onRestoreInstanceState] can drop it (#1030).
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_PENDING_CAMERA_PHOTO, cameraPhotoUri?.toString())
    }

    fun onRestoreInstanceState(state: Bundle?) {
        val uri = state?.getString(KEY_PENDING_CAMERA_PHOTO)?.let(Uri::parse) ?: return
        if (com.webtoapp.core.webview.FileChooserLifetime.isOpen()) {
            com.webtoapp.core.webview.FileChooserLifetime.holdCamera(uri)
            cameraPhotoUri = uri
            return
        }
        com.webtoapp.core.webview.CameraCaptureIntents.revokeOutput(activity, uri)
        runCatching { activity.contentResolver.delete(uri, null, null) }
    }

    private val fileChooserActivityLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val photoUri = cameraPhotoUri
            ?: com.webtoapp.core.webview.FileChooserLifetime.cameraUri()
        val callback = pendingFilePathCallback
            ?: com.webtoapp.core.webview.FileChooserLifetime.callback()
        try {
            if (callback == null) {
                AppLogger.w("ShellPermission", "fileChooserActivityLauncher: no pending callback")
                return@registerForActivityResult
            }

            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val resultUris = mutableListOf<Uri>()

                val data = result.data
                if (data == null || (data.data == null && data.clipData == null)) {

                    photoUri?.let { resultUris.add(it) }
                } else {

                    data.data?.let { uri -> resultUris.add(uri) }
                    data.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i).uri?.let { resultUris.add(it) }
                        }
                    }
                }

                AppLogger.d("ShellPermission", "File chooser result: ${resultUris.size} files")
                callback.onReceiveValue(resultUris.toTypedArray())
            } else {

                AppLogger.d("ShellPermission", "File chooser cancelled")
                callback.onReceiveValue(null)
            }
        } catch (e: Exception) {
            AppLogger.e("ShellPermission", "File chooser result failed", e)
            try {
                callback?.onReceiveValue(null)
            } catch (deliverError: Exception) {
                AppLogger.w("ShellPermission", "file chooser callback failed: ${deliverError.message}")
            }
        } finally {
            com.webtoapp.core.webview.CameraCaptureIntents.revokeOutput(activity, photoUri)
            pendingFilePathCallback = null
            cameraPhotoUri = null
            com.webtoapp.core.webview.FileChooserLifetime.markClosed()
        }
    }

    private var pendingFileChooserParams: WebChromeClient.FileChooserParams? = null
    private val cameraForFileChooserPermLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->

        launchFileChooserIntent(pendingFileChooserParams)
        pendingFileChooserParams = null
    }

    fun handleFileChooser(
        filePathCallback: android.webkit.ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean {

        pendingFilePathCallback?.onReceiveValue(null)
        pendingFilePathCallback = filePathCallback
        com.webtoapp.core.webview.FileChooserLifetime.holdCallback(filePathCallback)

        if (filePathCallback == null) return false

        // Issue #943: content the user just shared into the app is a better answer than the
        // system picker, and it is what they shared it for. This branch owns the callback
        // from here on — it always answers it, either with the shared file or by handing the
        // flow back to continueWithSystemPicker().
        if (tryUseSharedContent(fileChooserParams)) return true

        continueWithSystemPicker(fileChooserParams)

        return true
    }

    /**
     * Resume the normal picker flow: camera runtime permission first when the page asked for
     * something the camera can supply, then the chooser itself.
     */
    private fun continueWithSystemPicker(fileChooserParams: WebChromeClient.FileChooserParams?) {
        // Keep the WebView that owns the ValueCallback alive until the picker
        // returns. Tearing it down here reloads the start URL (#1319).
        com.webtoapp.core.webview.FileChooserLifetime.markOpen()
        val needsCamera = isCameraRequired(fileChooserParams)
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (needsCamera && !hasCameraPermission) {
            pendingFileChooserParams = fileChooserParams
            try {
                cameraForFileChooserPermLauncher.launch(Manifest.permission.CAMERA)
            } catch (e: Exception) {
                AppLogger.e("ShellPermission", "Camera permission request failed", e)
                pendingFileChooserParams = null
                launchFileChooserIntent(fileChooserParams)
            }
        } else {
            launchFileChooserIntent(fileChooserParams)
        }
    }

    /**
     * Offer the newest queued share that matches what the page is asking for (issue #943).
     *
     * @return true when this call took ownership of [pendingFilePathCallback]. It answers the
     *   callback in every branch, so a page never ends up stuck in its upload state — the
     *   failure mode this whole delegate exists to avoid.
     */
    private fun tryUseSharedContent(fileChooserParams: WebChromeClient.FileChooserParams?): Boolean {
        val config = try {
            com.webtoapp.WebToAppApplication.shellMode.getConfig()
        } catch (e: Exception) {
            null
        }
        val wv = config?.webViewConfig ?: return false
        // Both inbound channels feed the same inbox: an "open with" file should reach a
        // page's upload control even when share-sheet receiving itself is off.
        if (!wv.receiveShareImages && !wv.openWithEnabled) return false

        val mode = try {
            com.webtoapp.data.model.ShareDeliveryMode.valueOf(wv.shareDeliveryMode)
        } catch (e: Exception) {
            com.webtoapp.data.model.ShareDeliveryMode.BOTH
        }
        if (mode == com.webtoapp.data.model.ShareDeliveryMode.JS_EVENT) return false

        val acceptTypes = fileChooserParams?.acceptTypes
            ?.filter { !it.isNullOrBlank() }
            ?.map { it.trim() }

        activity.lifecycleScope.launch {
            // The callback has already been taken over, so *every* exit from here must answer
            // it — an exception escaping this coroutine would both leave the page stuck and
            // crash the process (uncaught exceptions in a lifecycle scope propagate).
            try {
                val item = com.webtoapp.core.share.SharedContentInbox.findForFileChooser(
                    activity,
                    acceptTypes
                )
                if (item == null) {
                    continueWithSystemPicker(fileChooserParams)
                } else if (wv.sharePromptBeforeUse) {
                    promptForSharedContent(item, fileChooserParams)
                } else {
                    answerWithSharedContent(item)
                }
            } catch (e: Exception) {
                AppLogger.e("ShellPermission", "Shared-content file chooser path failed", e)
                // Only fall back while the callback is still unanswered; a failure *after*
                // answerWithSharedContent() has already replied must not reply twice.
                if (pendingFilePathCallback != null) {
                    continueWithSystemPicker(fileChooserParams)
                }
            }
        }

        return true
    }

    /**
     * Ask before substituting the shared file, so the user keeps the option of picking
     * something else. The item is still in the queue at this point — it is only claimed once
     * the user commits.
     */
    private fun promptForSharedContent(
        item: com.webtoapp.core.share.SharedItem,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ) {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(Strings.shareReceivedPickTitle)
            .setMessage(
                if (item.name.isBlank()) Strings.shareReceivedPickMessage
                else Strings.shareReceivedPickMessageNamed(item.name)
            )
            .setPositiveButton(Strings.shareReceivedPickUse) { _, _ ->
                answerWithSharedContent(item)
            }
            .setNegativeButton(Strings.shareReceivedPickChoose) { _, _ ->
                continueWithSystemPicker(fileChooserParams)
            }
            .setOnCancelListener {
                // A dismissed dialog must still unblock the page.
                pendingFilePathCallback?.onReceiveValue(null)
                pendingFilePathCallback = null
            }
            .show()
    }

    /** Hand the shared file to the page and drop it from the queue. */
    private fun answerWithSharedContent(item: com.webtoapp.core.share.SharedItem) {
        val callback = pendingFilePathCallback
        pendingFilePathCallback = null
        // No camera capture is in flight on this path; clear any stale URI so a later
        // cancellation cannot resurrect it (see fileChooserActivityLauncher).
        cameraPhotoUri = null

        activity.lifecycleScope.launch {
            com.webtoapp.core.share.SharedContentInbox.claim(activity, item.id)
        }

        val uri = sharedItemUri(item)
        if (uri == null) {
            AppLogger.w("ShellPermission", "Shared item ${item.id} vanished before the chooser used it")
            callback?.onReceiveValue(null)
            return
        }
        AppLogger.i("ShellPermission", "Answered file chooser with shared item ${item.name}")
        callback?.onReceiveValue(arrayOf(uri))
    }

    private fun sharedItemUri(item: com.webtoapp.core.share.SharedItem): Uri? = try {
        val file = item.path?.let { File(it) }
        if (file == null || !file.exists()) {
            null
        } else {
            FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
        }
    } catch (e: Exception) {
        AppLogger.e("ShellPermission", "Failed to expose shared item to the chooser", e)
        null
    }

    private fun isCameraRequired(params: WebChromeClient.FileChooserParams?): Boolean {
        if (params == null) return false

        if (params.isCaptureEnabled) return true

        val acceptTypes = params.acceptTypes
        if (acceptTypes == null || acceptTypes.isEmpty() || (acceptTypes.size == 1 && acceptTypes[0].isNullOrBlank())) {

            return true
        }

        for (type in acceptTypes) {
            if (type.isNullOrBlank()) continue
            val lower = type.lowercase()

            if (lower.startsWith("image/") || lower.startsWith("video/")) return true

            if (lower in setOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".heic", ".heif",
                    ".bmp", ".svg", ".mp4", ".mov", ".avi", ".mkv", ".webm", ".3gp")) return true
        }

        return false
    }

    private fun launchFileChooserIntent(params: WebChromeClient.FileChooserParams?) {
        try {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (params?.isCaptureEnabled == true && hasCameraPermission) {
                val acceptTypes = params.acceptTypes ?: arrayOf("image/*")
                val isVideo = acceptTypes.any {
                    it?.lowercase()?.startsWith("video/") == true
                }
                val captureAction = if (isVideo) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE
                val captureIntent = Intent(captureAction)
                if (captureAction == MediaStore.ACTION_IMAGE_CAPTURE) {
                    try {
                        val photoFile = createImageFile()
                        val output = FileProvider.getUriForFile(
                            activity,
                            "${activity.packageName}.fileprovider",
                            photoFile
                        )
                        cameraPhotoUri = output
                        com.webtoapp.core.webview.FileChooserLifetime.holdCamera(output)
                        com.webtoapp.core.webview.CameraCaptureIntents.attachOutput(
                            activity, captureIntent, output
                        )
                    } catch (e: Exception) {
                        AppLogger.e("ShellPermission", "Camera capture file creation failed", e)
                        cameraPhotoUri = null
                    }
                }
                if (captureIntent.resolveActivity(activity.packageManager) != null) {
                    try {
                        fileChooserActivityLauncher.launch(captureIntent)
                        AppLogger.d("ShellPermission", "Direct camera capture launched: action=$captureAction")
                        return
                    } catch (e: Exception) {
                        AppLogger.e("ShellPermission", "Direct camera capture failed", e)
                    }
                }
            }

            val extraIntents = mutableListOf<Intent>()

            if (hasCameraPermission) {
                try {
                    com.webtoapp.core.webview.CameraCaptureIntents.revokeOutput(activity, cameraPhotoUri)
                    val photoFile = createImageFile()
                    val output = FileProvider.getUriForFile(
                        activity,
                        "${activity.packageName}.fileprovider",
                        photoFile
                    )
                    cameraPhotoUri = output
                    com.webtoapp.core.webview.FileChooserLifetime.holdCamera(output)

                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        com.webtoapp.core.webview.CameraCaptureIntents.attachOutput(
                            activity, this, output
                        )
                    }

                    if (cameraIntent.resolveActivity(activity.packageManager) != null) {
                        extraIntents.add(cameraIntent)
                    }

                    val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    if (videoIntent.resolveActivity(activity.packageManager) != null) {
                        extraIntents.add(videoIntent)
                    }
                } catch (e: Exception) {
                    AppLogger.e("ShellPermission", "Camera intent creation failed", e)
                }
            }

            val rawAcceptTypes = params?.acceptTypes ?: arrayOf("*/*")

            val resolvedMimeTypes = rawAcceptTypes
                .filter { !it.isNullOrBlank() }
                .map { type ->
                    if (type.startsWith(".")) {

                        extensionToMimeType(type.lowercase())
                    } else {
                        type
                    }
                }
                .distinct()

            val mimeType = when {
                resolvedMimeTypes.isEmpty() -> "*/*"
                resolvedMimeTypes.size == 1 -> resolvedMimeTypes[0]
                else -> "*/*"
            }

            val contentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType

                if (params?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                if (resolvedMimeTypes.size > 1) {
                    putExtra(Intent.EXTRA_MIME_TYPES, resolvedMimeTypes.toTypedArray())
                    type = "*/*"
                }
            }

            val chooserIntent = Intent.createChooser(contentIntent, null).apply {
                if (extraIntents.isNotEmpty()) {
                    putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toTypedArray())
                }
            }

            fileChooserActivityLauncher.launch(chooserIntent)
            AppLogger.d("ShellPermission", "File chooser launched: mimeType=$mimeType, camera=${extraIntents.isNotEmpty()}")

        } catch (e: Exception) {
            AppLogger.e("ShellPermission", "Failed to launch file chooser", e)
            pendingFilePathCallback?.onReceiveValue(null)
            pendingFilePathCallback = null
            com.webtoapp.core.webview.FileChooserLifetime.markClosed()
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageDir = File(activity.cacheDir, "camera_photos").apply { mkdirs() }
        return File.createTempFile("IMG_${timestamp}_", ".jpg", imageDir)
    }

    private fun extensionToMimeType(ext: String): String = mimeTypeForFileExtension(ext)

    @Deprecated("Use handleFileChooser() instead")
    var onFileChooserResult: ((Array<android.net.Uri>) -> Unit)? = null

    val fileChooserLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onFileChooserResult?.invoke(uris.toTypedArray())
    }

    private val storagePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {

            pendingDownload?.let { download ->
                val (mode, customDir) = resolveDownloadLocationConfig()
                DownloadHelper.handleDownload(
                    context = activity,
                    url = download.url,
                    userAgent = download.userAgent,
                    contentDisposition = download.contentDisposition,
                    mimeType = download.mimeType,
                    contentLength = download.contentLength,
                    scope = activity.lifecycleScope,
                    downloadLocationMode = mode,
                    customDownloadDirUri = customDir,
                    albumName = resolveAlbumName()
                )
            }
        } else {
            Toast.makeText(activity, Strings.storagePermissionRequired, Toast.LENGTH_SHORT).show()

            pendingDownload?.let { download ->
                val (mode, customDir) = resolveDownloadLocationConfig()
                DownloadHelper.handleDownload(
                    context = activity,
                    url = download.url,
                    userAgent = download.userAgent,
                    contentDisposition = download.contentDisposition,
                    mimeType = download.mimeType,
                    contentLength = download.contentLength,
                    scope = activity.lifecycleScope,
                    downloadLocationMode = mode,
                    customDownloadDirUri = customDir,
                    albumName = resolveAlbumName()
                )
            }
        }
        pendingDownload = null
    }

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        com.webtoapp.core.webview.FileChooserLifetime.endPermissionDialog()
        AppLogger.d("ShellActivity", "Permission result received: $permissions")
        val allGranted = permissions.isNotEmpty() && permissions.values.all { it }
        AppLogger.d("ShellActivity", "All permissions granted: $allGranted")
        if (capturePermission.onRuntimeResult(allGranted)) {
            AppLogger.d("ShellActivity", "Capture request died during the dialog; reloading")
            reloadAfterCaptureGrant()
        }
    }

    fun onCapturePermissionCanceled(request: PermissionRequest) {
        capturePermission.onCanceled(request)
    }

    private fun reloadAfterCaptureGrant() {
        val reload = { (activity as? ShellActivity)?.reloadForCapturePermission() }
        val decor = try {
            activity.window.decorView
        } catch (e: Exception) {
            null
        }
        if (decor != null) decor.post { reload() } else reload()
    }

    private var pendingAndroidPermissionsCallback: ((Boolean) -> Unit)? = null

    private val androidPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        com.webtoapp.core.webview.FileChooserLifetime.endPermissionDialog()
        val allGranted = permissions.values.all { it }
        AppLogger.d("ShellActivity", "Android permissions (engine) result: $permissions allGranted=$allGranted")
        val callback = pendingAndroidPermissionsCallback
        pendingAndroidPermissionsCallback = null
        callback?.invoke(allGranted)
    }

    /**
     * Request Android runtime permissions on behalf of an engine that cannot request them itself
     * (GeckoView's onAndroidPermissionsRequest, #344). Reports whether every permission ended up
     * granted; permissions already granted skip the system dialog.
     */
    fun requestAndroidPermissions(permissions: Array<String>, onResult: (Boolean) -> Unit) {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) {
            onResult(true)
            return
        }
        val previous = pendingAndroidPermissionsCallback
        if (previous != null) {
            pendingAndroidPermissionsCallback = { granted ->
                previous(granted)
                onResult(granted)
            }
            return
        }
        pendingAndroidPermissionsCallback = onResult
        val launched = com.webtoapp.core.webview.FileChooserLifetime.runPermissionDialog {
            androidPermissionLauncher.launch(notGranted.toTypedArray())
        }
        if (!launched) {
            pendingAndroidPermissionsCallback = null
            onResult(false)
        }
    }

    private val locationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (pendingLocationAccessCallbacks.isNotEmpty()) {
            if (granted && isSystemLocationEnabled()) settleLocationAccess(true)
            else if (granted) showLocationSettingsDialog()
            else settleLocationAccess(false)
            return@registerForActivityResult
        }
        if (granted && pendingGeolocationOrigin != null) {
            GeolocationPermissionsSingleton.addAllowedOrigin(pendingGeolocationOrigin!!)
            if (isSystemLocationEnabled()) {
                pendingGeolocationCallback?.invoke(pendingGeolocationOrigin, true, false)
                pendingGeolocationOrigin = null
                pendingGeolocationCallback = null
            } else {
                showLocationSettingsDialog()
            }
        } else {
            pendingGeolocationCallback?.invoke(pendingGeolocationOrigin, granted, false)
            pendingGeolocationOrigin = null
            pendingGeolocationCallback = null
        }
    }

    private val locationSettingsLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (pendingLocationAccessCallbacks.isNotEmpty()) {
            settleLocationAccess(isSystemLocationEnabled())
            return@registerForActivityResult
        }
        val origin = pendingGeolocationOrigin
        val cb = pendingGeolocationCallback
        if (origin != null && cb != null && isSystemLocationEnabled()) {
            cb.invoke(origin, true, false)
        } else {
            cb?.invoke(origin, false, false)
        }
        pendingGeolocationOrigin = null
        pendingGeolocationCallback = null
    }

    private fun isSystemLocationEnabled(): Boolean {
        val lm = activity.getSystemService(android.content.Context.LOCATION_SERVICE)
            as? android.location.LocationManager ?: return false
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(Strings.geolocationLocationOffTitle)
            .setMessage(Strings.geolocationLocationOffMessage)
            .setPositiveButton(Strings.geolocationLocationOffOpenSettings) { _, _ ->
                try {
                    locationSettingsLauncher.launch(
                        android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                } catch (e: Exception) {
                    AppLogger.w("ShellPermission", "location settings intent failed: ${e.message}")
                    settleLocationAccess(false)
                    pendingGeolocationCallback?.invoke(pendingGeolocationOrigin, false, false)
                    pendingGeolocationOrigin = null
                    pendingGeolocationCallback = null
                }
            }
            .setNegativeButton(Strings.geolocationLocationOffCancel) { _, _ ->
                settleLocationAccess(false)
                pendingGeolocationCallback?.invoke(pendingGeolocationOrigin, false, false)
                pendingGeolocationOrigin = null
                pendingGeolocationCallback = null
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Request location access (Android runtime permission + system location service check)
     * on behalf of GeolocationBridge's native navigator.geolocation backend. The host shows
     * the same system dialog and location-services dialog used by the Chromium prompt path.
     */
    fun requestLocationAccess(accuracy: String, onResult: (Boolean) -> Unit) {
        val perms = if (accuracy.equals("FINE", ignoreCase = true)) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        val notGranted = perms.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) {
            if (isSystemLocationEnabled()) {
                onResult(true)
            } else {
                pendingLocationAccessCallbacks.add(onResult)
                showLocationSettingsDialog()
            }
            return
        }
        pendingLocationAccessCallbacks.add(onResult)
        locationPermissionLauncher.launch(notGranted.toTypedArray())
    }

    private fun settleLocationAccess(granted: Boolean) {
        val callbacks = pendingLocationAccessCallbacks.toList()
        pendingLocationAccessCallbacks.clear()
        callbacks.forEach { it(granted) }
    }

    fun handlePermissionRequest(request: PermissionRequest) {
        val resources = request.resources
        val androidPermissions = mutableListOf<String>()

        AppLogger.d("ShellActivity", "handlePermissionRequest called, resources: ${resources.joinToString()}")

        resources.forEach { resource ->
            AppLogger.d("ShellActivity", "Processing resource: $resource")
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                    androidPermissions.add(Manifest.permission.CAMERA)
                    AppLogger.d("ShellActivity", "Added CAMERA permission request")
                }
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {

                    androidPermissions.add(Manifest.permission.RECORD_AUDIO)
                    AppLogger.d("ShellActivity", "Added RECORD_AUDIO permission request (MODIFY_AUDIO_SETTINGS is normal, auto-granted)")
                }
                PermissionRequest.RESOURCE_MIDI_SYSEX -> {

                    AppLogger.d("ShellActivity", "MIDI_SYSEX resource, no Android permission needed")
                }
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> {

                    AppLogger.d("ShellActivity", "PROTECTED_MEDIA_ID resource, no Android permission needed")
                }
                else -> {
                    AppLogger.d("ShellActivity", "Unknown resource: $resource, will grant directly")
                }
            }
        }

        val uniquePermissions = androidPermissions.distinct()

        AppLogger.d("ShellActivity", "Android permissions to request: ${uniquePermissions.joinToString()}")

        if (uniquePermissions.isEmpty()) {

            AppLogger.d("ShellActivity", "No Android permissions needed, granting WebView request directly")
            com.webtoapp.core.webview.WebPermissionRequests.settle(request, true)
            return
        }

        val notGranted = uniquePermissions.filter {
            androidx.core.content.ContextCompat.checkSelfPermission(
                activity, it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {

            AppLogger.d("ShellActivity", "All permissions already granted, granting WebView request directly")
            com.webtoapp.core.webview.WebPermissionRequests.settle(request, true)
        } else {

            AppLogger.d("ShellActivity", "Requesting Android permissions: ${notGranted.joinToString()}")
            capturePermission.remember(request)
            val launched = com.webtoapp.core.webview.FileChooserLifetime.runPermissionDialog {
                permissionLauncher.launch(notGranted.toTypedArray())
            }
            if (!launched) {
                capturePermission.clear()
                com.webtoapp.core.webview.WebPermissionRequests.settle(request, false)
            }
        }
    }

    fun handleGeolocationPermission(
        origin: String?,
        callback: GeolocationPermissions.Callback?,
        policy: String = "ALWAYS_ASK",
        accuracy: String = "COARSE"
    ) {
        when (policy.uppercase()) {
            "DENY_ALL" -> {
                AppLogger.d("ShellActivity", "Geolocation denied by policy DENY_ALL for origin: $origin")
                callback?.invoke(origin, false, false)
                return
            }
            "REMEMBER_PER_HOST" -> {
                val remembered = GeolocationPermissionsSingleton.getAllowedOrigins()
                val isAllowed = remembered.any { origin != null && it == origin }
                if (isAllowed) {
                    callback?.invoke(origin, true, false)
                    return
                }
            }
        }

        pendingGeolocationOrigin = origin
        pendingGeolocationCallback = callback
        val perms = if (accuracy.equals("FINE", ignoreCase = true)) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        locationPermissionLauncher.launch(perms)
    }

    fun handleDownloadWithPermission(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long,
        webView: android.webkit.WebView?
    ) {

        val onBlobDownload: ((String, String) -> Unit) = { blobUrl, filename ->
            val safeBlobUrl = org.json.JSONObject.quote(blobUrl)
            val safeFilename = org.json.JSONObject.quote(filename)

            webView?.evaluateJavascript("""
                (function() {
                    try {
                        const blobUrl = $safeBlobUrl;
                        let filename = $safeFilename;
                        const LARGE_FILE_THRESHOLD = 10 * 1024 * 1024;
                        const CHUNK_SIZE = 512 * 1024;

                        if (window.__wtaBlobNameMap) {
                            var cachedName = window.__wtaBlobNameMap.get(blobUrl);
                            if (cachedName) filename = cachedName;
                        }

                        function uint8ToBase64(u8) {
                            const S = 8192; const p = [];
                            for (let i = 0; i < u8.length; i += S) p.push(String.fromCharCode.apply(null, u8.subarray(i, i + S)));
                            return btoa(p.join(''));
                        }

                        function processChunked(blob, fname) {
                            const mimeType = blob.type || 'application/octet-stream';
                            if (!window.AndroidDownload || !window.AndroidDownload.startChunkedDownload) {
                                processSmall(blob, fname); return;
                            }
                            const did = window.AndroidDownload.startChunkedDownload(fname, mimeType, blob.size);
                            let off = 0, ci = 0; const tc = Math.ceil(blob.size / CHUNK_SIZE);
                            function next() {
                                if (off >= blob.size) { window.AndroidDownload.finishChunkedDownload(did); return; }
                                blob.slice(off, off + CHUNK_SIZE).arrayBuffer().then(function(ab) {
                                    window.AndroidDownload.appendChunk(did, uint8ToBase64(new Uint8Array(ab)), ci, tc);
                                    off += CHUNK_SIZE; ci++;
                                    setTimeout(next, 0);
                                });
                            }
                            next();
                        }

                        function processSmall(blob, fname) {
                            const reader = new FileReader();
                            reader.onloadend = function() {
                                const base64Data = reader.result.split(',')[1];
                                const mimeType = blob.type || 'application/octet-stream';
                                if (window.AndroidDownload && window.AndroidDownload.saveBase64File) {
                                    window.AndroidDownload.saveBase64File(base64Data, fname, mimeType);
                                }
                            };
                            reader.readAsDataURL(blob);
                        }

                        if (blobUrl.startsWith('data:')) {
                            const parts = blobUrl.split(',');
                            const meta = parts[0];
                            const base64Data = parts[1];
                            const mimeMatch = meta.match(/data:([^;]+)/);
                            const mimeType = mimeMatch ? mimeMatch[1] : 'application/octet-stream';
                            if (window.AndroidDownload && window.AndroidDownload.saveBase64File) {
                                window.AndroidDownload.saveBase64File(base64Data, filename, mimeType);
                            }
                        } else if (blobUrl.startsWith('blob:')) {
                            // 先查 DownloadBridge 拦截时缓存的 Blob（页面可能已同步 revoke 了 URL）
                            const cachedBlob = window.__wtaBlobMap && window.__wtaBlobMap.get(blobUrl);
                            function dispatch(blob) {
                                if (blob.size > LARGE_FILE_THRESHOLD) {
                                    processChunked(blob, filename);
                                } else {
                                    processSmall(blob, filename);
                                }
                            }
                            function reportFailure(err) {
                                console.error('[DownloadHelper] Blob fetch failed:', err);
                                if (window.AndroidDownload && window.AndroidDownload.showToast) {
                                    window.AndroidDownload.showToast('${Strings.downloadFailedPrefix}' + (err && err.message ? err.message : 'blob unavailable'));
                                }
                            }
                            if (cachedBlob) {
                                dispatch(cachedBlob);
                            } else if (window.__wtaResolveBlob) {
                                // Cross-context resolution (sandboxed iframe / worker blobs,
                                // blob:null opaque origins) — broadcast lookup before fetch.
                                window.__wtaResolveBlob(blobUrl)
                                    .then(function(blob) {
                                        if (blob) dispatch(blob);
                                        else reportFailure(new Error('blob not resolvable'));
                                    })
                                    .catch(reportFailure);
                            } else {
                                fetch(blobUrl)
                                    .then(function(r) { return r.blob(); })
                                    .then(dispatch)
                                    .catch(reportFailure);
                            }
                        }
                    } catch(e) {
                        console.error('[DownloadHelper] Error:', e);
                    }
                })();
            """.trimIndent(), null)
        }

        val (downloadLocationMode, customDownloadDirUri) = resolveDownloadLocationConfig()

        if (downloadLocationMode == com.webtoapp.data.model.DownloadLocationMode.ASK ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            DownloadHelper.handleDownload(
                context = activity,
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                contentLength = contentLength,
                scope = activity.lifecycleScope,
                onBlobDownload = onBlobDownload,
                downloadLocationMode = downloadLocationMode,
                customDownloadDirUri = customDownloadDirUri,
                albumName = resolveAlbumName()
            )
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            DownloadHelper.handleDownload(
                context = activity,
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                contentLength = contentLength,
                scope = activity.lifecycleScope,
                onBlobDownload = onBlobDownload,
                downloadLocationMode = downloadLocationMode,
                customDownloadDirUri = customDownloadDirUri,
                albumName = resolveAlbumName()
            )
        } else {

            pendingDownload = PendingDownload(url, userAgent, contentDisposition, mimeType, contentLength)
            storagePermissionLauncher.launch(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }
    }
}

internal object GeolocationPermissionsSingleton {
    private val allowedOrigins = mutableSetOf<String>()

    fun getAllowedOrigins(): Set<String> = synchronized(allowedOrigins) { allowedOrigins.toSet() }

    fun addAllowedOrigin(origin: String) {
        synchronized(allowedOrigins) { allowedOrigins.add(origin) }
    }

    fun clear() {
        synchronized(allowedOrigins) { allowedOrigins.clear() }
    }
}
