package com.webtoapp.ui.shell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.webtoapp.util.CreateDownloadDocument
import com.webtoapp.util.DownloadSavePrompter

/**
 * Invisible host for the system save dialog. Downloads can start from a
 * WebView callback or a service, neither of which can register an activity
 * result launcher of its own.
 */
class DownloadSaveActivity : AppCompatActivity() {

    private var launched = false
    private var delivered = false

    private val launcher = registerForActivityResult(CreateDownloadDocument()) { uri ->
        if (!delivered) {
            delivered = true
            DownloadSavePrompter.deliver(uri)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launched = savedInstanceState?.getBoolean(STATE_LAUNCHED) == true
    }

    override fun onStart() {
        super.onStart()
        if (launched) return
        launched = true
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "download"
        val mime = intent.getStringExtra(EXTRA_MIME) ?: "application/octet-stream"
        launcher.launch(mime to title)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_LAUNCHED, launched)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (isFinishing && !delivered) {
            delivered = true
            DownloadSavePrompter.deliver(null)
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TITLE = "com.webtoapp.DOWNLOAD_SAVE_TITLE"
        const val EXTRA_MIME = "com.webtoapp.DOWNLOAD_SAVE_MIME"
        private const val STATE_LAUNCHED = "launched"
    }
}
