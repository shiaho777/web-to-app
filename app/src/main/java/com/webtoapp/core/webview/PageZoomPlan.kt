package com.webtoapp.core.webview

/**
 * Whole-page zoom plan for [WebViewManager.configureWebView] (#654).
 *
 * `pageZoomPercent` is the build-time per-app zoom (100 = default). It is applied
 * via [android.webkit.WebView.setInitialScale], which scales the rendered page as
 * a whole — text AND layout/images/canvas — unlike `textZoom` (text glyphs only,
 * invisible on dashboard/canvas UIs such as OpenChamber).
 *
 * Pure logic, no Android dependencies: unit-tested on plain JVM.
 */
internal data class PageZoomPlan(
    /** True when an explicit non-100 zoom is configured. */
    val zoomActive: Boolean,
    /** Value for `setInitialScale`; 0 = auto (default path untouched). */
    val initialScalePercent: Int
)

internal fun planPageZoom(pageZoomPercent: Int, initialScaleField: Int): PageZoomPlan {
    // 0 = legacy data without the field; treat as 100 (default).
    val zoom = if (pageZoomPercent > 0) pageZoomPercent else 100
    if (zoom != 100) return PageZoomPlan(zoomActive = true, initialScalePercent = zoom)
    // Default path preserves the dormant `initialScale` field semantics.
    return PageZoomPlan(zoomActive = false, initialScalePercent = if (initialScaleField > 0) initialScaleField else 0)
}
