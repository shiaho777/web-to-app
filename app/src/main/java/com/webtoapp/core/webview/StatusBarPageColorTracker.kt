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
            return sampleTopArea(1) ||
                sampleTopArea(6) ||
                sampleTopArea(12) ||
                sampleTopArea(20) ||
                sampleTopArea((window.visualViewport && window.visualViewport.offsetTop) || 1) ||
                colorFromElement(document.body) ||
                colorFromElement(document.documentElement) ||
                metaThemeColor() ||
                null;
        })();
    """.trimIndent()

    fun sample(webView: WebView, onColorSampled: (String?) -> Unit) {
        try {
            webView.evaluateJavascript(sampleScript) { result ->
                onColorSampled(decodeJsString(result))
            }
        } catch (_: Exception) {
            onColorSampled(null)
        }
    }

    private fun decodeJsString(result: String?): String? {
        if (result.isNullOrBlank() || result == "null") return null
        return runCatching {
            JSONObject("""{"value":$result}""").optString("value").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

class StatusBarPageColorTracker(
    private val webView: WebView,
    private val shouldSample: () -> Boolean,
    private val onColorChanged: (String?) -> Unit
) {
    private val sampleRunnable = Runnable {
        if (!shouldSample()) {
            onColorChanged(null)
            return@Runnable
        }
        StatusBarPageColorSampler.sample(webView, onColorChanged)
    }

    fun attach() {
        webView.setOnScrollChangeListener { _, _, _, _, _ ->
            scheduleSample(48L)
        }
    }

    fun detach() {
        webView.removeCallbacks(sampleRunnable)
        webView.setOnScrollChangeListener(null)
    }

    fun reset() {
        webView.removeCallbacks(sampleRunnable)
        onColorChanged(null)
    }

    fun scheduleSample(delayMs: Long = 0L) {
        webView.removeCallbacks(sampleRunnable)
        if (!shouldSample()) {
            onColorChanged(null)
            return
        }
        webView.postDelayed(sampleRunnable, delayMs.coerceAtLeast(0L))
    }
}
