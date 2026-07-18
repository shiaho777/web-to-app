package com.webtoapp.core.shell

object ShellPreviewSession {
    @Volatile
    private var activeConfig: ShellConfig? = null

    @Volatile
    private var activeAppId: Long = -1L

    @Volatile
    private var active: Boolean = false

    fun begin(config: ShellConfig, appId: Long) {
        activeConfig = config
        activeAppId = if (appId > 0L) appId else -1L
        active = true
    }

    fun end() {
        active = false
        activeConfig = null
        activeAppId = -1L
    }

    fun endIfMatches(appId: Long) {
        if (!active) return
        if (appId <= 0L || activeAppId == appId || activeAppId == -1L) {
            end()
        }
    }

    fun isActive(): Boolean = active && activeConfig != null

    fun config(): ShellConfig? = if (active) activeConfig else null

    fun appId(): Long = if (active) activeAppId else -1L

    fun activationAppId(): Long = if (active && activeAppId > 0L) activeAppId else -1L
}
