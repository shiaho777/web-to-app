package com.webtoapp.util

/**
 * JS string-literal escaping without org.json — JSONObject.quote is an Android stub that
 * throws "not mocked" under plain-JVM unit tests (Robolectric does not mock it either),
 * and these escapes sit on hot paths that must stay unit-testable.
 */
object JsStrings {

    /** Escape [s] for embedding inside a single- or double-quoted JS string (no quotes added). */
    fun escape(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        val sb = StringBuilder(s.length + 8)
        for (ch in s) {
            when (ch) {
                '"', '\\', '\'' -> sb.append('\\').append(ch)
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    val code = ch.code
                    when {
                        code == 8 -> sb.append("\\b")   // backspace
                        code == 12 -> sb.append("\\f")  // form feed
                        code < 32 -> sb.append("\\u").append(String.format("%04x", code))
                        else -> sb.append(ch)
                    }
                }
            }
        }
        return sb.toString()
    }

    /** [escape] wrapped in double quotes — a complete JS string literal. */
    fun quote(s: String?): String = "\"" + escape(s) + "\""
}
