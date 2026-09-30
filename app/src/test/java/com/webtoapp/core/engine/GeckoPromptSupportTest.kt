package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Covers the pure prompt helpers plus the delegate-wiring contract:
 * GeckoView silently dismisses every prompt the delegate does not handle,
 * so the overrides must stay present (#1137).
 */
class GeckoPromptSupportTest {

    private fun choice(
        label: String,
        selected: Boolean = false,
        disabled: Boolean = false,
        separator: Boolean = false,
        children: List<GeckoPromptSupport.PromptChoice>? = null
    ) = GeckoPromptSupport.PromptChoice(label, selected, disabled, separator, children)

    @Test
    fun `flatten renders optgroup as header plus indented children`() {
        val rows = GeckoPromptSupport.flattenChoices(listOf(
            choice("Group", children = listOf(choice("A"), choice("B"))),
            choice("C")
        ))
        assertThat(rows.map { it.label }).containsExactly("Group", "A", "B", "C").inOrder()
        assertThat(rows[0].enabled).isFalse()
        assertThat(rows[0].depth).isEqualTo(0)
        assertThat(rows[1].depth).isEqualTo(1)
        assertThat(rows[2].depth).isEqualTo(1)
        assertThat(rows[3].depth).isEqualTo(0)
        assertThat(rows.drop(1).map { it.enabled }).containsExactly(true, true, true)
    }

    @Test
    fun `flatten marks separators and disabled items non-selectable`() {
        val rows = GeckoPromptSupport.flattenChoices(listOf(
            choice("A"), choice("--", separator = true), choice("B", disabled = true)
        ))
        assertThat(rows.map { it.enabled }).containsExactly(true, false, false).inOrder()
    }

    @Test
    fun `flatten preserves selected flags and nested groups`() {
        val rows = GeckoPromptSupport.flattenChoices(listOf(
            choice("G", children = listOf(
                choice("X", selected = true),
                choice("Sub", children = listOf(choice("Y")))
            ))
        ))
        assertThat(rows.map { it.depth }).containsExactly(0, 1, 1, 2).inOrder()
        assertThat(rows[1].checked).isTrue()
        assertThat(rows[3].label).isEqualTo("Y")
        assertThat(rows[3].choice?.source).isNull()
    }

    @Test
    fun `empty choice list yields empty rows`() {
        assertThat(GeckoPromptSupport.flattenChoices(emptyList())).isEmpty()
    }

    @Test
    fun `iso value parsers accept and reject the right shapes`() {
        assertThat(GeckoPromptSupport.parseDateValue("2025-03-04")).isEqualTo(Triple(2025, 3, 4))
        assertThat(GeckoPromptSupport.parseDateValue("bad")).isNull()
        assertThat(GeckoPromptSupport.parseTimeValue("09:45")).isEqualTo(Pair(9, 45))
        assertThat(GeckoPromptSupport.parseTimeValue("09:45:30")).isEqualTo(Pair(9, 45))
        assertThat(GeckoPromptSupport.parseTimeValue("x")).isNull()
        assertThat(GeckoPromptSupport.parseMonthValue("2025-07")).isEqualTo(Pair(2025, 7))
        assertThat(GeckoPromptSupport.parseMonthValue("2025-07-01")).isNull()
        assertThat(GeckoPromptSupport.parseWeekValue("2025-W06")).isEqualTo(Pair(2025, 6))
        assertThat(GeckoPromptSupport.parseWeekValue("2025-06")).isNull()
        val dt = GeckoPromptSupport.parseDateTimeLocal("2025-03-04T09:45")
        assertThat(dt).isEqualTo(GeckoPromptSupport.DateTimeParts(2025, 3, 4, 9, 45))
        assertThat(GeckoPromptSupport.parseDateTimeLocal("2025-03-04")).isNull()
    }

    @Test
    fun `iso formatters produce the contract strings`() {
        assertThat(GeckoPromptSupport.formatDate(2025, 3, 4)).isEqualTo("2025-03-04")
        assertThat(GeckoPromptSupport.formatTime(9, 5)).isEqualTo("09:05")
        assertThat(GeckoPromptSupport.formatDateTimeLocal(2025, 3, 4, 9, 5))
            .isEqualTo("2025-03-04T09:05")
        assertThat(GeckoPromptSupport.formatMonth(2025, 7)).isEqualTo("2025-07")
        assertThat(GeckoPromptSupport.formatWeek(2025, 6)).isEqualTo("2025-W06")
    }

    @Test
    fun `clampDate honors min and max bounds`() {
        assertThat(GeckoPromptSupport.clampDate(2025, 6, 15, "2025-06-10", "2025-06-20"))
            .isEqualTo(Triple(2025, 6, 15))
        assertThat(GeckoPromptSupport.clampDate(2025, 6, 5, "2025-06-10", null))
            .isEqualTo(Triple(2025, 6, 10))
        assertThat(GeckoPromptSupport.clampDate(2025, 7, 1, null, "2025-06-30"))
            .isEqualTo(Triple(2025, 6, 30))
        assertThat(GeckoPromptSupport.clampDate(2024, 2, 29, null, null))
            .isEqualTo(Triple(2024, 2, 29))
    }

    @Test
    fun `clampTime honors min and max bounds`() {
        assertThat(GeckoPromptSupport.clampTime(12, 30, "09:00", "18:00")).isEqualTo(Pair(12, 30))
        assertThat(GeckoPromptSupport.clampTime(7, 0, "09:00", null)).isEqualTo(Pair(9, 0))
        assertThat(GeckoPromptSupport.clampTime(23, 0, null, "18:00")).isEqualTo(Pair(18, 0))
    }

    @Test
    fun `normalizeHexColor accepts common shapes and rejects junk`() {
        assertThat(GeckoPromptSupport.normalizeHexColor("#FF0000")).isEqualTo("#ff0000")
        assertThat(GeckoPromptSupport.normalizeHexColor("f00")).isEqualTo("#ff0000")
        assertThat(GeckoPromptSupport.normalizeHexColor("#f00")).isEqualTo("#ff0000")
        assertThat(GeckoPromptSupport.normalizeHexColor("#80ff0000")).isEqualTo("#ff0000")
        assertThat(GeckoPromptSupport.normalizeHexColor("  #00FF7F ")).isEqualTo("#00ff7f")
        assertThat(GeckoPromptSupport.normalizeHexColor("red")).isNull()
        assertThat(GeckoPromptSupport.normalizeHexColor("#12345")).isNull()
        assertThat(GeckoPromptSupport.normalizeHexColor(null)).isNull()
        assertThat(GeckoPromptSupport.normalizeHexColor("")).isNull()
    }

    @Test
    fun `prompt delegate overrides the form prompts gecko cannot render`() {
        val src = readSanitized("com/webtoapp/core/engine/GeckoViewEngine.kt")
        val delegate = src.substringAfter("session.promptDelegate = object", "")
        for (hook in listOf(
            "onChoicePrompt", "onColorPrompt", "onDateTimePrompt",
            "onBeforeUnloadPrompt", "onRepostConfirmPrompt"
        )) {
            assertWithMessage("promptDelegate must override $hook (else it is silently dismissed)")
                .that(delegate).contains("override fun $hook(")
        }
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Test harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
