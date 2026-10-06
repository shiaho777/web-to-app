package com.webtoapp.core.webview

import android.webkit.WebView
import org.json.JSONObject

object StatusBarPageColorSampler {
    private val sampleScript = """
        (function() {
            function clamp(v) {
                return Math.max(0, Math.min(255, Math.round(v || 0)));
            }
            function toHex(v) {
                var hex = clamp(v).toString(16).toUpperCase();
                return hex.length === 1 ? '0' + hex : hex;
            }
            function rgbaToHex(r, g, b, a) {
                if (a == null) a = 1;
                if (a <= 0.02) return null;
                return '#' + toHex(r) + toHex(g) + toHex(b);
            }
            function parseColor(value) {
                if (!value) return null;
                var color = String(value).trim();
                if (!color || color === 'transparent') return null;
                if (color[0] === '#') {
                    var hex = color.slice(1);
                    if (hex.length === 3 || hex.length === 4) {
                        var r = parseInt(hex[0] + hex[0], 16);
                        var g = parseInt(hex[1] + hex[1], 16);
                        var b = parseInt(hex[2] + hex[2], 16);
                        var a = hex.length === 4 ? parseInt(hex[3] + hex[3], 16) / 255 : 1;
                        return rgbaToHex(r, g, b, a);
                    }
                    if (hex.length === 6 || hex.length === 8) {
                        var r = parseInt(hex.slice(0, 2), 16);
                        var g = parseInt(hex.slice(2, 4), 16);
                        var b = parseInt(hex.slice(4, 6), 16);
                        var a = hex.length === 8 ? parseInt(hex.slice(6, 8), 16) / 255 : 1;
                        return rgbaToHex(r, g, b, a);
                    }
                    return null;
                }
                var match = color.match(/^rgba?\(([^)]+)\)$/i);
                if (!match) return null;
                var parts = match[1].split(',').map(function(part) { return part.trim(); });
                if (parts.length < 3) return null;
                var r = parseFloat(parts[0]);
                var g = parseFloat(parts[1]);
                var b = parseFloat(parts[2]);
                var a = parts.length > 3 ? parseFloat(parts[3]) : 1;
                if (isNaN(r) || isNaN(g) || isNaN(b) || isNaN(a)) return null;
                return rgbaToHex(r, g, b, a);
            }
            function colorFromElement(element) {
                var current = element;
                while (current) {
                    try {
                        var color = parseColor(getComputedStyle(current).backgroundColor);
                        if (color) return color;
                    } catch (e) {
                    }
                    current = current.parentElement;
                }
                return null;
            }
            function sampleTopArea(y) {
                var width = Math.max(1, window.innerWidth || 0);
                var height = Math.max(1, window.innerHeight || 0);
                var sampleY = Math.max(1, Math.min(height - 1, Math.round(y)));
                var xs = [0.2, 0.5, 0.8].map(function(ratio) {
                    return Math.max(1, Math.min(width - 1, Math.round(width * ratio)));
                });
                for (var i = 0; i < xs.length; i++) {
                    var element = document.elementFromPoint(xs[i], sampleY);
                    var color = colorFromElement(element);
                    if (color) return color;
                }
                return null;
            }
            function metaThemeColor() {
                var meta = document.querySelector('meta[name="theme-color" i]');
                return meta ? parseColor(meta.getAttribute('content')) : null;
            }
            var top = sampleTopArea(1) ||
                sampleTopArea(6) ||
                sampleTopArea(12) ||
                sampleTopArea(20) ||
                sampleTopArea((window.visualViewport && window.visualViewport.offsetTop) || 1) ||
                colorFromElement(document.body) ||
                colorFromElement(document.documentElement) ||
                metaThemeColor() ||
                null;
            var bottom = sampleTopArea(heightEdge(-1)) ||
                sampleTopArea(heightEdge(-8)) ||
                sampleTopArea(heightEdge(-24)) ||
                top;
            function heightEdge(delta) {
                var height = Math.max(1, window.innerHeight || 0);
                return Math.max(1, Math.min(height - 1, height + delta));
            }
            return JSON.stringify({ top: top, bottom: bottom });
        })();
    """.trimIndent()

