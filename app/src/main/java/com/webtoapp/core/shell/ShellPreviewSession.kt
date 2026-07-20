package com.webtoapp.core.shell

object ShellPreviewSession {
    private val lock = Any()
    private val sessions = LinkedHashMap<Long, ShellConfig>()

    fun begin(config: ShellConfig, appId: Long) {
        val key = normalizeAppId(appId)
        synchronized(lock) {
            sessions[key] = config
        }
    }

    fun end() {
        synchronized(lock) {
            sessions.clear()
        }
    }

    fun end(appId: Long) {
        synchronized(lock) {
            sessions.remove(normalizeAppId(appId))
        }
    }

    fun endIfMatches(appId: Long) {
        synchronized(lock) {
            val key = normalizeAppId(appId)
            if (key > 0L) {
                sessions.remove(key)
            } else {
                sessions.clear()
            }
        }
    }

    fun isActive(): Boolean = synchronized(lock) { sessions.isNotEmpty() }

    fun config(): ShellConfig? = synchronized(lock) {
        when {
            sessions.isEmpty() -> null
            sessions.size == 1 -> sessions.values.first()
            else -> sessions.values.lastOrNull()
        }
    }

    fun config(appId: Long): ShellConfig? = synchronized(lock) {
        val key = normalizeAppId(appId)
        if (key > 0L) {
            sessions[key]
        } else {
            config()
        }
    }

    fun appId(): Long = synchronized(lock) {
        sessions.keys.lastOrNull { it > 0L } ?: sessions.keys.lastOrNull() ?: -1L
    }

    fun activationAppId(): Long {
        val id = appId()
        return if (id > 0L) id else -1L
    }

    private fun normalizeAppId(appId: Long): Long = if (appId > 0L) appId else -1L
}
