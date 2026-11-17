package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PacScriptEvaluatorTest {

    private val standardPac = """
        function FindProxyForURL(url, host) {
            if (isPlainHostName(host) ||
                shExpMatch(host, "*.local") ||
                isInNet(host, "10.0.0.0", "255.0.0.0")) {
                return "DIRECT";
            }
            return "PROXY proxy.example.com:8080; DIRECT";
        }
    """.trimIndent()

    @Test
    fun `standard pac keeps proxy and bypasses local hosts`() {
        val result = PacScriptEvaluator.evaluate(standardPac, listOf("10.1.2.3"))
        assertThat(result.proxies).containsExactly("PROXY proxy.example.com:8080").inOrder()
        assertThat(result.directHosts).containsAtLeast("localhost", "intranet", "10.1.2.3")
        assertThat(result.directHosts).doesNotContain("example.com")
    }

    @Test
    fun `plain host name is direct only for unqualified hosts`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (isPlainHostName(host)) return "DIRECT";
                return "PROXY p:8080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, emptyList())
        assertThat(result.proxies).containsExactly("PROXY p:8080")
        assertThat(result.directHosts).contains("localhost")
        assertThat(result.directHosts).doesNotContain("example.com")
    }

    @Test
    fun `dnsDomainIs condition routes matching domain direct`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (dnsDomainIs(host, ".internal.corp")) return "DIRECT";
                return "SOCKS5 socks.example.com:1080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("host.internal.corp"))
        assertThat(result.proxies).containsExactly("SOCKS5 socks.example.com:1080")
        assertThat(result.directHosts).contains("host.internal.corp")
    }

    @Test
    fun `negated condition is honored`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (!isPlainHostName(host)) return "PROXY p:8080";
                return "DIRECT";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, emptyList())
        assertThat(result.proxies).containsExactly("PROXY p:8080")
        assertThat(result.directHosts).contains("localhost")
    }

    @Test
    fun `unconditional direct yields no proxy`() {
        val pac = """
            function FindProxyForURL(url, host) { return "DIRECT"; }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, emptyList())
        assertThat(result.proxies).isEmpty()
    }

    @Test
    fun `unconditional proxy applies to all hosts`() {
        val pac = """
            function FindProxyForURL(url, host) { return "PROXY p:8080"; }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, emptyList())
        assertThat(result.proxies).containsExactly("PROXY p:8080")
        assertThat(result.directHosts).isEmpty()
    }

    @Test
    fun `shExpMatch wildcard does not swallow unrelated hosts`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (shExpMatch(host, "*.bank.com")) return "DIRECT";
                return "PROXY p:8080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("www.bank.com", "evil.com"))
        assertThat(result.directHosts).contains("www.bank.com")
        assertThat(result.directHosts).doesNotContain("evil.com")
        assertThat(result.proxies).containsExactly("PROXY p:8080")
    }

    @Test
    fun `or condition routes only matching hosts direct`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (isPlainHostName(host) || dnsDomainIs(host, ".local")) return "DIRECT";
                return "PROXY p:8080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("printer.local", "evil.com"))
        assertThat(result.proxies).containsExactly("PROXY p:8080")
        assertThat(result.directHosts).containsAtLeast("localhost", "printer.local")
        assertThat(result.directHosts).doesNotContain("evil.com")
        assertThat(result.directHosts).doesNotContain("example.com")
    }

    @Test
    fun `and binds tighter than or`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (shExpMatch(host, "*.corp") && dnsDomainIs(host, ".a.corp") || isPlainHostName(host)) return "DIRECT";
                return "PROXY p:8080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("x.a.corp", "x.b.corp", "intranet", "evil.com"))
        assertThat(result.directHosts).containsAtLeast("x.a.corp", "intranet")
        assertThat(result.directHosts).doesNotContain("x.b.corp")
        assertThat(result.directHosts).doesNotContain("evil.com")
        assertThat(result.proxies).containsExactly("PROXY p:8080")
    }

    @Test
    fun `parentheses override precedence`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (shExpMatch(host, "*.corp") && (dnsDomainIs(host, ".a.corp") || dnsDomainIs(host, ".b.corp"))) return "DIRECT";
                return "PROXY p:8080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("x.a.corp", "x.b.corp", "x.c.corp"))
        assertThat(result.directHosts).containsAtLeast("x.a.corp", "x.b.corp")
        assertThat(result.directHosts).doesNotContain("x.c.corp")
        assertThat(result.proxies).containsExactly("PROXY p:8080")
    }

    @Test
    fun `negated group is honored`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (!(isPlainHostName(host) || dnsDomainIs(host, ".local"))) return "PROXY p:8080";
                return "DIRECT";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("printer.local", "evil.com"))
        assertThat(result.proxies).containsExactly("PROXY p:8080")
        assertThat(result.directHosts).containsAtLeast("localhost", "printer.local")
        assertThat(result.directHosts).doesNotContain("evil.com")
    }

    @Test
    fun `dnsDomainIs enforces label boundary`() {
        val pac = """
            function FindProxyForURL(url, host) {
                if (dnsDomainIs(host, ".bank.com")) return "DIRECT";
                return "PROXY p:8080";
            }
        """.trimIndent()
        val result = PacScriptEvaluator.evaluate(pac, listOf("sub.bank.com", "bank.com", "evilbank.com"))
        assertThat(result.directHosts).containsAtLeast("sub.bank.com", "bank.com")
        assertThat(result.directHosts).doesNotContain("evilbank.com")
        assertThat(result.proxies).containsExactly("PROXY p:8080")
    }
}
