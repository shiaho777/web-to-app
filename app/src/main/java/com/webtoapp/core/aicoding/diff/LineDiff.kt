package com.webtoapp.core.aicoding.diff

object LineDiff {

    data class Line(val kind: Kind, val text: String) {
        enum class Kind { Context, Added, Removed }
    }

    fun diff(oldText: String, newText: String): List<Line> {
        if (oldText == newText) {
            return oldText.lines().map { Line(Line.Kind.Context, it) }
        }
        val a = oldText.lines()
        val b = newText.lines()
        val n = a.size
        val m = b.size

        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (a[i] == b[j]) dp[i + 1][j + 1] + 1
                           else maxOf(dp[i + 1][j], dp[i][j + 1])
            }
        }

        val out = ArrayList<Line>(n + m)
        var i = 0
        var j = 0
        while (i < n && j < m) {
            when {
                a[i] == b[j] -> {
                    out += Line(Line.Kind.Context, a[i]); i++; j++
                }
                dp[i + 1][j] >= dp[i][j + 1] -> {
                    out += Line(Line.Kind.Removed, a[i]); i++
                }
                else -> {
                    out += Line(Line.Kind.Added, b[j]); j++
                }
            }
        }
        while (i < n) { out += Line(Line.Kind.Removed, a[i]); i++ }
        while (j < m) { out += Line(Line.Kind.Added, b[j]); j++ }
        return out
    }

    data class Stats(val added: Int, val removed: Int)

    fun stats(lines: List<Line>): Stats = Stats(
        added = lines.count { it.kind == Line.Kind.Added },
        removed = lines.count { it.kind == Line.Kind.Removed }
    )
}
