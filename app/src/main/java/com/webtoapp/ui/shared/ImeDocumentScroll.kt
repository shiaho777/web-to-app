package com.webtoapp.ui.shared

import kotlin.math.roundToInt

/**
 * How far to scroll the document so the focused field stays above the keyboard
 * without moving the WebView itself.
 *
 * Coordinates are the field's position at capture, in WebView pixels. Later
 * frames subtract what has already been scrolled. A field that already sits
 * above the keyboard produces no delta (#1398).
 */
internal class ImeDocumentScroll {
    private var generation = 0
    private var trackingGeneration = -1
    private var snapshotRequested = false
    private var elementTopPx: Int? = null
    private var elementBottomPx: Int? = null
    private var scrolledPx = 0

    fun begin(): Int {
        generation += 1
        trackingGeneration = generation
        snapshotRequested = false
        elementTopPx = null
        elementBottomPx = null
        scrolledPx = 0
        return generation
    }

    fun end() {
        trackingGeneration = -1
    }

    val wantsSnapshot: Boolean
        get() = trackingGeneration == generation && elementBottomPx == null && !snapshotRequested

    /** @return the generation a single snapshot should be tagged with, or null if one is already in flight. */
    fun requestSnapshot(): Int? {
        if (trackingGeneration != generation || elementBottomPx != null || snapshotRequested) return null
        snapshotRequested = true
        return trackingGeneration
    }

    /** The page had no focused field yet. Allow the next frame to ask again. */
    fun snapshotMissed(generation: Int) {
        if (generation != trackingGeneration || elementBottomPx != null) return
        snapshotRequested = false
    }

    fun capture(
        topCss: Float,
        bottomCss: Float,
        viewportHeightCss: Float,
        webViewHeightPx: Int,
        generation: Int,
    ) {
        if (generation != trackingGeneration) return
        if (elementBottomPx != null) return
        if (viewportHeightCss <= 0f || webViewHeightPx <= 0) return
        val pxPerCss = webViewHeightPx.toFloat() / viewportHeightCss
        elementTopPx = (topCss * pxPerCss).roundToInt()
        elementBottomPx = (bottomCss * pxPerCss).roundToInt()
    }

    /**
     * Additional pixels to scroll the document down this frame.
     * Never negative: closing the keyboard leaves the scroll where it is.
     */
    fun scrollDelta(webViewHeightPx: Int, overlapPx: Int): Int {
        if (trackingGeneration != generation) return 0
        if (overlapPx <= 0 || webViewHeightPx <= 0) return 0
        val top0 = elementTopPx ?: return 0
        val bottom0 = elementBottomPx ?: return 0
        val visibleBottom = webViewHeightPx - overlapPx.coerceAtMost(webViewHeightPx)
        val currentTop = top0 - scrolledPx
        val currentBottom = bottom0 - scrolledPx
        if (currentTop >= 0 && currentBottom <= visibleBottom) return 0
        val fieldHeight = (bottom0 - top0).coerceAtLeast(0)
        val delta = if (fieldHeight <= visibleBottom.coerceAtLeast(0)) {
            (currentBottom - visibleBottom).coerceAtLeast(0)
        } else {
            currentTop.coerceAtLeast(0)
        }
        scrolledPx += delta
        return delta
    }
}

/** How many pixels of [webHeight] the keyboard covers. */
internal fun keyboardOverlapPx(
    webTop: Int,
    webHeight: Int,
    contentTop: Int,
    contentHeight: Int,
    imeBottomPx: Int,
): Int {
    if (imeBottomPx <= 0 || webHeight <= 0 || contentHeight <= 0) return 0
    val imeTop = contentTop + contentHeight - imeBottomPx
    val webBottom = webTop + webHeight
    return (webBottom - imeTop).coerceIn(0, webHeight)
}

internal data class FocusedFieldSnapshot(
    val topCss: Float,
    val bottomCss: Float,
    val viewportHeightCss: Float,
)

/**
 * Parses the string returned by [FOCUSED_FIELD_SNAPSHOT_JS].
 * `evaluateJavascript` JSON-encodes a string result, so the callback value
 * is quoted.
 */
internal fun parseFocusedFieldSnapshot(callbackJson: String?): FocusedFieldSnapshot? {
    if (callbackJson.isNullOrBlank() || callbackJson == "null") return null
    var raw = callbackJson.trim()
    if (raw.length >= 2 && raw.first() == '"' && raw.last() == '"') {
        raw = raw.substring(1, raw.length - 1)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
    if (raw.isEmpty()) return null
    val parts = raw.split(',')
    if (parts.size != 3) return null
    val top = parts[0].toFloatOrNull() ?: return null
    val bottom = parts[1].toFloatOrNull() ?: return null
    val viewport = parts[2].toFloatOrNull() ?: return null
    if (viewport <= 0f || bottom < top) return null
    return FocusedFieldSnapshot(top, bottom, viewport)
}

internal const val FOCUSED_FIELD_SNAPSHOT_JS = """
(function() {
  try {
    var el = document.activeElement;
    if (!el || (el.tagName !== 'INPUT' && el.tagName !== 'TEXTAREA' && !el.isContentEditable)) return '';
    var rect = el.getBoundingClientRect();
    var vh = window.visualViewport ? window.visualViewport.height : window.innerHeight;
    if (!vh) return '';
    return rect.top + ',' + rect.bottom + ',' + vh;
  } catch (e) {
    return '';
  }
})()
"""
