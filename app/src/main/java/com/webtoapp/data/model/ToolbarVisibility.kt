package com.webtoapp.data.model

/**
 * Resolved visibility of each toolbar control in the shell/preview browser toolbar.
 *
 * Semantics: the "browser toolbar" master switch (`browserToolbarEnabled`) decides
 * whether the toolbar renders at all — off means the page runs with no bar at all.
 * When enabled, each `toolbarShow*` flag controls its own button. The editor flips
 * all flags together with the master switch, so a fresh "on" state is all-on and a
 * fresh "off" state is all-off.
 */
data class ToolbarButtonVisibility(
    val showTitle: Boolean,
    val showUrl: Boolean,
    val showBack: Boolean,
    val showForward: Boolean,
    val showRefresh: Boolean,
    val showConsoleButton: Boolean,
    val showFind: Boolean
)

/**
 * Whether the enabled toolbar has at least one item to render. Console is a toolbar
 * item in its own right: a toolbar customized down to only that button must still
 * render. Both the host preview and the exported shell gate the toolbar on this
 * predicate — keep them on the shared function so the two paths cannot drift apart.
 * The console/find flags have NO defaults on purpose: every caller must pass them
 * explicitly, so a new flag can never be silently treated as "on" by one path and
 * not the other.
 */
fun hasAnyToolbarItem(
    toolbarShowTitle: Boolean,
    toolbarShowUrl: Boolean,
    toolbarShowBack: Boolean,
    toolbarShowForward: Boolean,
    toolbarShowRefresh: Boolean,
    toolbarShowConsole: Boolean,
    toolbarShowFind: Boolean
): Boolean = toolbarShowTitle || toolbarShowUrl || toolbarShowBack || toolbarShowForward ||
    toolbarShowRefresh || toolbarShowConsole || toolbarShowFind

/**
 * Resolves which toolbar buttons are visible. Every button requires BOTH the master
 * switch and its own flag. The console/find flags have NO defaults on purpose — same
 * anti-drift rule as [hasAnyToolbarItem].
 */
/**
 * The browser toolbar follows its own switch. Fullscreen (`hideToolbar`) only
 * hides the system bars, so turning both on keeps the toolbar.
 */
fun isBrowserToolbarVisible(toolbarEnabled: Boolean, hasAnyItem: Boolean): Boolean =
    toolbarEnabled && hasAnyItem

/**
 * Drawer mode draws its own site bar only when the browser toolbar is absent.
 * While that toolbar is up, the site menu joins it instead of stacking a second row.
 */
fun drawerSiteBarVisible(browserToolbarVisible: Boolean): Boolean = !browserToolbarVisible

/** Drop the scheme and the query so the address fits on the same line as the title. */
fun String.shortenForBrowserChrome(): String {
    val withoutScheme = when {
        startsWith("https://") -> substring(8)
        startsWith("http://") -> substring(7)
        else -> this
    }
    return withoutScheme.substringBefore('?').substringBefore('#')
}

/**
 * Resolves which toolbar buttons are visible. Every button requires BOTH the master
 * switch and its own flag. The console/find flags have NO defaults on purpose — same
 * anti-drift rule as [hasAnyToolbarItem].
 */
fun resolveToolbarButtons(
    toolbarEnabled: Boolean,
    toolbarShowTitle: Boolean,
    toolbarShowUrl: Boolean,
    toolbarShowBack: Boolean,
    toolbarShowForward: Boolean,
    toolbarShowRefresh: Boolean,
    toolbarShowConsole: Boolean,
    toolbarShowFind: Boolean
): ToolbarButtonVisibility = ToolbarButtonVisibility(
    showTitle = toolbarEnabled && toolbarShowTitle,
    showUrl = toolbarEnabled && toolbarShowUrl,
    showBack = toolbarEnabled && toolbarShowBack,
    showForward = toolbarEnabled && toolbarShowForward,
    showRefresh = toolbarEnabled && toolbarShowRefresh,
    showConsoleButton = toolbarEnabled && toolbarShowConsole,
    showFind = toolbarEnabled && toolbarShowFind
)
