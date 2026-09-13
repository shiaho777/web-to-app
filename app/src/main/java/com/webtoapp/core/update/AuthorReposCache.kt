package com.webtoapp.core.update

import android.content.Context
import com.webtoapp.core.logging.AppLogger

/**
 * SharedPreferences cache of the author's repo list (raw GitHub JSON).
 * The About page renders it instantly on revisit, then swaps in a fresh
 * fetch — matching the WebViewResumeStore / CategoryFilterStore pattern.
 */
object AuthorReposCache {

    private const val TAG = "AuthorReposCache"
    private const val PREFS = "author_repos_cache"
    private const val KEY_JSON = "repos_json"
    private const val KEY_FETCHED_AT = "fetched_at"

    fun read(context: Context): List<UpdateChecker.RepoSummary>? {
        val json = prefs(context).getString(KEY_JSON, null) ?: return null
        return try {
            UpdateChecker.parseAuthorRepos(json).takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Cached repos JSON failed to parse: ${e.message}")
            null
        }
    }

    fun write(context: Context, reposJson: String) {
        prefs(context).edit()
            .putString(KEY_JSON, reposJson)
            .putLong(KEY_FETCHED_AT, System.currentTimeMillis())
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
