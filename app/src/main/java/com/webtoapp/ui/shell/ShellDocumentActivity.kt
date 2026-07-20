package com.webtoapp.ui.shell

import java.util.Collections
import java.util.WeakHashMap

class ShellDocumentActivity : ShellActivity() {

    companion object {
        private val live = Collections.synchronizedSet(
            Collections.newSetFromMap(WeakHashMap<ShellDocumentActivity, Boolean>())
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

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        live.add(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        live.remove(this)
        super.onDestroy()
    }
}
