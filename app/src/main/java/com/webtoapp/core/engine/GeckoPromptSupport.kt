package com.webtoapp.core.engine

/**
 * Pure helpers backing [GeckoViewEngine]'s PromptDelegate dialogs.
 *
 * Everything here is deliberately Gecko-free and Android-free so the tree
 * flattening and value parsing can be unit-tested on the JVM. The delegate
 * maps Gecko prompt types into these neutral shapes, shows dialogs, and
 * maps results back to prompt.confirm()/dismiss().
 */
internal object GeckoPromptSupport {

    /**
     * Neutral view of one `ChoicePrompt.Choice` node. [children] non-null
     * marks an `<optgroup>` / submenu container. [source] carries the
     * original Gecko Choice for confirm(); it is unused by the pure logic.
     */
    data class PromptChoice(
        val label: String,
        val selected: Boolean = false,
        val disabled: Boolean = false,
        val separator: Boolean = false,
        val children: List<PromptChoice>? = null,
        val source: Any? = null
    )

    /**
     * One flattened render row. [choice] is null for group headers and
     * separators — those rows render but are never confirmable/clickable.
     */
    data class ChoiceRow(
        val choice: PromptChoice?,
        val label: String,
        val depth: Int,
        val enabled: Boolean,
        val checked: Boolean
    )

    /**
     * Flattens an optgroup/submenu tree into single-level rows. A node with
     * children becomes a non-selectable header followed by its children at
     * depth+1; separators and disabled items become non-selectable rows.
     */
    fun flattenChoices(
        choices: List<PromptChoice>,
        depth: Int = 0,
        out: MutableList<ChoiceRow> = mutableListOf()
    ): List<ChoiceRow> {
        for (c in choices) {
            if (!c.children.isNullOrEmpty()) {
                out += ChoiceRow(c, c.label, depth, enabled = false, checked = false)
                flattenChoices(c.children, depth + 1, out)
            } else {
                val enabled = !c.disabled && !c.separator
                out += ChoiceRow(c, c.label, depth, enabled, checked = c.selected)
            }
        }
        return out
    }

    // ---- ISO input value parsing (input type=date/time/month/week/datetime-local) ----

    private val DATE_RE = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})""")
    private val TIME_RE = Regex("""^(\d{1,2}):(\d{2})""")
    private val MONTH_RE = Regex("""^(\d{4})-(\d{1,2})$""")
    private val WEEK_RE = Regex("""^(\d{4})-W(\d{1,2})$""")
    private val DATETIME_LOCAL_RE = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})[T ](\d{1,2}):(\d{2})""")

    /** "yyyy-MM-dd" → (year, month 1-12, day) or null. */
    fun parseDateValue(s: String?): Triple<Int, Int, Int>? =
        s?.let { DATE_RE.find(it) }?.destructured?.let { (y, m, d) ->
            Triple(y.toInt(), m.toInt(), d.toInt())
        }

    /** "HH:mm[:ss]" → (hour, minute) or null. */
    fun parseTimeValue(s: String?): Pair<Int, Int>? =
        s?.let { TIME_RE.find(it) }?.destructured?.let { (h, m) ->
            Pair(h.toInt(), m.toInt())
        }

    data class DateTimeParts(val y: Int, val mo: Int, val d: Int, val h: Int, val mi: Int)

    /** "yyyy-MM-ddTHH:mm" → parts or null. */
    fun parseDateTimeLocal(s: String?): DateTimeParts? =
        s?.let { DATETIME_LOCAL_RE.find(it) }?.destructured?.let { (y, mo, d, h, mi) ->
            DateTimeParts(y.toInt(), mo.toInt(), d.toInt(), h.toInt(), mi.toInt())
        }

    /** "yyyy-MM" → (year, month 1-12) or null. */
    fun parseMonthValue(s: String?): Pair<Int, Int>? =
        s?.let { MONTH_RE.find(it) }?.destructured?.let { (y, m) ->
            Pair(y.toInt(), m.toInt())
        }

    /** "yyyy-Www" → (year, week) or null. */
    fun parseWeekValue(s: String?): Pair<Int, Int>? =
        s?.let { WEEK_RE.find(it) }?.destructured?.let { (y, w) ->
            Pair(y.toInt(), w.toInt())
        }

    fun formatDate(y: Int, m: Int, d: Int) = "%04d-%02d-%02d".format(y, m, d)
    fun formatTime(h: Int, m: Int) = "%02d:%02d".format(h, m)
    fun formatDateTimeLocal(y: Int, mo: Int, d: Int, h: Int, mi: Int) =
        "%04d-%02d-%02dT%02d:%02d".format(y, mo, d, h, mi)
    fun formatMonth(y: Int, m: Int) = "%04d-%02d".format(y, m)
    fun formatWeek(y: Int, w: Int) = "%04d-W%02d".format(y, w)

    private fun dayOrdinal(y: Int, m: Int, d: Int) = y * 10_000 + m * 100 + d
    private fun timeOrdinal(h: Int, m: Int) = h * 100 + m

    /** Clamps (y,m,d) into the optional "yyyy-MM-dd" [min]/[max] bounds. */
    fun clampDate(y: Int, m: Int, d: Int, min: String?, max: String?): Triple<Int, Int, Int> {
        var v = dayOrdinal(y, m, d)
        parseDateValue(min)?.let { v = v.coerceAtLeast(dayOrdinal(it.first, it.second, it.third)) }
        parseDateValue(max)?.let { v = v.coerceAtMost(dayOrdinal(it.first, it.second, it.third)) }
        return Triple(v / 10_000, (v / 100) % 100, v % 100)
    }

    /** Clamps (hour, minute) into the optional "HH:mm" [min]/[max] bounds. */
    fun clampTime(h: Int, m: Int, min: String?, max: String?): Pair<Int, Int> {
        var v = timeOrdinal(h, m)
        parseTimeValue(min)?.let { v = v.coerceAtLeast(timeOrdinal(it.first, it.second)) }
        parseTimeValue(max)?.let { v = v.coerceAtMost(timeOrdinal(it.first, it.second)) }
        return Pair(v / 100, v % 100)
    }

    // ---- <input type=color> ----

    /**
     * Normalizes a user/hex color to lowercase "#rrggbb". Accepts "#rgb",
     * "#rrggbb", "#rgba", "#rrggbbaa" and bare hex (alpha is dropped —
     * color inputs produce opaque colors). Returns null when unparsable.
     */
    fun normalizeHexColor(raw: String?): String? {
        var s = raw?.trim() ?: return null
        if (s.startsWith("#")) s = s.substring(1)
        s = when (s.length) {
            3 -> s.flatMap { listOf(it, it) }.joinToString("")
            4 -> s.flatMap { listOf(it, it) }.joinToString("").substring(0, 6)
            6 -> s
            8 -> s.substring(2)
            else -> return null
        }
        if (!s.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
        return "#${s.lowercase()}"
    }
}
