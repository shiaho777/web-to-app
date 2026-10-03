package com.webtoapp.ui.webview

import android.os.Bundle
import java.util.Collections
import java.util.WeakHashMap

/**
 * One recents task per document URI. Home preview and shortcuts use it when
 * the About-screen separate-tasks switch is on. The preview tool always uses
 * it, so an external agent has a page the user can watch. [WebViewActivity]
 * stays singleTask so the default path still reuses one preview.
 */
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
        PreviewSessions.onActivityReady(this)
    }

    override fun onDestroy() {
        PreviewSessions.onActivityGone(this)
        live.remove(this)
        super.onDestroy()
    }
}
