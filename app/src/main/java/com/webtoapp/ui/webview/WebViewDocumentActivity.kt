package com.webtoapp.ui.webview

import android.os.Bundle
import java.util.Collections
import java.util.WeakHashMap

/**
 * One recents task per document URI. Home preview uses it while the About
 * separate-tasks switch is on. Desktop shortcuts always use it, so Back
 * returns to the launcher (#1391). Opening the same URI brings that task
 * forward instead of stacking another card (#1250). The preview tool always
 * uses it, so an external agent has a page the user can watch.
 * [WebViewActivity] stays singleTask so the default preview path still reuses one task.
 */
class WebViewDocumentActivity : WebViewActivity() {

    companion object {
        private val live = Collections.synchronizedSet(
            Collections.newSetFromMap(WeakHashMap<WebViewDocumentActivity, Boolean>())
        )

        /**
         * Closes preview document tasks when the separate-tasks switch turns off.
         * Shortcut tasks stay: that switch does not own them.
         */
        fun finishAllDocumentTasks() {
            val snapshot = synchronized(live) { live.toList() }
            snapshot.forEach { activity ->
                if (activity.launchedFromShortcut || activity.isFinishing) return@forEach
                activity.finishAndRemoveTask()
            }
        }
    }

    internal var launchedFromShortcut: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        launchedFromShortcut = intent.getBooleanExtra(EXTRA_SHORTCUT_TASK, false)
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
