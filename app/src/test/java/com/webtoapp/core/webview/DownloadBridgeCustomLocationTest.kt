package com.webtoapp.core.webview

import android.content.Context
import android.os.Environment
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.DownloadLocationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

/**
 * Regression coverage for issue #1091: blob/data-url downloads whose payload is a
 * media file (canvas.toBlob -> image/png etc.) must honor the configured download
 * location instead of always landing in the public gallery folder Pictures/WebToApp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DownloadBridgeCustomLocationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // DownloadNotificationManager registers an unexported receiver guarded by the
        // app's own signature permission; declare it granted for the unit-test sandbox.
        shadowOf(context.applicationContext as android.app.Application)
            .grantPermissions("com.webtoapp.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")
    }

    private fun bridge(
        mode: DownloadLocationMode,
        customDir: File? = null
    ) = DownloadBridge(
        context = context,
        scope = CoroutineScope(Dispatchers.IO),
        downloadLocationMode = mode,
        customDownloadDirUri = customDir?.absolutePath ?: ""
    )

    private fun imagePngBase64(): String {
        // Minimal valid PNG header bytes; contents do not matter for routing.
        val bytes = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            1, 2, 3, 4, 5, 6, 7, 8
        )
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun awaitFile(dir: File, name: String, timeoutMs: Long = 5000): File? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            dir.listFiles()?.firstOrNull { it.name.startsWith(name.substringBeforeLast('.')) }
                ?.let { return it }
            Thread.sleep(40)
        }
        return null
    }

    @Test
    fun `custom mode routes media blob into the configured directory`() {
        val customDir = File(context.getExternalFilesDir(null), "MyApp").apply { mkdirs() }
        bridge(DownloadLocationMode.CUSTOM, customDir)
            .saveBase64File(imagePngBase64(), "shot.png", "image/png")

        val saved = awaitFile(customDir, "shot.png")
        assertThat(saved).isNotNull()
        assertThat(saved!!.name).isEqualTo("shot.png")
    }

    @Test
    fun `custom mode routes chunked media download into the configured directory`() {
        val customDir = File(context.getExternalFilesDir(null), "MyApp").apply { mkdirs() }
        val b = bridge(DownloadLocationMode.CUSTOM, customDir)
        val id = b.startChunkedDownload("clip.png", "image/png", 16)
        b.appendChunk(id, imagePngBase64(), 0, 1)
        b.finishChunkedDownload(id)

        val saved = awaitFile(customDir, "clip.png")
        assertThat(saved).isNotNull()
        assertThat(saved!!.name).isEqualTo("clip.png")
    }

    @Test
    fun `app private mode keeps media blob inside the app directory`() {
        bridge(DownloadLocationMode.APP_PRIVATE)
            .saveBase64File(imagePngBase64(), "shot.png", "image/png")

        val privateDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val saved = awaitFile(privateDir, "shot.png")
        assertThat(saved).isNotNull()
        assertThat(saved!!.name).isEqualTo("shot.png")
    }
}
