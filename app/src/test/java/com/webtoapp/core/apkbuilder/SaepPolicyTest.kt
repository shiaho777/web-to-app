package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import org.junit.Assert.assertThrows
import org.junit.Test

class SaepPolicyTest {
    @Test
    fun `policy binds the final application id and actual activity with boolean rules`() {
        val bytes = SaepPolicy.generate("org.example.custom", SaepPolicy.SHELL_ACTIVITY, 123456L)
        val root = JsonParser.parseString(bytes.toString(Charsets.UTF_8)).asJsonObject
        assertThat(bytes.size).isAtMost(SaepPolicy.MAX_BYTES)
        assertThat(root.get("schema").asString).isEqualTo("AGRP-Policy/1.0")
        assertThat(root.get("package").asString).isEqualTo("org.example.custom")
        assertThat(root.get("policy_version").asInt).isEqualTo(1)
        assertThat(root.get("policy_version").asJsonPrimitive.isNumber).isTrue()
        assertThat(root.get("updated_at").asJsonPrimitive.isString).isTrue()
        assertThat(root.get("updated_at").asString).isEqualTo("123456")
        val scope = root.getAsJsonObject("scope")
        val activities = scope.getAsJsonObject("activities")
        assertThat(activities.keySet()).containsExactly(SaepPolicy.SHELL_ACTIVITY)
        val activity = activities.getAsJsonObject(SaepPolicy.SHELL_ACTIVITY)
        assertThat(activity.get("name").asString).isNotEmpty()
        val rules = listOf(
            root.getAsJsonObject("default_policy").getAsJsonObject("app"),
            scope.getAsJsonObject("app"), activity.getAsJsonObject("page_scope")
        )
        rules.forEach { rule ->
            assertThat(rule.keySet()).containsExactly("global_disable", "screenshot_disable", "input_disable")
            rule.entrySet().forEach {
                assertThat(it.value.asJsonPrimitive.isBoolean).isTrue()
                assertThat(it.value.asBoolean).isFalse()
            }
        }
        val intents = scope.getAsJsonObject("agent_intents")
        assertThat(intents.keySet()).containsExactly(
            "modify_content", "post_content", "delete_content", "account_incentive"
        )
        intents.entrySet().forEach { assertThat(it.value.asBoolean).isFalse() }
    }

    @Test
    fun `json escaping preserves supplied activity and generation is deterministic`() {
        val name = "org.example.Screen\"中文"
        val bytes = SaepPolicy.generate("org.example.custom", name, 0)
        val root = JsonParser.parseString(bytes.toString(Charsets.UTF_8)).asJsonObject
        assertThat(root.getAsJsonObject("scope").getAsJsonObject("activities").keySet()).containsExactly(name)
        assertThat(SaepPolicy.generate("org.example.custom", name, 0)).isEqualTo(bytes)
    }

    @Test
    fun `invalid identity timestamp and oversized utf8 policy are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            SaepPolicy.generate("invalid/package", SaepPolicy.SHELL_ACTIVITY, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SaepPolicy.generate("org.example.test", " ", 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SaepPolicy.generate("org.example.test", SaepPolicy.SHELL_ACTIVITY, -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SaepPolicy.generate("org.example.test", "界".repeat(4000), 0)
        }
    }
}
