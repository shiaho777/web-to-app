package com.webtoapp.core.startup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RuntimeWarmupStartup(
    private val appContext: android.content.Context,
) {

    fun initialize(appScope: CoroutineScope) {
        appScope.launch {
            com.webtoapp.core.perf.SystemPerfOptimizer.initSystem(appContext)
            com.webtoapp.core.perf.SystemPerfOptimizer.readaheadCriticalFiles(appContext)
            // NOTE: do NOT preload adblock filters here. The full compiled rule set can be huge
            // (many large filter lists) and loading it on every cold start exhausted the heap and
            // crashed the app (issue #356). Filters are loaded lazily on first actual use
            // (preview/runtime via AdBlocker.loadHostsRules / prepareRuntimeFilters).
        }
    }

    fun shutdown() {
        com.webtoapp.core.webview.WebViewPool.release()
        com.webtoapp.core.perf.SystemPerfOptimizer.release()
    }
}
