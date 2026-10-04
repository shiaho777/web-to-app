package com.webtoapp.core.home

import android.content.Context
import com.webtoapp.data.home.AppListSort

/**
 * Home-list sort mode. Independent of the category filter: the choice is
 * always restored, including on a fresh install (recently updated).
 */
class AppListSortStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AppListSort = AppListSort.fromStored(prefs.getString(KEY_MODE, null))

    fun save(sort: AppListSort) {
        prefs.edit().putString(KEY_MODE, sort.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "home_app_list_sort"
        const val KEY_MODE = "mode"
    }
}
