package com.webtoapp.ui.webview

import android.os.Bundle
import java.util.Collections
import java.util.WeakHashMap

class WebViewDocumentActivity : WebViewActivity() {

    companion object {
        private val live = Collections.synchronizedSet(
            Collections.newSetFromMap(WeakHashMap<WebViewDocumentActivity, Boolean>())
        )

        fun finishAllDocumentTasks() {
            val snapshot = synchronized(live) { live.toList() }
            snapshot.forEach { activity ->
                if (!activity.isFinishing) {
                    activity.finishAndRemoveTask()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        live.add(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        live.remove(this)
        super.onDestroy()
    }
}