    fun sample(webView: WebView, onColors: (PageEdgeColors) -> Unit) {
        try {
            webView.evaluateJavascript(sampleScript) { result ->
                onColors(decodePageEdgeColors(result))
            }
        } catch (_: Exception) {
            onColors(PageEdgeColors(null, null))
        }
    }

    fun decodePageEdgeColors(result: String?): PageEdgeColors {
        if (result.isNullOrBlank() || result == "null") return PageEdgeColors(null, null)
        val text = unwrapJsString(result) ?: return PageEdgeColors(null, null)
        if (text.startsWith("#")) return PageEdgeColors(text, text)
        val parsed = runCatching { JSONObject(text) }.getOrNull()
            ?: return PageEdgeColors(null, null)
        return PageEdgeColors(
            top = parsed.optString("top").takeIf { it.isNotBlank() && it != "null" },
            bottom = parsed.optString("bottom").takeIf { it.isNotBlank() && it != "null" }
        )
    }

    private fun unwrapJsString(result: String): String? {
        val trimmed = result.trim()
        if (trimmed.startsWith("\"")) {
            return runCatching {
                JSONObject("""{"value":$trimmed}""").optString("value").takeIf { it.isNotBlank() }
            }.getOrNull()
        }
        return trimmed
    }
}

data class PageEdgeColors(val top: String?, val bottom: String?)

internal fun statusBarSampleDelays(firstDelayMs: Long, settle: Boolean): LongArray {
    val first = firstDelayMs.coerceAtLeast(0L)
    return if (settle) longArrayOf(first, first + 350L, first + 1200L) else longArrayOf(first)
}

/**
 * `evaluateJavascript` callbacks are not ordered. A slow early read must not
 * overwrite a color that a later read in the same burst already published,
 * and a cancelled burst must not publish at all (#1218).
 */
internal class PageColorSampleGate {
    private var generation = 0
    private var nextId = 0
    private var lastDelivered = 0

    fun begin(): Int {
        generation += 1
        return generation
    }

    fun open(burst: Int): Int? {
        if (burst != generation) return null
        nextId += 1
        return nextId
    }

    fun accept(burst: Int, sampleId: Int): Boolean {
        if (burst != generation || sampleId <= lastDelivered) return false
        lastDelivered = sampleId
        return true
    }
}

class StatusBarPageColorTracker(
    private val webView: WebView,
    private val shouldSample: () -> Boolean,
    private val onColors: (PageEdgeColors) -> Unit
) {
    private val gate = PageColorSampleGate()
    private val pending = ArrayList<Runnable>(3)

    fun attach() {
        webView.setOnScrollChangeListener { _, _, _, _, _ ->
            // One sample is enough once the user is moving the page.
            scheduleSample(48L)
        }
    }

    fun detach() {
        cancelPending()
        webView.setOnScrollChangeListener(null)
    }

    fun reset() {
        cancelPending()
        onColors(PageEdgeColors(null, null))
    }

    /**
     * [settle] posts two later samples. A page's background often paints
     * after the first `onPageFinished` tick, and a single early read stays
     * on the light fallback until the next scroll or tab change (#1218).
     */
    fun scheduleSample(delayMs: Long = 0L, settle: Boolean = false) {
        val burst = cancelPending()
        if (!shouldSample()) {
            onColors(PageEdgeColors(null, null))
            return
        }
        for (delay in statusBarSampleDelays(delayMs, settle)) {
            val task = Runnable {
                val sampleId = gate.open(burst) ?: return@Runnable
                if (!shouldSample()) {
                    if (gate.accept(burst, sampleId)) onColors(PageEdgeColors(null, null))
                    return@Runnable
                }
                StatusBarPageColorSampler.sample(webView) { colors ->
                    if (gate.accept(burst, sampleId)) onColors(colors)
                }
            }
            pending.add(task)
            webView.postDelayed(task, delay)
        }
    }

    private fun cancelPending(): Int {
        val burst = gate.begin()
        for (task in pending) webView.removeCallbacks(task)
        pending.clear()
        return burst
    }
}
