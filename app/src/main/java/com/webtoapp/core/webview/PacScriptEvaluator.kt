package com.webtoapp.core.webview

import java.net.InetAddress
import java.util.Locale

internal object PacScriptEvaluator {

    data class PacResult(val proxies: List<String>, val directHosts: Set<String>)

    private sealed class Expr {
        data class Atom(val fn: String, val args: List<String>) : Expr()
        data class Not(val inner: Expr) : Expr()
        data class And(val left: Expr, val right: Expr) : Expr()
        data class Or(val left: Expr, val right: Expr) : Expr()
        object False : Expr()
    }

    private data class Branch(val expr: Expr?, val result: String)

    fun evaluate(script: String, sampleHosts: List<String>): PacResult {
        val branches = parseBranches(script)
        if (branches.isEmpty()) return PacResult(emptyList(), emptySet())

        val hosts = (sampleHosts + DEFAULT_SAMPLES).distinct().filter { it.isNotBlank() }
        val proxyOrder = linkedSetOf<String>()
        val directHosts = linkedSetOf<String>()

        for (host in hosts) {
            val result = evalForHost(branches, host) ?: continue
            val entries = splitEntries(result)
            if (entries.isEmpty()) continue
            val first = entries.first()
            if (first.equals("DIRECT", ignoreCase = true)) {
                directHosts.add(host)
            } else {
                entries.forEach { if (!it.equals("DIRECT", ignoreCase = true)) proxyOrder.add(it) }
            }
        }

        val proxies = proxyOrder.toList()
        return PacResult(proxies.toList(), directHosts.toSet())
    }

    private val DEFAULT_SAMPLES = listOf("localhost", "127.0.0.1", "intranet", "example.com")

    private fun evalForHost(branches: List<Branch>, host: String): String? {
        for (branch in branches) {
            val expr = branch.expr
            if (expr == null || evalExpr(expr, host)) return branch.result
        }
        return null
    }

    private fun evalExpr(expr: Expr, host: String): Boolean = when (expr) {
        is Expr.Atom -> evalFunction(expr.fn, expr.args, host)
        is Expr.Not -> !evalExpr(expr.inner, host)
        is Expr.And -> evalExpr(expr.left, host) && evalExpr(expr.right, host)
        is Expr.Or -> evalExpr(expr.left, host) || evalExpr(expr.right, host)
        Expr.False -> false
    }

    private fun evalFunction(fn: String, args: List<String>, host: String): Boolean {
        val a0 = args.getOrElse(0) { "" }
        val a1 = args.getOrElse(1) { "" }
        return when (fn) {
            "isPlainHostName" -> !host.contains('.')
            "dnsDomainIs" -> {
                val h = host.lowercase(Locale.ROOT)
                val domain = a1.lowercase(Locale.ROOT).removePrefix(".")
                domain.isNotEmpty() && (h == domain || h.endsWith(".$domain"))
            }
            "localHostOrDomainIs" -> {
                val h = host.lowercase(Locale.ROOT)
                val d = a1.lowercase(Locale.ROOT)
                h == d || d.startsWith("$h.")
            }
            "shExpMatch" -> shExpMatch(host, a1)
            "isInNet" -> isInNet(host, a1, args.getOrElse(2) { "" })
            "dnsDomainLevels" -> host.count { it == '.' } >= (a0.toIntOrNull() ?: 0)
            "isResolvable" -> runCatching { InetAddress.getByName(host) }.isSuccess
            "dnsResolve" -> runCatching { InetAddress.getByName(host).hostAddress }.getOrNull() != null
            "myIpAddress" -> false
            "weekdayRange", "dateRange", "timeRange" -> false
            else -> false
        }
    }

    private fun shExpMatch(str: String, pattern: String): Boolean {
        val regex = buildString {
            append('^')
            for (ch in pattern) {
                when (ch) {
                    '*' -> append(".*")
                    '?' -> append('.')
                    '.', '(', ')', '+', '|', '^', '$', '@', '%', '{', '}', '[', ']', '\\' -> append('\\').append(ch)
                    else -> append(ch)
                }
            }
            append('$')
        }
        return runCatching { Regex(regex, RegexOption.IGNORE_CASE).matches(str) }.getOrDefault(false)
    }

    private fun isInNet(host: String, network: String, mask: String): Boolean {
        val hostBytes = literalBytes(host) ?: resolveBytes(host) ?: return false
        val netBytes = literalBytes(network) ?: return false
        val maskBytes = literalBytes(mask) ?: return false
        if (hostBytes.size != netBytes.size || hostBytes.size != maskBytes.size) return false
        for (i in hostBytes.indices) {
            if ((hostBytes[i].toInt() and maskBytes[i].toInt()) != (netBytes[i].toInt() and maskBytes[i].toInt())) {
                return false
            }
        }
        return true
    }

    private fun resolveBytes(host: String): ByteArray? =
        runCatching { InetAddress.getByName(host).address }.getOrNull()

    private fun literalBytes(value: String): ByteArray? {
        val cleaned = value.trim().removeSurrounding("[", "]")
        if (cleaned.isEmpty()) return null
        val isLiteral = cleaned.contains(':') ||
            (cleaned.all { it.isDigit() || it == '.' } && cleaned.count { it == '.' } == 3)
        if (!isLiteral) return null
        return runCatching { InetAddress.getByName(cleaned).address }.getOrNull()
    }

    private fun splitEntries(result: String): List<String> =
        result.split(';').map { it.trim() }.filter { it.isNotEmpty() }

