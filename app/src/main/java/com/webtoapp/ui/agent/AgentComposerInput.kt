package com.webtoapp.ui.agent

internal object AgentFileMentions {
    private val pathCharacters = """\p{L}\p{M}\p{N}_./\-"""
    private val quotedCharacters = """(?:\\["\\]|[^"\\\r\n])*"""
    private val complete = Regex("""(?<!\S)@(?:"($quotedCharacters)"|([$pathCharacters]+))""")
    private val active = Regex("""(?<!\S)@(?:"($quotedCharacters)|([$pathCharacters]*))\z""")
    private val plain = Regex("""[$pathCharacters]+""")
    private val trailingPunctuation = charArrayOf('.', ',', ';', ':', '!', '?', ')', ']')

    data class Query(val start: Int, val end: Int, val value: String)
    data class Edit(val text: String, val caret: Int)

    fun atCaret(text: String, caret: Int): Query? {
        val end = caret.coerceIn(0, text.length)
        val prefix = active.find(text.substring(0, end)) ?: return null
        val start = prefix.range.first
        val entire = complete.matchAt(text, start)
        val tokenEnd = maxOf(end, entire?.range?.last?.plus(1) ?: end)
        return Query(start, tokenEnd, prefix.groups[1]?.value?.let(::unescape) ?: prefix.groupValues[2])
    }

    fun insert(text: String, query: Query, path: String): Edit {
        val encoded = if (plain.matches(path)) path
        else "\"" + path.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        val after = text.substring(query.end)
        val space = if (after.firstOrNull()?.isWhitespace() == true) "" else " "
        val insertion = "@$encoded$space"
        val result = text.replaceRange(query.start, query.end, insertion)
        return Edit(result, query.start + insertion.length + if (space.isEmpty()) 1 else 0)
    }

    fun paths(text: String, exists: (String) -> Boolean): List<String> =
        complete.findAll(text).mapNotNull { match ->
            val quoted = match.groups[1]?.value?.let(::unescape)
            if (quoted != null) {
                quoted.takeIf { it.isNotEmpty() && exists(it) }
            } else {
                val path = match.groupValues[2]
                val trimmed = path.trimEnd(*trailingPunctuation)
                when {
                    exists(path) -> path
                    trimmed.isNotEmpty() && path != trimmed && exists(trimmed) -> trimmed
                    else -> null
                }
            }
        }.distinct().toList()

    private fun unescape(value: String): String = value.replace(Regex("""\\(["\\])""")) { it.groupValues[1] }
}
