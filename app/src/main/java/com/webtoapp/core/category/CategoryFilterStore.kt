package com.webtoapp.core.category

import android.content.Context

/**
 * Persistence for the home-screen category filter.
 *
 * The selection is tristate — All / Uncategorized / a specific category id —
 * encoded as a single Long: [VALUE_ALL] for All, -1 for Uncategorized
 * (same value the UI uses), anything else a real category id. An absent key
 * also means All, so a fresh install and a wiped record behave identically.
 *
 * The `remember` toggle only gates restore-at-launch: the selection is always
 * written, so flipping the toggle on later still resumes the current choice.
 */
class CategoryFilterStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var rememberEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER, true)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER, value).apply()

    /** Saved selection, or null for "All" / no record. */
    fun loadSelection(): Long? {
        if (!prefs.contains(KEY_SELECTED)) return null
        return when (val v = prefs.getLong(KEY_SELECTED, VALUE_ALL)) {
            VALUE_ALL -> null
            else -> v
        }
    }

    fun saveSelection(categoryId: Long?) {
        prefs.edit().putLong(KEY_SELECTED, categoryId ?: VALUE_ALL).apply()
    }

    private companion object {
        const val PREFS_NAME = "home_category_filter"
        const val KEY_REMEMBER = "remember_enabled"
        const val KEY_SELECTED = "selected_category"
        const val VALUE_ALL = Long.MIN_VALUE
    }
}
