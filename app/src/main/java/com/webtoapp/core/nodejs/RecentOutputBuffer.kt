package com.webtoapp.core.nodejs

/**
 * Bounded tail of Node stdout/stderr lines for failure reporting. When the server never
 * comes up the only surfaced message used to be "Node.js 服务器启动超时" while the real
 * error (missing module, EADDRINUSE, native addon dlopen failure, ...) already streamed
 * through [NodeBridge.OutputCallback] into ShellLogger — a file under Android/data that
 * most users cannot reach. Snapshotting the tail into the IPC failure message makes the
 * on-screen error report self-diagnosing.
 */
class RecentOutputBuffer(
    private val maxLines: Int = 80,
    private val maxChars: Int = 6000
) {

    private val lines = ArrayDeque<String>()

    @Synchronized
    fun add(line: String) {
        lines.addLast(line)
        while (lines.size > maxLines) lines.removeFirst()
    }

    @Synchronized
    fun clear() {
        lines.clear()
    }

    @Synchronized
    fun contains(substring: String): Boolean = lines.any { it.contains(substring) }

    /**
     * Last [maxLines] lines joined by newlines, trimmed from the front to at most
     * [maxChars] (preferring a line boundary so the first kept line isn't a fragment).
     * Returns "" when nothing was captured.
     */
    @Synchronized
    fun snapshot(): String {
        if (lines.isEmpty()) return ""
        var text = lines.joinToString("\n")
        if (text.length > maxChars) {
            val cut = text.length - maxChars
            val newline = text.indexOf('\n', cut)
            text = "…" + text.substring(if (newline > 0) newline + 1 else cut)
        }
        return text
    }
}
