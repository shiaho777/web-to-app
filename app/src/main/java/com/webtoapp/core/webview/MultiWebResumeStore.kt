package com.webtoapp.core.webview

import android.content.Context

/**
 * Selected-site persistence for multi-web apps.
 *
 * The shell Activity's savedInstanceState bundle only covers process-death
 * recreations; a cold relaunch (task killed, launcher restart) rebuilds the
 * composition from scratch and every display mode used to fall back to the
 * first site in the list (#1036). Recording the selected site id on every
 * switch lets a fresh start resume on the site the user was actually on —
 * the multi-web counterpart of WebViewResumeStore's last-URL resume.
 */
class MultiWebResumeStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Records [siteId] as the current selection for [key]. A null/blank id
     * clears the record — card mode's site grid is "no site selected" and a
     * cold start must not resurrect a closed site.
     */
    fun persistSelectedSiteId(key: String, siteId: String?) {
        val prefKey = key + SUFFIX_SITE
        if (siteId.isNullOrBlank()) {
            prefs.edit().remove(prefKey).apply()
        } else {
            prefs.edit().putString(prefKey, siteId).apply()
        }
    }

    /**
     * The last recorded site id, or null. Callers validate it against the
     * current site list — sites can be edited or reordered between sessions,
     * and an unknown id must fall back to the default selection.
     */
    fun resumeSiteId(key: String): String? = prefs.getString(key + SUFFIX_SITE, null)

    /** Site ids the user marked in the side drawer. Blank ids are dropped. */
    fun savedSiteIds(key: String): Set<String> {
        val raw = prefs.getStringSet(key + SUFFIX_SAVED, null) ?: return emptySet()
        return raw.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun persistSavedSiteIds(key: String, siteIds: Set<String>) {
        val clean = siteIds.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val prefKey = key + SUFFIX_SAVED
        if (clean.isEmpty()) {
            prefs.edit().remove(prefKey).apply()
        } else {
            prefs.edit().putStringSet(prefKey, clean).apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "multiweb_resume"
        const val SUFFIX_SITE = ".site"
        const val SUFFIX_SAVED = ".saved"
    }
}

/** Drawer search matches the visible name, the host, or the full URL. */
internal fun multiWebSiteMatchesQuery(name: String, url: String, domain: String, query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    return name.contains(q, ignoreCase = true) ||
        domain.contains(q, ignoreCase = true) ||
        url.contains(q, ignoreCase = true)
}
