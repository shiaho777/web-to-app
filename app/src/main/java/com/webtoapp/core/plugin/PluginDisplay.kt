package com.webtoapp.core.plugin

import com.webtoapp.core.i18n.Strings

/** Host UI and the plugin sheet show these; the on-disk manifest stays English. */
fun Plugin.localizedName(): String =
    if (builtIn) Strings.builtinPluginName(id, name) else name

fun Plugin.localizedDescription(): String =
    if (builtIn) Strings.builtinPluginDescription(id, description) else description
