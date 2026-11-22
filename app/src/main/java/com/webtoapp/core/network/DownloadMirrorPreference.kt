package com.webtoapp.core.network

import android.content.Context
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import java.util.Locale

internal object DownloadMirrorPreference {
    enum class Mode { AUTO, CN, GLOBAL }

    private const val PREFS_NAME = "dependency_download"
    private const val KEY_MODE = "mirror_mode"

    fun get(context: Context): Mode {
        val stored = preferences(context).getString(KEY_MODE, null)
        return Mode.entries.firstOrNull { it.name == stored } ?: Mode.AUTO
    }

    fun set(context: Context, mode: Mode) {
        preferences(context).edit().putString(KEY_MODE, mode.name).apply()
    }

    fun prefersChina(context: Context): Boolean = prefersChina(get(context), Strings.lang, Locale.getDefault())

    internal fun prefersChina(mode: Mode, language: AppLanguage, locale: Locale): Boolean = when (mode) {
        Mode.CN -> true
        Mode.GLOBAL -> false
        Mode.AUTO -> language == AppLanguage.CHINESE ||
            locale.language == "zh" || locale.country in setOf("CN", "HK", "MO", "TW")
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
