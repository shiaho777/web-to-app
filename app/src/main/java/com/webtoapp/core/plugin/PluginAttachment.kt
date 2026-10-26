package com.webtoapp.core.plugin

/**
 * Which plugin ids an app actually runs.
 *
 * An explicit attachment wins and keeps its order. With plugins switched on
 * and no attachment, the host's enabled set is used. Plugins switched off
 * run nothing. Blank ids are dropped. The enabled set is empty when the
 * state file has no `enabled` key, so an app that never picked a list does
 * not suddenly run every built-in.
 */
fun resolveAttachedPluginIds(
    pluginsEnabled: Boolean,
    attachedIds: List<String>,
    globalEnabledIds: List<String>,
): List<String> {
    if (!pluginsEnabled) return emptyList()
    val explicit = attachedIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    if (explicit.isNotEmpty()) return explicit
    return globalEnabledIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}

/** Add or remove one id. A blank id is ignored. Order of the remaining ids is kept. */
internal fun updateGlobalEnabledIds(current: List<String>, id: String, enabled: Boolean): List<String> {
    val clean = id.trim()
    if (clean.isEmpty()) return current
    return if (enabled) {
        if (clean in current) current else current + clean
    } else {
        current.filterNot { it == clean }
    }
}
