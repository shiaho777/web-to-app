package com.webtoapp.ui.shared

/**
 * Reads how a page sees the keyboard and decides whether the host must shrink
 * the WebView for it.
 *
 * Resize mode cannot scroll a page into view on its own: the document only has
 * as much scroll range as the WebView leaves it. A field at the end of a short
 * page cannot be scrolled above the keyboard while the WebView keeps its full
 * height, so the page has to lose that height exactly once (#1473).
 *
 * Some WebView builds already react to the keyboard by insetting the visual
 * viewport or by resizing the layout viewport. Shrinking the view again would
 * move the focused field a second time, which is the flash reported in #1419.
 * Those builds are detected and left alone.
 */
internal data class ImeProbe(
    val innerHeightCss: Float,
    val visualHeightCss: Float,
    val visualTopCss: Float,
    val fieldTopCss: Float?,
    val fieldBottomCss: Float?,
)

private const val VIEWPORT_TOLERANCE_CSS = 2f
private const val FIELD_EDGE_TOLERANCE_CSS = 1f
private const val NO_FIELD = -1f

internal fun parseImeProbe(callbackJson: String?): ImeProbe? {
    if (callbackJson.isNullOrBlank() || callbackJson == "null") return null
    var raw = callbackJson.trim()
    if (raw.length >= 2 && raw.first() == '"' && raw.last() == '"') {
        raw = raw.substring(1, raw.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
    }
    val parts = raw.split(',')
    if (parts.size != 5) return null
    val inner = parts[0].toFloatOrNull() ?: return null
    val visualHeight = parts[1].toFloatOrNull() ?: return null
    val visualTop = parts[2].toFloatOrNull() ?: return null
    val fieldTop = parts[3].toFloatOrNull() ?: return null
    val fieldBottom = parts[4].toFloatOrNull() ?: return null
    if (inner <= 0f || visualHeight < 0f) return null
    return ImeProbe(
        innerHeightCss = inner,
        visualHeightCss = visualHeight,
        visualTopCss = visualTop.coerceAtLeast(0f),
        fieldTopCss = fieldTop.takeIf { it != NO_FIELD },
        fieldBottomCss = fieldBottom.takeIf { it != NO_FIELD },
    )
}

/**
 * True when the WebView already keeps the page above the keyboard, either by
 * insetting the visual viewport or by resizing the layout viewport itself.
 *
 * [restCssPerPx] is `innerHeight / webViewHeight` measured with the keyboard
 * closed. Without it only the visual-viewport inset is detectable.
 */
internal fun resizedBySystem(
    probe: ImeProbe,
    webViewHeightPx: Int,
    restCssPerPx: Float?,
): Boolean {
    if (probe.visualHeightCss > 0f &&
        probe.visualHeightCss < probe.innerHeightCss - VIEWPORT_TOLERANCE_CSS
    ) {
        return true
    }
    if (restCssPerPx != null && restCssPerPx > 0f && webViewHeightPx > 0) {
        val expected = webViewHeightPx * restCssPerPx
        if (probe.innerHeightCss < expected - VIEWPORT_TOLERANCE_CSS) return true
    }
    return false
}

/** Bottom of the band a page keeps once the keyboard owns its lower edge, in CSS px. */
internal fun keyboardBandBottomCss(
    probe: ImeProbe,
    webViewHeightPx: Int,
    imeBottomPx: Int,
): Float {
    if (webViewHeightPx <= 0) return probe.innerHeightCss
    val cssPerPx = probe.innerHeightCss / webViewHeightPx
    return probe.innerHeightCss - imeBottomPx.coerceAtLeast(0) * cssPerPx
}

/** True when the focused field would sit outside the band left above the keyboard. */
internal fun fieldBehindKeyboard(
    probe: ImeProbe,
    webViewHeightPx: Int,
    imeBottomPx: Int,
): Boolean {
    val top = probe.fieldTopCss ?: return false
    val bottom = probe.fieldBottomCss ?: return false
    if (imeBottomPx <= 0) return false
    val bandBottom = keyboardBandBottomCss(probe, webViewHeightPx, imeBottomPx)
    return bottom > bandBottom + FIELD_EDGE_TOLERANCE_CSS || top < -FIELD_EDGE_TOLERANCE_CSS
}

/**
 * Whether the page needs the WebView to give up the keyboard height. Pages that
 * already avoid the keyboard keep their full height so the focused field is not
 * moved twice.
 */
internal fun needsKeyboardResize(
    probe: ImeProbe,
    webViewHeightPx: Int,
    imeBottomPx: Int,
    restCssPerPx: Float?,
): Boolean {
    if (imeBottomPx <= 0 || webViewHeightPx <= 0) return false
    if (probe.innerHeightCss <= 0f) return false
    return !resizedBySystem(probe, webViewHeightPx, restCssPerPx)
}

internal const val IME_PROBE_JS = """
(function() {
  try {
    var vv = window.visualViewport;
    var el = document.activeElement;
    var editable = el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.isContentEditable);
    var rect = editable ? el.getBoundingClientRect() : null;
    return (window.innerHeight || 0) + ',' +
      (vv ? vv.height : 0) + ',' +
      (vv ? vv.offsetTop : 0) + ',' +
      (rect ? rect.top : -1) + ',' +
      (rect ? rect.bottom : -1);
  } catch (e) {
    return '';
  }
})()
"""

internal const val IME_REVEAL_FIELD_JS = """
(function() {
  try {
    var el = document.activeElement;
    if (!el || (el.tagName !== 'INPUT' && el.tagName !== 'TEXTAREA' && !el.isContentEditable)) return '';
    var rect = el.getBoundingClientRect();
    var vv = window.visualViewport;
    var top = vv ? vv.offsetTop : 0;
    var bottom = vv ? vv.offsetTop + vv.height : (window.innerHeight || 0);
    if (rect.top >= top - 1 && rect.bottom <= bottom + 1) return 'visible';
    el.scrollIntoView({ block: 'nearest', inline: 'nearest' });
    return 'scrolled';
  } catch (e) {
    return '';
  }
})()
"""
