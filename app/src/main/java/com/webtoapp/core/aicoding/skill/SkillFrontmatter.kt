package com.webtoapp.core.aicoding.skill

internal object SkillFrontmatter {

    private val FENCE = Regex("^---\\s*$")

    fun split(text: String): Pair<Map<String, Any>, String> {
        val lines = text.split('\n')
        if (lines.isEmpty() || !FENCE.matches(lines[0].trimEnd())) return emptyMap<String, Any>() to text
        val closeIdx = lines.drop(1).indexOfFirst { FENCE.matches(it.trimEnd()) }
        if (closeIdx < 0) return emptyMap<String, Any>() to text
        val rawFront = lines.subList(1, closeIdx + 1)
        val body = lines.drop(closeIdx + 2).joinToString("\n")
        return parseFrontmatter(rawFront) to body
    }

    fun parseFrontmatter(lines: List<String>): Map<String, Any> {
        val out = LinkedHashMap<String, Any>()
        var i = 0
        while (i < lines.size) {
            val raw = lines[i]
            val trimmed = raw.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) { i++; continue }
            val colon = trimmed.indexOf(':')
            if (colon < 0) { i++; continue }
            val key = trimmed.substring(0, colon).trim().lowercase().replace('-', '_')
            val rest = trimmed.substring(colon + 1).trim()

            when {
                rest.isEmpty() -> {

                    val items = mutableListOf<String>()
                    var j = i + 1
                    while (j < lines.size && lines[j].startsWith(' ') && lines[j].trim().startsWith("- ")) {
                        items += stripQuotes(lines[j].trim().removePrefix("-").trim())
                        j++
                    }
                    if (items.isNotEmpty()) { out[key] = items; i = j; continue }
                    out[key] = ""
                    i++
                }
                rest.startsWith("[") && rest.endsWith("]") -> {
                    val inner = rest.substring(1, rest.length - 1)
                    out[key] = inner.split(',').map { stripQuotes(it.trim()) }.filter { it.isNotEmpty() }
                    i++
                }
                rest.equals("true", ignoreCase = true) || rest.equals("yes", ignoreCase = true) -> {
                    out[key] = true; i++
                }
                rest.equals("false", ignoreCase = true) || rest.equals("no", ignoreCase = true) -> {
                    out[key] = false; i++
                }
                rest.toIntOrNull() != null -> {
                    out[key] = rest.toInt(); i++
                }
                rest.contains(',') -> {
                    out[key] = rest.split(',').map { stripQuotes(it.trim()) }.filter { it.isNotEmpty() }
                    i++
                }
                else -> {
                    out[key] = stripQuotes(rest); i++
                }
            }
        }
        return out
    }

    private fun stripQuotes(s: String): String {
        if (s.length >= 2 && (s.startsWith('"') && s.endsWith('"') || s.startsWith('\'') && s.endsWith('\''))) {
            return s.substring(1, s.length - 1)
        }
        return s
    }
}
