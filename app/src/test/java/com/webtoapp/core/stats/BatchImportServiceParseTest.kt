package com.webtoapp.core.stats

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class BatchImportServiceParseTest {

    private val markdownLink = Regex("""\[([^\]]*)\]\((https?://[^)\s]+)\)""", RegexOption.IGNORE_CASE)
    private val bareUrl = Regex("""https?://[^\s<>"'`]+""", RegexOption.IGNORE_CASE)
    private val domain = Regex("""^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(?::\d{1,5})?(?:/[^\s]*)?$""")

    private fun looksLikeUrl(value: String): Boolean {
        val v = value.trim().trimEnd(',', ';', '.', ')', ']', '>')
        if (v.startsWith("http://", ignoreCase = true) || v.startsWith("https://", ignoreCase = true)) return true
        return domain.matches(v)
    }

    private fun normalizeUrl(url: String): String? {
        var value = url.trim().trimEnd(',', ';', '.', ')', ']', '>', '"', '\'').trimStart('<', '"', '\'')
        if (value.isBlank()) return null
        if (value.startsWith("www.", ignoreCase = true)) value = "https://$value"
        else if (!value.startsWith("http://", ignoreCase = true) && !value.startsWith("https://", ignoreCase = true)) {
            if (!domain.matches(value)) return null
            value = "https://$value"
        }
        return value
    }

    private fun extractName(url: String): String {
        return try {
            val host = java.net.URI(url).host ?: return url.take(30)
            host.removePrefix("www.")
                .substringBeforeLast(".")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } catch (_: Exception) {
            url.take(30)
        }
    }

    private fun parseFromText(text: String): List<TestParsedEntry> {
        val seen = linkedSetOf<String>()
        val entries = mutableListOf<TestParsedEntry>()
        fun accept(name: String, rawUrl: String) {
            val normalized = normalizeUrl(rawUrl) ?: return
            val key = normalized.lowercase(Locale.US)
            if (!seen.add(key)) return
            entries += TestParsedEntry(name.trim().ifBlank { extractName(normalized) }, normalized)
        }
        text.lines().forEach { rawLine ->
            val line = rawLine.trim().removePrefix("\uFEFF").trim()
            if (line.isBlank()) return@forEach
            if (line.startsWith("#") || line.startsWith("//") || line.startsWith(";")) return@forEach
            val md = markdownLink.find(line)
            if (md != null) {
                accept(md.groupValues[1], md.groupValues[2])
                return@forEach
            }
            when {
                line.contains("|") -> {
                    val parts = line.split("|", limit = 2)
                    accept(parts[0].trim().trim('"', '\''), parts.getOrNull(1)?.trim().orEmpty())
                }
                looksLikeUrl(line) -> accept("", line)
                line.contains(" ") -> {
                    val lastSpace = line.lastIndexOf(' ')
                    val possibleUrl = line.substring(lastSpace + 1).trim().trimEnd(',', ';', '.')
                    if (looksLikeUrl(possibleUrl)) accept(line.substring(0, lastSpace).trim(), possibleUrl)
                    else {
                        val bare = bareUrl.find(line)?.value
                        if (bare != null) accept(line.replace(bare, "").trim(), bare)
                    }
                }
                else -> bareUrl.find(line)?.value?.let { accept("", it) }
            }
        }
        return entries
    }

    data class TestParsedEntry(val name: String, val url: String)

    @Test
    fun `parseFromText parses single URL`() {
        val result = parseFromText("https://example.com")
        assertThat(result).hasSize(1)
        assertThat(result[0].url).isEqualTo("https://example.com")
        assertThat(result[0].name).isEqualTo("Example")
    }

    @Test
    fun `parseFromText parses pipe-separated name and URL`() {
        val result = parseFromText("My App|https://example.com")
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("My App")
        assertThat(result[0].url).isEqualTo("https://example.com")
    }

    @Test
    fun `parseFromText parses space-separated name and URL`() {
        val result = parseFromText("My App https://example.com")
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("My App")
        assertThat(result[0].url).isEqualTo("https://example.com")
    }

    @Test
    fun `parseFromText parses bare domain`() {
        val result = parseFromText("example.com")
        assertThat(result).hasSize(1)
        assertThat(result[0].url).isEqualTo("https://example.com")
    }

    @Test
    fun `parseFromText parses markdown link`() {
        val result = parseFromText("[Docs](https://example.com/docs)")
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Docs")
        assertThat(result[0].url).isEqualTo("https://example.com/docs")
    }

    @Test
    fun `parseFromText parses multiple URLs`() {
        val text = """
            https://example.com
            https://google.com
            https://github.com
        """.trimIndent()
        assertThat(parseFromText(text)).hasSize(3)
    }

    @Test
    fun `parseFromText skips comment lines starting with hash`() {
        val text = """
            # This is a comment
            https://example.com
        """.trimIndent()
        assertThat(parseFromText(text)).hasSize(1)
    }

    @Test
    fun `parseFromText skips comment lines starting with double slash`() {
        val text = """
            // This is a comment
            https://example.com
        """.trimIndent()
        assertThat(parseFromText(text)).hasSize(1)
    }

    @Test
    fun `parseFromText skips blank lines`() {
        val text = "\nhttps://example.com\n\nhttps://google.com\n"
        assertThat(parseFromText(text)).hasSize(2)
    }

    @Test
    fun `parseFromText deduplicates URLs`() {
        val text = """
            https://example.com
            https://example.com
            EXAMPLE.com
        """.trimIndent()
        assertThat(parseFromText(text)).hasSize(1)
    }

    @Test
    fun `parseFromText returns empty for non-URL text`() {
        assertThat(parseFromText("just some random text")).isEmpty()
    }

    @Test
    fun `parseFromText returns empty for empty string`() {
        assertThat(parseFromText("")).isEmpty()
    }

    @Test
    fun `parseFromText ignores pipe format with invalid URL`() {
        assertThat(parseFromText("My App|not-a-url")).isEmpty()
    }

    @Test
    fun `extractName removes www prefix and TLD`() {
        assertThat(extractName("https://www.example.com")).isEqualTo("Example")
    }

    @Test
    fun `extractName handles bare domain`() {
        assertThat(extractName("https://example.com")).isEqualTo("Example")
    }
}
