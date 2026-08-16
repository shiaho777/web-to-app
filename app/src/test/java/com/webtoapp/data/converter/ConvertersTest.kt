package com.webtoapp.data.converter

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.DnsProvider
import com.webtoapp.data.model.NotificationExportConfig
import com.webtoapp.data.model.NotificationType
import com.webtoapp.data.model.UserAgentMode
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `toStringList returns empty list for malformed json`() {
        assertThat(converters.toStringList("not-json")).isEmpty()
    }

    @Test
    fun `toStringList skips malformed items instead of returning empty`() {
        val encoded = """
            [
              "module-a",
              {"broken":true},
              "module-b"
            ]
        """.trimIndent()

        assertThat(converters.toStringList(encoded)).containsExactly("module-a", "module-b").inOrder()
    }

    @Test
    fun `app type converter falls back to WEB for unknown value`() {
        assertThat(converters.toAppType("NOT_EXISTING_TYPE")).isEqualTo(AppType.WEB)
    }

    @Test
    fun `web view config converter merges defaults for missing fields`() {
        val partial = """{"javaScriptEnabled":false,"userAgentMode":"CHROME_DESKTOP"}"""

        val config = converters.toWebViewConfig(partial)

        assertThat(config.javaScriptEnabled).isFalse()
        assertThat(config.userAgentMode).isEqualTo(UserAgentMode.CHROME_DESKTOP)
        assertThat(config.domStorageEnabled).isTrue()
        assertThat(config.hideToolbar).isFalse()
        assertThat(config.zoomEnabled).isTrue()
    }

    @Test
    fun `activation code list converter supports roundtrip`() {
        val list = listOf(
            ActivationCode(code = "A", note = "one"),
            ActivationCode(code = "B", note = "two")
        )

        val encoded = converters.fromActivationCodeList(list)
        val decoded = converters.toActivationCodeList(encoded)

        assertThat(decoded.map { it.code }).containsExactly("A", "B").inOrder()
        assertThat(decoded.map { it.note }).containsExactly("one", "two").inOrder()
    }

    @Test
    fun `activation code list converter skips malformed items instead of returning empty`() {
        val encoded = """
            [
              {"code":"GOOD","type":"PERMANENT","note":"ok"},
              "broken-item",
              {"code":"GOOD2","type":"USAGE_LIMITED","note":"ok2"}
            ]
        """.trimIndent()

        val decoded = converters.toActivationCodeList(encoded)

        assertThat(decoded.map { it.code }).containsExactly("GOOD", "GOOD2").inOrder()
        assertThat(decoded.map { it.note }).containsExactly("ok", "ok2").inOrder()
    }

    @Test
    fun `mergeMissingDefaults preserves current values and fills missing recursively`() {
        val defaults = JsonParser.parseString(
            """
            {
              "root": {
                "a": 1,
                "b": 2
              },
              "enabled": true
            }
            """.trimIndent()
        )
        val current = JsonParser.parseString(
            """
            {
              "root": {
                "a": 99
              },
              "extra": "ok"
            }
            """.trimIndent()
        )

        val merged = Converters.mergeMissingDefaults(defaults, current).asJsonObject

        assertThat(merged.getAsJsonObject("root").get("a").asInt).isEqualTo(99)
        assertThat(merged.getAsJsonObject("root").get("b").asInt).isEqualTo(2)
        assertThat(merged.get("enabled").asBoolean).isTrue()
        assertThat(merged.get("extra").asString).isEqualTo("ok")
    }

    @Test
    fun `apk export config keeps notification type across a roundtrip`() {
        val config = ApkExportConfig(
            notificationEnabled = true,
            notificationConfig = NotificationExportConfig(type = NotificationType.WEB_API)
        )

        val decoded = converters.toApkExportConfig(converters.fromApkExportConfig(config))

        assertThat(decoded?.notificationConfig?.type).isEqualTo(NotificationType.WEB_API)
    }

    @Test
    fun `enum decoding accepts the serialized name that encoding emits`() {
        // NotificationType declares lowercase @SerializedName values, so this is the
        // on-disk representation Gson writes.
        val encoded = """{"notificationConfig":{"type":"web_api"}}"""

        val decoded = converters.toApkExportConfig(encoded)

        assertThat(decoded?.notificationConfig?.type).isEqualTo(NotificationType.WEB_API)
    }

    @Test
    fun `enum decoding also accepts the raw constant name`() {
        val encoded = """{"notificationConfig":{"type":"WEB_API"}}"""

        val decoded = converters.toApkExportConfig(encoded)

        assertThat(decoded?.notificationConfig?.type).isEqualTo(NotificationType.WEB_API)
    }

    @Test
    fun `enum decoding falls back to the first constant for unknown values`() {
        val encoded = """{"notificationConfig":{"type":"carrier_pigeon"}}"""

        val decoded = converters.toApkExportConfig(encoded)

        assertThat(decoded?.notificationConfig?.type).isEqualTo(NotificationType.NONE)
    }

    @Test
    fun `dns provider survives a roundtrip instead of collapsing to the first constant`() {
        val encoded = Converters.toJson(DnsProvider.ADGUARD)

        assertThat(Converters.fromJson<DnsProvider>(encoded)).isEqualTo(DnsProvider.ADGUARD)
    }
}
