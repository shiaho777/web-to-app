package com.webtoapp.data.model

/**
 * Resolved visibility of each toolbar control in the shell/preview browser toolbar.
 *
 * Semantics: the "hide browser toolbar" toggle (`hideBrowserToolbar`) switches the
 * toolbar into a customized slim mode where only the explicitly-checked items
 * (`toolbarShow*`) appear. In the normal mode (`hideBrowserToolbar = false`) the
 * toolbar always shows the full button set — the `toolbarShow*` checkboxes are only
 * editable while the hide toggle is on, so their values must not be applied in the
 * normal mode (doing so leaves a toolbar with every button hidden once the toggle is
 * turned back off).
 */
data class ToolbarButtonVisibility(
    val showTitle: Boolean,
    val showUrl: Boolean,
    val showBack: Boolean,
    val showForward: Boolean,
    val showRefresh: Boolean,
    val showConsoleButton: Boolean,
    val showOverflowButton: Boolean
)

/**
 * Resolves which toolbar buttons are visible given the hide-toggle state and the
 * customized toolbar content flags. See [ToolbarButtonVisibility] for the contract.
 */
fun resolveToolbarButtons(
    hideBrowserToolbar: Boolean,
    browserToolbarCustomized: Boolean,
    toolbarShowTitle: Boolean,
    toolbarShowUrl: Boolean,
    toolbarShowBack: Boolean,
    toolbarShowForward: Boolean,
    toolbarShowRefresh: Boolean
): ToolbarButtonVisibility {
    val customizedSlim = hideBrowserToolbar && browserToolbarCustomized
    val hasAnyToolbarItem = toolbarShowTitle || toolbarShowUrl ||
        toolbarShowBack || toolbarShowForward || toolbarShowRefresh
    return ToolbarButtonVisibility(
        showTitle = !customizedSlim || toolbarShowTitle,
        showUrl = !customizedSlim || toolbarShowUrl,
        showBack = !customizedSlim || toolbarShowBack,
        showForward = !customizedSlim || toolbarShowForward,
        showRefresh = !customizedSlim || toolbarShowRefresh,
        showConsoleButton = !customizedSlim || hasAnyToolbarItem,
        showOverflowButton = !customizedSlim || hasAnyToolbarItem
    )
}
