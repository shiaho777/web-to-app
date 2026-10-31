package com.webtoapp.core.webview

import com.webtoapp.util.JsStrings

/**
 * Calls into `window._translateCallback`. The page accepts either a JSON
 * string or an array; the native path always sends a quoted JSON string so a
 * translation that contains quotes or line breaks cannot be read as script,
 * and `JSON.parse` receives text rather than an array's comma-joined form.
 */
internal fun translateCallbackSuccess(callbackId: String, results: List<String>): String {
    val payload = encodeJsonStringArray(results)
    return "window._translateCallback(${JsStrings.quote(callbackId)}, ${JsStrings.quote(payload)});"
}

internal fun encodeJsonStringArray(values: List<String>): String = buildString {
    append('[')
    values.forEachIndexed { index, value ->
        if (index > 0) append(',')
        append('"')
        for (ch in value) {
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (ch.code < 32) append("\\u").append("%04x".format(ch.code))
                    else append(ch)
                }
            }
        }
        append('"')
    }
    append(']')
}

internal fun translateCallbackError(callbackId: String, error: String): String {
    return "window._translateCallback(${JsStrings.quote(callbackId)}, null, ${JsStrings.quote(error)});"
}
