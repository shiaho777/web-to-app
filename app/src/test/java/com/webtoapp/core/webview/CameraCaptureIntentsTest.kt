package com.webtoapp.core.webview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * targetSdk 30+ drops a write grant on an implicit camera intent unless the
 * output uri is also [Intent.getClipData]. Generated apps ship targetSdk 35.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CameraCaptureIntentsTest {

    @Test
    fun `output uri is carried by clip data and read write flags`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val uri = Uri.parse("content://com.example.fileprovider/cache/camera_photos/a.jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        CameraCaptureIntents.attachOutput(context, intent, uri)

        assertThat(intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT, Uri::class.java)).isEqualTo(uri)
        val clip = intent.clipData
        assertThat(clip).isNotNull()
        assertThat(clip!!.getItemAt(0).uri).isEqualTo(uri)
        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION).isNotEqualTo(0)
        assertThat(intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION).isNotEqualTo(0)
    }
}