    private fun parseBranches(script: String): List<Branch> {
        val body = extractFunctionBody(script) ?: return emptyList()
        val branches = mutableListOf<Branch>()
        val ifPattern = Regex("""\bif\s*\(""")
        var cursor = 0
        while (cursor < body.length) {
            val ifMatch = ifPattern.find(body, cursor)
            val returnMatch = Regex("""\breturn\s+["']([^"']+)["']""").find(body, cursor)
            if (returnMatch == null) break
            if (ifMatch != null && ifMatch.range.first < returnMatch.range.first) {
                val condStart = ifMatch.range.last + 1
                val condEnd = matchParen(body, condStart - 1)
                if (condEnd < 0) { cursor = returnMatch.range.last + 1; continue }
                val condText = body.substring(condStart, condEnd)
                val expr = if (condText.isBlank()) null else (CondParser(condText).parse() ?: Expr.False)
                val afterCond = skipWhitespace(body, condEnd + 1)
                val rest = body.substring(afterCond)
                val block = rest.startsWith("{")
                val bodyText = if (block) {
                    val closeBrace = matchBrace(rest, 0)
                    if (closeBrace < 0) rest else rest.substring(1, closeBrace)
                } else {
                    rest
                }
                val branchReturn = Regex("""\breturn\s+["']([^"']+)["']""").find(bodyText)
                if (branchReturn != null) {
                    branches.add(Branch(expr, branchReturn.groupValues[1]))
                    cursor = afterCond + (if (block) 1 else 0) + branchReturn.range.last + 1
                    continue
                }
                cursor = condEnd + 1
                continue
            }
            branches.add(Branch(null, returnMatch.groupValues[1]))
            cursor = returnMatch.range.last + 1
        }
        return branches
    }

    private fun extractFunctionBody(script: String): String? {
        val header = Regex("""function\s+FindProxyForURL\s*\([^)]*\)\s*\{""", RegexOption.IGNORE_CASE)
            .find(script) ?: return null
        val open = header.range.last
        val close = matchBrace(script, open)
        if (close < 0) return null
        return script.substring(open + 1, close)
    }

    private class CondParser(private val text: String) {
        private var pos = 0

        fun parse(): Expr? {
            val expr = parseOr() ?: return null
            skipWs()
            return if (pos >= text.length) expr else null
        }

        private fun parseOr(): Expr? {
            var left = parseAnd() ?: return null
            while (consume("||")) {
                val right = parseAnd() ?: return null
                left = Expr.Or(left, right)
            }
            return left
        }

        private fun parseAnd(): Expr? {
            var left = parseUnary() ?: return null
            while (consume("&&")) {
                val right = parseUnary() ?: return null
                left = Expr.And(left, right)
            }
            return left
        }

        private fun parseUnary(): Expr? {
            skipWs()
            if (pos < text.length && text[pos] == '!' && !text.startsWith("!=", pos)) {
                pos++
                val inner = parseUnary() ?: return null
                return Expr.Not(inner)
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Expr? {
            skipWs()
            if (consume("(")) {
                val inner = parseOr() ?: return null
                if (!consume(")")) return null
                return inner
            }
            return parseAtom()
        }

        private fun parseAtom(): Expr? {
            skipWs()
            val start = pos
            while (pos < text.length && (text[pos].isLetterOrDigit() || text[pos] == '_')) pos++
            if (pos == start) return null
            val fn = text.substring(start, pos)
            if (!consume("(")) return null
            val args = parseArgs() ?: return null
            return Expr.Atom(fn, args)
        }

        private fun parseArgs(): List<String>? {
            val args = mutableListOf<String>()
            if (consume(")")) return args
            while (true) {
                val arg = parseArg() ?: return null
                args.add(arg)
                if (consume(")")) return args
                if (!consume(",")) return null
            }
        }

        private fun parseArg(): String? {
            skipWs()
            if (pos >= text.length) return null
            val ch = text[pos]
            if (ch == '"' || ch == '\'') {
                pos++
                val sb = StringBuilder()
                while (pos < text.length && text[pos] != ch) {
                    sb.append(text[pos])
                    pos++
                }
                if (pos >= text.length) return null
                pos++
                return sb.toString()
            }
            val start = pos
            var depth = 0
            while (pos < text.length) {
                val c = text[pos]
                when {
                    c == '(' -> depth++
                    c == ')' -> { if (depth == 0) break; depth-- }
                    c == ',' && depth == 0 -> break
                }
                pos++
            }
            if (pos == start) return null
            return text.substring(start, pos).trim()
        }

        private fun consume(token: String): Boolean {
            skipWs()
            if (text.startsWith(token, pos)) {
                pos += token.length
                return true
            }
            return false
        }

        private fun skipWs() {
            while (pos < text.length && text[pos].isWhitespace()) pos++
        }
    }

    private fun matchParen(text: String, openIndex: Int): Int = matchDelim(text, openIndex, '(', ')')
    private fun matchBrace(text: String, openIndex: Int): Int = matchDelim(text, openIndex, '{', '}')

    private fun matchDelim(text: String, openIndex: Int, open: Char, close: Char): Int {
        var depth = 0
        var quote: Char? = null
        var i = openIndex
        while (i < text.length) {
            val ch = text[i]
            if (quote != null) {
                if (ch == quote) quote = null
            } else when (ch) {
                '"', '\'' -> quote = ch
                open -> depth++
                close -> { depth--; if (depth == 0) return i }
            }
            i++
        }
        return -1
    }

    private fun skipWhitespace(text: String, index: Int): Int {
        var i = index
        while (i < text.length && text[i].isWhitespace()) i++
        return i
    }
}
