package com.webtoapp.core.webview

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import com.webtoapp.core.logging.AppLogger

/**
 * Hands a camera app a FileProvider uri it can actually write.
 *
 * From targetSdk 30 the platform drops [Intent.FLAG_GRANT_WRITE_URI_PERMISSION]
 * on an implicit intent unless the uri is also the intent's [Intent.getClipData].
 * Generated apps ship targetSdk 35, so a flag-only `EXTRA_OUTPUT` makes the
 * camera app exit immediately (and some OEM builds throw [SecurityException]
 * back into the result callback). A chooser's `EXTRA_INITIAL_INTENTS` does not
 * forward that flag grant either, so each resolved camera package is granted
 * explicitly.
 */
object CameraCaptureIntents {

    private const val TAG = "CameraCapture"

    const val OUTPUT_GRANT_FLAGS =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    fun attachOutput(context: Context, intent: Intent, output: Uri) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output)
        intent.clipData = ClipData.newRawUri("", output)
        intent.addFlags(OUTPUT_GRANT_FLAGS)
        val resolved = try {
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        } catch (e: Exception) {
            AppLogger.w(TAG, "query camera activities failed: ${e.message}")
            emptyList()
        }
        for (info in resolved) {
            val pkg = info.activityInfo?.packageName ?: continue
            try {
                context.grantUriPermission(pkg, output, OUTPUT_GRANT_FLAGS)
            } catch (e: Exception) {
                AppLogger.w(TAG, "grantUriPermission $pkg failed: ${e.message}")
            }
        }
    }

    fun revokeOutput(context: Context, output: Uri?) {
        if (output == null) return
        try {
            context.revokeUriPermission(output, OUTPUT_GRANT_FLAGS)
        } catch (e: Exception) {
            AppLogger.w(TAG, "revokeUriPermission failed: ${e.message}")
        }
    }
}
