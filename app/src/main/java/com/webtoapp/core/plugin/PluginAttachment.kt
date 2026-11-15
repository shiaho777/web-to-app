package com.webtoapp.core.plugin

fun resolveAttachedPluginIds(
    pluginsEnabled: Boolean,
    attachedIds: List<String>,
    fallbackIds: List<String>,
): List<String> {
    if (!pluginsEnabled) return emptyList()
    val explicit = attachedIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    if (explicit.isNotEmpty()) return explicit
    return fallbackIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}